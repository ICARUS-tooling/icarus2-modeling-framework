/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author Markus Gärtner
 *
 */
public class JmhUtils {

	private static final String JMH_RESULT_FOLDER = "build/jmh-results/";
	private static final String JMH_LOG = ".log";

	private static void ensureJmhResultFolder() {
		try {
			Path path = Paths.get(JMH_RESULT_FOLDER);
			if(Files.notExists(path)) {
				Files.createDirectories(path);
			}
		} catch(IOException e) {
			throw new InternalError("Failed to ensure JMH result fodler: "+JMH_RESULT_FOLDER, e);
		}
	}

	/**
	 * Creates {@link ChainedOptionsBuilder} to initiate benchmarks for the
	 * given {@code benchmarkClass}.
	 *
	 *
	 * @param benchmarkClass
	 * @return
	 */
	public static ChainedOptionsBuilder jmhOptions(Class<?> benchmarkClass) {
		String name = benchmarkClass.getSimpleName();

		return new OptionsBuilder()
				.jvmArgsPrepend("-Djmh.separateClasspathJAR=true")
				.resultFormat(ResultFormatType.CSV)
//				.shouldDoGC(true)	//disabled for now, as we want to let each benchamrk decide individually
				.shouldFailOnError(true)
				.include(name);
	}

	/**
	 * Creates {@link ChainedOptionsBuilder} to initiate benchmarks for the
	 * given {@code benchmarkClass} that depending on the {@code writeToLog}
	 * argument will write log data to a log file in the {@link #JMH_RESULT_FOLDER}
	 * that is named according to the class under test.
	 *
	 * @param benchmarkClass
	 * @param writeToLog
	 * @return
	 *
	 * @see #jmhOptions(Class)
	 */
	public static ChainedOptionsBuilder jmhOptions(Class<?> benchmarkClass,
			boolean writeToLog) {
		String name = benchmarkClass.getSimpleName();
		ChainedOptionsBuilder builder = jmhOptions(benchmarkClass);
		if(writeToLog) {
			ensureJmhResultFolder();
			builder.output(JMH_RESULT_FOLDER+name+JMH_LOG);
		}
		return builder;
	}

	/**
	 *
	 * @param benchmarkClass
	 * @param writeToLog
	 * @param resultFormatType
	 * @return
	 *
	 * @see #jmhOptions(Class, boolean)
	 * @see #jmhOptions(Class)
	 */
	public static ChainedOptionsBuilder jmhOptions(Class<?> benchmarkClass,
			boolean writeToLog, ResultFormatType resultFormatType) {
		String name = benchmarkClass.getSimpleName();
		ChainedOptionsBuilder builder = jmhOptions(benchmarkClass, writeToLog);
		ensureJmhResultFolder();
		builder.resultFormat(resultFormatType);
		builder.result(JMH_RESULT_FOLDER+name+'.'+resultFormatType.toString().toLowerCase());
		return builder;
	}

}
