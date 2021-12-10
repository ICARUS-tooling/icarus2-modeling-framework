/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.exp;

import static java.util.Objects.requireNonNull;

/**
 * Implements a blank and unoptimized cast to a given type.
 *
 * @author Markus Gärtner
 *
 */
public class Cast<T> implements Expression<T> {

	public static <T> Cast<T> cast(TypeInfo type, Expression<?> source) { return new Cast<>(type, source); }

	private final TypeInfo type;
	private final Expression<?> source;

	public Cast(TypeInfo type, Expression<?> source) {
		this.type = requireNonNull(type);
		this.source = requireNonNull(source);
	}

	@Override
	public TypeInfo getResultType() { return type; }

	@SuppressWarnings("unchecked")
	@Override
	public T compute() { return (T) type.getType().cast(source.compute()); }

	@Override
	public Expression<T> duplicate(EvaluationContext context) {
		return new Cast<>(type, source.duplicate(context));
	}
}
