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

import static de.ims.icarus2.test.TestUtils.K100;
import static de.ims.icarus2.test.TestUtils.randomLongs;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.AuxCounters.Type;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.Blackhole;

import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.BucketSetBuilder;

/**
 * @author Markus Gärtner
 *
 */
@State(Scope.Thread)
@AuxCounters(Type.EVENTS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
public class BucketSetBuilderBenchmark {

	// Benchmark parameters
	@Param({"10000", "100000"/*, "1000000", "10000000"*/})
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
//		System.out.println();
//		builder.printStats(System.out);
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
	@BenchmarkMode({Mode.SingleShotTime, Mode.AverageTime})
	@Measurement(iterations = 5, batchSize = K100)
	@Warmup(iterations = 5, batchSize = K100)
	@OperationsPerInvocation(K100)
	public void addSingleIncrement_100K() {
		builder.add(index++);
	}

	@Benchmark
	@BenchmarkMode({Mode.SingleShotTime, Mode.AverageTime})
	@Measurement(iterations = 5, batchSize = K100)
	@Warmup(iterations = 5, batchSize = K100)
	@OperationsPerInvocation(K100)
	public void addSingleRandom_100K() {
		builder.add(randomData[index++]);
	}
}
