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
 *
 * $Revision: 400 $
 * $Date: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/events/EventSource.java $
 *
 * $LastChangedDate: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 400 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.events;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;


/**
 * Base class for objects that dispatch arbitrary named events
 * @author Markus Gärtner
 */
public class EventSource implements EventManager, Serializable {

	private static final long serialVersionUID = -562707311400281776L;

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

	protected transient AtomicInteger deadListenerCount = new AtomicInteger(0);

	protected transient int deadListenerTreshold = 5;

	/**
	 * Flag to enable or disable firing of events.
	 */
	protected boolean eventsEnabled = true;

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
	public void setEventSource(Object value) {
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
		this.deadListenerTreshold = deadListenerTreshold;
	}

	/**
	 * Registers the given {@code listener} for events of the
	 * specified {@code eventName} or as a listener for all
	 * events in the case the {@code eventName} parameter is {@code null}
	 * @param eventName name of events to listen for or {@code null} if
	 * the listener is meant to receive all fired events
	 * @param listener the {@code EventListener} to be registered
	 */
	@Override
	public void addListener(String eventName, EventListener listener) {
		checkNotNull(listener);

		if (eventListeners == null) {
			eventListeners = new ArrayList<>();
		}

		eventListeners.add(eventName);
		eventListeners.add(listener);
	}

	/**
	 * Removes the given {@code EventListener} from all events
	 * it was previously registered for.
	 * @param listener the {@code EventListener} to be removed
	 */
	@Override
	public void removeListener(EventListener listener) {
		removeListener(listener, null);
	}

	/**
	 * Removes from the list of registered listeners all pairs
	 * matching the given combination of {@code EventListener}
	 * and {@code eventName}. If {@code eventName} is {@code null}
	 * then all occurrences of the given {@code listener} will be
	 * removed.
	 * @param listener
	 * @param eventName
	 */
	@Override
	public void removeListener(EventListener listener, String eventName) {
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
	 * for the call to {@link EventListener#invoke(Object, EventObject)}}
	 * if no source was specified by {@link #setEventSource(Object)}
	 * @param event
	 */
	public void fireEvent(EventObject event) {
		fireEvent(event, null);
	}

	public void fireEventEDT(final EventObject event) {
		if(SwingUtilities.isEventDispatchThread()) {
			fireEvent(event, null);
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					fireEvent(event, null);
				}
			});
		}
	}

	/**
	 * Dispatches the given {@code event} to all registered {@code EventListener}s
	 * that listen to the name of this {@code EventObject} or that are registered
	 * as {@code 'catch all'}-listeners
	 * @param event
	 * @param sender
	 */
	public void fireEvent(EventObject event, Object sender) {
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
				EventListener listener = (EventListener) eventListeners.get(i + 1);

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

	public void fireEventEDT(final EventObject event, final Object sender) {
		if(SwingUtilities.isEventDispatchThread()) {
			fireEvent(event, null);
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					fireEvent(event, sender);
				}
			});
		}
	}

}
