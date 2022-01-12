/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

/**
 * @author Markus Gärtner
 *
 */
class CharArraySequenceTest implements StringTest<CharArraySequence> {

	/**
	 * @see de.ims.icarus2.util.strings.StringTest#createEmptySequence()
	 */
	@Override
	public CharArraySequence createEmptySequence() {
		return new CharArraySequence();
	}

	/**
	 * @see de.ims.icarus2.util.strings.StringTest#createSequence(int)
	 */
	@Override
	public CharArraySequence createSequence(int length) {
		char[] tmp = new char[length];

		for(int i=0; i<tmp.length; i++) {
			tmp[i] = (i%2==0) ? 'a' : 'b';
		}

		return new CharArraySequence(tmp, 0, length);
	}

	/**
	 * @see de.ims.icarus2.util.strings.StringTest#createSequence(java.lang.String)
	 */
	@Override
	public CharArraySequence createSequence(String source) {
		if(source.isEmpty()) {
			return createEmptySequence();
		}

		char[] chars = source.toCharArray();
		return new CharArraySequence(chars, 0, chars.length);
	}

}
