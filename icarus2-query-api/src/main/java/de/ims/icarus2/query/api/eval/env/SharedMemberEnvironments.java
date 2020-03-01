/**
 *
 */
package de.ims.icarus2.query.api.eval.env;

import static de.ims.icarus2.query.api.eval.Expressions.wrapInt;
import static de.ims.icarus2.query.api.eval.Expressions.wrapObj;

import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.eval.Environment;
import de.ims.icarus2.query.api.eval.TypeInfo;

/**
 * @author Markus Gärtner
 *
 */
public class SharedMemberEnvironments {

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
				.method("getCorpus", TypeInfo.CORPUS)
				.noArgs()
				.aliases("corpus")
				.instantiator((e, t, args) -> wrapObj(e, CorpusMember::getCorpus, t, args))
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

			builder.method("getContainer", TypeInfo.CONTAINER)
				.noArgs()
				.aliases("container", "host")
				.instantiator((e, t, args) -> wrapObj(e, Item::getContainer, t, args))
				.commitAndReset();

			builder.method("getLayer", TypeInfo.LAYER)
				.noArgs()
				.aliases("layer")
				.instantiator((e, t, args) -> wrapObj(e, Item::getLayer, t, args))
				.commitAndReset();

			builder.method("getBeginOffset", TypeInfo.INTEGER)
				.noArgs()
				.aliases("offset", "begin", "beginOffset")
				.instantiator((e, t, args) -> wrapInt(e, Item::getBeginOffset, t, args))
				.commitAndReset();

			builder.method("getEndOffset", TypeInfo.INTEGER)
				.noArgs()
				.aliases("end", "endOffset")
				.instantiator((e, t, args) -> wrapInt(e, Item::getEndOffset, t, args))
				.commitAndReset();

			builder.method("getIndex", TypeInfo.INTEGER)
				.noArgs()
				.aliases("index", "position")
				.instantiator((e, t, args) -> wrapInt(e, Item::getIndex, t, args))
				.commitAndReset();

			builder.method("getId", TypeInfo.INTEGER)
				.noArgs()
				.aliases("id")
				.instantiator((e, t, args) -> wrapInt(e, Item::getId, t, args))
				.commitAndReset();
		}
	}

	//TODO add further wrappers for edges/fragments/containers/structures/etc...
}
