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

 * $Revision: 394 $
 * $Date: 2015-05-11 19:12:46 +0200 (Mo, 11 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/util/Messages.java $
 *
 * $LastChangedDate: 2015-05-11 19:12:46 +0200 (Mo, 11 Mai 2015) $
 * $LastChangedRevision: 394 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.util;

import de.ims.icarus2.model.manifest.api.LayerManifest;


/**
 * @author Markus Gärtner
 * @version $Id: Messages.java 394 2015-05-11 17:12:46Z mcgaerty $
 *
 */
public class Messages {

	private static String ensureMsg(String msg) {
		if(msg==null) {
			//FIXME Access stack trace and construct "Class.method(signature)" message
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
		return String.format("%s: no mapping from %s to %s", ensureMsg(msg), sourceLayer.getName(), targetLayer.getName()); //$NON-NLS-1$
	}

	public static String sizeMismatchMessage(String msg, long expected, long size) {
		return String.format("%s: expected size %d - got %d", ensureMsg(msg), expected, size); //$NON-NLS-1$
	}

	public static String offsetMismatchMessage(String msg, long expected, long index) {
		return String.format("%s: expected offset %d - got %d", ensureMsg(msg), expected, index); //$NON-NLS-1$
	}

	public static String illegalOffsetMessage(String msg, long index) {
		return String.format("%s: unexpected offset %d", ensureMsg(msg), index); //$NON-NLS-1$
	}

	public static String outOfBoundsMessage(String msg, long index, long min, long max) {
		return String.format("%s: value %d out of bounds [%d,%d]", ensureMsg(msg), index, min, max); //$NON-NLS-1$
	}

	public static String insufficientEdgesMessage(String msg, Object node, long required, long count) {
		return String.format("%s: insufficient edge count of %d at node %s - got %d", ensureMsg(msg), required, node, count); //$NON-NLS-1$
	}

	public static String edgesOverflowMessage(String msg, Object node, long max, long count) {
		return String.format("%s: edge count %d exceeds limit of %d at node %s", ensureMsg(msg), count, max, node); //$NON-NLS-1$
	}

	public static String indexOutOfBoundsMessage(String msg, Object container, long size, long index) {
		return String.format("%s: index %d exceeds size %d of container %s", ensureMsg(msg), index, size, container); //$NON-NLS-1$
	}

	public static String indexOutOfBoundsMessage(String msg, long min, long max, long index) {
		return String.format("%s: index %d exceeds range %d to %d", ensureMsg(msg), index, min, max); //$NON-NLS-1$
	}

	public static String indexNegativeMessage(String msg, Object container, long index) {
		return String.format("%s: negative index for container %s: %d", ensureMsg(msg), container, index); //$NON-NLS-1$
	}

	public static String foreignItemMessage(String msg, Object container, Object item) {
		return String.format("%s: item %s is not a valid member of the following container: %s", ensureMsg(msg), item, container); //$NON-NLS-1$
	}

	public static String foreignContainerMessage(String msg, Object expected, Object actual) {
		return String.format("%s: foreign container: expected %s - got %s", ensureMsg(msg), expected, actual); //$NON-NLS-1$
	}

	public static String nonOverlappingIntervalsMessage(String msg, long expectedBegin, long expectedEnd, long actualBegin, long actualEnd) {
		return String.format("%s: non overlapping intervals: [%d-%d] is outside legal space [%d,%d]", ensureMsg(msg),
				actualBegin, actualEnd, expectedBegin, expectedEnd);
	}

	public static String incompatibleFoundationLayersMessage(String msg, Object item0, Object item1, Object fLayer0, Object fLayer1) {
		return String.format("%s: expected identical foundation layers for items %s and %s - got %s and %s", ensureMsg(msg),
				item0, item1, fLayer0, fLayer1);
	}
}
