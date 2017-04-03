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
package de.ims.icarus2.filedriver.schema.table;

import static de.ims.icarus2.TestUtils.assertDeepEqual;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeTarget;
import de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.MemberSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.ResolverSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteType;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.AttributeSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.BlockSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.ColumnSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.MemberSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.ResolverSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.SubstituteSchemaImpl;
import de.ims.icarus2.model.api.members.MemberType;

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

			reader.init(sr, null);

			deserialized = reader.read();
		}

		return serializedForm;
	}

	private void checkSerializationResult(String msg) throws Exception {
		String serializedForm = writeAndRead();

		assertDeepEqual(msg, original, deserialized, serializedForm);
	}

	@Before
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
