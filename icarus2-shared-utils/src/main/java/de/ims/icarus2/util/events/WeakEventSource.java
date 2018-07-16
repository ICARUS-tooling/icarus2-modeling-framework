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
 * $Revision: 380 $
 *
 */
package de.ims.icarus2.util.events;





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
	 * @see de.ims.icarus2.util.events.EventSource#addListener(java.lang.String, de.ims.icarus2.util.events.EventListener)
	 */
	@Override
	public void addListener(String eventName, EventListener listener) {
		EventListener proxy = ListenerProxies.getProxy(EventListener.class, listener);
		super.addListener(eventName, proxy);
	}

	/**
	 * @see de.ims.icarus2.util.events.EventSource#removeListener(de.ims.icarus2.util.events.EventListener, java.lang.String)
	 */
	@Override
	public void removeListener(EventListener listener, String eventName) {
		EventListener proxy = ListenerProxies.getProxy(EventListener.class, listener);
		super.removeListener(proxy, eventName);
	}

//	/**
//	 * Registers the given {@code listener} for events of the
//	 * specified {@code eventName} or as a listener for all
//	 * events in the case the {@code eventName} parameter is {@code null}
//	 * @param eventName name of events to listen for or {@code null} if
//	 * the listener is meant to receive all fired events
//	 * @param listener the {@code EventListener} to be registered
//	 */
//	@Override
//	public void addListener(String eventName, EventListener listener) {
//		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
//		
//		if (eventListeners == null) {
//			eventListeners = new ArrayList<>();
//		}
//
//		eventListeners.add(new Entry(listener, eventName));
//	}

//	/**
//	 * Removes from the list of registered listeners all pairs
//	 * matching the given combination of {@code EventListener}
//	 * and {@code eventName}. If {@code eventName} is {@code null}
//	 * then all occurrences of the given {@code listener} will be
//	 * removed.
//	 * @param listener
//	 * @param eventName
//	 */
//	@Override
//	public void removeEventListener(EventListener listener, String eventName) {
//		if (eventListeners != null) {
//			for (int i=eventListeners.size()-1; i>-1; i--) {
//				Entry entry = (Entry)eventListeners.get(i);
//				if(entry==null) {
//					continue;
//				}
//				if(entry.ref==null || entry.ref.get()==null || (entry.ref.get()==listener
//						&& (eventName==null || String.valueOf(
//								entry.eventName).equals(eventName)))) {
//					entry.delete();
//					eventListeners.set(i, null);
//				}
//			}
//		}
//	}

//	/**
//	 * Dispatches the given {@code event} to all registered {@code EventListener}s
//	 * that listen to the name of this {@code EventObject} or that are registered
//	 * as {@code 'catch all'}-listeners
//	 * @param event
//	 * @param sender
//	 */
//	@Override
//	public void fireEvent(EventObject event, Object sender) {
//		if (eventListeners != null && !eventListeners.isEmpty()
//				&& isEventsEnabled()) {
//			
//			// Ensure a valid non-null source!
//			if (sender == null)
//				sender = getEventSource();
//			if (sender == null)
//				sender = this;
//			
//			for (int i = 0; i<eventListeners.size(); i++) {
//				Entry entry = (Entry) eventListeners.get(i);
//				if(entry==null || entry.ref==null) {
//					deadListenerCount++;
//					continue;
//				}
//				EventListener listener = entry.ref.get();
//				if(listener==null) {
//					deadListenerCount++;
//				} else if(entry.eventName==null || entry.eventName.equals(event.getName())) {
//					listener.invoke(sender, event);
//				}
//			}
//		}
//		
//		if(deadListenerCount>=deadListenerTreshold) {
//			clearEventListeners();
//		}
//	}
	
//	@Override
//	protected void clearEventListeners() {
//		for(int i=eventListeners.size()-1; i>-1; i--) {
//			if(eventListeners.get(i)==null) {
//				eventListeners.remove(i);
//			}
//		}
//		deadListenerCount = 0;
//	}

//	private class Entry {
//		private WeakReference<EventListener> ref;
//		private String eventName;
//		
//		private Entry(EventListener listener, String eventName) {
//			this.ref = new WeakReference<EventListener>(listener);
//			this.eventName = eventName;
//		}
//		
//		private void delete() {
//			ref.clear();
//		}
//	}
}