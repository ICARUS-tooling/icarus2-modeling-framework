/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.swing.SwingUtilities;


/**
 * Base class for objects that dispatch arbitrary named events
 * @author Markus Gärtner
 */
public class EventSource implements EventManager, Serializable {

	private static final long serialVersionUID = -562707311400281776L;

	public static final boolean DEFAULT_EVENTS_ENABLED = true;

	public static final int DEFAULT_DEAD_LISTENER_THRESHOLD = 5;

	/**
	 * Storage for registered listeners and the events they
	 * are listening to in the format
	 * ..[event_name][listener]..
	 */
	protected transient List<Object> eventListeners = null;

	/**
	 * Optional source to be used when firing event objects.
	 * If omitted this {@code EventSource} instance will be
	 * used in its place.
	 */
	protected Object eventSource;

	protected AtomicInteger deadListenerCount = new AtomicInteger(0);

	protected int deadListenerTreshold = DEFAULT_DEAD_LISTENER_THRESHOLD;

	/**
	 * Flag to enable or disable firing of events.
	 */
	protected boolean eventsEnabled = DEFAULT_EVENTS_ENABLED;

	/**
	 * Constructs a new event source using this as the source object.
	 */
	public EventSource() {
		this(null);
	}

	/**
	 * Constructs a new event source for the given source object.
	 */
	public EventSource(Object source) {
		setEventSource(source);
	}

	/**
	 *
	 */
	public Object getEventSource() {
		return eventSource;
	}

	/**
	 *
	 */
	public void setEventSource(@Nullable Object value) {
		this.eventSource = value;
	}

	/**
	 *
	 */
	public boolean isEventsEnabled() {
		return eventsEnabled;
	}

	public void setEventsEnabled(boolean eventsEnabled) {
		this.eventsEnabled = eventsEnabled;
	}

	public int getDeadListenerTreshold() {
		return deadListenerTreshold;
	}

	public void setDeadListenerTreshold(int deadListenerTreshold) {
		checkArgument("Threshold must be positive", deadListenerTreshold>0);
		this.deadListenerTreshold = deadListenerTreshold;
	}

	/**
	 * Registers the given {@code listener} for events of the
	 * specified {@code eventName} or as a listener for all
	 * events in the case the {@code eventName} parameter is {@code null}
	 * @param listener the {@code SimpleEventListener} to be registered
	 * @param eventName name of events to listen for or {@code null} if
	 * the listener is meant to receive all fired events
	 */
	@Override
	public void addListener(SimpleEventListener listener, @Nullable String eventName) {
		checkArgument("Event name cannot be empty", !"".equals(eventName));
		requireNonNull(listener);

		if (eventListeners == null) {
			eventListeners = new ArrayList<>();
		}

		eventListeners.add(eventName);
		eventListeners.add(listener);
	}

	/**
	 * Removes from the list of registered listeners all pairs
	 * matching the given combination of {@code SimpleEventListener}
	 * and {@code eventName}. If {@code eventName} is {@code null}
	 * then all occurrences of the given {@code listener} will be
	 * removed.
	 * @param listener
	 * @param eventName
	 */
	@Override
	public void removeListener(SimpleEventListener listener, @Nullable String eventName) {
		requireNonNull(listener);
		checkArgument("Event name cannot be empty", !"".equals(eventName));

		if (eventListeners != null) {
			for (int i = eventListeners.size() - 2; i > -1; i -= 2) {
				if (eventListeners.get(i + 1) == listener
						&& (eventName == null || String.valueOf(
								eventListeners.get(i)).equals(eventName))) {
					eventListeners.set(i+1, null);
					eventListeners.set(i, null);
				}
			}
		}
	}

	/**
	 * Fires the given {@code event} using this object as {@code source}
	 * for the call to {@link SimpleEventListener#invoke(Object, EventObject)}}
	 * if no source was specified by {@link #setEventSource(Object)}
	 * @param event
	 */
	public void fireEvent(EventObject event) {
		fireEvent(event, null);
	}

	public void fireEventEDT(final EventObject event) {
		requireNonNull(event);
		if(SwingUtilities.isEventDispatchThread()) {
			fireEvent(event, null);
		} else {
			SwingUtilities.invokeLater(() -> fireEvent(event, null));
		}
	}

	/**
	 * Dispatches the given {@code event} to all registered {@code SimpleEventListener}s
	 * that listen to the name of this {@code EventObject} or that are registered
	 * as {@code 'catch all'}-listeners
	 * @param event
	 * @param sender
	 */
	public void fireEvent(EventObject event, @Nullable Object sender) {
		requireNonNull(event);
		if (eventListeners != null && !eventListeners.isEmpty()
				&& isEventsEnabled()) {

			// ensure a valid non-null source!
			if (sender == null) {
				sender = getEventSource();
			}
			if (sender == null) {
				sender = this;
			}

			int size = eventListeners.size();

			for (int i = 0; i < size; i += 2) {
				String listen = (String) eventListeners.get(i);
				SimpleEventListener listener = (SimpleEventListener) eventListeners.get(i + 1);

				if(listener==null) {
					deadListenerCount.incrementAndGet();
				} else if (listen == null || listen.equals(event.getName())) {
					listener.invoke(sender, event);
				}
			}

			if(deadListenerCount.get()>=deadListenerTreshold) {
				clearEventListeners();
			}
		}
	}

	protected void clearEventListeners() {
		for(int i=eventListeners.size()-2; i>-1; i-=2) {
			if(eventListeners.get(i)==null && eventListeners.get(i+1)==null) {
				eventListeners.remove(i+1);
				eventListeners.remove(i);
			}
		}

		deadListenerCount.set(0);
	}

	public void fireEventEDT(final EventObject event, @Nullable final Object sender) {
		requireNonNull(event);

		if(SwingUtilities.isEventDispatchThread()) {
			fireEvent(event, null);
		} else {
			SwingUtilities.invokeLater(() -> fireEvent(event, sender));
		}
	}

}
