/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
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
 * $Revision: 229 $
 * $Date: 2014-02-20 15:53:33 +0100 (Do, 20 Feb 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/de/ims/icarus/util/CompactProperties.java $
 *
 * $LastChangedDate: 2014-02-20 15:53:33 +0100 (Do, 20 Feb 2014) $
 * $LastChangedRevision: 229 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import gnu.trove.map.hash.THashMap;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * A compact implementation of a properties map, i.e. a map like interface
 * to store and retrieve mappings between {@code String} keys and associated
 * {@code Object}s. Internally data is stored either in an array or a full grown
 * map, depending on the number of entries.
 *
 * @author Markus Gärtner
 * @version $Id: CompactProperties.java 229 2014-02-20 14:53:33Z mcgaerty $
 *
 */
public class CompactProperties implements Cloneable, Serializable {

	private static final long serialVersionUID = -492641053997637443L;

	//FIXME check if it's ok to leave this non-transient
	protected Object table;

	protected static final int ARRAY_SIZE_LIMIT = 8;

	public CompactProperties() {
		// no-op
	}

	public CompactProperties(Map<? extends String, ? extends Object> map) {
		if(!map.isEmpty()) {
			if(map.size()<=ARRAY_SIZE_LIMIT) {
				Object[] table = new Object[map.size()*2];
				int index = 0;
				for(Entry<? extends String, ? extends Object> entry : map.entrySet()) {
					table[index++] = entry.getKey();
					table[index++] = entry.getValue();
				}

				this.table = table;
			} else {
				this.table = new THashMap<>(map);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Object get(String key) {
		checkNotNull(key);

		if(table==null)
			return null;

		if(table instanceof Object[]) {
			Object[] table = (Object[]) this.table;
			for(int i = 0; i<table.length-1; i+=2)
				if(table[i]!=null && key.equals(table[i]))
					return table[i+1];

			return null;
		} else {
			return ((Map<String, Object>) table).get(key);
		}
	}

	public int size() {
		if(table==null) {
			return 0;
		} else if(table instanceof Object[]) {
			return ((Object[])table).length;
		} else {
			return ((Map<?, ?>)table).size();
		}
	}

	protected void grow() {
		Map<String, Object> map = new THashMap<>();
		Object[] table = (Object[]) this.table;

		for(int i=1; i<table.length; i+=2)
			if(table[i-1]!=null && table[i]!=null)
				map.put((String)table[i-1], table[i]);

		this.table = map;
	}

	@SuppressWarnings("unchecked")
	protected void shrink() {
		Map<String,Object> map = (Map<String, Object>) this.table;
		Object[] table = null;

		if(!map.isEmpty()) {
			table = new Object[map.size()*2];
			int index = 0;
			for(Entry<String, Object> entry : map.entrySet()) {
				table[index++] = entry.getKey();
				table[index++] = entry.getValue();
			}
		}

		this.table = table;
	}

	protected void clear() {
		table = null;
	}

	@SuppressWarnings("unchecked")
	public void put(String key, Object value) {
		checkNotNull(key);

		// nothing to do here
		if(value==null && table==null)
			return;

		if(table==null) {
			// INITIAL mode
			Object[] table = new Object[4];
			table[0] = key;
			table[1] = value;

			this.table = table;
		} else if(table instanceof Object[]) {
			// ARRAY mode and array is set
			Object[] table = (Object[]) this.table;
			int emptyIndex = -1;

			// try to insert
			for(int i=0; i<table.length-1; i+=2) {
				if(table[i]==null) {
					emptyIndex = i;
				} else if(key.equals(table[i])) {
					table[i+1] = value;
					if(value==null)
						table[i] = null;
					return;
				}
			}

			// key not present
			if(emptyIndex!=-1) {
				// empty slot available
				table[emptyIndex] = key;
				table[emptyIndex+1] = value;
			} else if(value!=null) { // only bother for non-null mappings
				// no empty slot found -> need to expand
				int size = table.length;
				Object[] newTable = new Object[size+2];
				System.arraycopy(table, 0, newTable, 0, size);
				newTable[size] = key;
				newTable[size+1] = value;
				this.table = newTable;

				if(++size > ARRAY_SIZE_LIMIT) {
					grow();
				}
			}

		} else {
			// TABLE mode
			Map<String,Object> table = (Map<String, Object>)this.table;

			if(value==null)
				table.remove(key);
			else
				table.put(key, value);

			if(table.size()<ARRAY_SIZE_LIMIT) {
				shrink();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> asMap() {
		Map<String, Object> map;

		if(table==null) {
			map = null;
		} else if(table instanceof Object[]) {
			map = new THashMap<>((int)(ARRAY_SIZE_LIMIT/0.75F));
			Object[] table = (Object[]) this.table;
			for(int i=1; i<table.length; i+=2) {
				if(table[i-1]!=null && table[i]!=null) {
					map.put((String)table[i-1], table[i]);
				}
			}
		} else {
			map = new THashMap<>((Map<String, Object>)this.table);
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	public void forEachEntry(BiConsumer<? super String, ? super Object> action) {
		if(table instanceof Object[]) {
			Object[] table = (Object[]) this.table;
			for(int i=1; i<table.length; i+=2) {
				if(table[i-1]!=null && table[i]!=null) {
					action.accept((String)table[i-1], table[i]);
				}
			}
		} else if(table!=null) {
			((Map<String, Object>)table).forEach(action);
		}
	}

	@SuppressWarnings("unchecked")
	public void forEachKey(Consumer<? super String> action) {
		if(table instanceof Object[]) {
			Object[] table = (Object[]) this.table;
			for(int i=1; i<table.length; i+=2) {
				if(table[i-1]!=null && table[i]!=null) {
					action.accept((String)table[i-1]);
				}
			}
		} else if(table!=null) {
			((Map<String, Object>)table).keySet().forEach(action);
		}
	}

	@SuppressWarnings("unchecked")
	public void forEachValue(Consumer<? super Object> action) {
		if(table instanceof Object[]) {
			Object[] table = (Object[]) this.table;
			for(int i=1; i<table.length; i+=2) {
				if(table[i-1]!=null && table[i]!=null) {
					action.accept(table[i]);
				}
			}
		} else if(table!=null) {
			((Map<String, Object>)table).values().forEach(action);
		}
	}

	@Override
	public CompactProperties clone() {
		CompactProperties clone = null;
		try {
			clone = (CompactProperties) super.clone();
		} catch (CloneNotSupportedException e) {
			// ignore
		}

		// One layer deep cloning
		clone.copyFrom(this);

		return clone;
	}

	@SuppressWarnings("unchecked")
	public void copyFrom(CompactProperties other) {
		checkNotNull(other);

		// One layer deep cloning
		if(other.table==null) {
			table = null;
		} else if(other.table instanceof Object[]) {
			table = ((Object[])other.table).clone();
		} else {
			table = new THashMap<>((Map<String, Object>) other.table);
		}
	}
}
