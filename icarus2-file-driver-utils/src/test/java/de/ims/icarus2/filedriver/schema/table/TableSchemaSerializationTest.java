/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.schema.table;

import static de.ims.icarus2.test.TestUtils.assertDeepEqual;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.filedriver.schema.tabular.TableSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.AttributeSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.AttributeTarget;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.BlockSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.ColumnSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.MemberSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.ResolverSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.SubstituteSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.SubstituteType;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl.AttributeSchemaImpl;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl.BlockSchemaImpl;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl.ColumnSchemaImpl;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl.MemberSchemaImpl;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl.ResolverSchemaImpl;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl.SubstituteSchemaImpl;
import de.ims.icarus2.filedriver.schema.tabular.xml.TableSchemaXmlReader;
import de.ims.icarus2.filedriver.schema.tabular.xml.TableSchemaXmlWriter;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public class TableSchemaSerializationTest {

	private TableSchemaImpl original;
	private TableSchema deserialized;

	private String writeAndRead() throws IOException, InterruptedException {

		String serializedForm = null;

		deserialized = null;
		try(TableSchemaXmlWriter writer = new TableSchemaXmlWriter()) {
			StringWriter sw = new StringWriter();

			writer.init(sw, null);

			writer.writeAll(Collections.singleton(original));

			serializedForm = sw.toString();
		}

//		System.out.println("--------------------------------------------");
//		System.out.println(serializedForm); //DEBUG

		try(TableSchemaXmlReader reader = new TableSchemaXmlReader()) {
			StringReader sr = new StringReader(serializedForm);

			deserialized = reader.read(sr, Options.NONE);
		}

		return serializedForm;
	}

	private void checkSerializationResult(String msg) throws Exception {
		String serializedForm = writeAndRead();

		assertDeepEqual(msg, original, deserialized, serializedForm);
	}

	@BeforeEach
	public void prepare() {
		original = new TableSchemaImpl();
	}

	private MemberSchema createMemberSchema(MemberType memberType, boolean reference) {
		return new MemberSchemaImpl()
				.setMemberType(memberType)
				.setIsReference(reference);
	}

	private SubstituteSchema createSubstitutechema(String name, SubstituteType type, MemberType memberType) {
		return new SubstituteSchemaImpl()
				.setName(name)
				.setMemberType(memberType)
				.setType(type);
	}

	private ResolverSchema createResolverSchema() {
		return new ResolverSchemaImpl()
				.setType("this.is.my.type.Impl")
				.addOption("option1", "value1")
				.addOption("anotherOption", "useless\nvalue");
	}

	private ColumnSchema createColumnSchema(int index) {
		return new ColumnSchemaImpl()
				.addSubstitute(createSubstitutechema("testSub"+index, SubstituteType.TARGET, MemberType.CONTAINER))
				.setAnnotationKey("someAnnotationKey"+index)
				.setIsIgnoreColumn(!ColumnSchema.DEFAULT_IGNORE_COLUMN)
				.setLayerId("myLayerID")
				.setName("column"+index)
				.setNoEntryLabel("_")
				.setResolver(createResolverSchema());
	}

	private AttributeSchema createAttributeSchema(String pattern, boolean resolver, AttributeTarget target) {
		AttributeSchemaImpl attributeSchema = new AttributeSchemaImpl();
		if(pattern!=null) {
			attributeSchema.setPattern(pattern);
		}
		if(resolver) {
			attributeSchema.setResolver(createResolverSchema());
		}
		if(target!=null) {
			attributeSchema.setTarget(target);
		}

		return attributeSchema;
	}

	private BlockSchema createBlock(ColumnSchema...columnSchemas) {
		return new BlockSchemaImpl()
				.setBeginDelimiter(createAttributeSchema("<s>", false, null))
				.setEndDelimiter(createAttributeSchema("</s>", false, null))
				.addAttribute(createAttributeSchema(null, true, AttributeTarget.NEXT_ITEM))
				.addColumns(columnSchemas)
				.setColumnOrderFixed(!BlockSchema.DEFAULT_COLUMN_ORDER_FIXED)
				.setComponentSchema(createMemberSchema(MemberType.ITEM, false))
				.setNoEntryLabel("_")
				.setSeparator(TableSchema.SEPARATOR_TAB)
				.setLayerId("primaryLayerOfGroup")
				.setFallbackColumn(createColumnSchema(0));
	}

	private void initBlock(BlockSchema blockSchema) {
		original.setRootBlock(blockSchema);
	}

	private void initIdentity() {
		original.setId("myId");
		original.setName("Some funny name");
		original.setDescription("This can be a rather\n long description\n\nspanning multiple lines");
	}

	private void initDefaults() {
		original.setSeparator(TableSchema.SEPARATOR_WHITESPACES);
		original.setGroupId("myLayerGroup1");

		initIdentity();
	}

	@Test
	public void testEmpty() throws Exception {
		initIdentity();
		checkSerializationResult("Empty");
	}

	@Test
	public void testShallow() throws Exception {
		initDefaults();

		checkSerializationResult("Shallow");
	}

	@Test
	public void testSimple() throws Exception {
		initDefaults();

		initBlock(createBlock());

		checkSerializationResult("Simple");
	}

	@Test
	public void testFull() throws Exception {
		initDefaults();

		initBlock(createBlock(createColumnSchema(1), createColumnSchema(2), createColumnSchema(3)));

		checkSerializationResult("Full");
	}
}
