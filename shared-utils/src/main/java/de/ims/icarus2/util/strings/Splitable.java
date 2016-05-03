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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/strings/Splitable.java $
 *
 * $LastChangedDate: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $LastChangedRevision: 380 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.strings;

import java.util.Arrays;
import java.util.regex.Matcher;

public abstract class Splitable extends AbstractString {

		// Scopes
		private int splitCount;
		private int[] splitIndices;

		@Override
		public abstract Splitable subSequence(int begin, int end);

		protected abstract Matcher getCachedMatcher(String regex);

		protected abstract void recycleMatcher(Matcher matcher);

		private void addSplit(int index0, int index1) {
			if(index0>index1) {
				return;
			}

			int idx = splitCount*2;

			if (splitIndices==null) {
//				System.out.println("creating split buffer");
				splitIndices = new int[Math.max(32, idx*2+2)];
			} else if(splitIndices.length<idx+1) {
//				System.out.println("expanding split buffer to capacity: "+(idx*2+1));
				splitIndices = Arrays.copyOf(splitIndices, idx*2+2);
			}

			splitIndices[idx] = index0;
			splitIndices[idx+1] = index1;

			splitCount++;
		}

		public int getSplitCount() {
			return splitCount;
		}

		public Splitable getSplitCursor(int index) {
			if(index<0 || index>=splitCount)
				throw new IndexOutOfBoundsException();

			index += index;

			return subSequence(splitIndices[index], splitIndices[index+1]);
		}

		public int getStart(int index) {
			if(index<0 || index>=splitCount)
				throw new IndexOutOfBoundsException();

			return splitIndices[index+index];
		}

		public int getEnd(int index) {
			if(index<0 || index>=splitCount)
				throw new IndexOutOfBoundsException();

			return splitIndices[index+index+1];
		}

		public int split(CharSequence regex, int limit) {

	        /* fastpath if the regex is a
	         (1)one-char String and this character is not one of the
	            RegEx's meta characters ".$|()[{^?*+\\", or
	         (2)two-char String and the first char is the backslash and
	            the second is not the ascii digit or ascii letter.
	         */
	        char ch = 0;
	        if (((regex.length() == 1 &&
	             ".$|()[{^?*+\\".indexOf(ch = regex.charAt(0)) == -1) || //$NON-NLS-1$
	             (regex.length() == 2 &&
	              regex.charAt(0) == '\\' &&
	              (((ch = regex.charAt(1))-'0')|('9'-ch)) < 0 &&
	              ((ch-'a')|('z'-ch)) < 0 &&
	              ((ch-'A')|('Z'-ch)) < 0)) &&
	            (ch < Character.MIN_HIGH_SURROGATE ||
	             ch > Character.MAX_LOW_SURROGATE))
	        {
	        	return split(ch, limit);
	        } else {
	        	resetSplits();

	        	Matcher m = getCachedMatcher(regex.toString());
	        	int off = 0;
	        	while(m.find() && (limit==0 || splitCount<limit)) {
	        		if(m.start()>off) {
		        		addSplit(off, m.start());
	        		}

	        		off = m.end();
	        	}

	        	if(off<length() && (limit==0 || splitCount<limit)) {
	        		addSplit(off, length());
	        	}

	        	recycleMatcher(m);

	            return splitCount;
	        }
		}

		public int split(CharSequence regex) {
			return split(regex, 0);
		}

		public int split(char ch, int limit) {
			resetSplits();

			int width = length();
            int off = 0;
            int next = 0;
            boolean limited = limit > 0;
            while ((next = indexOf(ch, off)) != -1) {
                if (!limited || splitCount < limit - 1) {
                    addSplit(off, next);
                    off = next + 1;
                } else {    // last one
                    //assert (list.size() == limit - 1);
                    addSplit(off, width);
                    off = width;
                    break;
                }
            }

            // If no match was found, return this
//            if (off == 0) {
//            	return 1;
//            }

            // Add remaining segment
            if (!limited || splitCount < limit)
            	addSplit(off, width);

            return splitCount;
		}

		public int split(char c) {
			return split(c, 0);
		}

		public abstract void recycle();

		protected void resetSplits() {
			splitCount = 0;
		}

		protected void closeSplits() {
			splitCount = 0;
//			System.out.println("deleting split buffer");
			splitIndices = null;
		}
	}