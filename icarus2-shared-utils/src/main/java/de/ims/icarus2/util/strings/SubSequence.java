/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

public class SubSequence extends AbstractString {

	private final CharSequence source;
	private final int offset;
	private final int len;

	public SubSequence(CharSequence source, int offset, int len) {
		if (source == null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$
		if(offset<0 || offset>=source.length())
			throw new IndexOutOfBoundsException("offset"); //$NON-NLS-1$
		if(len<0 || len>=source.length()-offset)
			throw new IllegalArgumentException("length"); //$NON-NLS-1$

		this.source = source;
		this.offset = offset;
		this.len = len;
	}

	/**
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length() {
		return len;
	}

	/**
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt(int index) {
		if(index<0 || index>=len)
			throw new IndexOutOfBoundsException();

		return source.charAt(offset+index);
	}
}