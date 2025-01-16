/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.util.strings;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Markus Gärtner
 *
 */
public class CharLineBuffer extends Splitable {

	// Data source
	private Reader reader;
	private char[] buffer;
	private boolean ignoreLF = false;

	private final int bufferSize;
	private int nextChar;

	// Cursor cursorCache
	private Stack<Cursor> cursorCache = new Stack<>();
	private Map<String, Matcher> regexCache;


	private static final char CR = '\r';
	private static final char LF = '\n';

	public CharLineBuffer() {
		this(8000);
	}

	public CharLineBuffer(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void startReading(Reader reader) throws IOException {
		requireNonNull(reader);

		reset();

		this.reader = reader;

		if(buffer==null) {
			buffer = new char[bufferSize];
		}

		ignoreLF = false;
	}

	public void reset() throws IOException {
		if(reader!=null) {
			reader.close();
			reader = null;
		}

		ignoreLF = false;
		resetHash();
	}

	public void close() throws IOException {
		reset();
		buffer = null;

		for(Cursor cursor : cursorCache) {
			cursor.closeSplits();
		}
	}

	/**
	 * Reads characters from the underlying reader until the end of the stream
	 * or a linebreak occurs.
	 */
	public boolean next() throws IOException {
		nextChar = 0;

		boolean eos = false;

		char_loop : for(;;) {
			int c = reader.read();

			switch (c) {
			case -1:
				eos = true;
				break char_loop;

			case CR:
				ignoreLF = true;
				break char_loop;

			case LF:
				if(!ignoreLF)
					break char_loop;
				break;

			default:
				if(nextChar>=buffer.length) {
					buffer = Arrays.copyOf(buffer, nextChar*2+1);
				}
				buffer[nextChar++] = (char) c;
				ignoreLF = false;
				break;
			}
		}
		resetHash();

		return !eos || nextChar>0;
	}

	private Cursor getCursor0(int index0, int index1) {
		Cursor cursor = null;

		// Check cursorCache
		if(!cursorCache.isEmpty()) {
			cursor = cursorCache.pop();
		}

		// Create new cursor only if required
		if(cursor==null) {
//			System.out.printf("creating new cursor: row=%d from=%d to=%d\n",row, index0, index1);
			cursor = new Cursor();
		} else {
			cursor.resetSplits();
		}

		// Now set scope
		cursor.setIndex0(index0);
		cursor.setIndex1(index1);
		cursor.resetHash();

		return cursor;
	}

	private void recycleCursor0(Cursor cursor) {
		if (cursor == null)
			throw new NullPointerException("Invalid cursor"); //$NON-NLS-1$
		cursorCache.push(cursor);
	}

	private Matcher getMatcher0(String regex, CharSequence input) {
		if (regex == null)
			throw new NullPointerException("Invalid regex"); //$NON-NLS-1$

		if(regexCache==null) {
			regexCache = new HashMap<>();
		}

		Matcher m = regexCache.remove(regex);

		if(m==null) {
//			System.out.println("Compiling pattern: "+regex);
			m = Pattern.compile(regex).matcher(input);

//			regexCache.put(regex, m);
		} else {
			m.reset(input);
		}

		return m;
	}

	private void recycleMatcher0(Matcher matcher) {
		if (matcher == null)
			throw new NullPointerException("Invalid matcher"); //$NON-NLS-1$

		if(regexCache==null) {
			regexCache = new HashMap<>();
		}

		matcher.reset();
		regexCache.put(matcher.pattern().pattern(), matcher);
	}

	/**
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length() {
		return nextChar;
	}

	/**
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt(int index) {
		if(index<0 || index>=nextChar)
			throw new IndexOutOfBoundsException();

		return buffer[index];
	}

	/**
	 * @see de.ims.icarus2.util.strings.Splitable#subSequence(int, int)
	 */
	@Override
	public Cursor subSequence(int begin, int end) {
		return getCursor0(begin, end-1);
	}

	public Cursor subSequence(int begin) {
		return getCursor0(begin, length()-1);
	}

	public String substring(int begin, int end) {
		Cursor c = subSequence(begin, end);
		String s = c.toString();
		c.recycle();
		return s;
	}

	public String substring(int begin) {
		Cursor c = subSequence(begin);
		String s = c.toString();
		c.recycle();
		return s;
	}

	/**
	 * @see de.ims.icarus2.util.strings.Splitable#recycle()
	 */
	@Override
	public void recycle() {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.util.strings.Splitable#getCachedMatcher(java.lang.String)
	 */
	@Override
	protected Matcher getCachedMatcher(String regex) {
		return getMatcher0(regex, this);
	}

	/**
	 * @see de.ims.icarus2.util.strings.Splitable#recycleMatcher(java.util.regex.Matcher)
	 */
	@Override
	protected void recycleMatcher(Matcher matcher) {
		recycleMatcher0(matcher);
	}

	public class Cursor extends Splitable {

		private int index0, index1;

		/**
		 * @see java.lang.CharSequence#length()
		 */
		@Override
		public int length() {
			return index1-index0+1;
		}

		/**
		 * @see java.lang.CharSequence#charAt(int)
		 */
		@Override
		public char charAt(int index) {
			if(index>index1-index0)
				throw new IndexOutOfBoundsException();

			return buffer[index0+index];
		}

		private void setIndex0(int index0) {
			this.index0 = index0;
			resetHash();
		}

		private void setIndex1(int index1) {
			this.index1 = index1;
			resetHash();
		}

		@Override
		public void recycle() {
			index0 = index1 = -1;
			resetHash();

			recycleCursor0(this);
		}

		/**
		 * @see de.ims.icarus2.util.strings.AbstractString#subSequence(int, int)
		 */
		@Override
		public Cursor subSequence(int start, int end) {
			return getCursor0(index0+start, index0+end-1);
		}

		/**
		 * @see de.ims.icarus2.util.strings.Splitable#getCachedMatcher(java.lang.String)
		 */
		@Override
		protected Matcher getCachedMatcher(String regex) {
			return getMatcher0(regex, this);
		}

		/**
		 * @see de.ims.icarus2.util.strings.Splitable#recycleMatcher(java.util.regex.Matcher)
		 */
		@Override
		protected void recycleMatcher(Matcher matcher) {
			recycleMatcher0(matcher);
		}

		/**
		 * @see de.ims.icarus2.util.strings.Splitable#getSplitCursor(int)
		 */
		@Override
		public Cursor getSplitCursor(int index) {
			return (Cursor) super.getSplitCursor(index);
		}
	}
}
