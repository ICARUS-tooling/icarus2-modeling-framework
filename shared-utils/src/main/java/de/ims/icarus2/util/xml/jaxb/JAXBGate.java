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
 */
package de.ims.icarus2.util.xml.jaxb;

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

/**
 * @author Markus Gärtner
 *
 */
public abstract class JAXBGate<B extends Object> {
	
	private static final Logger log = LoggerFactory.getLogger(JAXBGate.class);

	private final Path file;
	private final Object fileLock = new Object();

	private B pendingBuffer;
	private final Object bufferLock = new Object();

	private final Object gateLock = new Object();

	private AtomicBoolean updatePending = new AtomicBoolean(false);

	private final Class<B> bufferClass;

	public JAXBGate(Path file, Class<B> bufferClass) {
		if(file==null)
			throw new NullPointerException("Invalid file"); //$NON-NLS-1$

		this.file = file;
		this.bufferClass = bufferClass;
	}

	/**
	 * @return the file
	 */
	public final Path getFile() {
		return file;
	}

	protected abstract void readBuffer(B buffer) throws Exception;

	protected abstract B createBuffer() throws Exception;

	protected JAXBContext getJaxbContext() throws JAXBException {
		return JAXBContext.newInstance(bufferClass);
	}

	public void delete() throws IOException {
		Files.delete(file);
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
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
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
