/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.Optional;

import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MemberManifest;

/**
 * @author Markus Gärtner
 *
 */
@SuppressWarnings("rawtypes")
public class ImplementationManifestImpl extends AbstractMemberManifest<ImplementationManifest, MemberManifest>
		implements ImplementationManifest {

	private Optional<SourceType> sourceType = Optional.empty();
	private Optional<String> source = Optional.empty();
	private Optional<String> classname = Optional.empty();
	private Boolean useFactory;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public ImplementationManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, MemberManifest<?> hostManifest) {
		super(manifestLocation, registry, hostManifest, MemberManifest.class);
	}

	public ImplementationManifestImpl(MemberManifest<?> hostManifest) {
		super(hostManifest, hostIdentity(), MemberManifest.class);
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
	public Optional<SourceType> getSourceType() {
		return getDerivable(sourceType, ImplementationManifest::getSourceType);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest#getSource()
	 */
	@Override
	public Optional<String> getSource() {
		return getDerivable(source, ImplementationManifest::getSource);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest#getClassname()
	 */
	@Override
	public Optional<String> getClassname() {
		return getDerivable(classname, ImplementationManifest::getClassname);
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
	public ImplementationManifest setSourceType(SourceType sourceType) {
		checkNotLocked();

		setSourceType0(sourceType);

		return thisAsCast();
	}

	protected void setSourceType0(SourceType sourceType) {
		this.sourceType = Optional.of(sourceType);
	}

	/**
	 * @param source the source to set
	 */
	@Override
	public ImplementationManifest setSource(String source) {
		checkNotLocked();

		setSource0(source);

		return thisAsCast();
	}

	protected void setSource0(String source) {
		this.source = Optional.of(source);
	}

	/**
	 * @param classname the classname to set
	 */
	@Override
	public ImplementationManifest setClassname(String classname) {
		checkNotLocked();

		setClassname0(classname);

		return thisAsCast();
	}

	protected void setClassname0(String classname) {
		this.classname = Optional.of(classname);
	}

	/**
	 * @param useFactory the useFactory to set
	 */
	@Override
	public ImplementationManifest setUseFactory(boolean useFactory) {
		checkNotLocked();

		setUseFactory0(useFactory);

		return thisAsCast();
	}

	protected void setUseFactory0(boolean useFactory) {
		this.useFactory = (useFactory==DEFAULT_USE_FACTORY_VALUE && !hasTemplate()) ? null : Boolean.valueOf(useFactory);
	}

	@Override
	public boolean isLocalSourceType() {
		return sourceType.isPresent();
	}

	@Override
	public boolean isLocalSource() {
		return source.isPresent();
	}

	@Override
	public boolean isLocalClassname() {
		return classname.isPresent();
	}

	@Override
	public boolean isLocalUseFactory() {
		return useFactory!=null;
	}
}
