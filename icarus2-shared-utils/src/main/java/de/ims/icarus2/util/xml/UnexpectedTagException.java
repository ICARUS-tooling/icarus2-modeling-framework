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
package de.ims.icarus2.util.xml;

import org.xml.sax.SAXException;

/**
 * Signals an invalid element beginning or ending within a certain
 * context/environment.
 *
 * @author Markus Gärtner
 *
 */
public class UnexpectedTagException extends SAXException {

	private static final long serialVersionUID = -4329505837453734565L;

	private final String qName, environment;
	private final boolean isOpening;

	public UnexpectedTagException(String qName, boolean isOpening, String environment) {
		super("Unrecognized "+(isOpening ? "opening" : "end")+" tag '"+qName+"' in "+environment+" environment");

		this.isOpening = isOpening;
		this.qName = qName;
		this.environment = environment;
	}

	public String getqName() {
		return qName;
	}

	public String getEnvironment() {
		return environment;
	}

	public boolean isOpening() {
		return isOpening;
	}

}
