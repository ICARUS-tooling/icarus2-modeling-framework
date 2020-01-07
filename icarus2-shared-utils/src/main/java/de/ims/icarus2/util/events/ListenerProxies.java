/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.util.collections.ArrayUtils;
import de.ims.icarus2.util.collections.BiDiMap;
import de.ims.icarus2.util.lang.ClassUtils;

/**
 * @author Markus Gärtner
 *
 */
public final class ListenerProxies {
	
	private static final Logger log = LoggerFactory
			.getLogger(ListenerProxies.class);

	private static Map<Class<?>, Map<Object, Object>> proxies = new HashMap<>();
	private static Map<Class<?>, Map<Object, Object>> proxiesEDT = new HashMap<>();

	// Maps interface classes to proxy classes
	private static BiDiMap<Class<?>, Class<?>> proxyClasses = new BiDiMap<>();

	private static final Object lock = new Object();

	private ListenerProxies() {
		throw new AssertionError();
	}

	private static final Class<?>[] constructorParams = {
		InvocationHandler.class,
	};

	private static final Set<String> ignoredMethods = ArrayUtils.asSet(
			"toString", //$NON-NLS-1$
			"hashCode", //$NON-NLS-1$
			"equals" //$NON-NLS-1$
	);

	public static <T extends Object> T getProxy(Class<T> listenerClass, T owner) {
		return getProxy(listenerClass, owner, false);
	}

	public static <T extends Object> T getEDTProxy(Class<T> listenerClass, T owner) {
		return getProxy(listenerClass, owner, true);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object> T getEmptyListener(Class<T> listenerClass) {
		return (T) Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class[]{listenerClass}, emptyHandler);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Object> T getProxy(Class<T> listenerClass, T owner, boolean enforceEDT) {
		if(listenerClass==null)
			throw new NullPointerException("Invalid listener class"); //$NON-NLS-1$
		if(owner==null)
			throw new NullPointerException("Invalid owner"); //$NON-NLS-1$

		// Ensure we are not accidently creating proxies for a proxy!
		if(Proxy.isProxyClass(owner.getClass())) {
			return owner;
		}

		synchronized (lock) {
			Map<Class<?>, Map<Object, Object>> proxies = enforceEDT ?
					ListenerProxies.proxiesEDT : ListenerProxies.proxies;
			Map<Object, Object> proxyMap = proxies.get(listenerClass);
			if(proxyMap==null) {
				proxyMap = new WeakHashMap<>();
				proxies.put(listenerClass, proxyMap);
			}

			Object proxy = proxyMap.get(owner);

			if(proxy==null) {
				Class<?> proxyClass = proxyClasses.get(listenerClass);
				if(proxyClass==null) {
					proxyClass = Proxy.getProxyClass(listenerClass.getClassLoader(), listenerClass);
					proxyClasses.put(listenerClass, proxyClass);
				}

				try {
					ListenerProxy listenerProxy = new ListenerProxy(owner, listenerClass, enforceEDT);
					Constructor<?> cs = proxyClass.getConstructor(constructorParams);
					proxy = cs.newInstance(listenerProxy);

					proxyMap.put(owner, proxy);
				} catch (Exception e) {
					log.error("Unable to instantiate proxy: {}", proxyClass, e); //$NON-NLS-1$

					throw new IllegalStateException("Unable to produce proxy for listener class: "+listenerClass, e); //$NON-NLS-1$
				}
			}

			return (T) proxy;
		}
	}

	private static Object getSource(Object[] args) {
		if(args==null || args.length==0) {
			return null;
		}

		for(Object arg : args) {
			if(arg instanceof EventSource || arg instanceof EventObject) {
				return arg;
			}
		}

		return null;
	}

	private static void tryRemoveListener(Object target, Object proxy, Class<?> listenerClass) {

		Class<?> proxyClass = proxy.getClass();

		if(listenerClass==null)
			throw new IllegalArgumentException("No listener class defined for proxy: "+proxyClass); //$NON-NLS-1$

		String methodName = "remove"+listenerClass.getSimpleName(); //$NON-NLS-1$

		try {
			Method method = target.getClass().getMethod(methodName, listenerClass);

			method.invoke(target, proxy);
		} catch (NoSuchMethodException e) {
			log.debug("Unable to remove listener via method '{}({})' on target: {}",
							methodName, listenerClass.getSimpleName(), target.getClass(), e); //$NON-NLS-1$
		} catch (SecurityException | IllegalAccessException
				| InvocationTargetException e) {
			log.error("Not allowed to remove listener on target: {}", target.getClass(), e); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
			log.error("Listener argument rejected on target: {}", target.getClass(), e); //$NON-NLS-1$
		}
	}

	static final InvocationHandler emptyHandler =  new InvocationHandler() {

		/**
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			return null;
		}

	};

	/**
	 *
	 * @author Markus Gärtner
	 */
	static class ListenerProxy extends WeakReference<Object> implements InvocationHandler {

		private final int hash;
		private final Class<?> listenerClass;

		private boolean dead = false;

		private final boolean enforceEDT;

		public ListenerProxy(Object owner, Class<?> listenerClass, boolean enforceEDT) {
			super(owner);

			if(listenerClass==null)
				throw new NullPointerException("Invalid listener claass"); //$NON-NLS-1$

			hash = 31 + owner.hashCode();
			this.listenerClass = listenerClass;
			this.enforceEDT = enforceEDT;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return getClass().hashCode() * hash;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} else if(obj instanceof ListenerProxy) {
				ListenerProxy other = (ListenerProxy) obj;
				return ClassUtils.equals(get(), other.get())
						&& hashCode()==other.hashCode();
			}
			return false;
		}

		private void die() {
			dead = true;
		}

		private boolean isDead() {
			return dead;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("[ListenerProxy: class=%s owner=%s]",  //$NON-NLS-1$
					listenerClass.getName(), get());
		}

		/**
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
		 */
		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args)
				throws Throwable {
			// Ensure that several significant methods will be redirected
			// to this proxy object instead of its owner!
			if(ignoredMethods.contains(method.getName())) {
				return method.invoke(this, args);
			}

			// Early fail if owner is dead and we failed to unregister our
			// listener proxy
			if(isDead()) {
				return null;
			}

			Object owner = get();

			if(owner==null) {
				die();

				Object source = getSource(args);
				if(source!=null) {
					//System.out.println("Owner dead, unregistering"); //$NON-NLS-1$
					tryRemoveListener(source, proxy, listenerClass);
				}

				return null;
			}

			if(enforceEDT && !SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						try {
							invoke(proxy, method, args);
						} catch (Throwable e) {
							log.error("Error delegating event to EDT", e); //$NON-NLS-1$
						}
					}
				});

				return null;
			}

			return method.invoke(owner, args);
		}
	}
}
