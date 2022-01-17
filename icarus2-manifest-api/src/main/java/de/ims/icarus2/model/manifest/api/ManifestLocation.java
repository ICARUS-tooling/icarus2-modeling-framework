/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.util.AbstractBuilder;
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

	@Nullable
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

	/**
	 * Returns the character encoding used for this manifest. If not overridden
	 * by subclasses, this method returns {@link StandardCharsets#UTF_8} per default.
	 * @return
	 */
	public Charset getEncoding() {
		return StandardCharsets.UTF_8;
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
		 * Returns the encoding specified at construction time.
		 * @see de.ims.icarus2.model.manifest.api.ManifestLocation#getEncoding()
		 */
		@Override
		public Charset getEncoding() {
			return charset;
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
		 * Returns the encoding specified at construction time.
		 * @see de.ims.icarus2.model.manifest.api.ManifestLocation#getEncoding()
		 */
		@Override
		public Charset getEncoding() {
			return charset;
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

		public VirtualManifestInputLocation(String content, ClassLoader classLoader, boolean template) {
			super(classLoader, true, template);

			setContent(content);
		}

		public String getContent() {
			return content;
		}

		public void setContent(String text) {
			content = requireNonNull(text);
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

	public static Builder builder() {
		return new Builder();
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@Api(type=ApiType.BUILDER)
	public static class Builder extends AbstractBuilder<Builder, ManifestLocation> {

		private boolean template = false;
		private boolean readOnly = false;
		private ClassLoader classLoader;
		private URL url;
		private Path file;
		private Charset charset;
		private boolean virtual = false;
		private boolean input = false;
		private String content;

		private Builder() {
			// no-op
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="false")
		public boolean isTemplate() {
			return template;
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="false")
		public boolean isReadOnly() {
			return readOnly;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public URL getUrl() {
			return url;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public Path getFile() {
			return file;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public Charset getCharset() {
			return charset;
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="false")
		public boolean isVirtual() {
			return virtual;
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="false")
		public boolean isInput() {
			return input;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public String getContent() {
			return content;
		}

		@Guarded(methodType=MethodType.GETTER)
		public ClassLoader getClassLoader() {
			return classLoader==null ? getClass().getClassLoader() : classLoader;
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder file(Path file) {
			requireNonNull(file);
			checkState("File already set", this.file==null);
			checkState("URL already set", url==null);
			checkState("Already virtual", !virtual);

			this.file = file;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder url(URL url) {
			requireNonNull(url);
			checkState("URL already set", this.url==null);
			checkState("File already set", file==null);
			checkState("Already virtual", !virtual);

			this.url = url;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder charset(Charset charset) {
			requireNonNull(charset);
			checkState("Charset already set", this.charset==null);
			checkState("Already virtual", !virtual);

			this.charset = charset;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder template() {
			checkState("Already template", !template);
			checkState("Already virtual", !virtual);

			template = true;

			return thisAsCast();

		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder content(String content) {
			requireNonNull(content);
			checkState("Content already set", this.content==null);

			this.content = content;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder readOnly() {
			checkState("Already read-only", !readOnly);
			checkState("Already virtual", !virtual);

			readOnly = true;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder virtual() {
			checkState("Already virtual", !virtual);
			checkState("URL already set", url==null);
			checkState("File already set", file==null);

			virtual = true;

			return thisAsCast();

		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder input() {
			checkState("Already input", !input);

			input = true;

			return thisAsCast();

		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder classLoader(ClassLoader classLoader) {
			requireNonNull(classLoader);
			checkState("Classloader already set", this.classLoader==null);

			this.classLoader = classLoader;

			return thisAsCast();
		}

		public Builder utf8() {
			return charset(StandardCharsets.UTF_8);
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#validate()
		 */
		@Override
		protected void validate() {
			super.validate();

			checkState("Must define one of the following: virtual, url or file",
					virtual || url!=null || file!=null);

			if(virtual && input) {
				checkState("Must define content if declaring as virtual input", content!=null);
			}

			if(!input) {
				checkState("Can't define content if not declaring as input", content==null);
			}
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected ManifestLocation create() {
			validate();

			ClassLoader classLoader = getClassLoader();

			if(virtual) {
				if(input) {
					return new VirtualManifestInputLocation(content, classLoader, template);
				}
				return new VirtualManifestOutputLocation(classLoader, template);
			} else if(file!=null) {
				if(charset!=null) {
					return new FileManifestLocation(file, charset, classLoader, readOnly, template);
				}
				return new FileManifestLocation(file, classLoader, readOnly, template);
			} else {
				if(charset!=null) {
					return new URLManifestLocation(url, charset, classLoader, readOnly, template);
				}
				return new URLManifestLocation(url, classLoader, readOnly, template);
			}
		}
	}
}
