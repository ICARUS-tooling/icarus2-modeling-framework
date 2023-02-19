/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.cli;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;

import com.google.common.io.Files;

import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.standard.registry.metadata.Format;
import de.ims.icarus2.model.standard.registry.metadata.JAXBMetadataRegistry;
import de.ims.icarus2.model.standard.registry.metadata.PlainMetadataRegistry;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.cli.CliCommand;
import de.ims.icarus2.util.io.resource.FileResource;
import de.ims.icarus2.util.io.resource.IOResource;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * Allows conversion of a single metadata file into another format.
 *
 * @author Markus Gärtner
 *
 */
@Command(
		name = "mdconv",
		description = {"Convert metadata files for the ICARUS2 framework.","Note that missing format options lead to attempts of automatic format detection based on file name endings."},
		mixinStandardHelpOptions = true,
		version = "1.0",
		showDefaultValues = true
)
public class MetadataConverterCommand extends CliCommand {

	private static final String FORMATS = "XML or PLAIN";

	@Spec
	CommandSpec spec;

	@Parameters(index = "0", description = "Input file holding the metadata to be converted")
	private Path source;

	@Parameters(index = "1", description = "Output file to write converted metadata to")
	private Path target;

	@Option(names = {"-i", "--inputEncoding"}, description = "Character encoding of the input file", defaultValue = "UTF-8")
	private Charset sourceEncoding;

	@Option(names = {"-s", "--sourceFormat"}, description = "Format the input file is expected to be written in: one of "+FORMATS)
	private Format sourceFormat;

	@Option(names = {"-o", "--outputEncoding"}, description = "Character encoding of the output file", defaultValue = "UTF-8")
	private Charset targetEncoding;

	@Option(names = {"-t", "--targetFormat"}, description = "Format the output file should be written in: one of "+FORMATS)
	private Format targetFormat;

	@Option(names = {"-v", "--verbose"}, description = "Print human readable info during process")
	private boolean verbose;

	private boolean tryDeriveFormat(Path file, boolean isSource) {
		String suffix = Files.getFileExtension(file.getFileName().toString());
		Optional<Format> format = Format.forFileSuffix(suffix);
		if(format.isPresent()) {
			if(isSource) {
				sourceFormat = format.get();
				if(verbose) spec.commandLine().getOut().printf("Resolved format '%s' from input file.%n", sourceFormat);
			} else {
				targetFormat = format.get();
				if(verbose) spec.commandLine().getOut().printf("Resolved format '%s' from output file.%n", targetFormat);
			}
			return true;
		}
		return false;
	}

	private IOResource asResource(Path file, AccessMode mode) {
		return new FileResource(file, mode);
	}

	private MetadataRegistry createRegistry(IOResource resource, Format format, Charset encoding) {
		switch (format) {
		case PLAIN:
			return new PlainMetadataRegistry(resource, encoding);

		case XML:
			return new JAXBMetadataRegistry(resource, encoding);

		default:
			throw new InternalError("Unknown format: "+format);
		}
	}

	@Override
	public Integer call() {

		PrintWriter out = spec.commandLine().getOut();

		if(verbose) {
			out.printf("CONVERTING: sourceFormat=%s targetFormat=%s%n", sourceFormat, targetFormat);
			out.printf("  input:  %s%n", source);
			out.printf("  output: %s%n", target);
		}

		// Resolve missing format information

		if(sourceFormat==null && !tryDeriveFormat(source, true)) {
			if(verbose) out.println("Unable to resolve format from input file.");
			return FAILED;
		}
		if(targetFormat==null && !tryDeriveFormat(target, false)) {
			if(verbose) out.println("Unable to resolve format from output file.");
			return FAILED;
		}

		// Copy all entries from input registry over to output
		try(MetadataRegistry input = createRegistry(asResource(source, AccessMode.READ), sourceFormat, sourceEncoding);
				MetadataRegistry output = createRegistry(asResource(target, AccessMode.WRITE), targetFormat, targetEncoding)) {

			input.open();
			output.open();

			output.beginUpdate();
			try {
				input.forEachEntry(output::setValue);
			} finally {
				output.endUpdate();
			}
		}

		return SUCCESS;
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new MetadataConverterCommand()).execute(args);
		System.exit(exitCode);
	}


}
