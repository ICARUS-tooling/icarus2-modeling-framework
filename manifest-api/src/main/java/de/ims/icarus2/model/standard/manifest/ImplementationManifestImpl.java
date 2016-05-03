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

 * $Revision: 443 $
 * $Date: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/manifest/ImplementationManifestImpl.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.manifest;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import de.ims.icarus2.model.api.manifest.ImplementationManifest;
import de.ims.icarus2.model.api.manifest.ManifestLocation;
import de.ims.icarus2.model.api.manifest.ManifestRegistry;
import de.ims.icarus2.model.api.manifest.ManifestType;
import de.ims.icarus2.model.api.manifest.MemberManifest;

/**
 * @author Markus Gärtner
 * @version $Id: ImplementationManifestImpl.java 443 2016-01-11 11:31:11Z mcgaerty $
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
	 * @see de.ims.icarus2.model.api.manifest.Manifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.IMPLEMENTATION_MANIFEST;
	}


	/**
	 * @see de.ims.icarus2.model.api.manifest.ImplementationManifest#getSourceType()
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
	 * @see de.ims.icarus2.model.api.manifest.ImplementationManifest#getSource()
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
	 * @see de.ims.icarus2.model.api.manifest.ImplementationManifest#getClassname()
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
	 * @see de.ims.icarus2.model.api.manifest.ImplementationManifest#isUseFactory()
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
		checkNotNull(sourceType);

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
		checkNotNull(source);

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
		checkNotNull(classname);

		this.classname = classname;
	}

	/**
	 * @param useFactory the useFactory to set
	 */
	@Override
	public void setUseFactory(Boolean useFactory) {
		checkNotLocked();

		setUseFactory0(useFactory);
	}

	protected void setUseFactory0(Boolean useFactory) {
		this.useFactory = useFactory;
	}
}
