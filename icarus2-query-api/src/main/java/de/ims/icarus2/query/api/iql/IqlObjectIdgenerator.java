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
public class IqlObjectIdgenerator extends ObjectIdGenerator<String> {

	private static final long serialVersionUID = 5881761728858492729L;

	private Map<String, AtomicInteger> counters;

	private final Class<?> _scope;

	public IqlObjectIdgenerator() {
		this(Object.class, null);
	}

	private IqlObjectIdgenerator(Class<?> scope,
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
		return scope==_scope ? this : new IqlObjectIdgenerator(scope, counters());
	}

	/**
	 * @see com.fasterxml.jackson.annotation.ObjectIdGenerator#newForSerialization(java.lang.Object)
	 */
	@Override
	public ObjectIdGenerator<String> newForSerialization(Object context) {
		return new IqlObjectIdgenerator(_scope, null);
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
