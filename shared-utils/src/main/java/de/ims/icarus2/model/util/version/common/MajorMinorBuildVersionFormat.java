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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.util.version.common;

import java.util.StringTokenizer;

import de.ims.icarus2.model.util.version.Version;
import de.ims.icarus2.model.util.version.VersionFormat;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MajorMinorBuildVersionFormat implements VersionFormat {

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Version o1, Version o2) {
		MajorMinorBuildVersion v1 = (MajorMinorBuildVersion)o1;
		MajorMinorBuildVersion v2 = (MajorMinorBuildVersion)o2;
		return v1.compareTo(v2);
	}

    /**
     * Version identifier parts separator.
     */
    public static final char SEPARATOR = '.';

    private static enum Field {
    	MAJOR,
    	MINOR,
    	BUILD,
    	INFO,
    	;

    	private static final Field[] _values = values();

    	public Field next() {
    		int indexOfNext = ordinal()+1;
    		return indexOfNext>=_values.length ? null : _values[indexOfNext];
    	}
    }

	/**
	 * @see de.ims.icarus2.model.util.version.VersionFormat#parseVersion(java.lang.String)
	 */
	@Override
	public Version parseVersion(String versionString) {
        int major = 0;
        int minor = 0;
        int build = 0;
        String info = "";

        StringTokenizer st = new StringTokenizer(versionString, String.valueOf(SEPARATOR), false);
        Field field = Field.MAJOR;

        while(field!=null && st.hasMoreTokens()) {
        	String token = st.nextToken();

        	// Accumulate tokens into info field after other fields are done
        	if(field==Field.INFO) {
        		info += token;
        	} else {
        		try {
        			// Try parsing token as number field
        			int numericValue = Integer.parseInt(token);

        			switch (field) {
					case MAJOR: major = numericValue; break;
					case MINOR: minor = numericValue; break;
					case BUILD: build = numericValue; break;

					default:
						break;
					}

        			// Step through fields in proper order
        			field = field.next();
        		} catch(NumberFormatException e) {
        			// As soon as a token is not a number anymore we'll have to treat the remaining parts as info
        			field = Field.INFO;
        			info = token;
        		}
        	}
        }

        return new MajorMinorBuildVersion(versionString, this, major, minor, build, info);
	}


	public static final class MajorMinorBuildVersion extends Version implements Comparable<MajorMinorBuildVersion> {

		private final int major, minir, build;
		private final String info;

		public MajorMinorBuildVersion(String versionString,
				VersionFormat versionFormat, int major, int minor, int build, String info) {
			super(versionString, versionFormat);

			if(info==null) {
				info = "";
			}

			this.major = major;
			this.minir = minor;
			this.build = build;
			this.info = info;
		}

		public int getMajor() {
			return major;
		}

		public int getMinir() {
			return minir;
		}

		public int getBuild() {
			return build;
		}

		public String getInfo() {
			return info;
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(MajorMinorBuildVersion o) {
			int result = major-o.major;
			if(result!=0) {
				return result;
			}

			result = minir-o.minir;
			if(result!=0) {
				return result;
			}

			result = build-o.build;
			if(result!=0) {
				return result;
			}

			if(info.equals(o.info)) {
				return 0;
			} else if(info.isEmpty()) {
				return -1;
			} else {
				return o.info.isEmpty() ? 1 : info.compareTo(o.info);
			}
		}
	}
}
