/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static java.util.Objects.requireNonNull;

import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.ReportBuilder;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlTerm;
import de.ims.icarus2.query.api.iql.IqlElement.IqlEdge;
import de.ims.icarus2.query.api.iql.IqlElement.IqlElementDisjunction;
import de.ims.icarus2.query.api.iql.IqlElement.IqlGrouping;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlSet;
import de.ims.icarus2.query.api.iql.IqlElement.IqlTreeNode;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerExpression;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.id.StaticIdentity;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Utility class for validating an entire {@link IqlQuery} or even only
 * parts of it against the IQL specification.
 * <p>
 * Note that individual {@link IqlQueryElement} instances do not have the
 * full contextual knowledge to actually perform this validation themselves.
 * Providing for instance an individual {@link IqlNode}, no answer can be
 * given for the question whether or not the {@code all} quantifier is
 * allowed or not, as this requires a wide context (More specifically, access
 * to the tree of {@link IqlElement} objects above and around the node in
 * question).
 * <p>
 * An instance of this class is intended to be used as one-shot validator,
 * i.e. it should be initialized, have the desired validation methods called
 * against query elements and then be {@link IqlValidator#close() closed} to
 * release any buffered information.
 * In addition it is important to note that any modifications made to
 *
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public class IqlValidator {

	private static final Identity ID = new StaticIdentity(IqlValidator.class.getCanonicalName(),
			"IQL Validator", Identity.NO_DESCRIPTION);

	private static final Option[] DEFAULT_OPTIONS = {
			Option.TRACK_ISSUES
	};

	/**
	 * Creates a new validator for the given options. If no options are
	 * provided, a set of default options will be used and the returned
	 * validator will behave as if the {@link Option#TRACK_ISSUES}
	 * option was given.
	 * @param options
	 * @return
	 */
	public static IqlValidator create(IqlQueryElement root, Option...options) {
		if(options.length==0) {
			options = DEFAULT_OPTIONS;
		}
		return new IqlValidator(root, set(options));
	}

	/** Starting point of the validation */
	private final IqlQueryElement root;
	/** Buffer for all reported issues */
	private final ReportBuilder<ReportItem> issues;
	private final boolean ignoreIntegrity;
	private final boolean ignoreNested;

	final Stack<IqlQueryElement> trace = new ObjectArrayList<>();
	/** Flag to indicate whether {@link IqlQuantifier.QuantifierType#ALL} is allowed */
	boolean allowAllQuant = true;
	/** Keeps track of the accumulated validity state */
	boolean valid = true;
	/** Flag to indicate that validation has been performed */
	boolean validated = false;

	public enum Option {
		/** Track all validation issues and provide a {@link Report} at the end. */
		TRACK_ISSUES,
		/** For every validated element, remember the result and prevent redundant evaluations. */
		@Deprecated
		REMEMBER_ELEMENTS,
		/** Do not call {@link IqlQueryElement#checkIntegrity()} on elements. */
		IGNORE_INTEGRITY,
		/** Do not recursively validate nested elements. If this option is set, client code
		 * will have to manually call validation methods for nested elements of interest! */
		IGNORE_NESTED,
	}

	private IqlValidator(IqlQueryElement root, Set<Option> options) {
		requireNonNull(root);
		requireNonNull(options);

		this.root = root;
		issues = options.contains(Option.TRACK_ISSUES) ? ReportBuilder.builder(ID) : null;
		ignoreIntegrity = options.contains(Option.IGNORE_INTEGRITY);
		ignoreNested = options.contains(Option.IGNORE_NESTED);
	}

	/**
	 * Validates the previously specified root element.
	 *
	 * @return
	 */
	public boolean validate() {
		checkState("Validation already performed", !validated);
		try {
			/*
			 * Check integrity first if allowed (we do this here to avoid
			 * redundant calls during validation of query object graph and
			 * to prevent repeatedly reporting the same issue).
			 */
			if(!ignoreIntegrity) {
				try {
					root.checkIntegrity();
				} catch (IcarusRuntimeException e) {
					return false;
				}
			}

			validate(root);
		} finally {
			validated = true;
		}
		return valid;
	}

	private String trace() {
		throw new UnsupportedOperationException();
	}

	private void error(String msg) {
		if(issues!=null) {
			msg = trace() + ": " + msg;
			issues.addError(QueryErrorCode.CORRUPTED_QUERY, msg);
		}
		valid = false;
	}

	private void validateNested(IqlQueryElement element) {
		if(!ignoreNested) {
			validate(element);
		}
	}

	/** Multiplexing method for type specific validation */
	private void validate(IqlQueryElement element) {
		requireNonNull(element);

		switch (element.getType()) {
		case BINDING: validateBinding((IqlBinding) element); break;
		case CORPUS: validateCorpus((IqlCorpus) element); break;
		case DATA: validateData((IqlData) element); break;
		case DISJUNCTION: validateDisjunction((IqlElementDisjunction) element); break;
		case EDGE: validateEdge((IqlEdge) element); break;
		case EXPRESSION: validateExpression((IqlExpression) element); break;
		case GROUP: validateGroup((IqlGroup) element); break;
		case GROUPING: validateGrouping((IqlGrouping) element); break;
		case IMPORT: validateImport((IqlImport) element); break;
		case LANE: validateLane((IqlLane) element); break;
		case LAYER: validateLayer((IqlLayer) element); break;
		case MARKER_CALL: validateMarkerCall((IqlMarkerCall) element); break;
		case MARKER_EXPRESSION: validateMarkerExpression((IqlMarkerExpression) element); break;
		case NODE: validateNode((IqlNode) element); break;
		case PAYLOAD: validatePayload((IqlPayload) element); break;
		case PREDICATE: validatePredicate((IqlPredicate) element); break;
		case PROPERTY: validateProperty((IqlProperty) element); break;
		case QUANTIFIER: validateQuantifier((IqlQuantifier) element); break;
		case QUERY: validateQuery((IqlQuery) element); break;
		case REFERENCE: validateReference((IqlReference) element); break;
		case RESULT: validateResult((IqlResult) element); break;
		case RESULT_INSTRUCTION: validateResultInstruction((IqlResultInstruction) element); break;
		case SCOPE: validateScope((IqlScope) element); break;
		case SET: validateSet((IqlSet) element); break;
		case SORTING: validateSorting((IqlSorting) element); break;
		case STREAM: validateStream((IqlStream) element); break;
		case TERM: validateTerm((IqlTerm) element); break;
		case TREE_NODE: validateTreeNode((IqlTreeNode) element); break;

		default:
			throw new IcarusRuntimeException(GlobalErrorCode.INTERNAL_ERROR,
					"Unknown element type: "+element.getType());
		}
	}

	// Aspect-specific helper methods

	private void validateQuantifiable(IqlQuantifier.Quantifiable quantifiable) {

	}

	// Type-specific validation methods

	private void validateBinding(IqlBinding binding) {
		// TODO Auto-generated method stub
	}

	private void validateData(IqlData element) {
		// TODO Auto-generated method stub

	}

	private void validateCorpus(IqlCorpus element) {
		// TODO Auto-generated method stub

	}

	private void validateDisjunction(IqlElementDisjunction element) {
		// TODO Auto-generated method stub

	}

	private void validateEdge(IqlEdge element) {
		// TODO Auto-generated method stub

	}

	private void validateExpression(IqlExpression element) {
		// TODO Auto-generated method stub

	}

	private void validateGroup(IqlGroup element) {
		// TODO Auto-generated method stub

	}

	private void validateGrouping(IqlGrouping element) {
		// TODO Auto-generated method stub

	}

	private void validateImport(IqlImport element) {
		// TODO Auto-generated method stub

	}

	private void validateLane(IqlLane element) {
		// TODO Auto-generated method stub

	}

	private void validateLayer(IqlLayer element) {
		// TODO Auto-generated method stub

	}

	private void validateMarkerCall(IqlMarkerCall element) {
		// TODO Auto-generated method stub

	}

	private void validateMarkerExpression(IqlMarkerExpression element) {
		// TODO Auto-generated method stub

	}

	private void validateNode(IqlNode element) {
		// TODO Auto-generated method stub

	}

	private void validatePayload(IqlPayload element) {
		// TODO Auto-generated method stub

	}

	private void validatePredicate(IqlPredicate element) {
		// TODO Auto-generated method stub

	}

	private void validateProperty(IqlProperty element) {
		// TODO Auto-generated method stub

	}

	private void validateQuantifier(IqlQuantifier element) {
		// TODO Auto-generated method stub

	}

	private void validateQuery(IqlQuery element) {
		// TODO Auto-generated method stub

	}

	private void validateReference(IqlReference element) {
		// TODO Auto-generated method stub

	}

	private void validateResult(IqlResult element) {
		// TODO Auto-generated method stub

	}

	private void validateResultInstruction(IqlResultInstruction element) {
		// TODO Auto-generated method stub

	}

	private void validateScope(IqlScope element) {
		// TODO Auto-generated method stub

	}

	private void validateSet(IqlSet element) {
		// TODO Auto-generated method stub

	}

	private void validateSorting(IqlSorting element) {
		// TODO Auto-generated method stub

	}

	private void validateStream(IqlStream element) {
		// TODO Auto-generated method stub

	}

	private void validateTerm(IqlTerm element) {
		// TODO Auto-generated method stub

	}

	private void validateTreeNode(IqlTreeNode element) {
		// TODO Auto-generated method stub

	}

}
