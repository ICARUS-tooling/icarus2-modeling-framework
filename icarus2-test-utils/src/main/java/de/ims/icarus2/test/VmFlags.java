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
package de.ims.icarus2.test;

/**
 * @author Markus Gärtner
 *
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html">
 * https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html</a>
 */
public class VmFlags {

	private static final String PREFIX = "-XX:+";

	public static final String PRINT_COMPILATION = PREFIX+"PrintCompilation";

	/**
	 * Requires {@link #UNLOCK_DIAGNOSTIC}
	 */
	public static final String PRINT_INLINING = PREFIX+"PrintInlining";

	public static final String UNLOCK_DIAGNOSTIC = PREFIX+"UnlockDiagnosticVMOptions";
}
