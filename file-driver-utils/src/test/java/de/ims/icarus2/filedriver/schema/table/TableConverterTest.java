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

import static org.mockito.Mockito.mock;
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

		return new TableSchemaImpl()
			.setRootBlock(new BlockSchemaImpl()
				.setSeparator("SPACE")
				.setEndDelimiter(new AttributeSchemaImpl().setPattern("EMPTY_LINE"))
				.setContainerSchema(new MemberSchemaImpl().setLayerId("sentence"))
				.setComponentSchema(new MemberSchemaImpl().setLayerId("token"))
				.addColumn(new ColumnSchemaImpl().setIsIgnoreColumn(true))
				.addColumn(new ColumnSchemaImpl().setLayerId("annoLayer1"))
				.addColumn(new ColumnSchemaImpl().setLayerId("annoLayer2")));
	}

	private ContextManifest createSimpleContextManifest() {
		ContextManifest contextManifest = mock(ContextManifest.class);
	}
}
