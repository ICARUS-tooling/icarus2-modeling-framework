/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
	public void addListener(SimpleEventListener listener, String eventName) {
		SimpleEventListener proxy = ListenerProxies.getProxy(SimpleEventListener.class, listener);
		super.addListener(proxy, eventName);
	}

	/**
	 * @see de.ims.icarus2.util.events.EventSource#removeListener(de.ims.icarus2.util.events.SimpleEventListener, java.lang.String)
	 */
	@Override
	public void removeListener(SimpleEventListener listener, String eventName) {
		SimpleEventListener proxy = ListenerProxies.getProxy(SimpleEventListener.class, listener);
		super.removeListener(proxy, eventName);
	}

//	/**
//	 * Registers the given {@code listener} for events of the
//	 * specified {@code eventName} or as a listener for all
//	 * events in the case the {@code eventName} parameter is {@code null}
//	 * @param eventName name of events to listen for or {@code null} if
//	 * the listener is meant to receive all fired events
//	 * @param listener the {@code SimpleEventListener} to be registered
//	 */
//	@Override
//	public void addListener(String eventName, SimpleEventListener listener) {
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
//	 * matching the given combination of {@code SimpleEventListener}
//	 * and {@code eventName}. If {@code eventName} is {@code null}
//	 * then all occurrences of the given {@code listener} will be
//	 * removed.
//	 * @param listener
//	 * @param eventName
//	 */
//	@Override
//	public void removeEventListener(SimpleEventListener listener, String eventName) {
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
//	 * Dispatches the given {@code event} to all registered {@code SimpleEventListener}s
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
//				SimpleEventListener listener = entry.ref.get();
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
//		private WeakReference<SimpleEventListener> ref;
//		private String eventName;
//		
//		private Entry(SimpleEventListener listener, String eventName) {
//			this.ref = new WeakReference<SimpleEventListener>(listener);
//			this.eventName = eventName;
//		}
//		
//		private void delete() {
//			ref.clear();
//		}
//	}
}