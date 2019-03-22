/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.standard;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.BucketSetBuilder;

/**
 * @author Markus Gärtner
 *
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations=5, time=1, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=5, time=1, timeUnit=TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BucketSetBuilderBenchmark {

	@State(Scope.Thread)
	public static class StateData {

		// Benchmark parameters

		@Param({/*"100000", "1000000",*/ "10000000"})
		private int chunkSize;

		@Param({"true", "false"})
		private boolean useLastHitCache;

		// Utility
		private Random random;
		public long value;

		// For benchmarking
		public BucketSetBuilder builder;

		@Setup
		public void setUp() {
			value = 0;
			builder = new BucketSetBuilder(IndexValueType.LONG, chunkSize, useLastHitCache);

			random = new Random(System.currentTimeMillis());
		}

		@TearDown
		public void tearDown() {
			builder.build();
			builder.printStats(System.out);
		}
	}

	@Benchmark
	public void testAddSingleIncremental(StateData data) {
		long value = ++data.value;
		data.builder.add(value);
	}

	@Benchmark
	public void testAddSingleRandom(StateData data) {
		long value = Math.abs(data.random.nextLong());
		data.builder.add(value);
	}
}
