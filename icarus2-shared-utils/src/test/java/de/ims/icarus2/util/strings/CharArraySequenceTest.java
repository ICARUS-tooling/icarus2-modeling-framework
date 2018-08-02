/**
 *
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
