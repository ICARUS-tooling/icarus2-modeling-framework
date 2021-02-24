/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.json;

import static de.ims.icarus2.test.TestUtils.assertDeepEqual;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import de.ims.icarus2.test.LearnerTest;

/**
 * @author Markus Gärtner
 *
 */
@LearnerTest
public class JsonLearner {

	private ObjectMapper mapper;

	private static final JsonFactory FACTORY = new JsonFactory();
	static {
		//TODO configure factory
	}

	@BeforeEach
	void setUp() {
		mapper = new ObjectMapper(FACTORY);
	}

	@AfterEach
	void tearDown() {
		mapper = null;
	}

	@Test
	void testJsonNode() throws Exception {
		// Empty node
		JsonNode node = mapper.readTree("{}");
		assertThat(node.isEmpty()).isTrue();

		// Simple field
		node = mapper.readTree("{\"field\" : true}");
		assertThat(node.isEmpty()).isFalse();
		assertThat(node.get("field").isBoolean()).isTrue();

		node = mapper.readTree("{\"@type\" : \"myType\"}");
		assertThat(node.isEmpty()).isFalse();
		assertThat(node.get("@type").asText()).isEqualTo("myType");
	}

	@Test
	void testSerialization() throws Exception {
		A a = new A();
		a.field1 = 123;
		a.field2 = 345;
		String s = mapper.writeValueAsString(a);
		assertThat(s).isEqualTo("{\"field1\":123,\"field2\":345}");
	}

	@Test
	void testDeserialization() throws Exception {
		A a = new A();
		a.field1 = 123;
		a.field2 = 345;
		String s = mapper.writeValueAsString(a);
		A a2 = mapper.readValue(s, A.class);
		assertDeepEqual(null, a, a2, s);
	}

	public static class A {
		public int field1;
		public int field2;
	}

	public static class B {
		public int fieldX;
		public int fieldY;
	}

	// Dummy classes for annotation-driven polymorphic type deserialization

	@Test
	void testNestedSerialization() throws Exception {
		C c = new C();
		c.subA = new A();
		c.subA.field1 = 123;
		c.subA.field2 = 345;
		c.subB = new B();
		c.subB.fieldX = 987;
		c.subB.fieldY = 765;
		String s = mapper.writeValueAsString(c);
		assertThat(s).isEqualTo("{\"subA\":{\"field1\":123,\"field2\":345},\"subB\":{\"fieldX\":987,\"fieldY\":765}}");
	}

	public static class C {
		public A subA;
		public B subB;
	}

	// Dummy classes for annotation-driven polymorphic type deserialization

	@Test
	void testPolymorphicSerialization() throws Exception {
		Impl1_1 impl1_1 = new Impl1_1();
		impl1_1.field1 = 123;
		impl1_1.field2 = 345;
		String s = mapper.writeValueAsString(impl1_1);
		System.out.println(s);
	}

	@Test
	void testPolymorphicDeserialization() throws Exception {
		Impl1_1 impl1_1 = new Impl1_1();
		impl1_1.field1 = 123;
		impl1_1.field2 = 345;
		String s = mapper.writeValueAsString(impl1_1);
		Base1 result = mapper.readValue(s, Base1.class);
		assertDeepEqual(null, impl1_1, result, s);

		String s2 = "{\"type\":\"impl-2\",\"field1\":987,\"field3\":765}";
		Impl1_2 impl1_2 = (Impl1_2) mapper.readValue(s2, Base1.class);
		assertThat(impl1_2.field1).isEqualTo(987);
		assertThat(impl1_2.field3).isEqualTo(765);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.PROPERTY)
	@JsonSubTypes({
	    @JsonSubTypes.Type(value = Impl1_1.class, name = "impl-1"),
	    @JsonSubTypes.Type(value = Impl1_2.class, name = "impl-2") }
	)
	public static abstract class Base1 {
		public int field1;
		public abstract String type();
	}

	public static class Impl1_1 extends Base1 {
		public int field2;
		@Override
		public String type() { return "impl-1"; }
	}

	public static class Impl1_2 extends Base1 {
		public int field3;
		@Override
		public String type() { return "impl-2"; }
	}

	// Dummy classes for custom polymorphic type deserialization

	@Test
	void testCustomTypeIdResolver() throws Exception {
		Base2 impl1 = new Impl2_1();
		((Impl2_1)impl1).field2 = 123;
		String s = mapper.writeValueAsString(impl1);
		Base2 result = mapper.readValue(s, Base2.class);
		assertDeepEqual(null, impl1, result, s);
	}

	@JsonTypeInfo(
			use = JsonTypeInfo.Id.NAME,
			include = JsonTypeInfo.As.PROPERTY,
			property = "@type"
	)
	@JsonTypeIdResolver(CustomTypeIdResolver.class)
	public interface Base2 {
		String type();
	}

	@Test
	void testCustomDeserializer() throws Exception {
		Base3 impl1 = new Impl2_1();
		((Impl2_1)impl1).field2 = 123;
		String s = mapper.writeValueAsString(impl1);
		Base3 result = mapper.readValue(s, Base3.class);
		assertDeepEqual(null, impl1, result, s);
	}

	@JsonDeserialize(using=CustomDeserializer.class)
	public interface Base3 {
		@JsonGetter
		String type();
	}

	public static class Impl2_1 implements Base2, Base3 {
		public int field2;
		@Override
		public String type() { return "impl-1"; }
	}

	public static class Impl2_2 implements Base2, Base3 {
		public int field3;
		@Override
		public String type() { return "impl-2"; }
	}

	public static class CustomTypeIdResolver extends TypeIdResolverBase {

		@Override
		public String idFromValue(Object value) {
			return ((Base2)value).type();
		}

		@Override
		public String idFromValueAndType(Object value, Class<?> suggestedType) {
			return idFromValue(value);
		}

		@Override
		public JavaType typeFromId(DatabindContext context, String id) throws IOException {
			Class<?> type = null;
			switch (id) {
			case "impl-1": type = Impl2_1.class; break;
			case "impl-2": type = Impl2_2.class; break;

			default: throw new InternalError();
			}

			return context.getTypeFactory().constructType(type);
		}

		@Override
		public Id getMechanism() {
			return Id.NAME;
		}

	}

	public static class CustomDeserializer extends StdDeserializer<Base2> {
		private static final long serialVersionUID = -8097586403456470162L;

		public CustomDeserializer() {this(null);}
		public CustomDeserializer(Class<?> vc) {super(vc);}

		@Override
		public Base2 deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {

			JsonNode node = jp.readValueAsTree();

			String type = node.get("type").asText();
			switch (type) {
			case "impl-1":
				Impl2_1 impl1 = new Impl2_1();
				impl1.field2 = node.get("field2").asInt();
				return impl1;

			case "impl-2":
				Impl2_2 impl2 = new Impl2_2();
				impl2.field3 = node.get("field3").asInt();
				return impl2;

			default:
				throw new InternalError();
			}
		}
	}
}
