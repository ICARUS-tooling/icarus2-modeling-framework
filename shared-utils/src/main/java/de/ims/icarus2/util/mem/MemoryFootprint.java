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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/mem/MemoryFootprint.java $
 *
 * $LastChangedDate: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $LastChangedRevision: 380 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.mem;

import java.util.Set;

/**
 * @author Markus Gärtner
 * @version $Id: MemoryFootprint.java 380 2015-04-02 01:28:48Z mcgaerty $
 *
 */
public interface MemoryFootprint {

	public static final int OBJECT_SHELL_SIZE   = 8;
	public static final int OBJREF_SIZE_32      = 4;
	public static final int OBJREF_SIZE_64      = 8;
	public static final int LONG_FIELD_SIZE     = 8;
	public static final int INT_FIELD_SIZE      = 4;
	public static final int SHORT_FIELD_SIZE    = 2;
	public static final int CHAR_FIELD_SIZE     = 2;
	public static final int BYTE_FIELD_SIZE     = 1;
	public static final int BOOLEAN_FIELD_SIZE  = 1;
	public static final int DOUBLE_FIELD_SIZE   = 8;
	public static final int FLOAT_FIELD_SIZE    = 4;

	Object getRootObject();

	long getFootprint();

	long getObjectCount();

	long getReferenceCount();

	long getDownlinkCount();

	long getUplinkCount();

	long getPrimitiveCount();

	long getArrayCount();

	long getIntegerCount();
	long getLongCount();
	long getShortCount();
	long getByteCount();
	long getBooleanCount();
	long getCharacterCount();
	long getFloatCount();
	long getDoubleCount();

	Set<Class<?>> getClasses();

	long getInstanceCount(Class<?> clazz);

	long getInstanceFootprint(Class<?> clazz);
}
