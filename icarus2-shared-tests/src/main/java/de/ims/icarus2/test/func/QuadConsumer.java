/**
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
package de.ims.icarus2.test.func;

/**
 * @author Markus Gärtner
 *
 * @param <T_1> type of the first argument
 * @param <T_2> type of the second argument
 * @param <T_3> type of the third argument
 * @param <T_4> type of the forth argument
 */
@FunctionalInterface
public interface QuadConsumer<T_1, T_2, T_3, T_4> {

	void accept(T_1 first, T_2 seconds, T_3 third,T_4 forth);
}
