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
package de.ims.icarus2.util.xml.jaxb;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.util.concurrent.ExecutionUtil;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * @author Markus Gärtner
 *
 */
public abstract class JAXBGate<B extends Object> {

	private static final Logger log = LoggerFactory.getLogger(JAXBGate.class);

	private final IOResource resource;
	private final Object fileLock = new Object();

	private B pendingBuffer;
	private final Object bufferLock = new Object();

	private final Object gateLock = new Object();

	private AtomicBoolean updatePending = new AtomicBoolean(false);

	private final Class<B> bufferClass;

	public JAXBGate(IOResource resource, Class<B> bufferClass) {
		this.resource = requireNonNull(resource);
		this.bufferClass = requireNonNull(bufferClass);
	}

	/**
	 * @return the file
	 */
	public final IOResource getResource() {
		return resource;
	}

	protected abstract void readBuffer(B buffer) throws Exception;

	protected abstract B createBuffer() throws Exception;

	protected JAXBContext getJaxbContext() throws JAXBException {
		return JAXBContext.newInstance(bufferClass);
	}

	public void delete() throws IOException {
		resource.delete();
	}

	@SuppressWarnings("unchecked")
	public void load(Path file) throws Exception {

		B buffer = null;
		synchronized (bufferLock) {
			buffer = pendingBuffer;
		}

		// Try to load new buffer
		if(buffer==null) {
			synchronized (fileLock) {
				if(Files.notExists(file) || Files.size(file)==0) {
					return;
				}

				JAXBContext context = getJaxbContext();
				Unmarshaller unmarshaller = context.createUnmarshaller();
				buffer = (B) unmarshaller.unmarshal(Files.newInputStream(file));
			}
		}

		if(buffer==null) {
			return;
		}

		synchronized (gateLock) {
			readBuffer(buffer);
		}
	}

	public void loadBuffer() throws Exception {
		load(getFile());
	}

	public void save(Path file, boolean saveNow) throws Exception {
		if(!saveNow && !getFile().equals(file))
			throw new IllegalArgumentException("Cannot schedule timed export to foreign location: "+file); //$NON-NLS-1$

		B buffer = null;

		synchronized (gateLock) {
			buffer = createBuffer();
		}

		if(buffer==null) {
			return;
		}

		synchronized (bufferLock) {
			if(saveNow) {
				save(buffer, file);
			} else {
				pendingBuffer = buffer;

				scheduleUpdate();
			}
		}
	}

	public void saveBuffer() throws Exception {
		save(getFile(), false);
	}

	public void saveBufferNow() throws Exception {
		save(getFile(), true);
	}

	private void scheduleUpdate() {
		synchronized (bufferLock) {
			if(pendingBuffer==null) {
				return;
			}

			if(updatePending.compareAndSet(false, true)) {
				ExecutionUtil.execute(new SaveTask());
			}
		}
	}

	private void save(B buffer, Path file) throws Exception {
		synchronized (fileLock) {
			JAXBContext context = getJaxbContext();
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(buffer, Files.newOutputStream(file));
		}
	}

	private class SaveTask implements Runnable {

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			updatePending.set(false);

			B buffer = null;
			synchronized (bufferLock) {
				buffer = pendingBuffer;
				pendingBuffer = null;
			}

			if(buffer==null) {
				return;
			}

			try {
				save(buffer, getFile());
			} catch (Exception e) {
				log.error("Failed to save buffer", e); //$NON-NLS-1$
			}

			scheduleUpdate();
		}

	}
}
