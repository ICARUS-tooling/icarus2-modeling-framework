/**
 *
 */
package de.ims.icarus2.query.api.eval;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import java.util.stream.Stream;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.eval.Expression.ListExpression;
import de.ims.icarus2.query.api.eval.Expression.TextExpression;

/**
 * @author Markus Gärtner
 *
 */
public class AnnotationAccess {

	public static Expression<?> of(Expression<? extends Item> item, EvaluationContext context,
			TextExpression key) {

		if(key.isConstant()) {
			//TODO delegate to implementation that pre-fetches the annotation layers
		}

		//TODO delegate to implementation that looks up annotation layer at evaluation time
	}

	public static Expression<?> of(Expression<? extends Item> item, EvaluationContext context,
			TextExpression[] keys) {

		if(Stream.of(keys).allMatch(Expression::isConstant)) {
			//TODO delegate to implementation that pre-fetches the annotation layers
		}

		//TODO delegate to implementation that looks up annotation layer at evaluation time
	}

	public static ListExpression<?, ?> of(Expression<? extends Item> item,
			EvaluationContext context,
			ListExpression<?, CharSequence> keys) {
		//TODO see above info on single-key method and apply to lists
	}

	static final class SingleTextAnnotationAccess implements TextExpression {

		private final Expression<? extends Item> item;
		private final Function<Item, CharSequence> lookup;

		public SingleTextAnnotationAccess(Expression<? extends Item> item,
				Function<Item, CharSequence> lookup) {
			this.item = requireNonNull(item);
			this.lookup = requireNonNull(lookup);
		}

		@Override
		public CharSequence compute() { return lookup.apply(item.compute()); }

		@Override
		public Expression<CharSequence> duplicate(EvaluationContext context) {
			return new SingleTextAnnotationAccess(item.duplicate(context), lookup);
		}
	}
}
