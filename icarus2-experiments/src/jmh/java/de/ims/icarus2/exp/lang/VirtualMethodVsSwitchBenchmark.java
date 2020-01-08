/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.exp.lang;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
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
 * Compare performance cost of virtual methods with the lookup cost
 * of switch statements (enum and String).
 *
 * We use the following scenario as test bed: <br>
 * An unary integer operation is specified by a type (enum).
 * On the one hand each enum value receives its matching
 * implementation in a dedicated class. On the other hand
 * we have 2 methods that use switch statements to decide on
 * the fly what to do, once with the raw enums and another
 * by using the {@link Type#name() name} of the enum values
 * to switch on.
 *
 * @author Markus Gärtner
 *
 */
@State(Scope.Thread)
public class VirtualMethodVsSwitchBenchmark {

	private static final Random random = new Random(System.currentTimeMillis());

	// BENCHMARK SETTING CONSTANTS
	private static final int ITERATIONS = 5;
	private static final int TIME = 5;

	// TEST DATA SETTINGS
	private static final int SIZE = 1<<20;
	private static final int mask = SIZE-1;

	// HELPER OBJECTS
	private static final EnumSwitch enumSwitch = new EnumSwitch();
	private static final StringSwitch stringSwitch = new StringSwitch();

	// BENCHMARK STATE
	private int[] values;
	private Type[] types;
	private IntUnaryOperator[] lambdas;
	private IntUnaryOperator[] directLambdas;
	private IntUnaryOperator[] ops;
	private int cursor;

	private IntUnaryOperator makeLambda(int rand, int constant) {
		return value -> value + rand + constant;
	}

	@Setup(Level.Iteration)
	public void setupData() {
		values = new int[SIZE];
		types = new Type[SIZE];
		ops = new IntUnaryOperator[SIZE];
		lambdas = new IntUnaryOperator[SIZE];
		directLambdas = new IntUnaryOperator[SIZE];

		Type[] _types = Type.values();
		IntUnaryOperator[] _ops = {
			new ImplA(), new ImplB(), new ImplC(), new ImplD(), new ImplE(), new ImplF(), new ImplG()
		};
		int options = _types.length;
		int[] operands = IntStream.generate(random::nextInt).limit(options).toArray();
		int[] constants = {1, 17, -12, 123, 11, -999, 1_000_000};
		IntUnaryOperator[] _lambdas = {
			value -> value + operands[0] + 1,
			value -> value + operands[1] + 17,
			value -> value + operands[2] - 12,
			value -> value + operands[3] + 123,
			value -> value + operands[4] + 11,
			value -> value + operands[5] - 999,
			value -> value + operands[6] + 1_000_000,
		};
		IntUnaryOperator[] _directLambdas = new IntUnaryOperator[options];
		for (int i = 0; i < _directLambdas.length; i++) {
			_directLambdas[i] = makeLambda(operands[i], constants[i]);
		}

		for (int i = 0; i < ops.length; i++) {
			values[i] = random.nextInt();
			int n = random.nextInt(_types.length);
			types[i] = _types[n];
			lambdas[i] = _lambdas[n];
			directLambdas[i] = _directLambdas[n];
			ops[i] = _ops[n];
		}
	}

	@TearDown(Level.Iteration)
	public void cleanupData(Blackhole bh) {
		bh.consume(values);
		bh.consume(types);
		bh.consume(ops);

		values = null;
		types = null;
		ops = null;
	}
	/**
	 * Simulates the underlying integer addition without the intermediate
	 * method invocation.
	 */
	@Benchmark
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@BenchmarkMode(Mode.AverageTime)
	@Warmup(iterations=ITERATIONS, time=TIME, timeUnit=TimeUnit.SECONDS)
	@Measurement(iterations=ITERATIONS, time=TIME, timeUnit=TimeUnit.SECONDS)
	@Fork(1)
	public void testBaseline(Blackhole bh) {
		bh.consume(values[cursor] + cursor);
		cursor = ++cursor & mask;
	}

	/**
	 * Call {@link EnumSwitch#applyAsInt(int, Type)} and move cursor one position
	 */
	@Benchmark
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@BenchmarkMode(Mode.AverageTime)
	@Warmup(iterations=ITERATIONS, time=TIME, timeUnit=TimeUnit.SECONDS)
	@Measurement(iterations=ITERATIONS, time=TIME, timeUnit=TimeUnit.SECONDS)
	@Fork(1)
	public void testSwitchEnum(Blackhole bh) {
		bh.consume(enumSwitch.applyAsInt(values[cursor], types[cursor]));
		cursor = ++cursor & mask;
	}

	/**
	 * Call {@link StringSwitch#applyAsInt(int, Type)} and move cursor one position
	 */
	@Benchmark
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@BenchmarkMode(Mode.AverageTime)
	@Warmup(iterations=ITERATIONS, time=TIME, timeUnit=TimeUnit.SECONDS)
	@Measurement(iterations=ITERATIONS, time=TIME, timeUnit=TimeUnit.SECONDS)
	@Fork(1)
	public void testSwitchString(Blackhole bh) {
		bh.consume(stringSwitch.applyAsInt(values[cursor], types[cursor].name()));
		cursor = ++cursor & mask;
	}

	/**
	 * Call {@link IntUnaryOperator#applyAsInt(int)} on a random implementation
	 * and move cursor one position.
	 */
	@Benchmark
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@BenchmarkMode(Mode.AverageTime)
	@Warmup(iterations=ITERATIONS, time=TIME, timeUnit=TimeUnit.SECONDS)
	@Measurement(iterations=ITERATIONS, time=TIME, timeUnit=TimeUnit.SECONDS)
	@Fork(1)
	public void testVirtualMethod(Blackhole bh) {
		bh.consume(ops[cursor].applyAsInt(values[cursor]));
		cursor = ++cursor & mask;
	}

	/**
	 * Call {@link IntUnaryOperator#applyAsInt(int)} on a random lambda
	 * and move cursor one position.
	 */
	@Benchmark
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@BenchmarkMode(Mode.AverageTime)
	@Warmup(iterations=ITERATIONS, time=TIME, timeUnit=TimeUnit.SECONDS)
	@Measurement(iterations=ITERATIONS, time=TIME, timeUnit=TimeUnit.SECONDS)
	@Fork(1)
	public void testLambda(Blackhole bh) {
		bh.consume(lambdas[cursor].applyAsInt(values[cursor]));
		cursor = ++cursor & mask;
	}

	/**
	 * Call {@link IntUnaryOperator#applyAsInt(int)} on a random lambda
	 * that does not rely on an additional array lookup but can be fully precompiled
	 * and move cursor one position.
	 */
	@Benchmark
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@BenchmarkMode(Mode.AverageTime)
	@Warmup(iterations=ITERATIONS, time=TIME, timeUnit=TimeUnit.SECONDS)
	@Measurement(iterations=ITERATIONS, time=TIME, timeUnit=TimeUnit.SECONDS)
	@Fork(1)
	public void testDirectLambda(Blackhole bh) {
		bh.consume(directLambdas[cursor].applyAsInt(values[cursor]));
		cursor = ++cursor & mask;
	}

	/**
	 * Results with SIZE = 1<<20 and 7 different IntUnaryOperator implementations
	 *
Benchmark                                         Mode  Cnt   Score   Error  Units
VirtualMethodVsSwitchBenchmark.testBaseline       avgt    5   3.379 ± 0.454  ns/op
VirtualMethodVsSwitchBenchmark.testDirectLambda   avgt    5   4.897 ± 0.079  ns/op
VirtualMethodVsSwitchBenchmark.testLambda         avgt    5  25.453 ± 4.069  ns/op
VirtualMethodVsSwitchBenchmark.testSwitchEnum     avgt    5  20.041 ± 0.305  ns/op
VirtualMethodVsSwitchBenchmark.testSwitchString   avgt    5  23.440 ± 0.438  ns/op
VirtualMethodVsSwitchBenchmark.testVirtualMethod  avgt    5  25.233 ± 8.183  ns/op

	 * @param args
	 * @throws RunnerException
	 */
	public static void main(String[] args) throws RunnerException {
		ChainedOptionsBuilder builder = JmhUtils.jmhOptions(VirtualMethodVsSwitchBenchmark.class,
				false, ResultFormatType.TEXT);

		new Runner(builder.build()).run();
	}

	enum Type {
		A, B, C, D, E, F, G;
	}

	static class EnumSwitch {
		private static final int value = random.nextInt();
		int applyAsInt(int operand, Type type) {
			switch (type) {
			case A: return value + operand + 1;
			case B: return value + operand + 17;
			case C: return value + operand - 12;
			case D: return value + operand + 123;
			case E: return value + operand + 11;
			case F: return value + operand - 999;
			case G: return value + operand + 1_000_000;
			default:
				throw new InternalError();
			}
		}
	}

	static class StringSwitch {
		private static final int value = random.nextInt();
		int applyAsInt(int operand, String type) {
			switch (type) {
			case "A": return value + operand + 1;
			case "B": return value + operand + 17;
			case "C": return value + operand - 12;
			case "D": return value + operand + 123;
			case "E": return value + operand + 11;
			case "F": return value + operand - 999;
			case "G": return value + operand + 1_000_000;
			default:
				throw new InternalError();
			}
		}
	}

	static class ImplA implements IntUnaryOperator {
		private static final int value = random.nextInt();
		@Override
		public int applyAsInt(int operand) { return value + operand + 1; }
	}
	static class ImplB implements IntUnaryOperator {
		private static final int value = random.nextInt();
		@Override
		public int applyAsInt(int operand) { return value + operand + 17; }
	}
	static class ImplC implements IntUnaryOperator {
		private static final int value = random.nextInt();
		@Override
		public int applyAsInt(int operand) { return value + operand - 12; }
	}
	static class ImplD implements IntUnaryOperator {
		private static final int value = random.nextInt();
		@Override
		public int applyAsInt(int operand) { return value + operand + 123; }
	}
	static class ImplE implements IntUnaryOperator {
		private static final int value = random.nextInt();
		@Override
		public int applyAsInt(int operand) { return value + operand + 11; }
	}
	static class ImplF implements IntUnaryOperator {
		private static final int value = random.nextInt();
		@Override
		public int applyAsInt(int operand) { return value + operand - 999; }
	}
	static class ImplG implements IntUnaryOperator {
		private static final int value = random.nextInt();
		@Override
		public int applyAsInt(int operand) { return value + operand + 1_000_000; }
	}
}
