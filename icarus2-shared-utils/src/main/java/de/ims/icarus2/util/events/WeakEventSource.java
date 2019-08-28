/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import javax.annotation.Nullable;

/**
 * Extension of the basic {@code EventSource} class to allow
 * for storing of weak references to the listeners being
 * registered. This implementation does {@code not} keep
 * strong references to registered listeners so that listeners
 * which are not strongly referenced from their origin can be
 * gc-ed at any time.
 *
 * @author Markus Gärtner
 *
 */
public class WeakEventSource extends EventSource {

	private static final long serialVersionUID = -5414393603358435782L;

	/**
	 * Constructs a new event source using this as the source object.
	 */
	public WeakEventSource() {
		super(null);
	}

	/**
	 * Constructs a new event source for the given source object.
	 */
	public WeakEventSource(Object source) {
		super(source);
	}

	/**
	 * @see de.ims.icarus2.util.events.EventSource#addListener(de.ims.icarus2.util.events.SimpleEventListener, java.lang.String)
	 */
	@Override
	public void addListener(SimpleEventListener listener, @Nullable String eventName) {
		SimpleEventListener proxy = ListenerProxies.getProxy(SimpleEventListener.class, listener);
		super.addListener(proxy, eventName);
	}

	/**
	 * @see de.ims.icarus2.util.events.EventSource#removeListener(de.ims.icarus2.util.events.SimpleEventListener, java.lang.String)
	 */
	@Override
	public void removeListener(SimpleEventListener listener, @Nullable String eventName) {
		SimpleEventListener proxy = ListenerProxies.getProxy(SimpleEventListener.class, listener);
		super.removeListener(proxy, eventName);
	}
}