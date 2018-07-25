/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.layers.type;

import static java.util.Objects.requireNonNull;

import javax.swing.Icon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.util.lang.ClassProxy;
import de.ims.icarus2.util.lang.ClassUtils;

/**
 * @author Markus Gärtner
 *
 */
public class LayerTypeWrapper implements LayerType {

	private static final Logger log = LoggerFactory
			.getLogger(LayerTypeWrapper.class);

	private volatile LayerType proxy;

	private final Object source;
	private final String id;

	public LayerTypeWrapper(String id, ClassProxy proxy) {
		requireNonNull(id, "Invalid id");
		requireNonNull(proxy, "Invalid proxy");

		this.id = id;
		this.source = proxy;
	}

	public LayerTypeWrapper(String id, String className) {
		requireNonNull(id, "Invalid id");
		requireNonNull(className, "Invalid class name");

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
	 * @see de.ims.icarus2.model.manifest.api.Category#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return getProxy().getNamespace();
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
