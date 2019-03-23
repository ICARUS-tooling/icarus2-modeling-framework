/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.standard;

import java.util.Random;
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
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.BucketSetBuilder;

/**
 * @author Markus Gärtner
 *
 */
@State(Scope.Thread)
@AuxCounters(Type.EVENTS)
@BenchmarkMode(Mode.AverageTime)
@Fork(value=1)
@Warmup(iterations=5, time=2, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=5, time=2, timeUnit=TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BucketSetBuilderBenchmark {

	public static void main(String[] args) throws RunnerException {
		new Runner(new OptionsBuilder()
				.include(BucketSetBuilderBenchmark.class.getName())
				.jvmArgs("-Xms8g", "-Xmx8g")
				.build()).run();
	}

	// Benchmark parameters

	@Param({"10000", "24576", "100000", "1000000", "10000000"})
	private int chunkSize;

	@Param({"true"})
	private boolean useLastHitCache;

	@Param({"INTEGER"})
	private IndexValueType indexValueType;

	// Utility
	private long[] randomData;
	private long offset;
	private Random random;
	private int randomOffsetRange;
	private int randomOffset;

	private static final int size = 1<<19;

	// For benchmarking
	private BucketSetBuilder builder;

	@Setup(Level.Trial)
	public void setUp() {
		builder = new BucketSetBuilder(indexValueType, chunkSize, useLastHitCache);
		offset = 0;

		random = new Random(System.currentTimeMillis());
		randomOffsetRange = random.nextInt(Integer.MAX_VALUE/10);
		randomData = new long[size];

		int upperLimit = Integer.MAX_VALUE-randomOffsetRange;
		for (int i = 0; i < size; i++) {
			long value = random.nextInt(upperLimit);
			randomData[i] = value;
		}
		randomOffset = random.nextInt(randomOffsetRange);
	}

	@TearDown(Level.Trial)
	public void tearDown() {
		builder.build();
		System.out.println();
		builder.printStats(System.out);
	}

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

	@Benchmark
	@OperationsPerInvocation(size)
	public void testAddSingleIncremental() {
		for (int i = 0; i < size; i++) {
			builder.add(i + offset);
		}
		offset += size;
	}

	@Benchmark
	@OperationsPerInvocation(size)
	public void testAddSingleRandom() {
		for (int i = 0; i < size; i++) {
			builder.add(randomData[i] + randomOffset);
		}
		randomOffset = random.nextInt(randomOffsetRange);
	}
}
