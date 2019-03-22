/**
 *
 */
package de.ims.icarus2;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 * @author Markus Gärtner
 *
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations=5, time=1, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=5, time=1, timeUnit=TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Threads(7) // Artifact of running on a Quadcore hyperthreaded local machine with a GUI
public class MathBenchmark {

	@State(Scope.Thread)
	public static class BMState {

		private long value = 0;

		private Random random = new Random(System.currentTimeMillis());
	}

	@Benchmark
	public long incrementLong(BMState state) {
		return ++state.value;
	}


	@Benchmark
	public long testCreateRandom(BMState state) {
		return Math.abs(state.random.nextLong());
	}
}
