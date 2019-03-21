/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.standard;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.BucketSetBuilder;

/**
 * @author Markus Gärtner
 *
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class BucketSetBuilderBenchmark {
	@State(Scope.Thread)
	public static class StateData {

		@Param({"10", "100", "1000", "1000000"})
		private int chunkSize;

		@Param({"true", "false"})
		private boolean useLastHitCache;

		// For benchmarking
		public BucketSetBuilder builder;
		public long value;

		@Setup
		public void setUp() {
			value = 0;
			builder = new BucketSetBuilder(IndexValueType.LONG, chunkSize, useLastHitCache);
		}

		@TearDown
		public void tearDown() {
			builder.build();
		}
	}

	@Benchmark
	public void testAddSingle(StateData data) {
		long value = ++data.value;
		data.builder.add(value);
	}
}
