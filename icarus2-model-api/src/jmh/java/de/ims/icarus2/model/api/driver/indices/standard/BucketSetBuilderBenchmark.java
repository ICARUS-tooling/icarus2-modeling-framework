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

import static de.ims.icarus2.test.TestUtils.K10;
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
import org.openjdk.jmh.annotations.Fork;
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
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.SingleShotTime})
@Warmup(iterations=5, batchSize=M100)
@Measurement(iterations=5, batchSize=M100)
@Fork(value=5,jvmArgsAppend={"-Xmx8g", "-Xms8g"})
public class BucketSetBuilderBenchmark {

	// Benchmark parameters

	@Param({"10000","100000","1000000","10000000","100000000"})
	private int chunkSize;

	@Param({"true", "false"})
	private boolean useLastHitCache;

	@Param({"INTEGER", "LONG"})
	private IndexValueType indexValueType;

	// Utility
	private static long[] randomData;
	private int index;
	private int size;

	// For benchmarking
	private BucketSetBuilder builder;

	@Setup(Level.Trial)
	public void prepareData(BenchmarkParams params) {
		size = Math.max(params.getMeasurement().getBatchSize(),
				params.getWarmup().getBatchSize());
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
		builder = null;
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
	public void addSingleIncrement(Blackhole bh) {
		builder.add(index++);
		bh.consume(builder);
	}

	@Benchmark
	public void addSingleRandom(Blackhole bh) {
		builder.add(randomData[index++]);
		bh.consume(builder);
	}

	/**
	 * Prepares the {@code chunkSize} parameter for the given benchmark options by
	 * moving from {@code start} to {@code max} by multiplying by {@code 10} for every
	 * step. If the difference of {@code max-start} is not a multiple of {@code 10} the
	 * {@code max} values is appended in the end.
	 *
	 * @param builder
	 * @param start
	 * @param max
	 */
	private static void prepareChunkSizes(ChainedOptionsBuilder builder, int start, int max) {
		int current = start;
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

	/**
	 * Run with 100m insertions
Benchmark                                     (chunkSize)  (indexValueType)  (useLastHitCache)  Mode  Cnt      Score      Error  Units
BucketSetBuilderBenchmark.addSingleIncrement        10000           INTEGER               true    ss    5   2458.245 ±  172.516  ms/op
BucketSetBuilderBenchmark.addSingleIncrement       100000           INTEGER               true    ss    5   3168.540 ±  202.859  ms/op
BucketSetBuilderBenchmark.addSingleIncrement      1000000           INTEGER               true    ss    5   4540.997 ±  175.992  ms/op
BucketSetBuilderBenchmark.addSingleIncrement     10000000           INTEGER               true    ss    5   6446.320 ±  188.586  ms/op
BucketSetBuilderBenchmark.addSingleIncrement    100000000           INTEGER               true    ss    5   7204.305 ± 1126.107  ms/op
BucketSetBuilderBenchmark.addSingleRandom           10000           INTEGER               true    ss    5  40073.488 ± 2650.936  ms/op
BucketSetBuilderBenchmark.addSingleRandom          100000           INTEGER               true    ss    5  20741.011 ± 4647.739  ms/op
BucketSetBuilderBenchmark.addSingleRandom         1000000           INTEGER               true    ss    5  14793.947 ± 1042.352  ms/op
BucketSetBuilderBenchmark.addSingleRandom        10000000           INTEGER               true    ss    5  13435.807 ±  518.237  ms/op
BucketSetBuilderBenchmark.addSingleRandom       100000000           INTEGER               true    ss    5   8306.715 ±  421.758  ms/op

Benchmark                                     (chunkSize)  (indexValueType)  (useLastHitCache)  Mode  Cnt      Score      Error  Units
BucketSetBuilderBenchmark.addSingleIncrement        10000              LONG               true    ss    5   2644.238 ±  716.773  ms/op
BucketSetBuilderBenchmark.addSingleIncrement       100000              LONG               true    ss    5   5263.409 ± 2629.355  ms/op
BucketSetBuilderBenchmark.addSingleIncrement      1000000              LONG               true    ss    5   6246.867 ±  692.842  ms/op
BucketSetBuilderBenchmark.addSingleIncrement     10000000              LONG               true    ss    5  10421.895 ± 9545.232  ms/op
BucketSetBuilderBenchmark.addSingleIncrement    100000000              LONG               true    ss    5   9594.810 ±  475.333  ms/op
BucketSetBuilderBenchmark.addSingleRandom           10000              LONG               true    ss    5  36706.231 ±  871.281  ms/op
BucketSetBuilderBenchmark.addSingleRandom          100000              LONG               true    ss    5  21649.628 ±  464.815  ms/op
BucketSetBuilderBenchmark.addSingleRandom         1000000              LONG               true    ss    5  17344.435 ± 1405.489  ms/op
BucketSetBuilderBenchmark.addSingleRandom        10000000              LONG               true    ss    5  15773.119 ± 1697.045  ms/op
BucketSetBuilderBenchmark.addSingleRandom       100000000              LONG               true    ss    5   9899.917 ±  441.925  ms/op
	 */
	public static void main(String[] args) throws RunnerException {
		final int size = M100;

		ChainedOptionsBuilder builder =
				jmhOptions(BucketSetBuilderBenchmark.class, false, ResultFormatType.CSV)
				.jvmArgsAppend("-Xmx8g", "-Xms8g")
				.shouldDoGC(true)

				.warmupBatchSize(size)
				.warmupIterations(5)

				.measurementBatchSize(size)
				.measurementIterations(5)

				.timeUnit(TimeUnit.MILLISECONDS)

				.forks(1)

				.param("useLastHitCache", "true")
				.param("indexValueType", "LONG");

		prepareChunkSizes(builder, K10, size);

		builder.addProfiler("hs_rt");

		Collection<RunResult> results = new Runner(builder.build()).run();

//		System.out.println(results);
	}
}
