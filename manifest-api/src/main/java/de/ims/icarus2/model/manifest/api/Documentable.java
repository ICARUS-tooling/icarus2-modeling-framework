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

 * $Revision: 433 $
 * $Date: 2015-10-15 16:11:29 +0200 (Do, 15 Okt 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/Documentable.java $
 *
 * $LastChangedDate: 2015-10-15 16:11:29 +0200 (Do, 15 Okt 2015) $
 * $LastChangedRevision: 433 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.api;

import de.ims.icarus2.model.api.access.AccessControl;
import de.ims.icarus2.model.api.access.AccessMode;
import de.ims.icarus2.model.api.access.AccessPolicy;
import de.ims.icarus2.model.api.access.AccessRestriction;


/**
 *
 * @author Markus Gärtner
 * @version $Id: Documentable.java 433 2015-10-15 14:11:29Z mcgaerty $
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface Documentable extends Lockable {

	@AccessRestriction(AccessMode.READ)
	Documentation getDocumentation();

	@AccessRestriction(AccessMode.WRITE)
	void setDocumentation(Documentation documentation);
}
