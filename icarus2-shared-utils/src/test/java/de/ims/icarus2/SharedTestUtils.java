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
package de.ims.icarus2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public class SharedTestUtils {


	public static void assertIcarusException(ErrorCode errorCode, Executable executable, String msg) {
		IcarusRuntimeException exception = assertThrows(IcarusRuntimeException.class, executable, msg);
		assertEquals(errorCode, exception.getErrorCode(), msg);
	}

	@SuppressWarnings("boxing")
	public static <E extends Object> DataSequence<E> mockSequence(long size) {
		DataSequence<E> sequence = mock(DataSequence.class);
		when(sequence.entryCount()).thenReturn(size);
		return sequence;
	}

	public static <E extends Object> DataSequence<E> mockSequence(long size, E element) {
		DataSequence<E> sequence = mockSequence(size);

		when(sequence.elementAt(anyLong())).then(invocation -> {
			@SuppressWarnings("boxing")
			long index = invocation.getArgument(0);
			if(index<0 || index>=size)
				throw new IndexOutOfBoundsException();
			return element;
		});

		return sequence;
	}

	@SafeVarargs
	public static <E extends Object> DataSequence<E> mockSequence(E...elements) {
		DataSequence<E> sequence = mockSequence(elements.length);

		when(sequence.elementAt(anyLong())).then(invocation -> {
			@SuppressWarnings("boxing")
			long index = invocation.getArgument(0);
			if(index<0 || index>=elements.length)
				throw new IndexOutOfBoundsException();
			return elements[(int) index];
		});

		return sequence;
	}
}
