/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import org.junit.jupiter.api.Test;

import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.AttributeSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.BlockSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.ColumnSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.MemberSchemaImpl;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.view.paged.CorpusModel;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;
import de.ims.icarus2.test.annotations.ResourceTest;
import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
public class TableConverterTest {


	private TableSchema create2TierTableSchema() {

		return new TableSchemaImpl()
			.setRootBlock(new BlockSchemaImpl()
				.setLayerId("sentenceLayer")
				.setSeparator("SPACE")
				.setEndDelimiter(new AttributeSchemaImpl().setPattern("EMPTY_LINE"))
				.setComponentSchema(new MemberSchemaImpl().setMemberType(MemberType.CONTAINER))
				.addBlock(new BlockSchemaImpl()
					.setLayerId("tokenLayer")
					.setComponentSchema(new MemberSchemaImpl().setMemberType(MemberType.ITEM))
					.addColumn(new ColumnSchemaImpl("ID").setIsIgnoreColumn(true))
					.addColumn(new ColumnSchemaImpl("FORM").setLayerId("annoLayer1"))
					.addColumn(new ColumnSchemaImpl("LEMMA").setLayerId("annoLayer2")))
				);
	}

	private TableSchema create1TierTableSchema() {

		return new TableSchemaImpl()
			.setRootBlock(new BlockSchemaImpl()
				.setLayerId("token")
				.setSeparator("WHITESPACES")
				.setEndDelimiter(new AttributeSchemaImpl().setPattern("EMPTY_LINE"))
				.setComponentSchema(new MemberSchemaImpl().setMemberType(MemberType.ITEM))
				.addColumn(new ColumnSchemaImpl("ID").setIsIgnoreColumn(true))
				.addColumn(new ColumnSchemaImpl("FORM").setLayerId("form"))
				.addColumn(new ColumnSchemaImpl("LEMMA").setLayerId("lemma"))
				);
	}

	@Test
	@ResourceTest
	public void test1TierSchema() throws Exception {

		CorpusManager corpusManager = DefaultCorpusManager.newBuilder()
			.defaultEnvironment()
			.build();

		// Get template
		ManifestXmlReader manifestXmlReader = ManifestXmlReader.newBuilder()
				.registry(corpusManager.getManifestRegistry())
				.useImplementationDefaults()
				.build();
		manifestXmlReader.addSource(new ManifestLocation.URLManifestLocation(
				TableConverterTest.class.getResource("tier1.imf.xml"),
				getClass().getClassLoader(), true, false));
		manifestXmlReader.readAndRegisterAll();

		CorpusManifest corpusManifest = corpusManager.getManifestRegistry().getCorpusManifest("testCorpus").get();

		Corpus corpus = corpusManager.connect(corpusManifest);
		PagedCorpusView view = corpus.createFullView(AccessMode.READ, null);

		System.out.println(view.getSize());

		PageControl pageControl = view.getPageControl();
		pageControl.load();

		CorpusModel model = view.getModel();

		Container rootContainer = model.getRootContainer();
		System.out.println(rootContainer.getItemCount());

		AnnotationLayer formLayer = view.fetchLayer("form");
		AnnotationLayer lemmaLayer = view.fetchLayer("lemma");

		for(int i=0; i<rootContainer.getItemCount(); i++) {
			Item item = rootContainer.getItemAt(i);
			Object form = formLayer.getValue(item);
			Object lemma = lemmaLayer.getValue(item);
			System.out.printf("index=%d form=%s lemma=%s\n",item.getIndex(),form, lemma);
		}
	}
}
