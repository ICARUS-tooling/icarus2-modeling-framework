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

import static de.ims.icarus2.test.TestUtils.M1;
import static de.ims.icarus2.test.TestUtils.randomLongs;

import java.util.concurrent.TimeUnit;

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
import org.openjdk.jmh.infra.BenchmarkParams;

import de.ims.icarus2.model.api.driver.indices.IndexValueType;

/**
 * @author Markus Gärtner
 *
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
public class IndexBufferBenchmark {

	private IndexBuffer buffer;
	private int position;
	private long[] values;
	private int size;

	@Param({"INTEGER"})
	public IndexValueType indexValueType;

	@Setup(Level.Trial)
	public void initRandoms(BenchmarkParams params) {
		size = Math.max(params.getMeasurement().getBatchSize(),
				params.getWarmup().getBatchSize());
		values = randomLongs(size+1, 0, indexValueType.maxValue());
	}

	@Setup(Level.Iteration)
	public void initBuffer() {
		position = 0;
		buffer = new IndexBuffer(indexValueType, size);
	}

	@TearDown(Level.Iteration)
	public void cleanup() {
		buffer.size();
	}

	@Benchmark
	@Measurement(iterations = 5, batchSize = M1)
	@OperationsPerInvocation(M1)
	@BenchmarkMode(Mode.SingleShotTime)
	public void testAddSingle_1M() {
		buffer.add(values[position++]);
	}
}
