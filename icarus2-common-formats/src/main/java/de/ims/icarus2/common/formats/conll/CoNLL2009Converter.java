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
/**
 *
 */
package de.ims.icarus2.common.formats.conll;

import static de.ims.icarus2.util.Conditions.checkState;

import java.io.IOException;
import java.nio.charset.Charset;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.filedriver.AbstractConverter;
import de.ims.icarus2.filedriver.FileDriver;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.registry.LayerMemberFactory;
import de.ims.icarus2.model.standard.driver.ChunkConsumer;

/**
 * @author Markus Gärtner
 *
 */
public class CoNLL2009Converter extends AbstractConverter {

	public static final String LAYER_TOKEN = "token";
	public static final String LAYER_SENTENCE = "sentence";
	public static final String LAYER_FORM = "form";
	public static final String LAYER_SYNTAX = "goldDependency";
	public static final String LAYER_PREDICTED_SYNTAX = "predictedDependency";
	public static final String LAYER_SURFACE = "goldPos";
	public static final String LAYER_PREDICTED_SURFACE = "predictedPos";
	public static final String LAYER_DEPENDENCY = "goldDependencyRelation";
	public static final String LAYER_PREDICTED_DEPENDENCY = "predictedDependencyRelation";

	private Charset encoding;
	private LayerMemberFactory memberFactory;

	private ItemLayer l_token;
	private ItemLayer l_sentence;
	private StructureLayer s_syntax;
	private StructureLayer s_psyntax;
	private AnnotationLayer a_form;
	private AnnotationLayer a_surface;
	private AnnotationLayer a_psurface;
	private AnnotationLayer a_deprel;
	private AnnotationLayer a_pdeprel;

	/**
	 * @see de.ims.icarus2.filedriver.AbstractConverter#addNotify(de.ims.icarus2.model.api.driver.Driver)
	 */
	@Override
	public void addNotify(Driver owner) {
		super.addNotify(owner);

		FileDriver driver = (FileDriver) owner;

		encoding = driver.getEncoding();
		memberFactory = driver.newMemberFactory();

		Context context = driver.getContext();
		checkState(context!=null);
	}

	/**
	 * @see de.ims.icarus2.filedriver.AbstractConverter#removeNotify(de.ims.icarus2.model.api.driver.Driver)
	 */
	@Override
	public void removeNotify(Driver owner) {
		// TODO Auto-generated method stub
		super.removeNotify(owner);
	}

	/**
	 * @see de.ims.icarus2.filedriver.Converter#scanFile(int)
	 */
	@Override
	public Report<ReportItem> scanFile(int fileIndex) throws IOException, InterruptedException, IcarusApiException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus2.filedriver.Converter#loadFile(int, de.ims.icarus2.model.standard.driver.ChunkConsumer)
	 */
	@Override
	public LoadResult loadFile(int fileIndex, ChunkConsumer action)
			throws IOException, InterruptedException, IcarusApiException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This implementation does not support chunking and as such has no use for cursors.
	 *
	 * @see de.ims.icarus2.filedriver.AbstractConverter#createDelegatingCursor(int, de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	protected DelegatingCursor<?> createDelegatingCursor(int fileIndex, ItemLayer layer) {
		return null;
	}

	/**
	 * @see de.ims.icarus2.filedriver.AbstractConverter#readItemFromCursor(de.ims.icarus2.filedriver.AbstractConverter.DelegatingCursor)
	 */
	@Override
	protected Item readItemFromCursor(DelegatingCursor<?> cursor)
			throws IOException, InterruptedException, IcarusApiException {
		throw new IcarusRuntimeException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Implementatio ndoes not support cursor interactions");
	}
}
