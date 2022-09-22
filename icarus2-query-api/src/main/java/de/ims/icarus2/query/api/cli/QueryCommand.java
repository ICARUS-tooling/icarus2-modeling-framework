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
package de.ims.icarus2.query.api.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader;
import de.ims.icarus2.model.standard.io.DefaultFileManager;
import de.ims.icarus2.model.standard.registry.DefaultCorpusManager;
import de.ims.icarus2.model.standard.registry.metadata.PlainMetadataRegistry;
import de.ims.icarus2.model.standard.registry.metadata.VirtualMetadataRegistry;
import de.ims.icarus2.query.api.engine.EngineSettings;
import de.ims.icarus2.query.api.engine.EngineSettings.IntField;
import de.ims.icarus2.query.api.engine.QueryEngine;
import de.ims.icarus2.query.api.engine.QueryJob;
import de.ims.icarus2.query.api.engine.QueryJob.JobController;
import de.ims.icarus2.query.api.engine.result.ResultSink;
import de.ims.icarus2.query.api.engine.result.io.IdPrintingResultSink;
import de.ims.icarus2.query.api.iql.IqlCorpus;
import de.ims.icarus2.query.api.iql.IqlObjectIdGenerator;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlResult;
import de.ims.icarus2.query.api.iql.IqlResult.ResultType;
import de.ims.icarus2.query.api.iql.IqlScope;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.query.api.iql.IqlUtils;
import de.ims.icarus2.util.cli.CliCommand;
import de.ims.icarus2.util.cli.DurationConverter;
import de.ims.icarus2.util.function.ThrowingConsumer;
import de.ims.icarus2.util.io.resource.FileResource;
import de.ims.icarus2.util.io.resource.FileResourceProvider;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/**
 * @author Markus Gärtner
 *
 */
@Command(
		name = "query",
		description = "Query a corpus resource using the IQL syntax",
		version = "1.0",
		mixinStandardHelpOptions = true,
		showDefaultValues = true
)
public class QueryCommand extends CliCommand {

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	@Spec
	private CommandSpec spec;

	@Option(names = {"-v", "--verbose"}, description = "Print human readable information during processing")
	private boolean verbose;

	@Option(names = {"-l", "--log-file"}, description = {"File to stream log output to.", "If not defined, log output will be streamed to console."})
	private Path logFile;
	@Option(names = {"-o", "--output-file"}, description = {"File to stream final search results to.", "If not defined, search results will be streamed to console."})
	private Path outputFile;

	@ArgGroup(exclusive = false, multiplicity = "1", heading = "Corpus data:%n")
	private CorpusData corpusData;

	@ArgGroup(exclusive = true, multiplicity = "1", heading = "Query source:%n")
	private QueryData queryData;

	@ArgGroup(validate = false, heading = "Engine configuration:%n")
	private EngineConfig engineConfig = new EngineConfig();

	//TODO add

	static class CorpusData {

		@Option(names = {"-c", "--corpus-id"}, description = "Id of the corpus to use in case the manifest contains multiple corpora.")
		private String corpusId;

		@Option(names = {"-m", "--manifest-file"}, required = true, description = "Path to the manifest file describing the target corpus")
		private Path manifestFile;

		@Option(names = {"-d", "--metadata-file"}, description = {"Folder to read or store metadata from.", "If not defined, the corpus manager will use a virtual storage for metadata."})
		private Path metadataFolder;

		@Option(names = {"-e", "--template"}, description = "Template class to load for the manifest registry.")
		private List<String> templates;
	}

	static class QueryData {

		@Option(names = {"-q", "--query-payload"}, description = "The query payload following IQL format")
		private String payload;

		@Option(names = {"-f", "--query-file"}, description = "File containing a complete IQL query in JSON-LD")
		private Path queryFile;

		@Option(names = {"-p", "--payload-file"}, description = "File containing the IQL payload of a query")
		private Path payloadFile;
	}

	static class EngineConfig {

		@Option(names = {"-n", "--thread-count"}, description = {"Number of threads to use for parallelization.",
				"A value smaller than 1 will use all but one logical core available on the machine."}, defaultValue = "-1")
		private int threadCount = -1;

		@Option(names = {"-t", "--timeout"}, description = {"Time after which the search will be forcibly canceled."
				+ " Time unit to be used can be selected via suffixes S,M,H for seconds, minutes and hours.",
				"A value smaller than 1 deactivates any timeout checks."}, defaultValue = "0", converter = DurationConverter.class)
		private int timeout = 0;

		//TODO add key-value pair syntax for additional direct configuration properties
	}

	private PrintWriter out() {
		return spec.commandLine().getOut();
	}

	private PrintWriter err() {
		return spec.commandLine().getErr();
	}

	@Override
	public Integer call() throws Exception {

		if(logFile!=null) {
			Writer writer = Files.newBufferedWriter(logFile, UTF_8);
			PrintWriter printWriter = new PrintWriter(writer);
			spec.commandLine().setOut(printWriter);
			spec.commandLine().setErr(printWriter);
		}

		SearchState state = new SearchState();

		if(!state.initCorpus()) {
			out().println("Failed to prepare corpus");
			return FAILED;
		}

		if(!state.initQuery()) {
			out().println("Failed to prepare query");
			return FAILED;
		}

		if(!state.initResult()) {
			out().println("Failed to prepare result processor");
			return FAILED;
		}

		if(!state.initEngine()) {
			out().println("Failed to prepare query engine");
			return FAILED;
		}

		if(!state.executeSearch()) {
			out().println("Failed to exeute search");
			return FAILED;
		}

		return SUCCESS;
	}

	class SearchState {
		private CorpusManager corpusManager;
		private CorpusManifest corpusManifest;
		private QueryEngine queryEngine;
		private IqlQuery query;
		private ResultSink resultSink;
		private int workerLimit;

		private void addTemplate(ManifestRegistry registry, Object template) throws IcarusApiException {
			if(template instanceof ThrowingConsumer) {
				@SuppressWarnings("unchecked")
				ThrowingConsumer<ManifestRegistry, IcarusApiException> tpl =
						(ThrowingConsumer<ManifestRegistry, IcarusApiException>) template;
				tpl.accept(registry);
			}
			//TODO add other modes for handling/converting templates
			else
				throw new IllegalArgumentException("Not a usable template: "+template.getClass());
		}

		private void loadTemplates(ManifestRegistry registry) throws ClassNotFoundException,
				InstantiationException, IllegalAccessException, IcarusApiException {
			for(String templateName : corpusData.templates) {
				String value = null;
				if(templateName.contains("$")) {
					int sep = templateName.lastIndexOf('$');
					value = templateName.substring(sep+1);
					templateName = templateName.substring(0, sep);
				}
				Class<?> clazz = Class.forName(templateName);
				if(clazz.isEnum()) {
					for(Object template : clazz.getEnumConstants()) {
						if(value==null || value.equalsIgnoreCase(((Enum<?>)template).name())) {
							addTemplate(registry, template);
						}
					}
				} else {
					addTemplate(registry, clazz.newInstance());
				}
			}
		}

		/** Prepare the corpus and corpus manager */
		boolean initCorpus() {
			DefaultCorpusManager.Builder builder = DefaultCorpusManager.builder();
			Path root = corpusData.metadataFolder==null ? Paths.get(System.getProperty("user.dir")) : corpusData.metadataFolder;
			builder.fileManager(new DefaultFileManager(root));
			builder.resourceProvider(new FileResourceProvider());
			builder.manifestRegistry(new DefaultManifestRegistry());

			if(corpusData.metadataFolder!=null) {
				Path file = corpusData.metadataFolder.resolve("metadata"+PlainMetadataRegistry.DEFAULT_FILE_ENDING);
				builder.metadataRegistry(new PlainMetadataRegistry(new FileResource(file), UTF_8));
			} else {
				builder.metadataRegistry(new VirtualMetadataRegistry());
			}

			corpusManager = builder.build();

			ManifestRegistry manifestRegistry = corpusManager.getManifestRegistry();

			if(corpusData.templates!=null && !corpusData.templates.isEmpty()) {
				try {
					loadTemplates(manifestRegistry);
				} catch(Exception e) {
					err().println("Failed to load templates");
					e.printStackTrace(err());
					return false;
				}
			}

			ManifestXmlReader reader = ManifestXmlReader.builder()
					.registry(manifestRegistry)
					.useImplementationDefaults()
					.build();

			reader.addSource(ManifestLocation.builder()
					.file(corpusData.manifestFile)
					.input()
					.build());

			List<CorpusManifest> corpora;

			try {
				corpora = reader.parseCorpora();
			} catch (IOException e) {
				err().println("Failed to read manifest file");
				e.printStackTrace(err());
				return false;
			} catch (SAXException e) {
				err().println("Parsing error while reading manifest file");
				e.printStackTrace(err());
				return false;
			}

			if(corpora.isEmpty()) {
				err().println("No corpora in provided manifest");
				return false;
			}
			if(corpora.size()>1) {
				if(corpusData.corpusId==null) {
					err().println("Multiple corpora found, but corpus id is missing");
					return false;
				}
				for(CorpusManifest candidate : corpora) {
					String id = candidate.getId().orElse(null);
					if(id!=null && id.equals(corpusData.corpusId)) {
						corpusManifest = candidate;
					}
				}
				if(corpusManifest==null) {
					err().println("No corpus found for id "+corpusData.corpusId);
					return false;
				}
			} else {
				corpusManifest = corpora.get(0);
			}

			manifestRegistry.addCorpusManifest(corpusManifest);
			corpusManager.enableCorpus(corpusManifest);

			return true;
		}

		private IqlQuery wrapPayload(String rawPayload) {
			IqlObjectIdGenerator gen = new IqlObjectIdGenerator();

			IqlCorpus corpus = new IqlCorpus();
			gen.assignId(corpus);
			corpus.setName(ManifestUtils.requireId(corpusManifest));

			IqlResult result = new IqlResult();
			result.addResultType(ResultType.ID);

			IqlScope scope = new IqlScope();
			gen.assignId(scope);
			scope.setFull(true);

			IqlStream stream = new IqlStream();
			gen.assignId(stream);
			stream.setCorpus(corpus);
			stream.setRawPayload(rawPayload);
			stream.setResult(result);
			stream.setScope(scope);

			IqlQuery query = new IqlQuery();
			gen.assignId(query);
			query.addStream(stream);

			return query;
		}

		/** Prepare the actual query and payload*/
		boolean initQuery() {
			// Ready to use query, just parse and verify it
			if(queryData.queryFile!=null) {
				try {
					query = IqlUtils.createMapper().readValue(Files.newBufferedReader(queryData.queryFile, UTF_8), IqlQuery.class);
				} catch (JsonParseException e) {
					err().println("JSON syntax error in query file");
					e.printStackTrace(err());
					return false;
				} catch (JsonMappingException e) {
					err().println("Unable to parse query content");
					e.printStackTrace(err());
					return false;
				} catch (IOException e) {
					err().println("Failed to read query file");
					e.printStackTrace(err());
					return false;
				}
			} else {
				// Obtain payload from file or argument
				String rawPayload = queryData.payload;
				if(queryData.payloadFile!=null) {
					try {
						rawPayload = new String(Files.readAllBytes(queryData.payloadFile), UTF_8);
					} catch (IOException e) {
						err().println("Failed to read payload file");
						e.printStackTrace(err());
						return false;
					}
				}

				// Wrap raw payload into query object
				query = wrapPayload(rawPayload);
			}

			return true;
		}

		/** Prepare the result output/sink */
		boolean initResult() {
			PrintWriter writer = out();
			if(outputFile!=null) {
				try {
					writer = new PrintWriter(Files.newBufferedWriter(outputFile, UTF_8));
				} catch (IOException e) {
					err().println("Failed to prepare output file");
					e.printStackTrace(err());
					return false;
				}
			}

			resultSink = new IdPrintingResultSink(writer);

			return true;
		}

		/** Prepare the query engine */
		boolean initEngine() {

			workerLimit = engineConfig.threadCount;
			if(workerLimit<1) {
				// Fallback: use N-1 cores, but never less than 1
				workerLimit = Runtime.getRuntime().availableProcessors();
				if(workerLimit>1) {
					workerLimit--;
				}
			}

			EngineSettings settings = new EngineSettings();
			settings.setInt(IntField.WORKER_LIMIT, workerLimit);
			settings.setInt(IntField.TIMEOUT, engineConfig.timeout);

			queryEngine = QueryEngine.builder()
					.corpusManager(corpusManager)
					.useDefaultMapper()
					.settings(settings)
					.build();

			return true;
		}

		/** Execute the (parallel) search. This might block the current thread until the search is done. */
		boolean executeSearch() {
			JobController jobController;
			try {
				QueryJob job = queryEngine.evaluateQuery(query, resultSink);

				jobController = job.execute(workerLimit);
			} catch (InterruptedException e) {
				err().println("Query evaluation canceled before actual search started");
				return false;
			}

			jobController.start();

			if(engineConfig.timeout>0) {
				try {
					jobController.awaitFinish(engineConfig.timeout, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					err().println("Query evaluation timed out or was canceled");
					return false;
				}
			} else {
				try {
					jobController.awaitFinish();
				} catch (InterruptedException e) {
					err().println("Query evaluation canceled");
					return false;
				}
			}

			return true;
		}
	}

	public static void main(String[] args) {
		new CommandLine(new QueryCommand()).execute(args);
	}
}
