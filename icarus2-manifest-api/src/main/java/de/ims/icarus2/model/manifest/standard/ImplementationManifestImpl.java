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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MemberManifest;

/**
 * @author Markus Gärtner
 *
 */
public class ImplementationManifestImpl extends AbstractMemberManifest<ImplementationManifest> implements ImplementationManifest {

	private SourceType sourceType;
	private String source;
	private String classname;
	private Boolean useFactory;

	private final MemberManifest hostManifest;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public ImplementationManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, MemberManifest hostManifest) {
		super(manifestLocation, registry);

		this.hostManifest = hostManifest;
	}

	public ImplementationManifestImpl(MemberManifest hostManifest) {
		this(hostManifest.getManifestLocation(), hostManifest.getRegistry(), hostManifest);
	}

	@Override
	public MemberManifest getHost() {
		return hostManifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Manifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.IMPLEMENTATION_MANIFEST;
	}


	/**
	 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest#getSourceType()
	 */
	@Override
	public SourceType getSourceType() {
		SourceType result = sourceType;
		if(result==null && hasTemplate()) {
			result = getTemplate().getSourceType();
		}
		if(result==null) {
			result = DEFAULT_SOURCE_TYPE;
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest#getSource()
	 */
	@Override
	public String getSource() {
		String result = source;
		if(result==null && hasTemplate()) {
			result = getTemplate().getSource();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest#getClassname()
	 */
	@Override
	public String getClassname() {
		String result = classname;
		if(result==null && hasTemplate()) {
			result = getTemplate().getClassname();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest#isUseFactory()
	 */
	@Override
	public boolean isUseFactory() {
		if(useFactory==null) {
			return hasTemplate() ? getTemplate().isUseFactory() : DEFAULT_USE_FACTORY_VALUE;
		} else {
			return useFactory.booleanValue();
		}
	}

	/**
	 * @param sourceType the sourceType to set
	 */
	@Override
	public void setSourceType(SourceType sourceType) {
		checkNotLocked();

		setSourceType0(sourceType);
	}

	protected void setSourceType0(SourceType sourceType) {
		requireNonNull(sourceType);

		this.sourceType = sourceType;
	}

	/**
	 * @param source the source to set
	 */
	@Override
	public void setSource(String source) {
		checkNotLocked();

		setSource0(source);
	}

	protected void setSource0(String source) {
		requireNonNull(source);

		this.source = source;
	}

	/**
	 * @param classname the classname to set
	 */
	@Override
	public void setClassname(String classname) {
		checkNotLocked();

		setClassname0(classname);
	}

	protected void setClassname0(String classname) {
		requireNonNull(classname);

		this.classname = classname;
	}

	/**
	 * @param useFactory the useFactory to set
	 */
	@Override
	public void setUseFactory(boolean useFactory) {
		checkNotLocked();

		setUseFactory0(useFactory);
	}

	protected void setUseFactory0(boolean useFactory) {
		this.useFactory = (useFactory==DEFAULT_USE_FACTORY_VALUE && !hasTemplate()) ? null : Boolean.valueOf(useFactory);
	}
}
