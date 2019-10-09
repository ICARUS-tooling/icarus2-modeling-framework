# ICARUS2 File Driver Utils

## Overview

This part of the ICARUS2 framework provides driver implementations and supporting classes for handling classic file-based corpora. Central aspects of the implemented approaches are the following:

1. Delegation of the low-level physical transformation between a resource's physical representation and the actual in-memory members of the framework to a dedicated `Converter` interface.
1. Multi-pass processing of resources to allow proper scanning and/or indexing of its content to maximize optimization potential.
1. Support for distributed corpus resources, i.e. content divided into multiple physical files.
1. Use of schemata for even greater flexibility. Currently we provide a schema for tabular data and a schema framework for xml-based corpora is in the making.

## Usage

To define and fully enable a file-based corpus driver, the associated manifest definition has to contain three elemental parts (location, converter and schema):

### 1. Location

One or more `<imf:location>...</imf:location>` definitions are required to point to the actual corpus data. The current implementations included in the ICARUS2 framework support the `file`, `folder` or `resource` root-path types, as well as inline data provided by a location within the manifest.

Example of a simple single-file corpus resource where `path/to/my/corpus/MyCorpus.txt` is the path to a resource shipped with the manifest file (usually bundled together within the same Jar archive):

```xml
<imf:location>
	<imf:path type="resource">path/to/my/corpus/MyCorpus.txt</imf:path>
</imf:location>
```

Example of a single-file corpus where `mount/resources/corpora/MyCorpus.txt`is the file path on the physical file system:

```xml
<imf:location>
	<imf:path type="file">mount/resources/corpora/MyCorpus.txt</imf:path>
</imf:location>
```

Example of a multi-file corpus where `mount/resources/corpora/MyCorpus` points to a folder on the physical file system (without any `pathEntry` sub-elements this definition will result in all regular files in the designated folder to be treated as part of the corpus):

```xml
<imf:location>
	<imf:path type="folder">mount/resources/corpora/MyCorpus</imf:path>
</imf:location>
```

Example of a multi-file corpus with explicitly set parts:

```xml
<imf:location>
	<imf:path type="folder">mount/resources/corpora/MyCorpus</imf:path>
	<imf:pathEntry type="file">part1.zip</imf:path>
	<imf:pathEntry type="file">part2.zip</imf:path>
	<imf:pathEntry type="file">part3.zip</imf:path>
	<imf:pathEntry type="file">part4.zip</imf:path>
	<imf:pathEntry type="file">part5.zip</imf:path>
</imf:location>
```

Example of the same multi-file corpus as above with its parts specified via pattern(s):

```xml
<imf:location>
	<imf:path type="folder">mount/resources/corpora/MyCorpus</imf:path>
	<imf:pathEntry type="pattern">part[1-5].zip</imf:path>
</imf:location>
```

Note that the order in which locations are declared in the manifest **does** matter! The framework will analyze and resolve all the locations declared in a manifest and then produce a single sequence of resolved file resources for the corpus or context. It is also possible to declare the exact same collection of files with a variety of different location definitions (e.g. the sequence of the five zip files above can also be achieved with 5 distinct locations each containing exactly one file declaration). As a rule of thumb one should try to use as few location declarations as possible to specify a given set of files.

### 1.1 Path Resolvers

Besides declaring the physical location of corpus files through aforementioned mechanisms, it is also possible to specify the desired `de.ims.icarus2.model.api.io.PathResolver` implementation to be used for actually resolving the `location` element to its corresponding files. Per default implementations are used that follow the above protocols for resolving simple corpus structures. For corpora of a more complex structure, for instance with integrated additional resources or hierarchical file structures, users can implement their own `PathResolver` variations and use them for manifest declarations:

```xml
<imf:location>
	<imf:path type="resource">path/to/my/custom/corpus</imf:path>
	<imf:pathResolver>
		<imf:implementation classname="my.awsome.CustomPathResolver" />
	</imf:pathResolver>
</imf:location>
```

### 2. Converter

Converters shoulder the ultimate responsibility of transforming between the physical representation of a corpus resource and the in-memory framework members used to model it. Currently the framework supports a `Converter` usable for converting from tabular formats, such as the popular CoNLL formats used for various shared tasks over the years. The converter to be used in a given driver context is defined via a normal `module` section with the `de.ims.icarus2.filedriver.converter` id in the manifest:

```xml			
<imf:module id="de.ims.icarus2.filedriver.converter">
	<imf:implementation classname="de.ims.icarus2.filedriver.schema.DefaultSchemaConverterFactory" factory="true">
	...
	</imf:implementation>
</imf:module>
```

Currently the default approach to defining and customizing a converter is to use the `DefaultSchemaConverterFactory` factory to pick and initialize the desired converter based on a series of properties given inside the `module` section. The following properties are supported in order to select the actual converter implementation and schema instance (note that all but the `typeId` property are mutually exclusive):

|          Property                            |               Description                        |
|:---------------------------------------------|:-------------------------------------------------| 
| de.ims.icarus2.filedriver.schema.typeId      | Type definition to indicate how the schema should be interpreted |
| de.ims.icarus2.filedriver.schema.content     | The inline definition of the schema              |
| de.ims.icarus2.filedriver.schema.name        | For shared schema definition (usually established formats) this designates the unique identifier of the desired schema |
| de.ims.icarus2.filedriver.schema.file        | Path to the schema definition within the local file system |
| de.ims.icarus2.filedriver.schema.resource    | Path to the schema definition as a local resource (typically bundled together with the manifest in a jar archive) |
| de.ims.icarus2.filedriver.schema.url         | Remote location of the schema definition |

### 3. Schema

### 3.1 Tabular Schema

```xml
<imf:corpus>
	<imf:rootContext>
		<imf:location>
			<imf:path type="resource">path/to/my/corpus/MyCorpus.txt</imf:path>
		</imf:location>
		
		<imf:driver>
			<imf:implementation classname="de.ims.icarus2.filedriver.DefaultFileDriverFactory" factory="true" />			
			<imf:module id="converter">
			</imf:module>
		</imf:driver>
	</imf:rootContext>
</imf:corpus>
```


A complete example for a single-file corpus with tabular structure and inline schema definition could look like the following:

```xml
<imf:corpus>
	<imf:rootContext>
		<imf:location>
			<imf:path type="resource">path/to/my/corpus/MyCorpus.txt</imf:path>
		</imf:location>
		
		<imf:driver>
			<imf:implementation classname="de.ims.icarus2.filedriver.DefaultFileDriverFactory" factory="true" />			
			<imf:module id="converter">
				<imf:implementation classname="de.ims.icarus2.filedriver.schema.DefaultSchemaConverterFactory" factory="true">
					<imf:properties>
						<imf:property name="schemaTypeId">de.ims.icarus2.filedriver.schema.tabular</imf:property>
						<imf:property name="schema"><![CDATA[
						<table id="test.tier1.tbl" name="Tier-1 Tabular Format"  group-id="main">
							<block layerId="token">
								<separator>WHITESPACES</separator>
								<component memberType="ITEM"/>
								<endDelimiter>
									<pattern>EMPTY_LINE</pattern>
								</endDelimiter>
								<columns>
									<column name="ID" ignore="true"/>
									<column name="FORM" layerId="form"/>
									<column name="LEMMA" layerId="lemma"/>
								</columns>
							</block>
						</table>
						]]>
						</imf:property>
					</imf:properties>
				</imf:implementation>
			</imf:module>
		</imf:driver>
	</imf:rootContext>
</imf:corpus>
```

