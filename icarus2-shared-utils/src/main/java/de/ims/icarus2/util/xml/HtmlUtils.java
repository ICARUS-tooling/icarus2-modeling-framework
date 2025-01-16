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
package de.ims.icarus2.util.xml;

import static de.ims.icarus2.util.lang.Primitives._int;

import java.awt.Color;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus2.util.collections.ArrayUtils;

/**
 *
 * @author Markus Gärtner
 *
 */
public final class HtmlUtils {

	private HtmlUtils() {
	}

	public static String hexString(Color color) {
		return String.format("#%02X%02X%02X",  //$NON-NLS-1$
				_int(color.getRed()),
				_int(color.getGreen()),
				_int(color.getBlue()));
	}

	//TODO refactor to move use primitive map
	@SuppressWarnings("boxing")
	private static Map<Object, Object> htmlReplacements = ArrayUtils.asMap(
		60, "&lt;", //< //$NON-NLS-1$
		62, "&gt;", //> //$NON-NLS-1$
		38, "&amp;", //& //$NON-NLS-1$
		34, "&quot;", //" //$NON-NLS-1$
		224, "&agrave;", //à //$NON-NLS-1$
		192, "&Agrave;", //À //$NON-NLS-1$
		226, "&acirc;", //â //$NON-NLS-1$
		194, "&Acirc;", //Â //$NON-NLS-1$
		228, "&auml;", //ä //$NON-NLS-1$
		196, "&Auml;", //Ä //$NON-NLS-1$
		229, "&aring;", //å //$NON-NLS-1$
		197, "&Aring;", //Å //$NON-NLS-1$
		230, "&aelig;", //æ //$NON-NLS-1$
		198, "&AElig;", //Æ //$NON-NLS-1$
		231, "&ccedil;", //ç //$NON-NLS-1$
		199, "&Ccedil;", //Ç //$NON-NLS-1$
		233, "&eacute;", //é //$NON-NLS-1$
		201, "&Eacute;", //É //$NON-NLS-1$
		232, "&egrave;", //è //$NON-NLS-1$
		200, "&Egrave;", //È //$NON-NLS-1$
		234, "&ecirc;", //ê //$NON-NLS-1$
		202, "&Ecirc;", //Ê //$NON-NLS-1$
		235, "&euml;", //ë //$NON-NLS-1$
		203, "&Euml;", //Ë //$NON-NLS-1$
		239, "&iuml;", //ï //$NON-NLS-1$
		207, "&Iuml;", //Ï //$NON-NLS-1$
		244, "&ocirc;", //ô //$NON-NLS-1$
		212, "&Ocirc;", //Ô //$NON-NLS-1$
		246, "&ouml;", //ö //$NON-NLS-1$
		214, "&Ouml;", //Ö //$NON-NLS-1$
		248, "&oslash;", //ø //$NON-NLS-1$
		216, "&Oslash;", //Ø //$NON-NLS-1$
		223, "&szlig;", //ß //$NON-NLS-1$
		249, "&ugrave;", //ù //$NON-NLS-1$
		217, "&Ugrave;", //Ù //$NON-NLS-1$
		251, "&ucirc;", //û //$NON-NLS-1$
		219, "&Ucirc;", //Û //$NON-NLS-1$
		252, "&uuml;", //ü //$NON-NLS-1$
		220, "&Uuml;", //Ü //$NON-NLS-1$
		174, "&reg;", //® //$NON-NLS-1$
		169, "&copy;", //© //$NON-NLS-1$
		8364, "&euro;" //€ //$NON-NLS-1$
	);

	@SuppressWarnings("boxing")
	public static String escapeHTML(String s) {
		StringBuffer sb = new StringBuffer(s.length());
		int n = s.length();
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			Object r = htmlReplacements.get((int)c);

			sb.append(r==null ? c : r);
		}
		return sb.toString();
	}

	private static final Matcher BREAK_MATCHER = Pattern.compile("<[bB][rR]>").matcher("");

	/**
	 * Replaces all occurrences of the html break tag
	 * {@code <br>} into regular newline symbols.
	 * @param s
	 * @return
	 */
	public static String revertBreaks(CharSequence s) {
		synchronized (BREAK_MATCHER) {
			return BREAK_MATCHER.reset(s).replaceAll("\n");
		}
	}

	public static abstract class HtmlBuilder {
		protected StringBuilder builder;

		protected boolean building = false;

		protected HtmlBuilder(int capacity) {
			builder = new StringBuilder(capacity);
		}

		protected HtmlBuilder() {
			this(40);
		}

		public HtmlBuilder start() {
			builder.setLength(0);
			building = true;
			start0();

			return this;
		}

		public boolean isBuilding() {
			return building;
		}

		public HtmlBuilder finish() {
			finish0();
			building = false;

			return this;
		}

		protected abstract void finish0();

		protected abstract void start0();

		public String getResult() {
			return building ? null : builder.toString();
		}
	}

	public static class HtmlTableBuilder extends HtmlBuilder {

		protected int columnCount = DEFAULT_COLUMN_COUNT;

		protected boolean leadingColon = false;
		protected boolean headerSet = false;

		public static final int DEFAULT_COLUMN_COUNT = 2;

		/**
		 *
		 */
		public HtmlTableBuilder() {
			super(100);
		}

		/**
		 * @param capacity
		 */
		public HtmlTableBuilder(int capacity) {
			super(capacity);
		}

		@Override
		public HtmlTableBuilder start() {
			return start(DEFAULT_COLUMN_COUNT, false);
		}

		public HtmlTableBuilder start(int columnCount) {
			return start(columnCount, false);
		}

		public HtmlTableBuilder start(boolean leadingColon) {
			return start(DEFAULT_COLUMN_COUNT, leadingColon);
		}

		public HtmlTableBuilder start(int columnCount, boolean leadingColon) {
			super.start();
			this.columnCount = columnCount;
			this.leadingColon = leadingColon;
			headerSet = false;

			return this;
		}

		@Override
		protected void finish0() {
			builder.append("</table></html>"); //$NON-NLS-1$
		}

		@Override
		protected void start0() {
			builder.append("<html><table width=\"100%\">"); //$NON-NLS-1$
		}

		public HtmlTableBuilder setHeader(Object...items) {
			if(headerSet || items.length!=columnCount)
				throw new IllegalArgumentException();

			builder.append("<colgroup>"); //$NON-NLS-1$

			for(Object item : items)
				builder.append("<col width=\"").append(String.valueOf(item)) //$NON-NLS-1$
						.append("\" />"); //$NON-NLS-1$

			builder.append("</colgroup>"); //$NON-NLS-1$

			return this;
		}

		public HtmlTableBuilder addRowEscaped(String...items) {
			builder.append("<tr>"); //$NON-NLS-1$

			for(int i=0; i<items.length; i++) {
				if(i>=columnCount)
					break;

				builder.append("<td>").append(escapeHTML(items[i])) //$NON-NLS-1$
						.append((i==0 && leadingColon) ? ":</td>" : "</td>"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if(items.length<columnCount)
				for(int i=columnCount-1; i>=items.length; i--)
					builder.append("<td></td>"); //$NON-NLS-1$

			builder.append("</tr>"); //$NON-NLS-1$

			return this;
		}

		public HtmlTableBuilder addRow(String...items) {
			builder.append("<tr>"); //$NON-NLS-1$

			for(int i=0; i<items.length; i++) {
				if(i>=columnCount)
					break;

				builder.append("<td>").append(items[i]) //$NON-NLS-1$
						.append((i==0 && leadingColon) ? ":</td>" : "</td>"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if(items.length<columnCount)
				for(int i=columnCount-1; i>=items.length; i--)
					builder.append("<td></td>"); //$NON-NLS-1$

			builder.append("</tr>"); //$NON-NLS-1$

			return this;
		}

		/**
		 * @return the columnCount
		 */
		public int getColumnCount() {
			return columnCount;
		}

		/**
		 * @param columnCount the columnCount to set
		 */
		public HtmlTableBuilder setColumnCount(int columnCount) {
			if(isBuilding())
				throw new IllegalStateException();

			this.columnCount = columnCount;

			return this;
		}

		/**
		 * @return the leadingColon
		 */
		public boolean isLeadingColon() {
			return leadingColon;
		}

		/**
		 * @param leadingColon the leadingColon to set
		 */
		public HtmlTableBuilder setLeadingColon(boolean leadingColon) {
			this.leadingColon = leadingColon;

			return this;
		}
	}

	public static class HtmlLineBuilder extends HtmlBuilder {

		protected boolean omitBreakIfEmpty = true;

		/**
		 *
		 */
		public HtmlLineBuilder() {
			super();
		}

		/**
		 * @param capacity
		 */
		public HtmlLineBuilder(int capacity) {
			super(capacity);
		}

		@Override
		public HtmlLineBuilder start() {
			return start(true);
		}

		public HtmlLineBuilder start(boolean omitBreakIfEmpty) {
			super.start();
			this.omitBreakIfEmpty = omitBreakIfEmpty;

			return this;
		}

		@Override
		protected void start0() {
			builder.append("<html>"); //$NON-NLS-1$
		}

		@Override
		protected void finish0() {
			builder.append("</html>"); //$NON-NLS-1$
		}

		public HtmlLineBuilder newLine() {
			if(!omitBreakIfEmpty || builder.length()>6) // length of '<html>'
				builder.append("<br>"); //$NON-NLS-1$

			return this;
		}

		public HtmlLineBuilder appendEscaped(String item) {
			return appendEscaped(item, null);
		}

		public HtmlLineBuilder appendEscaped(String item, Color color) {
			return append(escapeHTML(item), color);
		}

		public HtmlLineBuilder append(String item) {
			return append(item, null);
		}

		public HtmlLineBuilder append(String item, Color color) {
			if(color!=null)
				builder.append("<font color=\"").append(hexString(color)).append("\">"); //$NON-NLS-1$ //$NON-NLS-2$
			builder.append(item);
			if(color!=null)
				builder.append("</font>"); //$NON-NLS-1$

			return this;
		}

		public HtmlLineBuilder appendIfNonempty(String item) {
			return appendIfNonempty(item, null);
		}

		public HtmlLineBuilder appendIfNonempty(String item, Color color) {
			if(builder.length()>6) {
				return append(item, color);
			}

			return this;
		}

		public boolean isEmpty() {
			return builder.length()<= (building ? 6 : 13);
		}

		/**
		 * @return the omitBreakIfEmpty
		 */
		public boolean isOmitBreakIfEmpty() {
			return omitBreakIfEmpty;
		}

		/**
		 * @param omitBreakIfEmpty the omitBreakIfEmpty to set
		 */
		public HtmlLineBuilder setOmitBreakIfEmpty(boolean omitBreakIfEmpty) {
			this.omitBreakIfEmpty = omitBreakIfEmpty;

			return this;
		}
	}
}
