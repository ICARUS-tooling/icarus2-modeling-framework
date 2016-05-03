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

 * $Revision: 443 $
 * $Date: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/xml/UnexpectedTagException.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.xml;

import org.xml.sax.SAXException;

/**
 * @author Markus Gärtner
 * @version $Id: UnexpectedTagException.java 443 2016-01-11 11:31:11Z mcgaerty $
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
