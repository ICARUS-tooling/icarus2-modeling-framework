# ICARUS2 Manifest Framework

## Overview

The manifest framework provides the foundation for all other members of the ICARUS2 project. Manifests are formal descriptions of the structure, inner composition and outer dependencies of (parts) of a corpus resource. Corpora are consist of a hierarchy of different components which all describe different aspects of a corpus.
TODO  

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

Above code will create 3 layers representing tokens, sentences and surface form annotations and group them into a context. For a proper specification a lot of additional information is needed, such as value types for annotations or tagsets, etc. Since this can be quite cumbersome via code, and since corpus resources typically change much wrt their structure, the preferred way of obtaining manifests for a resource is to read its accompanying manifest file as explained in the next section.

## Reading Manifests

The framework ships with a default (de)serialization facility for XML. The XML representation follows [this](https://github.com/ICARUS-tooling/icarus2-modeling-framework/blob/dev/icarus2-manifest-api/src/main/resources/de/ims/icarus2/model/manifest/xml/corpus.xsd) schema.

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

Assuming the file at path `myCorpus.imf.xml` contains the following XML data, we will get a `ContextManifest` very similar to the one created manually above (the XML data contains some additional information which was omitted previously to shorten the code snippet).

```xml
<?xml version="1.0" encoding="UTF-8"?>
<imf:manifest  xmlns:imf="http://www.ims.uni-stuttgart.de/icarus/xml/manifest"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.ims.uni-stuttgart.de/icarus/xml/manifest http://www.ims.uni-stuttgart.de/icarus/xml/manifest/corpus.xsd">
	<imf:templates>
		<imf:context id="exampleContext">
			<imf:layerGroup primaryLayer="sentences" id="surface">
				<imf:itemLayer id="tokens">
					<imf:hierarchy>
						<imf:container containerType="list" />
					</imf:hierarchy>
				</imf:itemLayer>
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

Once a manifest is constructed either programmatically or by parsing a manifest XML file, it can be processed further. Imagine for example an annotation tool that wishes to improve its usability by providing specialized UI components to the user depending on the annotation constraints of the current resource. Examining an `AnnotationLayerManifest` allows client code to decide on required UI functionality without having to consult additional information or hard-code the settings into the application.

```java

```

```java
default boolean isProxy() {
	return false;
}
```
