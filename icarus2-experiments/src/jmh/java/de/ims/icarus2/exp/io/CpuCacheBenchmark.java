/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.exp.io;

import java.util.Random;
import java.util.concurrent.TimeUnit;

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
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

import de.ims.icarus2.test.JmhUtils;

/**
 * @author Markus Gärtner
 *
 */
@State(Scope.Thread)
public class CpuCacheBenchmark {

	@Param({"8192","16384","24576","32768","40960","49152","57344","65536",
			"73728","81920","90112","98304","106496","114688","122880","131072"})
	private int cacheSize;

	// Utility

	private static final int MAX_SIZE = 128 * 1024;

	/*
	 *  Need this again if we want to construct the cacheSize values
	 *  dynamically in the main() method.
	 */
	@SuppressWarnings("unused")
	private static final int STEP_SIZE = 8192;

	private static final long RUNS = 1_000_000_000L;

	private byte[] array;
	private byte value;

	@Setup(Level.Iteration)
	public void setupBuffer() {
		array = new byte[MAX_SIZE];
		new Random().nextBytes(array);
		value = array[98765];
	}

	@TearDown(Level.Iteration)
	public void cleanupBuffer(Blackhole bh) {
		bh.consume(array);
	}

	/**
	 * Runs {@value #RUNS} modifications on the internal byte array.
	 */
	@Benchmark
	@OutputTimeUnit(TimeUnit.SECONDS)
	@BenchmarkMode(Mode.SingleShotTime)
	@Warmup(iterations=5)
	@Measurement(iterations=5)
	@Fork(1)
	public void testAccess() {
		long runs = RUNS;
		while(runs>0) {
	        for (int k = 0; k < cacheSize; k += 64) {
	            array[k] = value;
	            runs--;
	        }
		}
	}

	/**
	 *
	 * Results with RUNS = 10_000_000_000:
	 *
Benchmark                     (cacheSize)  Mode  Cnt   Score   Error  Units
CpuCacheBenchmark.testAccess         8192    ss    5   8.505 ± 0.568   s/op
CpuCacheBenchmark.testAccess        16384    ss    5   8.180 ± 0.231   s/op
CpuCacheBenchmark.testAccess        24576    ss    5   8.018 ± 0.124   s/op
CpuCacheBenchmark.testAccess        32768    ss    5   8.291 ± 0.894   s/op
CpuCacheBenchmark.testAccess        40960    ss    5  13.200 ± 0.575   s/op
CpuCacheBenchmark.testAccess        49152    ss    5  12.851 ± 0.299   s/op
CpuCacheBenchmark.testAccess        57344    ss    5  12.398 ± 0.251   s/op
CpuCacheBenchmark.testAccess        65536    ss    5  12.315 ± 0.776   s/op
CpuCacheBenchmark.testAccess        73728    ss    5  12.209 ± 0.124   s/op
CpuCacheBenchmark.testAccess        81920    ss    5  12.214 ± 0.205   s/op
CpuCacheBenchmark.testAccess        90112    ss    5  12.323 ± 0.351   s/op
CpuCacheBenchmark.testAccess        98304    ss    5  12.322 ± 0.326   s/op
CpuCacheBenchmark.testAccess       106496    ss    5  12.358 ± 0.346   s/op
CpuCacheBenchmark.testAccess       114688    ss    5  12.611 ± 1.340   s/op
CpuCacheBenchmark.testAccess       122880    ss    5  12.658 ± 0.773   s/op
CpuCacheBenchmark.testAccess       131072    ss    5  12.544 ± 0.497   s/op
	 *
	 * @param args
	 * @throws RunnerException
	 */
	public static void main(String[] args) throws RunnerException {
		ChainedOptionsBuilder builder = JmhUtils.jmhOptions(CpuCacheBenchmark.class,
				false, ResultFormatType.TEXT);

		new Runner(builder.build()).run();
	}
}
