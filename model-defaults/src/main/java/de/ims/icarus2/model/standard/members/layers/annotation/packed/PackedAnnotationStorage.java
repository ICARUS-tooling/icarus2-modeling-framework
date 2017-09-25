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
package de.ims.icarus2.model.standard.members.layers.annotation.packed;

import java.util.function.Consumer;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
public class PackedAnnotationStorage implements AnnotationStorage {

	/**
	 * The (potentially shared) storage for our annotations.
	 */
	private PackedDataManager dataManager;


	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#collectKeys(de.ims.icarus2.model.api.members.item.Item, java.util.function.Consumer)
	 */
	@Override
	public boolean collectKeys(Item item, Consumer<String> action) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public Object getValue(Item item, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getIntegerValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public int getIntegerValue(Item item, String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getFloatValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public float getFloatValue(Item item, String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getDoubleValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public double getDoubleValue(Item item, String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getLongValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public long getLongValue(Item item, String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getBooleanValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public boolean getBooleanValue(Item item, String key) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues()
	 */
	@Override
	public void removeAllValues() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.lang.String)
	 */
	@Override
	public void removeAllValues(String key) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.util.function.Supplier)
	 */
	@Override
	public void removeAllValues(Supplier<? extends Item> source) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setIntegerValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, int)
	 */
	@Override
	public void setIntegerValue(Item item, String key, int value) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setLongValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, long)
	 */
	@Override
	public void setLongValue(Item item, String key, long value) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setFloatValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, float)
	 */
	@Override
	public void setFloatValue(Item item, String key, float value) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setDoubleValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, double)
	 */
	@Override
	public void setDoubleValue(Item item, String key, double value) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setBooleanValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, boolean)
	 */
	@Override
	public void setBooleanValue(Item item, String key, boolean value) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations()
	 */
	@Override
	public boolean hasAnnotations() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean hasAnnotations(Item item) {
		// TODO Auto-generated method stub
		return false;
	}

}
