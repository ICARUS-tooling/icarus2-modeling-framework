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
/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class IqlObjectIdGenerator extends ObjectIdGenerator<String> {

	private static final long serialVersionUID = 5881761728858492729L;

	private Map<String, AtomicInteger> counters;

	private final Class<?> _scope;

	public IqlObjectIdGenerator() {
		this(Object.class, null);
	}

	private IqlObjectIdGenerator(Class<?> scope,
			Map<String, AtomicInteger> counters) {
		_scope = requireNonNull(scope);
		this.counters = counters;
	}

	private Map<String, AtomicInteger> counters() {
		Map<String, AtomicInteger> counters = this.counters;
		if(counters==null) {
			synchronized (this) {
				if((counters = this.counters) == null) {
					counters = this.counters = new Object2ObjectOpenHashMap<>();
					for(IqlType type : IqlType.values()) {
						String idPrefix = type.getUidPrefix();
						if(idPrefix==null) {
							continue;
						}
						counters.put(idPrefix, new AtomicInteger(1));
					}
				}
			}
		}
		return counters;
	}

	/**
	 * @see com.fasterxml.jackson.annotation.ObjectIdGenerator#getScope()
	 */
	@Override
	public Class<?> getScope() {
		return _scope;
	}

	/**
	 * @see com.fasterxml.jackson.annotation.ObjectIdGenerator#canUseFor(com.fasterxml.jackson.annotation.ObjectIdGenerator)
	 */
	@Override
	public boolean canUseFor(ObjectIdGenerator<?> gen) {
        return (gen.getClass() == getClass()) && (gen.getScope() == _scope);
	}

	/**
	 * @see com.fasterxml.jackson.annotation.ObjectIdGenerator#forScope(java.lang.Class)
	 */
	@Override
	public ObjectIdGenerator<String> forScope(Class<?> scope) {
		return scope==_scope ? this : new IqlObjectIdGenerator(scope, counters());
	}

	/**
	 * @see com.fasterxml.jackson.annotation.ObjectIdGenerator#newForSerialization(java.lang.Object)
	 */
	@Override
	public ObjectIdGenerator<String> newForSerialization(Object context) {
		return new IqlObjectIdGenerator(_scope, null);
	}

	/**
	 * @see com.fasterxml.jackson.annotation.ObjectIdGenerator#key(java.lang.Object)
	 */
	@Override
	public IdKey key(Object key) {
		if (key == null) {
            return null;
        }
        return new IdKey(getClass(), getScope(), key);
	}

	/**
	 * @see com.fasterxml.jackson.annotation.ObjectIdGenerator#generateId(java.lang.Object)
	 */
	@Override
	public String generateId(Object forPojo) {
		if(forPojo==null) {
			return null;
		}
		IqlType type = ((IqlQueryElement)forPojo).getType();
		String idPrefix = type.getUidPrefix();
		if(idPrefix==null) {
			return null;
		}
		int count = counters().get(idPrefix).getAndIncrement();
		return idPrefix+'_'+count;
	}

}
