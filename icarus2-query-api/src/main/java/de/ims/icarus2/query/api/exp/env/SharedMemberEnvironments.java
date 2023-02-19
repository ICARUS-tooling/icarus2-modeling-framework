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
/**
 *
 */
package de.ims.icarus2.query.api.exp.env;

import static de.ims.icarus2.query.api.exp.Expressions.wrapInt;
import static de.ims.icarus2.query.api.exp.Expressions.wrapObj;

import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.exp.Environment;
import de.ims.icarus2.query.api.exp.TypeInfo;

/**
 * @author Markus Gärtner
 *
 */
public class SharedMemberEnvironments {

	public static Environment[] all() {
		return new Environment[] {
				forCorpusMember(),
				forItem(),
				//TODO
		};
	}

	public static Environment forCorpusMember() {
		return CorpusMemberEnvironment.getInstance();
	}

	/**
	 * Default environment for {@link CorpusMember}
	 *
	 * @author Markus Gärtner
	 *
	 */
	private static final class CorpusMemberEnvironment extends AbstractEnvironment {

		private volatile static SharedMemberEnvironments.CorpusMemberEnvironment instance;

		static SharedMemberEnvironments.CorpusMemberEnvironment getInstance() {
			SharedMemberEnvironments.CorpusMemberEnvironment result = instance;

			if (result == null) {
				synchronized (SharedMemberEnvironments.CorpusMemberEnvironment.class) {
					result = instance;

					if (result == null) {
						instance = new SharedMemberEnvironments.CorpusMemberEnvironment();
						result = instance;
					}
				}
			}

			return result;
		}

		private CorpusMemberEnvironment() { super(null, CorpusMember.class); }

		@Override
		protected void createEntries() {
			entryBuilder()
				/** {@link CorpusMember#getCorpus} */
				.method("getCorpus", TypeInfo.CORPUS)
				.aliases("corpus")
				.instantiator((e, ctx, t, args) -> wrapObj(e, CorpusMember::getCorpus, t, args))
				.commitAndReset();
		}

	}

	public static Environment forItem() {
		return ItemEnvironment.getInstance();
	}

	/**
	 * Default environment for {@link Item}
	 *
	 * @author Markus Gärtner
	 *
	 */
	private static final class ItemEnvironment extends AbstractEnvironment {

		private volatile static SharedMemberEnvironments.ItemEnvironment instance;

		public static SharedMemberEnvironments.ItemEnvironment getInstance() {
			SharedMemberEnvironments.ItemEnvironment result = instance;

			if (result == null) {
				synchronized (SharedMemberEnvironments.ItemEnvironment.class) {
					result = instance;

					if (result == null) {
						instance = new SharedMemberEnvironments.ItemEnvironment();
						result = instance;
					}
				}
			}

			return result;
		}

		private ItemEnvironment() { super(forCorpusMember(), Item.class); }

		@Override
		protected void createEntries() {
			EntryBuilder builder = entryBuilder();

			/** {@link Item#getContainer} */
			builder.method("getContainer", TypeInfo.CONTAINER)
				.aliases("container", "host")
				.instantiator((e, ctx, t, args) -> wrapObj(e, Item::getContainer, t, args))
				.commitAndReset();

			/** {@link Item#getLayer} */
			builder.method("getLayer", TypeInfo.LAYER)
				.aliases("layer")
				.instantiator((e, ctx, t, args) -> wrapObj(e, Item::getLayer, t, args))
				.commitAndReset();

			/** {@link Item#getBeginOffset} */
			builder.method("getBeginOffset", TypeInfo.INTEGER)
				.aliases("offset", "begin", "beginOffset")
				.instantiator((e, ctx, t, args) -> wrapInt(e, Item::getBeginOffset, t, args))
				.commitAndReset();

			/** {@link Item#getEndOffset} */
			builder.method("getEndOffset", TypeInfo.INTEGER)
				.aliases("end", "endOffset")
				.instantiator((e, ctx, t, args) -> wrapInt(e, Item::getEndOffset, t, args))
				.commitAndReset();

			/** {@link Item#getIndex} */
			builder.method("getIndex", TypeInfo.INTEGER)
				.aliases("index", "position")
				.instantiator((e, ctx, t, args) -> wrapInt(e, Item::getIndex, t, args))
				.commitAndReset();

			/** {@link Item#getId} */
			builder.method("getId", TypeInfo.INTEGER)
				.aliases("id")
				.instantiator((e, ctx, t, args) -> wrapInt(e, Item::getId, t, args))
				.commitAndReset();
		}
	}

	//TODO add further wrappers for edges/fragments/containers/structures/etc...
}
