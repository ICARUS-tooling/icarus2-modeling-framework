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
package de.ims.icarus2.filedriver.schema;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.FileDriverUtils;
import de.ims.icarus2.filedriver.schema.tabular.TableConverter;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema;
import de.ims.icarus2.filedriver.schema.tabular.xml.TableSchemaXmlReader;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.io.IOUtil;

/**
 * Implements a factory that picks or loads a {@link Converter} implementation based
 * on the properties defined in the the provided {@link ImplementationManifest}.
 *
 * @author Markus Gärtner
 *
 */
public class DefaultSchemaConverterFactory implements Factory {

	private static final String SHARED_PROPERTY_PREFIX = FileDriverUtils.SHARED_PROPERTY_PREFIX+".schema";

	/**
	 * Property containing an inline definition of the schema to be used.
	 *
	 * Needs {@link #PROPERTY_SCHEMA_TYPE} to be able to decide how to
	 * read the schema text.
	 */
	public static final String PROPERTY_SCHEMA = SHARED_PROPERTY_PREFIX+".content";

	/**
	 * Type declaration, specifying how to interpret the schema.
	 *
	 * @see TableSchema#SCHEMA_TYPE_ID
	 */
	public static final String PROPERTY_SCHEMA_ID = SHARED_PROPERTY_PREFIX+".typeId";

	/**
	 * Property denoting one of the available default schemas.
	 */
	public static final String PROPERTY_SCHEMA_NAME = SHARED_PROPERTY_PREFIX+".name";

	/**
	 * Path to the schema file that should be read
	 */
	public static final String PROPERTY_SCHEMA_FILE = SHARED_PROPERTY_PREFIX+".file";

	/**
	 * Path to the schema that should be read, accessibe from the manifest's class loader.
	 */
	public static final String PROPERTY_SCHEMA_RESOURCE = SHARED_PROPERTY_PREFIX+".resource";

	/**
	 * Remote location of the schema definition.
	 *
	 *  TODO mention caching?
	 */
	public static final String PROPERTY_SCHEMA_URL = SHARED_PROPERTY_PREFIX+".url";

	/**
	 * The character encoding to use for reading the schema.
	 * This is only relevant when the schema is to be read from a file
	 * or remote source.
	 * <p>
	 * When missing, the UTF-8 unicode charset will be used.
	 */
	public static final String PROPERTY_SCHEMA_ENCODING = SHARED_PROPERTY_PREFIX+".encoding";

	/**
	 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory#create(java.lang.Class, de.ims.icarus2.model.manifest.api.ImplementationManifest, de.ims.icarus2.model.manifest.api.ImplementationLoader)
	 */
	@Override
	public <T> T create(Class<T> resultClass, ImplementationManifest manifest, ImplementationLoader<?> environment)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException {

		//TODO check that we can actually cast Converter to the given resultClass before instantiating stuff?

		String schemaType = ManifestUtils.require(
				manifest, m -> m.getPropertyValue(PROPERTY_SCHEMA_ID), PROPERTY_SCHEMA_ID);

		Converter converter = null;

		switch(schemaType) {
			case TableSchema.SCHEMA_TYPE_ID:
				converter = buildTableConverter(manifest);
				break;

			//TODO

			default:
				throw new ModelException(ManifestErrorCode.IMPLEMENTATION_ERROR, "Unknown schema type declared: "+schemaType);
		}

		// Finally ensure assignment compatibility
		return resultClass.cast(converter);
	}

	private Charset getCharset(ImplementationManifest manifest) {

		return manifest.<String>getPropertyValue(PROPERTY_SCHEMA_ENCODING)
				.map(Charset::forName)
				.orElse(StandardCharsets.UTF_8);
	}

	private Reader createSchemaReader(ImplementationManifest manifest) throws IOException {

		String name = manifest.<String>getPropertyValue(PROPERTY_SCHEMA_NAME).orElse(null);
		if(name!=null) {
			//TODO link this with a registry facility to load schemata from
			throw new UnsupportedOperationException("Resolution of schemate by name not supported yet");
		}

		// Direct inline declaration
		String schema = manifest.<String>getPropertyValue(PROPERTY_SCHEMA).orElse(null);
		if(schema!=null) {
			return new StringReader(schema);
		}

		// Resource avialable from manifest location
		String resource = manifest.<String>getPropertyValue(PROPERTY_SCHEMA_RESOURCE).orElse(null);
		if(resource!=null) {
			ClassLoader classLoader = manifest.getManifestLocation().getClassLoader();
			return new InputStreamReader(classLoader.getResourceAsStream(resource), getCharset(manifest));
		}

		// Physical file on local file system
		String path = manifest.<String>getPropertyValue(PROPERTY_SCHEMA_FILE).orElse(null);
		if(path!=null) {
			Path file = Paths.get(path);

			InputStream in = Files.newInputStream(file);
			// Kinda easy to decide whether local file is a zip source
			if(IOUtil.isGZipSource(path)) {
				in = new GZIPInputStream(in);
			}

			return new InputStreamReader(in, getCharset(manifest));
		}

		String url = manifest.<String>getPropertyValue(PROPERTY_SCHEMA_URL).orElse(null);
		if(url!=null) {
			//TODO support compression on this stream as well! (<- difficult to decide?)
			return new InputStreamReader(new URL(url).openStream(), getCharset(manifest));
		}

		throw new ModelException(ManifestErrorCode.IMPLEMENTATION_ERROR,
				"Cannot determine what schema to load or how to locate it");
	}

	private Converter buildTableConverter(ImplementationManifest manifest) {

		Reader reader;

		try {
			reader = createSchemaReader(manifest);
		} catch (IOException e) {
			throw new ModelException(GlobalErrorCode.IO_ERROR, "Failed to prepare access to schema location", e);
		}

		reader = IOUtil.buffer(reader);

		TableSchema schema;

		try(TableSchemaXmlReader schemaReader = new TableSchemaXmlReader()) {
			schemaReader.init(reader, manifest.getPropertiesAsOptions());

			schema = schemaReader.read();
		} catch (IOException | InterruptedException e) {
			throw new ModelException(GlobalErrorCode.IO_ERROR, "Failed to load table schema", e);
		} finally {
			IOUtil.closeSilently(reader);
		}

		return new TableConverter(schema);
	}
}
