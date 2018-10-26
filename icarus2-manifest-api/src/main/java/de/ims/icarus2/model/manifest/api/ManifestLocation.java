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
package de.ims.icarus2.model.manifest.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.util.io.IOUtil;

/**
 * Models information about the physical location of a manifest resource
 * and defines whether the source is read-only and/or template-only.
 *
 * @author Markus Gärtner
 *
 */
public abstract class ManifestLocation {

	private static final Logger log = LoggerFactory
			.getLogger(ManifestLocation.class);

	//TODO add public static methods to create default manifest locations for URL, FILE and Virtual

	private final ClassLoader classLoader;
	private final boolean readOnly;
	private final boolean template;

	protected ManifestLocation(ClassLoader classLoader, boolean readOnly, boolean template) {
		if(classLoader==null) {
			classLoader = getClass().getClassLoader();
		}

		this.classLoader = classLoader;
		this.readOnly = readOnly;
		this.template = template;
	}

	public URL getUrl() {
		return null;
	}

	/**
	 * Open the underlying resource for read access.
	 *
	 * @return
	 * @throws IOException
	 * @throws UnsupportedOperationException in case the location is write-only
	 */
	public abstract Reader getInput() throws IOException;

	/**
	 * Open the underlying resource for write access.
	 *
	 * @return
	 * @throws IOException
	 * @throws UnsupportedOperationException in case the location is read-only
	 */
	public abstract Writer getOutput() throws IOException;

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @return the template
	 */
	public boolean isTemplate() {
		return template;
	}

	/**
	 * Returns the {@code ClassLoader} instance that is associated with the
	 * physical location this {@code ManifestLocation} wraps.
	 *
	 * @return the classLoader
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public static class URLManifestLocation extends ManifestLocation {

		private final URL url;
		private final Charset charset;

		public URLManifestLocation(URL url, ClassLoader classLoader,
				boolean readOnly, boolean template) {
			this(url, IOUtil.UTF8_CHARSET, classLoader, readOnly, template);
		}

		public URLManifestLocation(URL url, Charset charset, ClassLoader classLoader,
				boolean readOnly, boolean template) {
			super(classLoader, readOnly, template);

			if (url == null)
				throw new NullPointerException("Invalid url"); //$NON-NLS-1$
			if (charset == null)
				throw new NullPointerException("Invalid charset");

			this.url = url;
			this.charset = charset;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ManifestLocation#getUrl()
		 */
		@Override
		public URL getUrl() {
			return url;
		}

		/**
		 * @throws IOException
		 * @see de.ims.icarus2.model.manifest.api.ManifestLocation#getInput()
		 */
		@Override
		public Reader getInput() throws IOException {
			return new InputStreamReader(url.openConnection().getInputStream(), charset);
		}

		/**
		 * @throws IOException
		 * @see de.ims.icarus2.model.manifest.api.ManifestLocation#getOutput()
		 */
		@Override
		public Writer getOutput() throws IOException {
			return new OutputStreamWriter(url.openConnection().getOutputStream(), charset);
		}

	}

	public static class FileManifestLocation extends ManifestLocation {

		private final Path path;
		private final Charset charset;

		public FileManifestLocation(Path path, ClassLoader classLoader,
				boolean readOnly, boolean template) {
			this(path, IOUtil.UTF8_CHARSET, classLoader, readOnly, template);
		}

		public FileManifestLocation(Path path, Charset charset, ClassLoader classLoader,
				boolean readOnly, boolean template) {
			super(classLoader, readOnly, template);

			if (path == null)
				throw new NullPointerException("Invalid path"); //$NON-NLS-1$
			if (charset == null)
				throw new NullPointerException("Invalid charset");

			this.path = path;
			this.charset = charset;
		}

		/**
		 * @return the path
		 */
		public Path getPath() {
			return path;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ManifestLocation#getUrl()
		 */
		@Override
		public URL getUrl() {
			try {
				return path.toUri().toURL();
			} catch (MalformedURLException e) {

				log.warn("Failed to convert path into URL", e); //$NON-NLS-1$

				return null;
			}
		}

		/**
		 * @throws IOException
		 * @see de.ims.icarus2.model.manifest.api.ManifestLocation#getInput()
		 */
		@Override
		public Reader getInput() throws IOException {
			return Files.newBufferedReader(path, charset);
		}

		/**
		 * @throws IOException
		 * @see de.ims.icarus2.model.manifest.api.ManifestLocation#getOutput()
		 */
		@Override
		public Writer getOutput() throws IOException {
			return Files.newBufferedWriter(path, charset,
					StandardOpenOption.WRITE,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		}
	}

	public static class VirtualManifestInputLocation extends ManifestLocation {

		private String content;

		public VirtualManifestInputLocation(ClassLoader classLoader, boolean template) {
			super(classLoader, true, template);
		}

		public VirtualManifestInputLocation(String content, ClassLoader classLoader, boolean template) {
			super(classLoader, true, template);

			setContent(content);
		}

		public String getContent() {
			return content;
		}

		public void setContent(String text) {
			if (text == null)
				throw new NullPointerException("Invalid text"); //$NON-NLS-1$

			content = text;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ManifestLocation#getInput()
		 */
		@Override
		public Reader getInput() throws IOException {
			return new StringReader(content);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ManifestLocation#getOutput()
		 */
		@Override
		public Writer getOutput() throws IOException {
			throw new UnsupportedOperationException();
		}

	}

	public static class VirtualManifestOutputLocation extends ManifestLocation {

		private StringWriter buffer;

		public VirtualManifestOutputLocation(ClassLoader classLoader, boolean template) {
			super(classLoader, false, template);
		}

		public String getContent() {
			return buffer.toString();
		}

		public void reset() {
			buffer = new StringWriter();
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ManifestLocation#getInput()
		 */
		@Override
		public Reader getInput() throws IOException {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.ManifestLocation#getOutput()
		 */
		@Override
		public Writer getOutput() throws IOException {
			reset();
			return buffer;
		}

	}
}
