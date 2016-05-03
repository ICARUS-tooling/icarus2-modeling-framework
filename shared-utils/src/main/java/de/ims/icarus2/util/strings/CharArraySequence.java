/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.

 * $Revision: 380 $
 * $Date: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/strings/CharArraySequence.java $
 *
 * $LastChangedDate: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $LastChangedRevision: 380 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.strings;

public class CharArraySequence extends AbstractString {

	private final char[] buffer;
	private final int offset;
	private final int len;

	public CharArraySequence(char[] buffer, int offset, int len) {
		if (buffer == null)
			throw new NullPointerException("Invalid buffer"); //$NON-NLS-1$
		if(offset<0 || offset>=buffer.length)
			throw new IndexOutOfBoundsException("offset"); //$NON-NLS-1$
		if(len<0 || len>=buffer.length-offset)
			throw new IllegalArgumentException("length"); //$NON-NLS-1$

		this.buffer = buffer;
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
		if(index>=len || index<0)
			throw new IndexOutOfBoundsException();

		return buffer[offset+index];
	}

	/**
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence(int start, int end) {
		if(start<0 || start>=len)
			throw new IllegalArgumentException("start"); //$NON-NLS-1$
		if(end<0 || end>=len || end<start)
			throw new IllegalArgumentException("end"); //$NON-NLS-1$

		return new CharArraySequence(buffer, offset+start, end-start+1);
	}
}