/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.query.api.engine;

/**
 * @author Markus Gärtner
 *
 */
public class Tripwire {

	/** Indicates that frequent checks via {@link ThreadVerifier#checkThread()} are desired. */
	public static final boolean ACTIVE;

	public static final String TRIPWIRE_PROPERTY = "de.ims.icarus2.tripwire";

	/** Intended for the first testing phase to force {@link #ACTIVE} to always be {@code true}. */
	private static final boolean ACTIVE_OVERRIDE = true;

	static {
		ACTIVE = Boolean.parseBoolean(System.getProperty(TRIPWIRE_PROPERTY, "false")) || ACTIVE_OVERRIDE;
	}
}
