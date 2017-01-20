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
	public MemberManifest getHostManifest() {
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
		this.useFactory = useFactory==DEFAULT_USE_FACTORY_VALUE ? null : Boolean.valueOf(useFactory);
	}
}
