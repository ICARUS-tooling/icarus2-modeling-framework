/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.standard.driver.conll09;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.driver.ChunkInfo;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.layer.ItemLayer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoNLL09Connector extends FileConnector {

	public static final String ID = "ConLL09Connector";

	/**
	 * @param builder
	 * @param id
	 */
	protected CoNLL09Connector(FileConnectorBuilder<?> builder) {
		super(builder, ID);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see de.ims.icarus2.model.standard.driver.file.connector.FileConnector#prepareInit()
	 */
	@Override
	protected void prepareInit() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.model.standard.driver.file.connector.FileConnector#prepareFinalize()
	 */
	@Override
	protected void prepareFinalize() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.model.standard.driver.file.connector.FileConnector#scanFile(int)
	 */
	@Override
	public void scanFile(int fileIndex) {
		Path file = dataFiles.getFileAt(fileIndex);
	}

	/**
	 * @see de.ims.icarus2.model.standard.driver.file.connector.FileConnector#loadFile(int, java.util.function.Consumer)
	 */
	@Override
	public long loadFile(int fileIndex, Consumer<ChunkInfo> action)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus2.model.standard.driver.file.connector.FileConnector#loadChunks(de.ims.icarus2.model.api.layer.ItemLayer, de.ims.icarus2.model.api.driver.indices.IndexSet[], java.util.function.Consumer)
	 */
	@Override
	public long loadChunks(ItemLayer layer, IndexSet[] indices,
			Consumer<ChunkInfo> action) throws IOException,
			InterruptedException {
		// TODO Auto-generated method stub
		return 0;
	}

}
