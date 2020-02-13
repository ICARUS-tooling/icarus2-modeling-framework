/**
 *
 */
package de.ims.icarus2.query.api.eval;

import java.util.stream.Stream;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.eval.Expression.ListExpression;
import de.ims.icarus2.query.api.eval.Expression.TextExpression;

/**
 * @author Markus Gärtner
 *
 */
public class AnnotationAccess {

	public static TextExpression of(Expression<Item> item, EvaluationContext context,
			TextExpression key) {

		if(key.isConstant()) {
			//TODO delegate to implementation that pre-fetches the annotation layers
		}

		//TODO delegate to implementation that looks up annotation layer at evaluation time
	}

	public static TextExpression of(Expression<Item> item, EvaluationContext context,
			TextExpression[] keys) {

		if(Stream.of(keys).allMatch(Expression::isConstant)) {
			//TODO delegate to implementation that pre-fetches the annotation layers
		}

		//TODO delegate to implementation that looks up annotation layer at evaluation time
	}

	public static ListExpression<?, CharSequence> of(Expression<Item> item,
			EvaluationContext context,
			ListExpression<?, CharSequence> keys) {
		//TODO see above info on single-key method and apply to lists
	}
}
