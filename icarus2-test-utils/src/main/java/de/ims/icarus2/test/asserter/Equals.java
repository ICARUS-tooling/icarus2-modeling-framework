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
package de.ims.icarus2.test.asserter;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Markus Gärtner
 *
 */
public class Equals<T extends Object> extends Asserter<Equals<T>> {

	public static <T extends Object> Equals<T> create(T target) {
		return new Equals<>(target);
	}

	private Equals(T target) {
		this.target = requireNonNull(target);
	}

	private final T target;

	private final List<Object> equalItems = new ArrayList<>();
	private final List<Object> unequalItems = new ArrayList<>();

	private boolean checkHashContract = true;

	private boolean checkHashConsistency = true;

	private int hashConsistencyRepeats = 10;

	private boolean checkReflexiveEquals = true;

	@SafeVarargs
	public final Equals<T> addEqual(Object...items) {
		assertTrue(items.length>0);
		Collections.addAll(equalItems, items);
		return thisAsCast();
	}

	@SafeVarargs
	public final Equals<T> addUnequal(Object...items) {
		assertTrue(items.length>0);
		Collections.addAll(unequalItems, items);
		return thisAsCast();
	}

	public Equals<T> checkHashContract(boolean value) {
		this.checkHashContract = value;
		return thisAsCast();
	}

	public Equals<T> checkHashConsistency(boolean value) {
		this.checkHashConsistency = value;
		return thisAsCast();
	}

	public Equals<T> hashConsistencyRepeats(int value) {
		assertTrue(value>1);
		this.hashConsistencyRepeats = value;
		return thisAsCast();
	}

	public Equals<T> checkReflexiveEquals(boolean value) {
		this.checkReflexiveEquals = value;
		return thisAsCast();
	}

	/**
	 * @see de.ims.icarus2.test.asserter.Asserter#test()
	 */
	@Override
	public void test() {
		assertFalse(equalItems.isEmpty() || unequalItems.isEmpty(), "No items to check");


		if(checkReflexiveEquals) {
			assertEquals(target, target, "Reflexive equals");
		}

		if(checkHashConsistency) {
			int hash = target.hashCode();
			for(int i=1; i<hashConsistencyRepeats; i++) {
				assertEquals(hash, target.hashCode());
			}
		}

		for(int i=0; i<equalItems.size(); i++) {
			Object item = equalItems.get(i);

			assertTrue(target.equals(item), "Mismatch for index "+i);
			assertTrue(item.equals(target), "Mismatch reverse for index "+i);

			if(checkHashContract) {
				assertEquals(target.hashCode(), item.hashCode(), "Mismatch of hash for index "+i);
			}
		}


		for(int i=0; i<unequalItems.size(); i++) {
			Object item = unequalItems.get(i);

			assertFalse(target.equals(item), "Unexpected match for index "+i);
			assertFalse(item.equals(target), "Unexpected reverse match for index "+i);
		}
	}
}
