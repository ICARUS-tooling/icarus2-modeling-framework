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
import java.nio.file.Paths;

import org.junit.Test;
import org.xml.sax.SAXException;

import de.ims.icarus2.filedriver.FileDriver;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.AttributeSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.BlockSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.ColumnSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.MemberSchemaImpl;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.view.CorpusModel;
import de.ims.icarus2.model.api.view.CorpusView;
import de.ims.icarus2.model.api.view.CorpusView.PageControl;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.standard.DefaultManifestFactory;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.model.standard.io.DefaultFileManager;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;
import de.ims.icarus2.model.standard.registry.metadata.VirtualMetadataRegistry;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public class TableConverterTest {

	private static final String SINGLE_BLOCK =
			 "1 A a\n"
			+"2 B b\n"
			+"3 C c\n"
			+"4 D d\n"
			+"5 E e";


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

	private ContextManifest create2TierContextManifest(ManifestRegistry registry) {
		ManifestFactory factory = new DefaultManifestFactory(
				new ManifestLocation.VirtualManifestInputLocation(null, false),
				registry);

		ContextManifest contextManifest = factory.create(ManifestType.CONTEXT_MANIFEST);

		LayerGroupManifest groupManifest = factory.create(ManifestType.LAYER_GROUP_MANIFEST, contextManifest);

		// Sentence layer
		ItemLayerManifest sentenceLayerManifest = factory.create(ManifestType.ITEM_LAYER_MANIFEST, groupManifest);
		sentenceLayerManifest.setId("sentence");
		sentenceLayerManifest.addBaseLayerId("token");
		ContainerManifest sentenceManifest = factory.create(ManifestType.CONTAINER_MANIFEST, sentenceLayerManifest);
		sentenceManifest.setContainerType(ContainerType.LIST);
		sentenceLayerManifest.addContainerManifest(sentenceManifest);
		groupManifest.addLayerManifest(sentenceLayerManifest);
		groupManifest.setPrimaryLayerId(sentenceLayerManifest.getId());

		// Token layer
		ItemLayerManifest tokenLayerManifest = factory.create(ManifestType.ITEM_LAYER_MANIFEST, groupManifest);
		tokenLayerManifest.setId("token");
		groupManifest.addLayerManifest(tokenLayerManifest);

		// Annotations 1
		AnnotationLayerManifest annotationLayerManifest1 = factory.create(ManifestType.ANNOTATION_LAYER_MANIFEST, groupManifest);
		annotationLayerManifest1.addBaseLayerId("token");
		AnnotationManifest annotationManifest1 = factory.create(ManifestType.ANNOTATION_MANIFEST, annotationLayerManifest1);
		annotationManifest1.setKey("anno1");
		annotationLayerManifest1.addAnnotationManifest(annotationManifest1);
		groupManifest.addLayerManifest(annotationLayerManifest1);

		// Annotations 2
		AnnotationLayerManifest annotationLayerManifest2 = factory.create(ManifestType.ANNOTATION_LAYER_MANIFEST, groupManifest);
		annotationLayerManifest2.addBaseLayerId("token");
		AnnotationManifest annotationManifest2 = factory.create(ManifestType.ANNOTATION_MANIFEST, annotationLayerManifest2);
		annotationManifest2.setKey("anno2");
		annotationLayerManifest2.addAnnotationManifest(annotationManifest2);
		groupManifest.addLayerManifest(annotationLayerManifest2);

		contextManifest.addLayerGroup(groupManifest);
		contextManifest.setPrimaryLayerId(sentenceLayerManifest.getId());

		return contextManifest;
	}

	private ContextManifest create1TierContextManifest(ManifestRegistry registry) {
		ManifestFactory factory = new DefaultManifestFactory(
				new ManifestLocation.VirtualManifestInputLocation(null, false),
				registry);

		ContextManifest contextManifest = factory.create(ManifestType.CONTEXT_MANIFEST);

		LayerGroupManifest groupManifest = factory.create(ManifestType.LAYER_GROUP_MANIFEST, contextManifest);

		// Sentence layer

		// Token layer
		ItemLayerManifest tokenLayerManifest = factory.create(ManifestType.ITEM_LAYER_MANIFEST, groupManifest);
		tokenLayerManifest.setId("token");
		groupManifest.addLayerManifest(tokenLayerManifest);
		groupManifest.setPrimaryLayerId(tokenLayerManifest.getId());

		// Annotations 1
		AnnotationLayerManifest annotationLayerManifest1 = factory.create(ManifestType.ANNOTATION_LAYER_MANIFEST, groupManifest);
		annotationLayerManifest1.setId("annoLayer1");
		annotationLayerManifest1.addBaseLayerId("token");
		AnnotationManifest annotationManifest1 = factory.create(ManifestType.ANNOTATION_MANIFEST, annotationLayerManifest1);
		annotationManifest1.setKey("anno1");
		annotationLayerManifest1.addAnnotationManifest(annotationManifest1);
		groupManifest.addLayerManifest(annotationLayerManifest1);

		// Annotations 2
		AnnotationLayerManifest annotationLayerManifest2 = factory.create(ManifestType.ANNOTATION_LAYER_MANIFEST, groupManifest);
		annotationLayerManifest2.setId("annoLayer2");
		annotationLayerManifest2.addBaseLayerId("token");
		AnnotationManifest annotationManifest2 = factory.create(ManifestType.ANNOTATION_MANIFEST, annotationLayerManifest2);
		annotationManifest2.setKey("anno2");
		annotationLayerManifest2.addAnnotationManifest(annotationManifest2);
		groupManifest.addLayerManifest(annotationLayerManifest2);

		contextManifest.addLayerGroup(groupManifest);
		contextManifest.setPrimaryLayerId(tokenLayerManifest.getId());

		return contextManifest;
	}

	@Test
	public void test1TierSchema() throws IOException, SAXException, InterruptedException {

		CorpusManager corpusManager = DefaultCorpusManager.newBuilder()
			.defaultEnvironment()
			.build();

		// Get template
		ManifestXmlReader manifestXmlReader = new ManifestXmlReader(corpusManager.getManifestRegistry());
		manifestXmlReader.addSource(new ManifestLocation.URLManifestLocation(
				TableConverterTest.class.getResource("tier1.imf.xml"),
				getClass().getClassLoader(), true, false));
		manifestXmlReader.readAndRegisterAll();

		CorpusManifest corpusManifest = corpusManager.getManifestRegistry().getCorpusManifest("testCorpus");

		TableSchema tableSchema = create1TierTableSchema();
		corpusManifest.getRootContextManifest().getDriverManifest().setPropertyValue("tableSchema", tableSchema);

		Corpus corpus = corpusManager.connect(corpusManifest);
		IndexSet[] indices = IndexUtils.wrap(0L, 4L);
		CorpusView view = corpus.createView(corpus.createCompleteScope(), indices, AccessMode.READ, Options.emptyOptions);

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

	public void xxx() {
		CorpusManager corpusManager = DefaultCorpusManager.newBuilder()
			.manifestRegistry(new DefaultManifestRegistry())
			.metadataRegistry(new VirtualMetadataRegistry())
			.fileManager(new DefaultFileManager(Paths.get(".")))
			.build();

		TableSchema tableSchema = create1TierTableSchema();
		ContextManifest contextManifest = create1TierContextManifest(corpusManager.getManifestRegistry());
		MetadataRegistry metadataRegistry = new VirtualMetadataRegistry();

		FileDriver fileDriver = new FileDriver.FileDriverBuilder()
			.metadataRegistry(metadataRegistry)
			.build();
	}
}
