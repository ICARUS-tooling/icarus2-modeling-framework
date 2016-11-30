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
 */
package de.ims.icarus2.filedriver.schema.resolve;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;

/**
 * @author Markus Gärtner
 *
 */
public class ResolverFactory {

	public static ResolverFactory newInstance() {
		return new ResolverFactory();
	}

	public Resolver createResolver(String s) {
		try {
			return (Resolver) Class.forName(s).newInstance();
		} catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new ModelException(GlobalErrorCode.INSTANTIATION_ERROR,
					"Failed to instantiate requested resolver: "+s, e);
		}
	}
}
