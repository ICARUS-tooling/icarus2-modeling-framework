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
package de.ims.icarus2.model.api.members;


/**
 * A temporary utility object that can be used to query whether or not
 * certain actions are currently being possible for an associated object.
 * <p>
 * Note that client code should make sure to always {@link #close() close}
 * a verifier instance to make sure there are no dangling references to
 * the associated object.
 *
 * @author Markus Gärtner
 *
 */
public interface EditVerifier<E extends Object> {

	E getSource();

	void close();
}
