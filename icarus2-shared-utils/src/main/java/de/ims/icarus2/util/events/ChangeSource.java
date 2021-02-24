/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import de.ims.icarus2.util.Changeable;

/**
 * @author Markus Gärtner
 *
 */
public class ChangeSource implements Changeable, AutoCloseable {

	protected final Object source;

	public ChangeSource(Object source) {
		if (source == null)
			throw new NullPointerException("Invalid source");

		this.source = source;
	}

	public ChangeSource() {
		source = null;
	}

	protected EventListenerList listenerList = new EventListenerList();

	@Override
	public void addChangeListener(ChangeListener listener) {
		requireNonNull(listener);
		listenerList.add(ChangeListener.class, listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		requireNonNull(listener);
		listenerList.remove(ChangeListener.class, listener);
	}

	public Object getSource() {
		return source==null ? this : source;
	}

	public void fireStateChanged() {
		Object[] pairs = listenerList.getListenerList();

		ChangeEvent event = null;

		for (int i = pairs.length - 2; i >= 0; i -= 2) {
			if (pairs[i] == ChangeListener.class) {
				if (event == null) {
					event = new ChangeEvent(getSource());
				}

				((ChangeListener) pairs[i + 1]).stateChanged(event);
			}
		}
	}

	/**
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		// Violation of the original method contract, since we modify internal data from the listener list!
		Arrays.fill(listenerList.getListenerList(), null);
	}
}
