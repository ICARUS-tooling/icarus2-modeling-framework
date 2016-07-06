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
package de.ims.icarus2.util.xml;

import org.xml.sax.SAXException;

/**
 * @author Markus Gärtner
 *
 */
public class UnsupportedNestingException extends SAXException {

	private static final long serialVersionUID = -1625330590270502105L;

	private final String qName, environment;

	public UnsupportedNestingException(String qName, String environment) {
		super("No nesting of type "+qName+" allowed within "+environment+" environment");

		this.qName = qName;
		this.environment = environment;
	}

	public String getqName() {
		return qName;
	}

	public String getEnvironment() {
		return environment;
	}
}
