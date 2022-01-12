/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test.guard;

/**
 * @author Markus Gärtner
 *
 */
public class GuardException extends RuntimeException {

	private static final long serialVersionUID = -8368707973333304787L;

	/**
	 * @param message
	 * @param cause
	 */
	public GuardException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public GuardException(String message) {
		super(message);
	}

}
