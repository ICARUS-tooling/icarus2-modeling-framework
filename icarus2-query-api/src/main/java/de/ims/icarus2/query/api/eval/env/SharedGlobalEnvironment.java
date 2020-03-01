/**
 *
 */
package de.ims.icarus2.query.api.eval.env;

import static de.ims.icarus2.query.api.eval.Expressions.wrapBool;

import de.ims.icarus2.query.api.eval.Expression;
import de.ims.icarus2.query.api.eval.Expressions;
import de.ims.icarus2.query.api.eval.TypeInfo;

/**
 * @author Markus Gärtner
 *
 */
public class SharedGlobalEnvironment extends AbstractEnvironment {

	private volatile static SharedGlobalEnvironment instance;

	public static SharedGlobalEnvironment getInstance() {
		SharedGlobalEnvironment result = instance;

		if (result == null) {
			synchronized (SharedGlobalEnvironment.class) {
				result = instance;

				if (result == null) {
					instance = new SharedGlobalEnvironment();
					result = instance;
				}
			}
		}

		return result;
	}

	private SharedGlobalEnvironment() {
		super(null, null);
	}

	@Override
	protected void createEntries() {
		EntryBuilder builder = entryBuilder();

		//TODO move those entries to an environment focused on java.lang.Object
		builder.method("toString", TypeInfo.TEXT)
			.noArgs()
			.instantiator((e, t, args) -> Expressions.<Object,String>wrapObj(e, Object::toString, t, args))
			.commitAndReset();

		builder.method("equals", TypeInfo.BOOLEAN)
			.argumentTypes(TypeInfo.GENERIC)
			.instantiator((e, t, args) -> {
				Expression<?> other = args[0];
				return wrapBool(e, obj -> obj.equals(other.compute()), t, args);
			})
			.commitAndReset();


		// TODO add more
	}

}
