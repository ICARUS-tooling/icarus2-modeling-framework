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
package de.ims.icarus2.query.api.engine.result;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.engine.QueryOutput;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.iql.IqlGroup;
import de.ims.icarus2.query.api.iql.IqlResult.ResultType;
import de.ims.icarus2.query.api.iql.IqlSorting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class QueryOutputFactory {

	/*
	 * Payload info:
	 *
	 * We have the following types to handle here for sorting:
	 * Integer -> long
	 * FloatingPoint -> double
	 * Text -> CharSequence
	 *
	 */

	private final Set<ResultType> resultTypes = EnumSet.noneOf(ResultType.class);

	private final List<IqlSorting> sortings = new ObjectArrayList<>();
	private final List<IqlGroup> groups = new ObjectArrayList<>();

	private Boolean first;
	private Long limit;
	private Boolean percent;

	/** Root context used for the entire query */
	private EvaluationContext context;
	private final List<StructurePattern> patterns = new ObjectArrayList<>();

	/** Consumer to intercept a single match live while the container is loaded */
	private BiConsumer<Container, Match> liveMatchConsumer;
	/** Consumer to intercept a multi-match live while the containers are loaded */
	private BiConsumer<Container[], MultiMatch> liveMultiMatchConsumer;

	/** Consumer to intercept a single match when the result state is stable
	 *  and the associated container is not available anymore  */
	private Consumer<Match> finalMatchConsumer;
	/** Consumer to intercept a multi-match when the result state is stable
	 *  and the associated containers are not available anymore  */
	private Consumer<MultiMatch> finalMultiMatchConsumer;


	public QueryOutput createOutput() {
		//TODO
	}
}
