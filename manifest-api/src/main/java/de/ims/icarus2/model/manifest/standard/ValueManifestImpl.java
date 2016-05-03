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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/manifest/ValueManifestImpl.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.util.Set;

import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.ValueManifest;
import de.ims.icarus2.model.manifest.types.UnsupportedValueTypeException;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 * @version $Id: ValueManifestImpl.java 443 2016-01-11 11:31:11Z mcgaerty $
 *
 */
public class ValueManifestImpl extends AbstractLockable implements ValueManifest {

	private Object value;
	private String name;
	private String description;
	private Documentation documentation;
	private final ValueType valueType;

	private static final Set<ValueType> supportedValueTypes = ValueType.filterWithout(
			ValueType.UNKNOWN,
			ValueType.CUSTOM,
			ValueType.IMAGE_RESOURCE,
			ValueType.URL_RESOURCE);

	public ValueManifestImpl(ValueType valueType) {
		checkNotNull(valueType);

		if(!supportedValueTypes.contains(valueType))
			throw new UnsupportedValueTypeException(valueType);

		this.valueType = valueType;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ValueManifest#getValueType()
	 */
	@Override
	public ValueType getValueType() {
		return valueType;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ValueManifest#getValue()
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ValueManifest#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ValueManifest#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @return the documentation
	 */
	@Override
	public Documentation getDocumentation() {
		return documentation;
	}

	/**
	 * @param documentation the documentation to set
	 */
	@Override
	public void setDocumentation(Documentation documentation) {
		checkNotLocked();

		setDocumentation0(documentation);
	}

	protected void setDocumentation0(Documentation documentation) {
		this.documentation = documentation;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		checkNotLocked();

		setValue0(value);
	}

	protected void setValue0(Object value) {
		checkNotNull(value);

		this.value = value;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		checkNotLocked();

		setName0(name);
	}

	protected void setName0(String name) {
		checkNotNull(name);

		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		checkNotLocked();

		setDescription0(description);
	}

	protected void setDescription0(String description) {
		//FIXME really allow null for description?
//		if (description == null)
//			throw new NullPointerException("Invalid description");  //$NON-NLS-1$

		this.description = description;
	}

}
