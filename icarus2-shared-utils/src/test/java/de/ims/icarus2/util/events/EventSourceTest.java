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
/**
 *
 */
package de.ims.icarus2.util.events;

import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.ILLEGAL_ARGUMENT_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class EventSourceTest<E extends EventSource> implements EventManagerTest<E> {


	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends E> getTestTargetClass() {
		return (Class<E>)EventSource.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E createTestInstance(TestSettings settings) {
		return (E) settings.process(new EventSource());
	}

	@SuppressWarnings("unchecked")
	public E withSource(TestSettings settings, Object source) {
		return (E) settings.process(new EventSource(source));
	}

	@Override
	public void testMandatoryConstructors() throws Exception {
		Object source = new Object();
		E instance = withSource(settings(), source);

		assertSame(source, instance.getEventSource());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventSource#getEventSource()}.
	 */
	@Test
	void testGetEventSource() {
		assertGetter(create(),
				new Object(), new Object(),
				NO_DEFAULT(),
				EventSource::getEventSource,
				EventSource::setEventSource);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventSource#setEventSource(java.lang.Object)}.
	 */
	@Test
	void testSetEventSource() {
		assertSetter(create(),
				EventSource::setEventSource,
				new Object(), NO_NPE_CHECK, NO_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventSource#isEventsEnabled()}.
	 */
	@Test
	void testIsEventsEnabled() {
		assertGetter(create(),
				Boolean.TRUE, Boolean.FALSE,
				DEFAULT(Boolean.valueOf(EventSource.DEFAULT_EVENTS_ENABLED)),
				EventSource::isEventsEnabled,
				EventSource::setEventsEnabled);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventSource#setEventsEnabled(boolean)}.
	 */
	@Test
	void testSetEventsEnabled() {
		assertSetter(create(), EventSource::setEventsEnabled);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventSource#getDeadListenerTreshold()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	void testGetDeadListenerTreshold() {
		assertGetter(create(),
				3, 99,
				DEFAULT(EventSource.DEFAULT_DEAD_LISTENER_THRESHOLD),
				EventSource::getDeadListenerTreshold,
				EventSource::setDeadListenerTreshold);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventSource#setDeadListenerTreshold(int)}.
	 */
	@SuppressWarnings("boxing")
	@Test
	void testSetDeadListenerTreshold() {
		assertSetter(create(),
				EventSource::setDeadListenerTreshold,
				3, NO_NPE_CHECK, ILLEGAL_ARGUMENT_CHECK, -1);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventSource#fireEvent(de.ims.icarus2.util.events.EventObject)}.
	 */
	@Test
	void testFireEventEventObject() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventSource#fireEventEDT(de.ims.icarus2.util.events.EventObject)}.
	 */
	@Test
	void testFireEventEDTEventObject() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventSource#fireEvent(de.ims.icarus2.util.events.EventObject, java.lang.Object)}.
	 */
	@Test
	void testFireEventEventObjectObject() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventSource#fireEventEDT(de.ims.icarus2.util.events.EventObject, java.lang.Object)}.
	 */
	@Test
	void testFireEventEDTEventObjectObject() {
		fail("Not yet implemented"); // TODO
	}

}
