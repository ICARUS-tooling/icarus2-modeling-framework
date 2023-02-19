/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.version.common;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringTokenizer;

import de.ims.icarus2.util.strings.StringPrimitives;
import de.ims.icarus2.util.version.Version;
import de.ims.icarus2.util.version.VersionFormat;

/**
 * Implements a versioning scheme composed of the following 4 fields:
 *
 * <tt>MAJOR.MINOR.RELEASE INFO</tt>
 *
 * @author Markus Gärtner
 *
 */
public class MajorMinorReleaseVersionFormat implements VersionFormat, Serializable {

	private static final long serialVersionUID = 4387226642863523438L;


	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Version o1, Version o2) {
		MajorMinorReleaseVersion v1 = (MajorMinorReleaseVersion)o1;
		MajorMinorReleaseVersion v2 = (MajorMinorReleaseVersion)o2;
		return v1.compareTo(v2);
	}

    /**
     * Version identifier parts separator.
     */
    public static final char SEPARATOR = '.';

    private static enum Field {
    	MAJOR,
    	MINOR,
    	RELEASE,
    	INFO,
    	;

    	private static final Field[] _values = values();

    	public Field next() {
    		int indexOfNext = ordinal()+1;
    		return indexOfNext>=_values.length ? null : _values[indexOfNext];
    	}
    }

	/**
	 * @see de.ims.icarus2.util.version.VersionFormat#parseVersion(java.lang.String)
	 */
	@Override
	public MajorMinorReleaseVersion parseVersion(String versionString) {
		requireNonNull(versionString);
    	checkArgument("Version string must not be empty", !versionString.isEmpty());

        int major = 0;
        int minor = 0;
        int release = 0;
        String info = "";

        StringTokenizer st = new StringTokenizer(versionString, String.valueOf(SEPARATOR), false);
        Field field = Field.MAJOR;

        while(field!=null && st.hasMoreTokens()) {
        	String token = st.nextToken();

        	// Accumulate tokens into info field after other fields are done
        	if(field==Field.INFO) {
        		info += token;
        	} else {

        		int lastChar = token.length()-1;

        		// Scan till end of digit sequence
        		int lastDigit = -1;
        		for(int i=0; i<token.length(); i++) {
        			if(!Character.isDigit(token.charAt(i))) {
        				break;
        			}

        			lastDigit++;
        		}

        		if(lastDigit>-1) {
        			int numericValue = StringPrimitives.parseInt(token, 0, lastDigit);

        			switch (field) {
					case MAJOR: major = numericValue; break;
					case MINOR: minor = numericValue; break;
					case RELEASE: release = numericValue; break;

					default:
						break;
					}

        			// Step through fields in proper order
        			field = field.next();
        		}

        		if(lastDigit<lastChar) {
        			// As soon as a token contains more than digits we'll have to treat the remaining parts as info
        			field = Field.INFO;
        			info = token.substring(lastDigit+1);
        		}
        	}
        }

        if(info!=null) {
        	info = info.trim();
        }

        return new MajorMinorReleaseVersion(versionString, this, major, minor, release, info);
	}


	public static final class MajorMinorReleaseVersion extends Version implements Comparable<MajorMinorReleaseVersion> {

		private final int major, minor, release;
		private final String info;

		private MajorMinorReleaseVersion(String versionString,
				VersionFormat versionFormat, int major, int minor, int release, String info) {
			super(versionString, versionFormat);

			if("".equals(info)) {
				info = null;
			}

			this.major = major;
			this.minor = minor;
			this.release = release;
			this.info = info;
		}

		public int getMajor() {
			return major;
		}

		public int getMinor() {
			return minor;
		}

		public int getRelease() {
			return release;
		}

		public String getInfo() {
			return info;
		}

//		/**
//		 * @see de.ims.icarus2.util.version.Version#toString()
//		 */
//		@Override
//		public String toString() {
//			return String.format("%d%s%d%s%d%s",
//					_int(major), _char(SEPARATOR), _int(minor), _char(SEPARATOR), _int(release),
//					info==null ? "" : " "+info);
//		}

		/**
		 * @see de.ims.icarus2.util.version.Version#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj==this) return true;
			if(obj==null || obj.getClass()!=getClass()) return false;
			MajorMinorReleaseVersion other = (MajorMinorReleaseVersion) obj;
			return major==other.major
					&& minor==other.minor
					&& release==other.release
					&& Objects.equals(info, other.info);
		}

		/**
		 * @see de.ims.icarus2.util.version.Version#hashCode()
		 */
		@Override
		public int hashCode() {
			return Arrays.hashCode(new int[] {
					major, minor, release, info==null ? 0 : info.hashCode()
			});
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(MajorMinorReleaseVersion o) {
			int result = major-o.major;
			if(result!=0) {
				return result;
			}

			result = minor-o.minor;
			if(result!=0) {
				return result;
			}

			result = release-o.release;
			if(result!=0) {
				return result;
			}

			if(Objects.equals(info, o.info)) {
				return 0;
			} else if(info==null) {
				return -1;
			} else {
				return o.info==null ? 1 : info.compareTo(o.info);
			}
		}
	}
}
