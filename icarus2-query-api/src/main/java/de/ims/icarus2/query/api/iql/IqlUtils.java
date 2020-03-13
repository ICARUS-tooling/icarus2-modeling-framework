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

import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.fasterxml.jackson.module.jsonSchema.factories.WrapperFactory;

import de.ims.icarus2.query.api.QueryFragment;

/**
 * @author Markus Gärtner
 *
 */
public final class IqlUtils {

	public static void main(String[] args) throws IOException {
		Path file = null;
		boolean printHelp = false;
		boolean cleanSchema = false;

		Class<?> clazz = IqlQuery.class;

		for (int i = 0; i < args.length; i++) {
			if("-file".equals(args[i]) || "-f".equals(args[i])) {
				file = Paths.get(args[++i]);
			} else if("-help".equals(args[i]) || "-h".equals(args[i])) {
				printHelp = true;
			} else if("-clean".equals(args[i]) || "-c".equals(args[i])) {
				cleanSchema = true;
			}
		}

		if(printHelp) {
			System.out.println("Create and print a JSON schema for the IQL exchange format:");
			System.out.println("    -file <path> Path to the file to store schema in. (shorthand version: -f <path>)");
			System.out.println("    -clean       Remove 'id' attributes from schema declarations. (shorthand version: -c)");
			System.out.println("    -help        Print this help info. (shorthand version: -h)");
			return;
		}

		ObjectMapper mapper = createMapper();

		JsonSchema schema;

		if(cleanSchema) {
	        IgnoreURNSchemaFactoryWrapper visitor = new IgnoreURNSchemaFactoryWrapper();
	        mapper.acceptJsonFormatVisitor(clazz, visitor);
	        schema = visitor.finalSchema();
	        schema.setId(null);
		} else {
			JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
			schema = schemaGen.generateSchema(clazz);
		}

		ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

		if(file!=null) {
			writer.writeValue(file.toFile(), schema);
		} else {
			writer.writeValue(System.out, schema);
		}
	}

	/*
	 * Inspired by https://stackoverflow.com/questions/41114170/removing-id-from-json-schema-when-creating-it-from-pojo-using-jackson
	 */
	private static class IgnoreURNSchemaFactoryWrapper extends SchemaFactoryWrapper {
	    public IgnoreURNSchemaFactoryWrapper() {
	        this(null, new WrapperFactory());
	    }

	    public IgnoreURNSchemaFactoryWrapper(SerializerProvider p, WrapperFactory wrapperFactory) {
	        super(p, wrapperFactory);
	        visitorContext = new VisitorContext() {
	        	/**
	        	 * Ensures our generated schema instances don't use 'id' attribute
	        	 * with hardcoded type name of our POJO classes.
	        	 */
	            @Override
				public String javaTypeToUrn(JavaType jt) { return null; }
	        };
	    }
	}

	public static ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jdk8Module()); // Properly handles java.util.Optional
		//TODO apply default configuration for IQL
		return mapper;
	}

	public static QueryFragment fragment(String query, JsonLocation loc) {
		requireNonNull(query);
		if(loc==null) {
			return null;
		}
		long offset = loc.getCharOffset();
		if(offset==-1L) {
			return null;
		}
		int _offset = strictToInt(offset);
		return new QueryFragment(query, _offset, _offset);
	}
}
