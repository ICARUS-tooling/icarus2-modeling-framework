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
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.test.TestUtils.M1;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;

import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.test.JmhUtils;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
@State(Scope.Thread)
public class IndexBufferBenchmark {

	private final RandomGenerator rand = RandomGenerator.random();

	private IndexBuffer buffer;
	private long[] values;

	@Param({"1000000"})
	private int size;

	@Param({"INTEGER"})
	public IndexValueType indexValueType;

	@Setup(Level.Trial)
	public void initRandoms(BenchmarkParams params) {
		values = rand.randomLongs(size, 0, indexValueType.maxValue());
	}

	@Setup(Level.Iteration)
	public void initBuffer() {
		buffer = new IndexBuffer(indexValueType, size);
	}

	@Benchmark
	@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
	@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
	@OperationsPerInvocation(M1)
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public IndexBuffer testAddSingle_1M() {
		for (int i = 0; i < values.length; i++) {
			buffer.add(values[i]);
		}
		buffer.clear();
		return buffer;
	}

	/**
# JMH version: 1.21
# VM version: JDK 1.8.0_162, Java HotSpot(TM) 64-Bit Server VM, 25.162-b12
# VM invoker: C:\Program Files\Java\jdk1.8.0_162\jre\bin\java.exe
# VM options: -Djmh.separateClasspathJAR=true -Dfile.encoding=UTF-8
# Warmup: 5 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: de.ims.icarus2.model.api.driver.indices.standard.IndexBufferBenchmark.testAddSingle_1M
# Parameters: (indexValueType = INTEGER, size = 1000000)
Benchmark                              (indexValueType)   (size)  Mode  Cnt  Score   Error  Units
IndexBufferBenchmark.testAddSingle_1M           INTEGER  1000000  avgt   25  4.636 ± 0.210  ns/op
	 */
	public static void main(String[] args) throws RunnerException {
		new Runner(JmhUtils.jmhOptions(IndexBufferBenchmark.class, true, ResultFormatType.CSV)
				.param("size", "1000000")
				.param("indexValueType", "INTEGER")

				.build())
		.run();
	}
}
