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
package de.ims.icarus2.util.mem;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import de.ims.icarus2.test.JmhUtils;
import de.ims.icarus2.util.mem.ByteAllocator.Cursor;

/**
 * @author Markus Gärtner
 *
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations=3, time=5, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=5, timeUnit=TimeUnit.SECONDS)
@Fork(value=3, jvmArgsAppend={"-Xms1g"})
public class ByteAllocatorBenchmark {

	@State(Scope.Benchmark)
	public static class Shared {

		private final int slotSize = 8;

		@Param({"7", "10", "17"})
		/** Defines granularity of locking on nativeSync and optimistic variants [7...17] */
		private int chunkPower;

		@Param({"10", "15", "20"})
		/** Defines total number of used slots [10..20] */
		private int slotPower;

		@Param({"NONE", "NATIVE", "OPTIMISTIC"})
		private ByteAllocator.LockType lockType;

		private ByteAllocator allocator;

		@Setup
		public void setUp() {
			allocator = new ByteAllocator(slotSize, chunkPower, lockType);

			int slots = 1 << slotPower;

			for (int i = 0; i < slots; i++) {
				int id = allocator.alloc();
				if(id!=i)
					throw new IllegalStateException("Corrupted slot allocation: "+i);
			}
		}
	}

	@State(Scope.Thread)
	public static class Local {

		private int slots;
		private int slotMask;

		private int id = 0;
		private int value = 0;

		private Cursor cursor;

		@Setup
		public void setUp(Shared shared) {
			slots = 1<<shared.slotPower;
			slotMask = slots-1;

			cursor = shared.allocator.newCursor();
		}

		private int randomId() {
			id = (int)((id ^ System.nanoTime()) & slotMask);
			return id;
		}

		private int randomValue() {
			value = (int)(value ^ System.nanoTime());
			return value;
		}

		private int nextId() {
			id = ++id & slotMask;
			return id;
		}

		private int nextValue() {
			return value++;
		}
	}

	// BEGIN BASELINES

	@Benchmark
	public int randomValueBaseline(Local local) {
		return local.randomValue();
	}

	@Benchmark
	public int randomIdBaseline(Local local) {
		return local.randomId();
	}

	@Benchmark
	public int nextValueBaseline(Local local) {
		return local.nextValue();
	}

	@Benchmark
	public int nextIdBaseline(Local local) {
		return local.nextId();
	}

	// END BASELINES


	// BEGIN INDIVIDUAL METHODS

	@Benchmark
	public void writeRandom(Shared shared, Local local) {
		shared.allocator.setInt(local.randomId(), 0, local.randomValue());
	}

	@Benchmark
	public void writeIncremental(Shared shared, Local local) {
		shared.allocator.setInt(local.nextId(), 0, local.nextValue());
	}

	@Benchmark
	public void writeStatic(Shared shared, Local local) {
		shared.allocator.setInt(3, 0, local.value++);
	}

	@Benchmark
	public int readRandom(Shared shared, Local local) {
		return shared.allocator.getInt(local.randomId(), 0);
	}

	@Benchmark
	public int readIncremental(Shared shared, Local local) {
		return shared.allocator.getInt(local.nextId(), 0);
	}

	@Benchmark
	public int readStatic(Shared shared, Local local) {
		return shared.allocator.getInt(3, 0);
	}

	// END INDIVIDUAL METHODS

	// BEGIN CURSOR

	@Benchmark
	@GroupThreads(4)
	public void cursorWriteRandom(Shared shared, Local local) {
		local.cursor.moveTo(local.randomId()).setInt(0, local.randomValue());
	}

	@Benchmark
	@GroupThreads(4)
	public int cursorReadRandom(Shared shared, Local local) {
		return local.cursor.moveTo(local.randomId()).getInt(0);
	}

	@Benchmark
	@GroupThreads(4)
	public void cursorWriteIncremental(Shared shared, Local local) {
		local.cursor.moveTo(local.nextId()).setInt(0, local.randomValue());
	}

	@Benchmark
	@GroupThreads(4)
	public int cursorReadIncremental(Shared shared, Local local) {
		return local.cursor.moveTo(local.nextId()).getInt(0);
	}

	@Benchmark
	@Group("cursorRwConcurrent")
	@GroupThreads(2)
	public void cursorRwConcurrent_writer(Shared shared, Local local) {
		local.cursor.moveTo(local.randomId()).setInt(0, local.randomValue());
	}

	@Benchmark
	@Group("cursorRwConcurrent")
	@GroupThreads(8)
	public int cursorRwConcurrent_reader(Shared shared, Local local) {
		return local.cursor.moveTo(local.randomId()).getInt(0);
	}

	// END CURSOR


	// BEGIN CONCURRENT

	@Benchmark
	@GroupThreads(4)
	public void writeConcurrent(Shared shared, Local local) {
		shared.allocator.setInt(local.randomId(), 0, local.randomValue());
	}

	@Benchmark
	@GroupThreads(4)
	public int readConcurrent(Shared shared, Local local) {
		return shared.allocator.getInt(local.randomId(), 0);
	}

	@Benchmark
	@Group("rwConcurrent")
	@GroupThreads(2)
	public void rwConcurrent_writer(Shared shared, Local local) {
		shared.allocator.setInt(local.randomId(), 0, local.randomValue());
	}

	@Benchmark
	@Group("rwConcurrent")
	@GroupThreads(8)
	public int rwConcurrent_reader(Shared shared, Local local) {
		return shared.allocator.getInt(local.randomId(), 0);
	}

	// END CONCURRENT

	/**
	 * <pre>
	 *
Benchmark                                   (chunkPower)  (lockType)  (slotPower)  Mode  Cnt   Score   Error  Units
ByteAllocatorBenchmark.nextIdBaseline                  7        NONE           10  avgt    2   2.334          ns/op
ByteAllocatorBenchmark.nextValueBaseline               7        NONE           10  avgt    2   2.000          ns/op
ByteAllocatorBenchmark.randomIdBaseline                7        NONE           10  avgt    2  37.187          ns/op
ByteAllocatorBenchmark.randomValueBaseline             7        NONE           10  avgt    2  35.499          ns/op
ByteAllocatorBenchmark.readIncremental                 7        NONE           10  avgt    2  14.959          ns/op
ByteAllocatorBenchmark.readRandom                      7        NONE           10  avgt    2  50.227          ns/op
ByteAllocatorBenchmark.readStatic                      7        NONE           10  avgt    2  13.775          ns/op
ByteAllocatorBenchmark.writeConcurrent                 7        NONE           10  avgt    2  93.842          ns/op
ByteAllocatorBenchmark.writeIncremental                7        NONE           10  avgt    2  12.035          ns/op
ByteAllocatorBenchmark.writeRandom                     7        NONE           10  avgt    2  80.703          ns/op
ByteAllocatorBenchmark.writeStatic                     7        NONE           10  avgt    2  11.112          ns/op
	 * </pre>
	 */

	public static void main(String[] args) throws RunnerException {
		ChainedOptionsBuilder builder = JmhUtils.jmhOptions(
				ByteAllocatorBenchmark.class, false, ResultFormatType.CSV);

		builder
			.param("chunkPower", "7", "10"/*, "17"*/)
			.param("slotPower", "10", "17"/*, "22"*/)
			.param("lockType", "NONE", "NATIVE", "OPTIMISTIC")

			.exclude("Baseline")

//			.addProfiler("gc")
			.shouldDoGC(true)

			.warmupIterations(2).warmupTime(TimeValue.seconds(2))
			.measurementIterations(2).measurementTime(TimeValue.seconds(2))
			.jvmArgs("-Xms2g")
			.forks(1);

		new Runner(builder.build()).run();
	}
}
