# ICARUS2 Model API

## Overview

TODO  

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
