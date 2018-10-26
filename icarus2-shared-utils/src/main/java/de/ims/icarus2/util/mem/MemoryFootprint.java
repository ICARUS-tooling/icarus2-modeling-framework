/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.mem;

import java.util.Set;

/**
 * @author Markus Gärtner
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
