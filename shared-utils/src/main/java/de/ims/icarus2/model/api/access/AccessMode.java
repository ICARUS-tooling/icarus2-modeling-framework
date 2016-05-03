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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/access/AccessMode.java $
 *
 * $LastChangedDate: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $LastChangedRevision: 380 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.access;

/**
 * @author Markus Gärtner
 * @version $Id: AccessMode.java 380 2015-04-02 01:28:48Z mcgaerty $
 *
 */
public enum AccessMode {

	/**
	 * A client attempts to read data from an object
	 */
	READ,

	/**
	 * A client attempts to modify the state of an object
	 */
	WRITE,

	/**
	 * Privileged write access - used to mark methods that
	 * are intended for framework internal usage only but need
	 * to be publicly declared.
	 */
	MANAGE,

	/**
	 * Unlimited access is granted to the annotated method, regardless
	 * of the context in which it is being called.
	 */
	ALL;
}
