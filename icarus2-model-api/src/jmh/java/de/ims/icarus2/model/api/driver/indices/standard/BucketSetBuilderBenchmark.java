/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.test.TestUtils.M100;
import static de.ims.icarus2.test.TestUtils.jmhOptions;
import static de.ims.icarus2.test.TestUtils.randomLongs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.AuxCounters.Type;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.BucketSetBuilder;

/**
 * @author Markus Gärtner
 *
 */
@State(Scope.Thread)
@AuxCounters(Type.EVENTS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BucketSetBuilderBenchmark {

	// Benchmark parameters

	@Param({"100000"})
	private int chunkSize;

	@Param({"true"})
	private boolean useLastHitCache;

	@Param({"INTEGER"})
	private IndexValueType indexValueType;

	// Utility
	private static long[] randomData;
	private int index;
	private int size;

	// For benchmarking
	private BucketSetBuilder builder;

	@Setup(Level.Trial)
	public void prepareData(BenchmarkParams params) {
		size = params.getOpsPerInvocation();
		if(size<=1)
			throw new IllegalStateException("Expecting batch size!!");

		randomData = randomLongs(size, 0, indexValueType.maxValue());
	}

	@Setup(Level.Iteration)
	public void setUp() {
		builder = new BucketSetBuilder(indexValueType, chunkSize, useLastHitCache);
		index = 0;
	}

	@TearDown(Level.Iteration)
	public void tearDown(Blackhole bh) {
		bh.consume(builder.build());
	}

	// AUX COUNTERS

	public long insertions() {
		return builder.getInsertions();
	}

	public long cacheMisses() {
		return builder.getCacheMisses();
	}

	public long duplicates() {
		return builder.getDuplicates();
	}

	public long buckets() {
		return builder.getUsedBucketCount();
	}

	public int size() {
		return size;
	}

	// END AUX COUNTERS

	@Benchmark
	@BenchmarkMode({Mode.SingleShotTime})
	@Measurement(iterations = 5)
	@Warmup(iterations = 5)
	public void addSingleIncrement(Blackhole bh) {
		for (int i = 0; i < size; i++) {
			builder.add(index++);
		}
		bh.consume(builder);
	}

	@Benchmark
	@BenchmarkMode({Mode.SingleShotTime})
	@Measurement(iterations = 5)
	@Warmup(iterations = 5)
	public void addSingleRandom(Blackhole bh) {
		for (int i = 0; i < size; i++) {
			builder.add(randomData[index++]);
		}
		bh.consume(builder);
	}

	private static void prepareChunkSizes(ChainedOptionsBuilder builder, int max) {
		int current = 10_000;
		List<String> tmp = new ArrayList<>();
		while(current>0 && current<=max) {
			tmp.add(String.valueOf(current));
			current *= 10;
		}
		if(current>0 && current<max) {
			tmp.add(String.valueOf(tmp));
		}

		builder.param("chunkSize", tmp.toArray(new String[tmp.size()]));
	}

	public static void main(String[] args) throws RunnerException {
		final int size = M100;

		ChainedOptionsBuilder builder =
				jmhOptions(BucketSetBuilderBenchmark.class, false, ResultFormatType.CSV)
				.jvmArgsAppend("-Xmx8g", "-Xms8g")

				.operationsPerInvocation(size)

				.param("useLastHitCache", "true")
				.param("indexValueType", "INTEGER");

		prepareChunkSizes(builder, size);


		Collection<RunResult> results = new Runner(builder.build()).run();

		System.out.println(results);
	}
}
