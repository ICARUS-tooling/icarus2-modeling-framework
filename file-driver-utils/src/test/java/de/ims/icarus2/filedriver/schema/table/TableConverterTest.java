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

import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.AttributeSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.BlockSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.ColumnSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.MemberSchemaImpl;
import de.ims.icarus2.model.manifest.api.ContextManifest;

/**
 * @author Markus Gärtner
 *
 */
public class TableConverterTest {


	private TableSchema createSimpleTableSchema() {
		BlockSchemaImpl blockSchema = new BlockSchemaImpl();
		blockSchema.setSeparator("SPACE");

		AttributeSchemaImpl endDelimiter = new AttributeSchemaImpl();
		endDelimiter.setPattern("EMPTY_LINE");
		blockSchema.setEndDelimiter(endDelimiter);

		MemberSchemaImpl containerSchema = new MemberSchemaImpl();
		containerSchema.setLayerId("sentence");
		blockSchema.setContainerSchema(containerSchema);

		MemberSchemaImpl componentSchema = new MemberSchemaImpl();
		componentSchema.setLayerId("token");
		blockSchema.setComponentSchema(componentSchema);

		// Column 0
		ColumnSchemaImpl column0 = new ColumnSchemaImpl();
		column0.setIsIgnoreColumn(true);
		blockSchema.addColumn(column0);

		//Column 1
		ColumnSchemaImpl column1 = new ColumnSchemaImpl();
		column1.setLayerId("annoLayer1");
		blockSchema.addColumn(column1);

		//Column 2
		ColumnSchemaImpl column2 = new ColumnSchemaImpl();
		column2.setLayerId("annoLayer2");
		blockSchema.addColumn(column2);

		TableSchemaImpl tableSchema = new TableSchemaImpl();
		tableSchema.setRootBlock(blockSchema);

		return tableSchema;
	}

	private ContextManifest createSimpleContextManifest() {

	}
}
