# ICARUS2 Corpus Modeling Framework

[![Build Status](https://github.com/icarus-tooling/icarus2-modeling-framework/workflows/build/badge.svg)](https://github.com/ICARUS-tooling/icarus2-modeling-framework/actions?workflow=build)
[![codecov](https://codecov.io/gh/icarus-tooling/icarus2-modeling-framework/branch/dev/graph/badge.svg)](https://codecov.io/gh/icarus-tooling/icarus2-modeling-framework)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellowgreen.svg)](https://github.com/icarus-tooling/icarus2-modeling-framework/blob/master/LICENSE)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/ICARUS-tooling/icarus2-modeling-framework/issues)
[![HitCount](http://hits.dwyl.com/icarus-tooling/icarus2-modeling-framework.svg)](http://hits.dwyl.com/icarus-tooling/icarus2-modeling-framework)
![GitHub language count](https://img.shields.io/github/languages/count/icarus-tooling/icarus2-modeling-framework.svg)


ICARUS2 is a collection of Java8 libraries and associated specifications for systematically describing, accessing, modifying and querying linguistic corpora of arbitrary complexity and/or modality. Note that the majority of ICARUS2 components are geared towards use by developers to create rich corpus applications (such as NLP pipelines, query interfaces or visualization tools). 

The entire framework is also separated into several sub-projects that cover manifest management, corpus modeling and querying, respectively. Below the main components of the framework and the associated projects are described.

## Manifest API

At its very core is a framework for describing corpus composition and content via so called manifests that are used by all other ICARUS2 components to make them universally applicable to arbitrary corpus resources. Manifests can be programmatically created or provided by as XML files (the preferred way for sustainable corpus data), similar to the following example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<imf:manifest xmlns:imf="http://www.ims.uni-stuttgart.de/icarus/xml/manifest"
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

The `icarus2-manifest-api` project contains the entire code base related to the creating, handling and (de-)serialization of manifests in ICARUS2. For a detailed introduction to manifests consult the respective [README](icarus2-manifest-api/README.md).


## Model API

On top of the manifest API the modeling framework project defines the basic model and data structures used for in-memory representation, (de-)serialization or storage of corpus resources in ICARUS2. This part of the framework is actually split into two projects; one for defining the basic modeling contracts (interfaces) in `icarus2-model-api` and a second one that provides default implementations for all the core interfaces in `icarus2-model-defaults` that already cover a wide variety of use cases and allow a quick start with ICARUS2. Their respective introductions can be found [here](icarus2-model-api/README.md) for the API and [here](icarus2-model-defaults/README.md) for the default implementations.

**Utility Projects**

ICARUS2 completely abstracts away from any physical aspects for the consuming party related to corpus storage, format or other technical properties. This way it is completely irrelevant if the corpus data is being read from a file, some distributed web-locations, a database or created in real-time by some integrated text generation pipeline. Several utility projects exist within the ICARUS2 ecosystem to make plugging-in certain corpus resources easier. Currently the `icarus2-file-driver-utils` project is the most mature one of those (README found [here](icarus2-file-driver-utils/README.md)) and it is (currently only) dedicated to reading arbitrarily complex tabular corpus data and transform it into ready to use in-memory representations.  

## Query API

ICARUS2 applies a novel approach to corpus querying that combines the performance benefits from dedicated (database) storage and retrieval solutions with a maximum of query expressiveness in a hybrid architecture. All components related to the query workflow are bundled in the `icarus2-query-api` project, including the default implementation of the query processor. The README is located [here](icarus2-query-api/README.md) and a very detailed specification and introduction for the ICARUS2 query language used by this framework is available as [PDF](icarus2-query-api/doc/iql_specification.pdf). Note that the query API and associated components are all work in progress and as such still subject to frequent changes in design. 

## Examples

Various parts of the ICARUS2 documentation use or reference ready to use code examples. All of those examples are collected in the `icarus2-examples` project. The associated [README](icarus2-examples/README.md) lists the available code examples and serves as a good entry point when exploring the code base.

## License

ICARUS2 is licensed under an [Apache 2.0](LICENSE) open-source license.

## References

TODO