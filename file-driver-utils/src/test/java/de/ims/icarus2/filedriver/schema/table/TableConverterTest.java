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

import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

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
import de.ims.icarus2.model.api.view.CorpusModel;
import de.ims.icarus2.model.api.view.CorpusView;
import de.ims.icarus2.model.api.view.CorpusView.PageControl;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;
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
	public void test1TierSchema() throws IOException, SAXException, InterruptedException {

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

		CorpusManifest corpusManifest = corpusManager.getManifestRegistry().getCorpusManifest("testCorpus");

		Corpus corpus = corpusManager.connect(corpusManifest);
		CorpusView view = corpus.createFullView(AccessMode.READ, null);

		System.out.println(view.getSize());

		PageControl pageControl = view.getPageControl();
		pageControl.load();

		CorpusModel model = view.getModel();

		Container rootContainer = model.getRootContainer();
		System.out.println(rootContainer.getItemCount());

		AnnotationLayer formLayer = model.fetchLayer("form");
		AnnotationLayer lemmaLayer = model.fetchLayer("lemma");

		for(int i=0; i<rootContainer.getItemCount(); i++) {
			Item item = rootContainer.getItemAt(i);
			Object form = formLayer.getAnnotationStorage().getValue(item, null);
			Object lemma = lemmaLayer.getAnnotationStorage().getValue(item, null);
			System.out.printf("index=%d form=%s lemma=%s\n",item.getIndex(),form, lemma);
		}
	}
}
