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

	public static String noSuchElement(String msg, Object element, Object host) {
		return String.format("%s: no such element in %s: %s", ensureMsg(msg), host, element);
	}

	public static String unexpectedElement(String msg, Object element, Object host) {
		return String.format("%s: unexpected element in %s: %s", ensureMsg(msg), host, element);
	}

	public static String mismatch(String msg, Object expected, Object provided) {
		return String.format("%s: expected %s - got %s", ensureMsg(msg), expected, provided); //$NON-NLS-1$
	}

	public static String alreadySet(String msg, Object present, Object given) {
		return String.format("%s: %s already present - cant set %s", ensureMsg(msg), present, given); //$NON-NLS-1$
	}

	public static String missingMapping(String msg, LayerManifest<?> sourceLayer, LayerManifest<?> targetLayer) {
		return String.format("%s: no mapping from %s to %s", ensureMsg(msg), sourceLayer.getId(), targetLayer.getId()); //$NON-NLS-1$
	}

	public static String sizeMismatch(String msg, long expected, long size) {
		return String.format("%s: expected size %d - got %d", ensureMsg(msg), _long(expected), _long(size)); //$NON-NLS-1$
	}

	public static String offsetMismatch(String msg, long expected, long index) {
		return String.format("%s: expected offset %d - got %d", ensureMsg(msg), _long(expected), _long(index)); //$NON-NLS-1$
	}

	public static String illegalOffset(String msg, long index) {
		return String.format("%s: unexpected offset %d", ensureMsg(msg), _long(index)); //$NON-NLS-1$
	}

	public static String outOfBounds(String msg, long index, long min, long max) {
		return String.format("%s: value %d out of bounds [%d,%d]", ensureMsg(msg), _long(index), _long(min), _long(max)); //$NON-NLS-1$
	}

	public static String insufficientEdges(String msg, Object node, long required, long count) {
		return String.format("%s: insufficient edge count of %d at node %s - got %d", ensureMsg(msg), _long(required), node, _long(count)); //$NON-NLS-1$
	}

	public static String edgesOverflow(String msg, Object node, long max, long count) {
		return String.format("%s: edge count %d exceeds limit of %d at node %s", ensureMsg(msg), _long(count), _long(max), node); //$NON-NLS-1$
	}

	public static String indexOutOfBounds(String msg, Object container, long size, long index) {
		return String.format("%s: index %d exceeds size %d of container %s", ensureMsg(msg), _long(index), _long(size), container); //$NON-NLS-1$
	}

	public static String indexOutOfBounds(String msg, long min, long max, long index) {
		return String.format("%s: index %d exceeds range %d to %d", ensureMsg(msg), _long(index), _long(min), _long(max)); //$NON-NLS-1$
	}

	public static String indexNegative(String msg, Object container, long index) {
		return String.format("%s: negative index for container %s: %d", ensureMsg(msg), container, _long(index)); //$NON-NLS-1$
	}

	public static String foreignItem(String msg, Object container, Object item) {
		return String.format("%s: item %s is not a valid member of the following container: %s", ensureMsg(msg), item, container); //$NON-NLS-1$
	}

	public static String foreignContainer(String msg, Object expected, Object actual) {
		return String.format("%s: foreign container: expected %s - got %s", ensureMsg(msg), expected, actual); //$NON-NLS-1$
	}

	public static String nonOverlappingIntervals(String msg, long expectedBegin, long expectedEnd, long actualBegin, long actualEnd) {
		return String.format("%s: non overlapping intervals: [%d-%d] is outside legal space [%d,%d]", ensureMsg(msg),
				_long(actualBegin), _long(actualEnd), _long(expectedBegin), _long(expectedEnd));
	}

	public static String incompatibleFoundationLayers(String msg, Object item0, Object item1, Object fLayer0, Object fLayer1) {
		return String.format("%s: expected identical foundation layers for items %s and %s - got %s and %s", ensureMsg(msg),
				item0, item1, fLayer0, fLayer1);
	}
}
