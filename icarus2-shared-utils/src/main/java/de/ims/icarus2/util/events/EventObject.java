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
package de.ims.icarus2.util.events;

import java.util.Hashtable;
import java.util.Map;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * Base class for objects that dispatch named events.
 * @author Markus Gärtner
 */
public class EventObject {

	/**
	 * Holds the name of the event.
	 */
	protected String name;

	/**
	 * Holds the properties of the event.
	 */
	protected Map<String, Object> properties;

	/**
	 * Holds the consumed state of the event. Default is false.
	 */
	protected boolean consumed = false;

	/**
	 * Constructs a new event for the given name.
	 */
	public EventObject(String name) {
		this(name, (Object[]) null);
	}

	/**
	 * Constructs a new event for the given name and properties. The optional
	 * properties are specified using a sequence of keys and values, eg.
	 * {@code new EventObject("eventName", key1, val1, .., keyN, valN))}
	 */
	public EventObject(String name, Object... args) {
		this.name = name;
		properties = new Hashtable<String, Object>();

		if (args != null) {
			for (int i = 0; i < args.length; i += 2) {
				if (args[i + 1] != null) {
					properties.put(String.valueOf(args[i]), args[i + 1]);
				}
			}
		}
	}

	/**
	 * Returns the name of the event.
	 */
	public String getName() {
		return name;
	}

	/**
	 *
	 */
	public Map<String, Object> getProperties() {
		return CollectionUtils.getMapProxy(properties);
	}

	/**
	 *
	 */
	public Object getProperty(String key) {
		return properties.get(key);
	}

	@SuppressWarnings("unchecked")
	public <O extends Object> O getProperty(String key, O defaultValue) {
		Object value = properties.get(key);
		return value==null ? defaultValue : (O) value;
	}

	public boolean isPropertiesDefined(String...keys) {
		for(String key : keys) {
			if(!properties.containsKey(key)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns true if the event has been consumed.
	 */
	public boolean isConsumed() {
		return consumed;
	}

	/**
	 * Consumes the event.
	 */
	public void consume() {
		consumed = true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(name);
		if(consumed) {
			sb.append(" (consumed)"); //$NON-NLS-1$
		}

		sb.append('[');
		for(Map.Entry<String, Object> entry : properties.entrySet()) {
			sb.append(entry.getKey()).append('=').append(entry.getValue()).append(' ');
		}
		sb.append(']');

		return sb.toString();
	}

	public static EventObject propertyEvent(String name, Object oldValue, Object newValue) {
		return new EventObject(Events.PROPERTY, "property", name,  //$NON-NLS-1$
				"oldValue", oldValue, "newValue", newValue); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
