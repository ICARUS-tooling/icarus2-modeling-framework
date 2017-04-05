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
 */
package de.ims.icarus2.filedriver.schema;

import java.io.BufferedReader;
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
import de.ims.icarus2.filedriver.schema.table.TableConverter;
import de.ims.icarus2.filedriver.schema.table.TableSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchemaXmlReader;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.util.io.IOUtil;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultSchemaConverterFactory implements Factory {

	/**
	 * Property containing an inline definition of the schema to be used.
	 *
	 * Needs {@link #PROPERTY_SCHEMA_TYPE} to be able to decide how to
	 * read the schema text.
	 */
	public static final String PROPERTY_SCHEMA = "schema";

	/**
	 * Type declaration, specifying how to interprete the schema.
	 *
	 * @see TableSchema#SCHEMA_ID
	 */
	public static final String PROPERTY_SCHEMA_ID = "schemaId";

	/**
	 * Property denoting one of the available default schemas.
	 */
	public static final String PROPERTY_SCHEMA_NAME = "schemaName";

	/**
	 * Path to the schema file that should be read
	 */
	public static final String PROPERTY_SCHEMA_FILE = "schemaFile";

	/**
	 * Remote location of the schema definition.
	 *
	 *  TODO mention caching?
	 */
	public static final String PROPERTY_SCHEMA_URL = "schemaUrl";

	/**
	 * The character encoding to use for reading the schema.
	 * This is only relevant when the schema is to be read from a file
	 * or remote source.
	 * <p>
	 * When missing, the UTF-8 unicode charset will be used.
	 */
	public static final String PROPERTY_SCHEMA_ENCODING = "schemaEncoding";

	/**
	 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory#create(java.lang.Class, de.ims.icarus2.model.manifest.api.ImplementationManifest, de.ims.icarus2.model.manifest.api.ImplementationLoader)
	 */
	@Override
	public <T> T create(Class<T> resultClass, ImplementationManifest manifest, ImplementationLoader<?> environment)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException {

		//TODO check that we can actually cast Converter to the given resultClass before instantiating stuff?

		String schemaType = manifest.getPropertyValue(PROPERTY_SCHEMA_ID);
		if(schemaType==null)
			throw new ModelException(ManifestErrorCode.IMPLEMENTATION_ERROR, "Missing declaration of schema type: "+PROPERTY_SCHEMA_ID);

		Converter converter = null;

		switch(schemaType) {
			case TableSchema.SCHEMA_ID:
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

		Charset charset = StandardCharsets.UTF_8;

		String encoding = manifest.getPropertyValue(PROPERTY_SCHEMA_ENCODING);
		if(encoding!=null) {
			charset = Charset.forName(encoding);
		}

		return charset;
	}

	private Reader createSchemaReader(ImplementationManifest manifest) throws IOException {

		String name = manifest.getPropertyValue(PROPERTY_SCHEMA_NAME);
		if(name!=null) {
			//TODO
			throw new UnsupportedOperationException();
		}

		// Direct inline declaration
		String schema = manifest.getPropertyValue(PROPERTY_SCHEMA);
		if(schema!=null) {
			return new StringReader(schema);
		}

		String path = manifest.getPropertyValue(PROPERTY_SCHEMA_FILE);
		if(path!=null) {
			Path file = Paths.get(path);

			InputStream in = Files.newInputStream(file);
			if(IOUtil.isGZipSource(path)) {
				in = new GZIPInputStream(in);
			}

			return new InputStreamReader(in, getCharset(manifest));
		}

		String url = manifest.getPropertyValue(PROPERTY_SCHEMA_URL);
		if(url!=null) {
			//TODO support compression on this stream as well!
			return new InputStreamReader(new URL(url).openStream(), getCharset(manifest));
		}

		throw new ModelException(ManifestErrorCode.IMPLEMENTATION_ERROR,
				"Cannot determine what schema to load or how to locate it");
	}

	@SuppressWarnings("resource")
	private Converter buildTableConverter(ImplementationManifest manifest) {

		Reader reader;

		try {
			reader = createSchemaReader(manifest);
		} catch (IOException e) {
			throw new ModelException(GlobalErrorCode.IO_ERROR, "Failed to prepare access to schema location", e);
		}

		if(!(reader instanceof BufferedReader)) {
			reader = new BufferedReader(reader);
		}

		TableSchema schema;

		try(TableSchemaXmlReader schemaReader = new TableSchemaXmlReader()) {
			schemaReader.init(reader, manifest.getPropertiesAsOptions());

			schema = schemaReader.read();
		} catch (IOException | InterruptedException e) {
			throw new ModelException(GlobalErrorCode.IO_ERROR, "Failed to load table schema", e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// ignore
			}
		}

		return new TableConverter(schema);
	}
}
