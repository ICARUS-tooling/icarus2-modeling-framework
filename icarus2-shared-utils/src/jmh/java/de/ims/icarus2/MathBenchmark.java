/**
 *
 */
package de.ims.icarus2;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
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
@State(Scope.Thread)
@Fork(value=2)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations=5, time=1, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=5, time=1, timeUnit=TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Threads(7) // Artifact of running on a Quadcore hyperthreaded local machine with a GUI
public class MathBenchmark {

/**
 * <pre>
Benchmark                       Mode  Cnt   Score   Error  Units
MathBenchmark.incrementInt      avgt   10   5.814 ▒ 0.553  ns/op
MathBenchmark.incrementLong     avgt   10   5.927 ▒ 0.313  ns/op
MathBenchmark.incrementShort    avgt   10   5.826 ▒ 0.196  ns/op
MathBenchmark.noOp              avgt   10   0.781 ▒ 0.058  ns/op
MathBenchmark.testIntAbs        avgt   10   5.774 ▒ 0.124  ns/op
MathBenchmark.testLongAbs       avgt   10   6.539 ▒ 0.286  ns/op
MathBenchmark.testRandomDouble  avgt   10  33.799 ▒ 4.010  ns/op
MathBenchmark.testRandomFloat   avgt   10  16.300 ▒ 1.652  ns/op
MathBenchmark.testRandomInt     avgt   10  16.346 ▒ 1.476  ns/op
MathBenchmark.testRandomLong    avgt   10  32.511 ▒ 2.001  ns/op
 * </pre
 */

	private long longValue = 0;
	private int intValue = 0;
	private short shortValue = 0;

	private Random random = new Random(System.currentTimeMillis());

	@Benchmark
	public void noOp() {
		// do nothing
	}

	@Benchmark
	public long incrementLong() {
		return longValue++;
	}

	@Benchmark
	public int incrementInt() {
		return intValue++;
	}

	@Benchmark
	public short incrementShort() {
		return shortValue++;
	}

	@Benchmark
	public long testRandomLong() {
		return random.nextLong();
	}

	@Benchmark
	public int testRandomInt() {
		return random.nextInt();
	}

	@Benchmark
	public float testRandomFloat() {
		return random.nextFloat();
	}

	@Benchmark
	public double testRandomDouble() {
		return random.nextDouble();
	}

	@Benchmark
	public long testLongAbs() {
		return Math.abs(longValue++);
	}

	@Benchmark
	public int testIntAbs() {
		return Math.abs(intValue++);
	}
}
