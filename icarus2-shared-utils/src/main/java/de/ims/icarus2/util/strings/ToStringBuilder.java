/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.text.DecimalFormat;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public class ToStringBuilder {

	/**
	 * Per default, square brackets '[]' are used.
	 */
	public static final BracketStyle DEFAULT_BRACKET_STYLE = BracketStyle.SQUARE;

	/**
	 * Creates a fresh new builder that will leave all formatting
	 * to the user code, except for adding commas as delimiters
	 * between the output of field assignments.
	 */
	public static ToStringBuilder create() {
		return new ToStringBuilder(null);
	}

	/**
	 * Creates a fresh new builder that will automatically prepend
	 * the given target's class name to the field list and also wrap
	 * the entire output into {@link #DEFAULT_BRACKET_STYLE brackets}.
	 */
	public static ToStringBuilder create(Object target) {
		return create(target, DEFAULT_BRACKET_STYLE);
	}

	/**
	 * Creates a fresh new builder that will automatically prepend
	 * the given target's class name to the field list and also wrap
	 * the entire output into brackets of the specified style.
	 */
	public static ToStringBuilder create(Object target, BracketStyle bracketStyle) {
		return new ToStringBuilder(bracketStyle)
				.add(bracketStyle.openingBracket)
				.add(target.getClass().getSimpleName())
				.requireSpace();
	}

	private final StringBuilder buffer;
	private final BracketStyle bracketStyle;

	private boolean closed;
	private boolean needsDelimiter;
	private boolean needsSpace;

	private DecimalFormat decimalFormat, fractionDecimalFormat;

	private ToStringBuilder(BracketStyle bracketStyle) {
		this.bracketStyle = bracketStyle;
		buffer = new StringBuilder(50);
	}

	public ToStringBuilder requireSpace() {
		needsSpace = true;
		return this;
	}

	private String format(long value) {
		if(decimalFormat==null) {
			decimalFormat = new DecimalFormat("#,###");
		}
		return decimalFormat.format(value);
	}

	private String format(double value) {
		if(fractionDecimalFormat==null) {
			fractionDecimalFormat = new DecimalFormat("#,##0.00");
		}
		return fractionDecimalFormat.format(value);
	}

	private void maybeAddSpace() {
		if(needsSpace) {
			buffer.append(' ');
			needsSpace = false;
		}
	}

	public ToStringBuilder add(String s) {
		requireNonNull(s);
		maybeAddSpace();
		buffer.append(s);
		return this;
	}

	public ToStringBuilder add(char c) {
		maybeAddSpace();
		buffer.append(c);
		return this;
	}

	public ToStringBuilder add(String name, String value) {
		maybeAddSpace();

		// Add field separator if needed
		if(needsDelimiter) {
			buffer.append(", ");
		}

		buffer.append(name).append('=').append(value);
		needsDelimiter = true;

		return this;
	}

	/**
	 * Automatically extracts the value of {@link Optional} if given.
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	public ToStringBuilder add(String name, Object value) {
		while(value instanceof Optional) {
			value = ((Optional<?>)value).orElse(null);
		}
		return add(name, value==null ? null : value.toString());
	}

	public ToStringBuilder add(String name, byte value) {
		return add(name, String.valueOf(value));
	}

	public ToStringBuilder add(String name, short value) {
		return add(name, String.valueOf(value));
	}

	public ToStringBuilder addFormatted(String name, short value) {
		return add(name, format(value));
	}

	public ToStringBuilder add(String name, int value) {
		return add(name, String.valueOf(value));
	}

	public ToStringBuilder addFormatted(String name, int value) {
		return add(name, format(value));
	}

	public ToStringBuilder add(String name, long value) {
		return add(name, String.valueOf(value));
	}

	public ToStringBuilder addFormatted(String name, long value) {
		return add(name, format(value));
	}

	public ToStringBuilder add(String name, float value) {
		return add(name, String.valueOf(value));
	}

	public ToStringBuilder addFormatted(String name, float value) {
		return add(name, format(value));
	}

	public ToStringBuilder add(String name, double value) {
		return add(name, String.valueOf(value));
	}

	public ToStringBuilder addFormatted(String name, double value) {
		return add(name, format(value));
	}

	public ToStringBuilder add(String name, boolean value) {
		return add(name, String.valueOf(value));
	}

	public ToStringBuilder add(String name, char value) {
		return add(name, String.valueOf(value));
	}

	public String build() {
		if(!closed && bracketStyle!=null) {
			buffer.append(bracketStyle.closingBracket);
			closed = true;
		}
		return buffer.toString();
	}

	/**
	 * Defaults to {@link #build()}.
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return build();
	}
}
