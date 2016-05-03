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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.manifest.api;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.IcarusException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestException extends IcarusException {

	private static final long serialVersionUID = 7579478541873972798L;

	//FIXME add ManifestFragment field

	/**
	 * @param errorCode
	 * @param message
	 * @param cause
	 */
	public ManifestException(ErrorCode errorCode, String message,
			Throwable cause) {
		super(errorCode, message, cause);
	}

	/**
	 * @param errorCode
	 * @param message
	 */
	public ManifestException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	/**
	 * @param errorCode
	 * @param cause
	 */
	public ManifestException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
