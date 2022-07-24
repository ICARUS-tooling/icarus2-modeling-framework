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
package de.ims.icarus2.model.standard.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.standard.registry.metadata.Format;
import de.ims.icarus2.model.standard.registry.metadata.JAXBMetadataRegistry;
import de.ims.icarus2.model.standard.registry.metadata.PlainMetadataRegistry;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.io.resource.FileResource;
import de.ims.icarus2.util.io.resource.IOResource;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import picocli.CommandLine;

/**
 * @author Markus Gärtner
 *
 */
class MetadataConverterCommandTest {

	private static final Format[] FORMATS = Format.values();
	private static final Charset[] ENCODINGS = {
			StandardCharsets.UTF_8,
			StandardCharsets.US_ASCII,
			StandardCharsets.ISO_8859_1,
			StandardCharsets.UTF_16,
	};

	@Nested
	class WithData {

		@TempDir
		private Path folder;

		@TestFactory
		Stream<DynamicTest> testEmpty() {
			return configs(folder).map(config -> dynamicTest(config.label(), () -> {
				assertSuccess(config);

				MetadataRegistry target = createTarget(config);
				BiConsumer<String, String> action = mock(BiConsumer.class);
				target.forEachEntry(action);
				verify(action, never()).accept(anyString(), anyString());
			}));
		}

		@TestFactory
		Stream<DynamicTest> testStringValue() {
			return configs(folder).map(config -> dynamicTest(config.label(), () -> {
				try(MetadataRegistry source = createSource(config)) {
					source.beginUpdate();
					try {
						source.setValue(KEY_1, VALUE_STRING);
					} finally {
						source.endUpdate();
					}
				}

				assertSuccess(config);

				try(MetadataRegistry target = createTarget(config)) {
					assertThat(target.getValue(KEY_1)).isEqualTo(VALUE_STRING);
				}
			}));
		}

		@TestFactory
		Stream<DynamicTest> testBooleanValue() {
			return configs(folder).map(config -> dynamicTest(config.label(), () -> {
				try(MetadataRegistry source = createSource(config)) {
					source.beginUpdate();
					try {
						source.setBooleanValue(KEY_1, VALUE_BOOL);
					} finally {
						source.endUpdate();
					}
				}

				assertSuccess(config);

				try(MetadataRegistry target = createTarget(config)) {
					assertThat(target.getBooleanValue(KEY_1, !VALUE_BOOL)).isEqualTo(VALUE_BOOL);
				}
			}));
		}

		@TestFactory
		Stream<DynamicTest> testByteValue() {
			return configs(folder).map(config -> dynamicTest(config.label(), () -> {
				try(MetadataRegistry source = createSource(config)) {
					source.beginUpdate();
					try {
						source.setByteValue(KEY_1, VALUE_BYTE);
					} finally {
						source.endUpdate();
					}
				}

				assertSuccess(config);

				try(MetadataRegistry target = createTarget(config)) {
					assertThat(target.getByteValue(KEY_1, (byte)0)).isEqualTo(VALUE_BYTE);
				}
			}));
		}

		@TestFactory
		Stream<DynamicTest> testShortValue() {
			return configs(folder).map(config -> dynamicTest(config.label(), () -> {
				try(MetadataRegistry source = createSource(config)) {
					source.beginUpdate();
					try {
						source.setShortValue(KEY_1, VALUE_SHORT);
					} finally {
						source.endUpdate();
					}
				}

				assertSuccess(config);

				try(MetadataRegistry target = createTarget(config)) {
					assertThat(target.getShortValue(KEY_1, (short)0)).isEqualTo(VALUE_SHORT);
				}
			}));
		}

		@TestFactory
		Stream<DynamicTest> testIntValue() {
			return configs(folder).map(config -> dynamicTest(config.label(), () -> {
				try(MetadataRegistry source = createSource(config)) {
					source.beginUpdate();
					try {
						source.setIntValue(KEY_1, VALUE_INT);
					} finally {
						source.endUpdate();
					}
				}

				assertSuccess(config);

				try(MetadataRegistry target = createTarget(config)) {
					assertThat(target.getIntValue(KEY_1, 0)).isEqualTo(VALUE_INT);
				}
			}));
		}

		@TestFactory
		Stream<DynamicTest> testLongValue() {
			return configs(folder).map(config -> dynamicTest(config.label(), () -> {
				try(MetadataRegistry source = createSource(config)) {
					source.beginUpdate();
					try {
						source.setLongValue(KEY_1, VALUE_LONG);
					} finally {
						source.endUpdate();
					}
				}

				assertSuccess(config);

				try(MetadataRegistry target = createTarget(config)) {
					assertThat(target.getLongValue(KEY_1, 0)).isEqualTo(VALUE_LONG);
				}
			}));
		}

		@TestFactory
		Stream<DynamicTest> testFloatValue() {
			return configs(folder).map(config -> dynamicTest(config.label(), () -> {
				try(MetadataRegistry source = createSource(config)) {
					source.beginUpdate();
					try {
						source.setFloatValue(KEY_1, VALUE_FOAT);
					} finally {
						source.endUpdate();
					}
				}

				assertSuccess(config);

				try(MetadataRegistry target = createTarget(config)) {
					assertThat(target.getFloatValue(KEY_1, 0.0F)).isEqualTo(VALUE_FOAT);
				}
			}));
		}

		@TestFactory
		Stream<DynamicTest> testDoubleValue() {
			return configs(folder).map(config -> dynamicTest(config.label(), () -> {
				try(MetadataRegistry source = createSource(config)) {
					source.beginUpdate();
					try {
						source.setDoubleValue(KEY_1, VALUE_DOUBLE);
					} finally {
						source.endUpdate();
					}
				}

				assertSuccess(config);

				try(MetadataRegistry target = createTarget(config)) {
					assertThat(target.getDoubleValue(KEY_1, 0.0)).isEqualTo(VALUE_DOUBLE);
				}
			}));
		}
	}

	@Test
	void testHelp() {
		new CommandLine(new MetadataConverterCommand()).execute("-h");
	}

	private static final String KEY_1 = "some.random.key";
	private static final String KEY_2 = "another.key";
	private static final String KEY_3 = "key";

	private static final String VALUE_STRING = "my value";
	private static final boolean VALUE_BOOL = true;
	private static final byte VALUE_BYTE = 123;
	private static final short VALUE_SHORT = 5432;
	private static final int VALUE_INT = 1234567;
	private static final long VALUE_LONG = Long.MAX_VALUE/2;
	private static final float VALUE_FOAT = 123.456F;
	private static final double VALUE_DOUBLE = 1234.5678;

	static void assertSuccess(Config config) {
		assertThat(execute(config)).isEqualTo(0);
	}

	private static Stream<Config> configs(Path folder) {
		return Stream.of(FORMATS)
				.flatMap(sourceFormat -> Stream.of(ENCODINGS)
						.flatMap(inputEncoding -> Stream.of(FORMATS)
								.flatMap(targetFormat -> Stream.of(ENCODINGS)
										.map(outputEncoding -> {
											Config config = new Config();
											config.sourceFormat = sourceFormat;
											config.inputEncoding = inputEncoding;
											config.targetFormat = targetFormat;
											config.outputEncoding = outputEncoding;
											config.source = makeFile(folder, true, sourceFormat);
											config.target = makeFile(folder, false, targetFormat);
											return config;
										}))));
	}

	private static Path makeFile(Path folder, boolean input, Format format) {
		try {
			return Files.createTempFile(folder, input ? "input" : "output", format.getFileSuffix());
		} catch (IOException e) {
			throw new AssertionError("Unable to create temp file for test", e);
		}
	}

	static class Config {
		Path source, target;
		Format sourceFormat, targetFormat;
		Charset inputEncoding = StandardCharsets.UTF_8, outputEncoding = StandardCharsets.UTF_8;

		void applyTo(Consumer<? super String> append) {
			assert source!=null : "no source file defined";
			assert target!=null : "no target file defined";

			if(sourceFormat!=null) {
				append.accept("-s");
				append.accept(sourceFormat.toString());
			}
			if(inputEncoding!=null) {
				append.accept("-i");
				append.accept(inputEncoding.displayName());
			}
			if(targetFormat!=null) {
				append.accept("-t");
				append.accept(targetFormat.toString());
			}
			if(outputEncoding!=null) {
				append.accept("-o");
				append.accept(outputEncoding.displayName());
			}

			append.accept(source.toString());
			append.accept(target.toString());
		}

		public String label() {
			return String.format("-s %s -i %s -t %s -o %s", sourceFormat, inputEncoding, targetFormat, outputEncoding);
		}
	}

	static int execute(Config config) {
		List<String> args = new ObjectArrayList<>();
		config.applyTo(args::add);

		return new CommandLine(new MetadataConverterCommand()).execute(CollectionUtils.toArray(args, String[]::new));
	}

	static MetadataRegistry makeRegistry(IOResource resource, Format format, Charset encoding) {
		switch (format) {
		case PLAIN: return new PlainMetadataRegistry(resource, encoding);
		case XML: return new JAXBMetadataRegistry(resource, encoding);

		default:
			throw new InternalError("Unknown format: "+format);
		}
	}

	static MetadataRegistry createSource(Config config) {
		IOResource resource = new FileResource(config.source, AccessMode.WRITE);
		return makeRegistry(resource, config.sourceFormat, config.inputEncoding);
	}

	static MetadataRegistry createTarget(Config config) {
		IOResource resource = new FileResource(config.target, AccessMode.READ);
		MetadataRegistry registry = makeRegistry(resource, config.targetFormat, config.outputEncoding);
		registry.open();
		return registry;
	}
}
