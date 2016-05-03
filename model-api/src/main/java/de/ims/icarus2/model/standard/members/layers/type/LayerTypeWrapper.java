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

 * $Revision: 429 $
 * $Date: 2015-10-07 17:08:17 +0200 (Mi, 07 Okt 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/layers/type/LayerTypeWrapper.java $
 *
 * $LastChangedDate: 2015-10-07 17:08:17 +0200 (Mi, 07 Okt 2015) $
 * $LastChangedRevision: 429 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.layers.type;

import javax.swing.Icon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.util.classes.ClassProxy;
import de.ims.icarus2.util.classes.ClassUtils;

/**
 * @author Markus Gärtner
 * @version $Id: LayerTypeWrapper.java 429 2015-10-07 15:08:17Z mcgaerty $
 *
 */
public class LayerTypeWrapper implements LayerType {
	
	private static final Logger log = LoggerFactory
			.getLogger(LayerTypeWrapper.class);

	private volatile LayerType proxy;

	private final Object source;
	private final String id;

	public LayerTypeWrapper(String id, ClassProxy proxy) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
		if (proxy == null)
			throw new NullPointerException("Invalid proxy"); //$NON-NLS-1$

		this.id = id;
		this.source = proxy;
	}

	public LayerTypeWrapper(String id, String className) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
		if (className == null)
			throw new NullPointerException("Invalid className"); //$NON-NLS-1$

		this.id = id;
		this.source = className;
	}

	private LayerType getProxy() {
		if(proxy==null) {
			synchronized (this) {
				if(proxy==null) {
					try {
						proxy = (LayerType) ClassUtils.instantiate(source);
					} catch (ClassNotFoundException | InstantiationException
							| IllegalAccessException e) {
						log.error("Failed to instantiate layer type proxy: {}", source, e); //$NON-NLS-1$

						throw new IllegalStateException("Unable to load layer type proxy", e); //$NON-NLS-1$
					}
				}
			}
		}
		return proxy;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return getProxy().getName();
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return getProxy().getDescription();
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return getProxy().getIcon();
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return getProxy().getOwner();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerType#getSharedManifest()
	 */
	@Override
	public LayerManifest getSharedManifest() {
		return getProxy().getSharedManifest();
	}
}
