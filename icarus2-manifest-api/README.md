# ICARUS2 Manifest API

## Overview

The manifest framework provides the foundation for all other members of the ICARUS2 project. Manifests are formal descriptions of the structure, inner composition and outer dependencies of (parts) of a corpus resource. Corpora consist of a hierarchy of different components which all describe different aspects of a corpus.
TODO  

## Useful Links

* [Full schema file](src/main/resources/de/ims/icarus2/model/manifest/xml/corpus.xsd)

## Creating Manifests

Any manifest needs to be associated with a registry and location. The registry defines the namespace in which each manifest's identifier has to be unique. It also serves as a means of resolving references, e.g. for templates, and is the place to send or receive manifest-related events. 

```java
ManifestRegistry registry = new DefaultManifestRegistry();
// fill registry with manifests
CorpusManifest corpus = registry.getCorpusManifest("theCorpusIWant").get();
// do something with the manifest
```

Each manifest only *knows* about other manifests that are properly registered with the same registry instance. As such every application typically has one global registry to host all the manifests it needs.

To instantiate manifests you can either directly use the default implementations provided in the `de.ims.icarus2.model.manifest.standard` package or delegate the job in a more comfortable fashion to an instance of the `de.ims.icarus2.model.manifest.api.ManifestFactory`:

```java
// Set up the factory
ManifestRegistry registry = new DefaultManifestRegistry();
ManifestLocation location = ManifestLocation.newBuilder().virtual().build();
ManifestFactory factory = new DefaultManifestFactory(location, registry);

// Start creating and assembling manifests
ContextManifest context = factory.create(ManifestType.CONTEXT_MANIFEST);

LayerGroupManifest group = factory.create(ManifestType.LAYER_GROUP_MANIFEST, context);
context.addLayerGroup(group);

ItemLayerManifest tokenLayer = factory.create(ManifestType.ITEM_LAYER_MANIFEST, group);
tokenLayer.setId("tokens");

ItemLayerManifest sentenceLayer = factory.create(ManifestType.ITEM_LAYER_MANIFEST, group);
sentenceLayer.setId("sentences");
sentenceLayer.setFoundationLayerId("tokens");
sentenceLayer.addBaseLayerId("tokens");

AnnotationLayerManifest annoLayer = factory.create(ManifestType.ANNOTATION_LAYER_MANIFEST, group);
annoLayer.addBaseLayerId("tokens");

AnnotationManifest forms = factory.create(ManifestType.ANNOTATION_MANIFEST, annoLayer);
forms.setId("forms");
annoLayer.addAnnotationManifest(forms);
annoLayer.setDefaultKey("forms");

group.addLayerManifest(tokenLayer);
group.addLayerManifest(sentenceLayer);
group.addLayerManifest(annoLayer);
```

Above code will create 3 layers representing tokens, sentences and surface form annotations and group them into a context. For a proper specification a lot of additional information is needed, such as value types for annotations or tagsets, etc. Since this can be quite cumbersome via code, and since corpus resources typically change much wrt their structure, the preferred way of obtaining manifests for a resource is to read its accompanying manifest file as explained in the next section. [example source code](../icarus2-examples/src/main/java/de/ims/icarus2/examples/ICARUS2Sample_01_CreateManifestsWithFactory.java)

The same result can be achieved by using the `de.ims.icarus2.model.manifest.util.ManifestBuilder` and chaining all the construction calls to generate a more compact code block:

```java
// Set up the factory
ManifestRegistry registry = new DefaultManifestRegistry();
ManifestLocation location = ManifestLocation.newBuilder().virtual().build();
ManifestFactory factory = new DefaultManifestFactory(location, registry);

ContextManifest contextManifest;

try(ManifestBuilder builder = new ManifestBuilder(factory)) {
	// Start creating and assembling manifests
	contextManifest = builder.create(ContextManifest.class, "myContext")
			.addLayerGroup(builder.create(LayerGroupManifest.class, "myGroup", "myContext")
					.addLayerManifest(builder.create(ItemLayerManifest.class, "tokens", "myGroup"))
					.addLayerManifest(builder.create(ItemLayerManifest.class, "sentences", "myGroup")
							.setFoundationLayerId("tokens", DO_NOTHING)
							.addBaseLayerId("tokens", DO_NOTHING))
					.addLayerManifest(builder.create(AnnotationLayerManifest.class, "surface", "myGroup")
							.addBaseLayerId("tokens", DO_NOTHING)
							.addAnnotationManifest(builder.create(AnnotationManifest.class, "forms", "surface")
									.setKey("forms")
									.setValueType(ValueType.STRING)
									.setAllowUnknownValues(true))
							.setDefaultKey("forms")));
}
```

The `ManifestBuilder` utility makes it easier to directly link manifest at their time of creation. It also allows for easy lookups of any manifest that has been created by it with an id. [example source code](../icarus2-examples/src/main/java/de/ims/icarus2/examples/ICARUS2Sample_02_CreateManifestsWithBuilder.java)

## Reading Manifests

The framework ships with a default (de)serialization facility for XML. The XML representation follows [this](src/main/resources/de/ims/icarus2/model/manifest/xml/corpus.xsd) schema.

Reading in manifests starts again with a registry where the final manifest instances are to be stored. An instance of `ManifestXmlReader` is then used together with an arbitrary number of `ManifestLocations` definitions to do the actual parsing and registration:

```java
ManifestRegistry registry = new DefaultManifestRegistry();

// Configure the reader (here with direct location definition via the builder)
ManifestXmlReader manifestXmlReader = ManifestXmlReader.newBuilder()
		.registry(registry)
		.useImplementationDefaults()
		.source(ManifestLocation.newBuilder()
				.file(Paths.get("myCorpus.imf.xml"))
				.template()
				.build())
		.build();

// Read manifests and automatically register them
manifestXmlReader.readAndRegisterAll();

// Process manifest(s)
System.out.println(registry.getTemplates());
```

Example source code is also available in the examples project [here](../icarus2-examples/src/main/java/de/ims/icarus2/examples/ICARUS2Sample_03_ReadManifests.java).

Assuming the file at path `myCorpus.imf.xml` contains the following XML data, we will get a `ContextManifest` very similar to the one created manually above (the XML data contains some additional information which was omitted previously to shorten the code snippet).

```xml
<?xml version="1.0" encoding="UTF-8"?>
<imf:manifest xmlns:imf="http://www.ims.uni-stuttgart.de/icarus/xml/manifest"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.ims.uni-stuttgart.de/icarus/xml/manifest http://www.ims.uni-stuttgart.de/icarus/xml/manifest/corpus.xsd">
	<imf:templates>
		<imf:context id="exampleContext">
			<imf:layerGroup primaryLayer="sentences" id="surface">
				<imf:itemLayer id="tokens" />
				<imf:itemLayer id="sentences">
					<imf:hierarchy>
						<imf:container containerType="list" />
						<imf:container containerType="span" />
					</imf:hierarchy>
				</imf:itemLayer>
				<imf:annotationLayer id="anno" defaultKey="form">
					<imf:annotation id="form" valueType="string" key="form" />
				</imf:annotationLayer>
			</imf:layerGroup>
		</imf:context>
	</imf:templates>
</imf:manifest>
```

A very important aspect of manifests is the ability to inherit fields/settings from templates. Assuming above `ContextManifest` is used frequently in the description of different corpora, then it can be used as a template, thereby greatly reducing redundancy and at the same time improving consistency:

```xml
<imf:context id="derivedContext" templateId="exampleContext">
	<!-- define additional local fields/settings -->
</imf:context>
```

Inheritance is supported on a vast number of attributes in the manifest framework. Typically a manifest class defines a utility method for every inheritable attribute to check whether or not an available value is set locally or inherited (e.g. `isLocalPrimaryLayerManifest()` in the `ContextManifest` class).

## Processing Manifests

Once a manifest is constructed either programmatically or by parsing a manifest XML file, it can be processed further. Imagine for example an annotation tool that wishes to improve its usability by providing specialized UI components to the user depending on the annotation constraints of the current resource. Examining an `AnnotationLayerManifest` allows client code to decide on required UI functionality without having to consult additional information or hard-code the settings into the application. Assuming a given `ManifestBuilder` and a list to store finished annotation manifests in, the following code generates common types of annotation definitions (categorial, bounded numerical, free text):

```java
ManifestBuilder builder = ...

annotationManifests.add(builder.create(AnnotationManifest.class, "anno1",
			Options.of(ManifestFactory.OPTION_VALUE_TYPE, ValueType.STRING))
		.setKey("stringValues")
		.setName("Fixed Annotation")
		.setValueType(ValueType.STRING)
		.setValueSet(builder.create(ValueSet.class)
				.addValue(builder.create(ValueManifest.class)
						.setValue("xyz")
						.setName("value1")
						.setDescription("a specific value"))
				.addValue(builder.create(ValueManifest.class)
						.setValue("foo")
						.setName("value2")
						.setDescription("another cool value"))
				.addValue(builder.create(ValueManifest.class)
						.setValue("bar")
						.setName("value3")
						.setDescription("the most awesome value of them all ^^")))
		.setAllowUnknownValues(true));

annotationManifests.add(builder.create(AnnotationManifest.class, "anno2",
			Options.of(ManifestFactory.OPTION_VALUE_TYPE, ValueType.INTEGER))
		.setKey("intValues")
		.setName("Range Annotation")
		.setValueRange(builder.create(ValueRange.class,
					Options.of(ManifestFactory.OPTION_VALUE_TYPE, ValueType.INTEGER))
				.setLowerBound(Integer.valueOf(10))
				.setUpperBound(Integer.valueOf(100))
				.setStepSize(Integer.valueOf(5)))
		.setNoEntryValue(Integer.valueOf(45))
		.setAllowUnknownValues(true));

annotationManifests.add(builder.create(AnnotationManifest.class, "anno3",
			Options.of(ManifestFactory.OPTION_VALUE_TYPE, ValueType.INTEGER))
		.setKey("freeValues")
		.setName("Free Annotation")
		.setNoEntryValue("Nothing set yet...")
		.setAllowUnknownValues(true));
```

We now can define GUI-related methods to produce specialized widgets...

```java
private void addChoice(AnnotationManifest manifest, ValueSet valueSet, JComponent container) {
	JComboBox<Object> comboBox = new JComboBox<>(valueSet.getValues());
	comboBox.setEditable(manifest.isAllowUnknownValues());
	manifest.getNoEntryValue().ifPresent(comboBox::setSelectedItem);
	comboBox.setRenderer(new ValueRenderer());
	container.add(comboBox);
}

private void addRange(AnnotationManifest manifest, ValueRange valueRange, JComponent container) {
	SpinnerNumberModel model = new SpinnerNumberModel();
	valueRange.<Comparable<?>>getLowerBound().ifPresent(model::setMinimum);
	valueRange.<Comparable<?>>getUpperBound().ifPresent(model::setMaximum);
	valueRange.<Number>getStepSize().ifPresent(model::setStepSize);
	manifest.getNoEntryValue().ifPresent(model::setValue);
	JSpinner spinner = new JSpinner(model);
	container.add(spinner);
}

private void addFreeText(AnnotationManifest manifest, JComponent container) {
	JTextField textField = new JTextField(20);
	manifest.getNoEntryValue().ifPresent(noEntryValue -> textField.setText(noEntryValue.toString()));
	container.add(new JScrollPane(textField));
}
```

... and finally use those specialized methods to dynamically generate GUI elements from our list of annotation manifests:

```java
for(AnnotationManifest annotationManifest : annotationManifests) {
	JPanel panel = new JPanel();
	annotationManifest.getValueSet().ifPresent(
			set -> addChoice(annotationManifest, set, panel));
	annotationManifest.getValueRange().ifPresent(
			range -> addRange(annotationManifest, range, panel));

	if(panel.getComponentCount()==0 && annotationManifest.isAllowUnknownValues()) {
		addFreeText(annotationManifest, panel);
	}

	contentPanel.add(panel);
}
```

For a more complete code example (GUI code tends to be quite verbose) look [here](../icarus2-examples/src/main/java/de/ims/icarus2/examples/ICARUS2Sample_04_ProcessManifestsForGui.java)
