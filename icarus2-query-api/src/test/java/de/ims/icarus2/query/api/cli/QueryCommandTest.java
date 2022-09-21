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

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.common.formats.Template;
import de.ims.icarus2.common.formats.conll.CoNLLUtils;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.standard.DefaultManifestFactory;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.manifest.util.ManifestBuilder;
import de.ims.icarus2.model.manifest.xml.ManifestXmlWriter;
import de.ims.icarus2.query.api.QueryAssertions;
import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.query.api.engine.result.io.MatchCodec.MatchReader;
import de.ims.icarus2.query.api.engine.result.io.TabularMatchCodec;
import de.ims.icarus2.util.cli.CliCommand;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.io.resource.FileResource;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import picocli.CommandLine;

/**
 * @author Markus Gärtner
 *
 */
class QueryCommandTest {

	static class Config {

		Path manifestFile, logFile, outputFile, metadataFolder, queryFile, payloadFile;
		boolean verbose = false;
		String payload;
		int threadCount = UNSET_INT, timeout = UNSET_INT;
		final List<String> templates = new ObjectArrayList<>();

		void applyTo(Consumer<? super String> append) {
			if(verbose) {
				append.accept("-v");
			}

			if(payload!=null) {
				append.accept("-q");
				append.accept(payload);
			}

			if(manifestFile!=null) {
				append.accept("-m");
				append.accept(manifestFile.toString());
			}

			if(logFile!=null) {
				append.accept("-l");
				append.accept(logFile.toString());
			}

			if(outputFile!=null) {
				append.accept("-o");
				append.accept(outputFile.toString());
			}

			if(metadataFolder!=null) {
				append.accept("-d");
				append.accept(metadataFolder.toString());
			}

			if(queryFile!=null) {
				append.accept("-f");
				append.accept(queryFile.toString());
			}

			if(payloadFile!=null) {
				append.accept("-p");
				append.accept(payloadFile.toString());
			}

			if(threadCount!=UNSET_INT) {
				append.accept("-n");
				append.accept(String.valueOf(threadCount));
			}

			if(timeout!=UNSET_INT) {
				append.accept("-t");
				append.accept(String.valueOf(timeout));
			}

			if(!templates.isEmpty()) {
				append.accept("-e");
				templates.forEach(append);
			}
		}

		String[] toArgs() {
			List<String> args = new ObjectArrayList<>();
			applyTo(args::add);
			return CollectionUtils.toArray(args, String[]::new);
		}
	}

	@TempDir
	private static Path folder;

	private static Path TEST_CORPUS;
	private static Path TEST_MANIFEST;

	@BeforeAll
	static void setUpCorpus() throws IOException, XMLStreamException, IcarusApiException {
//		<imf:manifest xmlns:imf="http://www.ims.uni-stuttgart.de/icarus/xml/manifest"
//				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
//				<imf:corpora>
//					<imf:corpus editable="false" id="exampleCorpus" name="Example Corpus - Icarus Wiki Entry">
//						<imf:rootContext id="context0" independent="true" name="Main Context" templateId="common.format.conll09">
//							<imf:location>
//								<imf:path type="resource">/de/ims/icarus2/common/formats/conll/icarus.conll09</imf:path>
//							</imf:location>
//						</imf:rootContext>
//					</imf:corpus>
//				</imf:corpora>
//			</imf:manifest>

		TEST_CORPUS = folder.resolve("corpus.conll").toAbsolutePath();
		TEST_MANIFEST = folder.resolve("manifest.imf.xml").toAbsolutePath();

		ManifestLocation location = ManifestLocation.builder()
				.charset(StandardCharsets.UTF_8)
				.file(TEST_MANIFEST)
				.input()
				.build();

		ManifestRegistry registry = new DefaultManifestRegistry();
		Template.applyTemplates(registry, Template.CONLL);

		try(ManifestBuilder builder = new ManifestBuilder(new DefaultManifestFactory(location, registry))) {
			CorpusManifest corpusManifest = builder.create(CorpusManifest.class, "exampleCorpus")
					.setEditable(false)
					.setName("Example Corpus - Icarus Wiki Entry")
					.addRootContextManifest((ContextManifest) builder.create(ContextManifest.class, "context0", "exampleCorpus")
							.setIndependentContext(true)
							.setName("Main Context")
							.addLocationManifest(builder.create(LocationManifest.class)
									.setRootPathType(PathType.FILE)
									.setRootPath(TEST_CORPUS.toString()))
							.setTemplateId("common.format.conll09"));
			ManifestXmlWriter writer = new ManifestXmlWriter(location);
			writer.addManifest(corpusManifest);
			writer.writeAll();
		}

		try(InputStream in = CoNLLUtils.getCorpusUrl().openStream();
				OutputStream out = Files.newOutputStream(TEST_CORPUS)) {
			IOUtil.copyStream(in, out);
		}
	}

	void execute(Config config, int expectedStatus) {
		int status = new CommandLine(new QueryCommand()).execute(config.toArgs());
		assertThat(status).as("Execution status").isEqualTo(expectedStatus);
	}

	List<Match> readMatches(Path file) throws IOException {
		List<Match> buffer = new ObjectArrayList<>();
		try(MatchReader reader = new TabularMatchCodec().newReader(new FileResource(file))) {
			reader.readAll(buffer::add);
		}
		return buffer;
	}

	@Test
	void test() throws IOException {
		Config config = new Config();
		config.manifestFile = TEST_MANIFEST;
		config.templates.add("de.ims.icarus2.common.formats.Template$CONLL");
		config.outputFile = Files.createTempFile(folder, "test_", ".out.txt");
		config.payload = "FIND [form==\"He\"]";

		execute(config, CliCommand.SUCCESS.intValue());

		List<Match> matches = readMatches(config.outputFile);
		assertThat(matches).isNotEmpty();
		assertThat(matches).element(0, as(QueryAssertions.MATCH))
			.hasIndex(3)
			.hasOnlyMapping(0, 0);
		assertThat(matches).element(1, as(QueryAssertions.MATCH))
			.hasIndex(4)
			.hasOnlyMapping(0, 0);
	}

}
