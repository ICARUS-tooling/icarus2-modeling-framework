/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
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
MathBenchmark.incrementInt      avgt   10   5.746 ▒ 0.211  ns/op
MathBenchmark.incrementLong     avgt   10   5.894 ▒ 0.343  ns/op
MathBenchmark.incrementShort    avgt   10   5.986 ▒ 0.426  ns/op
MathBenchmark.noOp              avgt   10   0.779 ▒ 0.163  ns/op
MathBenchmark.testIntAbs        avgt   10   5.803 ▒ 0.256  ns/op
MathBenchmark.testIntGen        avgt   10  17.078 ▒ 1.708  ns/op
MathBenchmark.testIntGenAbs     avgt   10  16.747 ▒ 1.480  ns/op
MathBenchmark.testLongAbs       avgt   10   6.774 ▒ 0.373  ns/op
MathBenchmark.testLongGen       avgt   10  31.606 ▒ 1.607  ns/op
MathBenchmark.testLongGenAbs    avgt   10  31.400 ▒ 1.487  ns/op
MathBenchmark.testRandomDouble  avgt   10  31.278 ▒ 0.613  ns/op
MathBenchmark.testRandomFloat   avgt   10  16.110 ▒ 1.149  ns/op
MathBenchmark.testRandomInt     avgt   10  15.632 ▒ 0.412  ns/op
MathBenchmark.testRandomLong    avgt   10  30.988 ▒ 0.929  ns/op
 * </pre
 */

	private long longValue;
	private int intValue;
	private short shortValue;

	private Random random;

	private LongSupplier longGen;
	private IntSupplier intGen;

	@Setup
	public void init() {
		longValue = 0L;
		intValue = 0;
		shortValue = 0;
		random = new Random(System.currentTimeMillis());

		longGen = () -> random.nextLong();
		intGen = () -> random.nextInt();
	}

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

	@Benchmark
	public int testIntGen() {
		return intGen.getAsInt();
	}

	@Benchmark
	public long testLongGen() {
		return longGen.getAsLong();
	}

	@Benchmark
	public int testIntGenAbs() {
		return Math.abs(intGen.getAsInt());
	}

	@Benchmark
	public long testLongGenAbs() {
		return Math.abs(longGen.getAsLong());
	}
}
