/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static java.util.Objects.requireNonNull;

import java.util.Set;

import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.ReportBuilder;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.id.StaticIdentity;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;

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
public class IqlValidator implements AutoCloseable {

	private static final Identity ID = new StaticIdentity(IqlValidator.class.getCanonicalName(),
			"IQL Validator", Identity.NO_DESCRIPTION);

	private static final Option[] DEFAULT_OPTIONS = {
			Option.REMEMBER_ELEMENTS,
			Option.TRACK_ISSUES
	};

	/**
	 * Creates a new validator for the given options. If no options are
	 * provided, a set of default options will be used and the returned
	 * validator will behave as if the {@link Option#TRACK_ISSUES} and
	 * {@link Option#REMEMBER_ELEMENTS} options were given.
	 * @param options
	 * @return
	 */
	public static IqlValidator create(Option...options) {
		if(options.length==0) {
			options = DEFAULT_OPTIONS;
		}
		return new IqlValidator(set(options));
	}

	/** Buffer for all reported issues */
	private final ReportBuilder<ReportItem> issues;
	/** Keeps track of validated elements and the respective results */
	private final Reference2BooleanMap<IqlQueryElement> processed;
	private final boolean ignoreIntegrity;
	private final boolean ignoreNested;

	public enum Option {
		/** Track all validation issues and provide a {@link Report} at the end. */
		TRACK_ISSUES,
		/** For every validated element, remember the result and prevent redundant evaluations. */
		REMEMBER_ELEMENTS,
		/** Do not call {@link IqlQueryElement#checkIntegrity()} on elements. */
		IGNORE_INTEGRITY,
		/** Do not recursively validate nested elements. If this option is set, client code
		 * will have to manually call validation methods for nested elements of interest! */
		IGNORE_NESTED,
	}

	private IqlValidator(Set<Option> options) {
		requireNonNull(options);
		issues = options.contains(Option.TRACK_ISSUES) ? ReportBuilder.builder(ID) : null;
		processed = options.contains(Option.REMEMBER_ELEMENTS) ? new Reference2BooleanOpenHashMap<>() : null;
		ignoreIntegrity = options.contains(Option.IGNORE_INTEGRITY);
		ignoreNested = options.contains(Option.IGNORE_NESTED);
	}

	/**
	 * Discard any issue buffer and clear the history of processed elements.
	 */
	@Override
	public void close() {
		if(issues!=null) {
			issues.discard();
		}
		if(processed!=null) {
			processed.clear();
		}
	}

	private static class Context {
		final Stack<IqlQueryElement> trace = new ObjectArrayList<>();
		/** Flag to indicate whether {@link IqlQuantifier.QuantifierType#ALL} is allowed */
		boolean allowAllQuant = true;
	}

	/**
	 * Validates the given element. This general method is primarily intended for
	 * multiplexing to type-specific methods internally.
	 *
	 * @param element
	 * @return
	 */
	public boolean validate(IqlQueryElement element) {
	}

	private boolean
}
