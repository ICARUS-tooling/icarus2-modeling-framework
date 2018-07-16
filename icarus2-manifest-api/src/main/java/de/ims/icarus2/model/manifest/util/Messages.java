/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 *
 */
package de.ims.icarus2.model.manifest.util;

import static de.ims.icarus2.util.lang.Primitives._long;

import de.ims.icarus2.model.manifest.api.LayerManifest;


/**
 * @author Markus Gärtner
 *
 */
public class Messages {

	/**
	 * Internal helper class to access call stack information in a cheaper way than
	 * via {@link Thread#getStackTrace()}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	private static final class CallingClass extends SecurityManager {
	    public static final CallingClass INSTANCE = new CallingClass();

	    /**
	     * @see SecurityManager#getClassContext()
	     */
	    @SuppressWarnings("rawtypes")
		public Class[] getCallingClasses() {
	        return getClassContext();
	    }
	}

	private static String ensureMsg(String msg) {
		if(msg==null) {
			@SuppressWarnings("rawtypes")
			Class[] trace = CallingClass.INSTANCE.getCallingClasses();

			for(int i=1; i<trace.length; i++) {
				@SuppressWarnings("rawtypes")
				Class clazz = trace[i];

				if(clazz!=CallingClass.class && clazz!=Messages.class) {
					return clazz.getName();
				}
			}
		}

		return msg;
	}

	public static String mismatchMessage(String msg, Object expected, Object provided) {
		return String.format("%s: expected %s - got %s", ensureMsg(msg), expected, provided); //$NON-NLS-1$
	}

	public static String alreadySetMessage(String msg, Object present, Object given) {
		return String.format("%s: %s already present - cant set %s", ensureMsg(msg), present, given); //$NON-NLS-1$
	}

	public static String missingMappingMessage(String msg, LayerManifest sourceLayer, LayerManifest targetLayer) {
		return String.format("%s: no mapping from %s to %s", ensureMsg(msg), sourceLayer.getId(), targetLayer.getId()); //$NON-NLS-1$
	}

	public static String sizeMismatchMessage(String msg, long expected, long size) {
		return String.format("%s: expected size %d - got %d", ensureMsg(msg), _long(expected), _long(size)); //$NON-NLS-1$
	}

	public static String offsetMismatchMessage(String msg, long expected, long index) {
		return String.format("%s: expected offset %d - got %d", ensureMsg(msg), _long(expected), _long(index)); //$NON-NLS-1$
	}

	public static String illegalOffsetMessage(String msg, long index) {
		return String.format("%s: unexpected offset %d", ensureMsg(msg), _long(index)); //$NON-NLS-1$
	}

	public static String outOfBoundsMessage(String msg, long index, long min, long max) {
		return String.format("%s: value %d out of bounds [%d,%d]", ensureMsg(msg), _long(index), _long(min), _long(max)); //$NON-NLS-1$
	}

	public static String insufficientEdgesMessage(String msg, Object node, long required, long count) {
		return String.format("%s: insufficient edge count of %d at node %s - got %d", ensureMsg(msg), _long(required), node, _long(count)); //$NON-NLS-1$
	}

	public static String edgesOverflowMessage(String msg, Object node, long max, long count) {
		return String.format("%s: edge count %d exceeds limit of %d at node %s", ensureMsg(msg), _long(count), _long(max), node); //$NON-NLS-1$
	}

	public static String indexOutOfBoundsMessage(String msg, Object container, long size, long index) {
		return String.format("%s: index %d exceeds size %d of container %s", ensureMsg(msg), _long(index), _long(size), container); //$NON-NLS-1$
	}

	public static String indexOutOfBoundsMessage(String msg, long min, long max, long index) {
		return String.format("%s: index %d exceeds range %d to %d", ensureMsg(msg), _long(index), _long(min), _long(max)); //$NON-NLS-1$
	}

	public static String indexNegativeMessage(String msg, Object container, long index) {
		return String.format("%s: negative index for container %s: %d", ensureMsg(msg), container, _long(index)); //$NON-NLS-1$
	}

	public static String foreignItemMessage(String msg, Object container, Object item) {
		return String.format("%s: item %s is not a valid member of the following container: %s", ensureMsg(msg), item, container); //$NON-NLS-1$
	}

	public static String foreignContainerMessage(String msg, Object expected, Object actual) {
		return String.format("%s: foreign container: expected %s - got %s", ensureMsg(msg), expected, actual); //$NON-NLS-1$
	}

	public static String nonOverlappingIntervalsMessage(String msg, long expectedBegin, long expectedEnd, long actualBegin, long actualEnd) {
		return String.format("%s: non overlapping intervals: [%d-%d] is outside legal space [%d,%d]", ensureMsg(msg),
				_long(actualBegin), _long(actualEnd), _long(expectedBegin), _long(expectedEnd));
	}

	public static String incompatibleFoundationLayersMessage(String msg, Object item0, Object item1, Object fLayer0, Object fLayer1) {
		return String.format("%s: expected identical foundation layers for items %s and %s - got %s and %s", ensureMsg(msg),
				item0, item1, fLayer0, fLayer1);
	}
}
