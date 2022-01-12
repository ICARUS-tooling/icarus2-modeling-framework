/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine.matcher.mark;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.lang.Primitives._int;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.query.api.exp.EvaluationUtils;
import de.ims.icarus2.query.api.iql.IqlMarker;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerExpression;
import de.ims.icarus2.query.api.iql.IqlMarker.MarkerExpressionType;
import de.ims.icarus2.query.api.iql.IqlType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

public class MarkerTransform {
	public static class MarkerSetup {
		public final IqlMarker regularMarker, generationMarker;

		public MarkerSetup(IqlMarker generationMarker, IqlMarker normalMarker) {
			this.regularMarker = normalMarker;
			this.generationMarker = generationMarker;
		}

		@Override
		public String toString() {
			return String.format("[gen=%s reg=%s]", generationMarker, regularMarker);
		}
	}

	/**
	 *
	 * @param marker
	 * @param callAction called for any {@link IqlMarkerCall}
	 * @param expAction called for any {@link IqlMarkerExpression} after all children have been visited
	 */
	public static void traverse(IqlMarker marker,
			Consumer<? super IqlMarkerCall> callAction,
			Consumer<? super IqlMarkerExpression> expAction) {
		switch (marker.getType()) {
		case MARKER_CALL: {
			IqlMarkerCall call = (IqlMarkerCall) marker;
			callAction.accept(call);
		} break;

		case MARKER_EXPRESSION: {
			IqlMarkerExpression exp = (IqlMarkerExpression) marker;
			exp.getItems().forEach(m -> traverse(m, callAction, expAction));
			expAction.accept(exp);
		} break;

		default:
			throw EvaluationUtils.forUnsupportedQueryFragment("marker", marker.getType());
		}
	}

	public static <T> T compute(IqlMarker marker,
			Function<? super IqlMarkerCall, T> callAction,
			Function<? super IqlMarkerExpression, T> expAction) {
		switch (marker.getType()) {
		case MARKER_CALL: {
			IqlMarkerCall call = (IqlMarkerCall) marker;
			return callAction.apply(call);
		}

		case MARKER_EXPRESSION: {
			IqlMarkerExpression exp = (IqlMarkerExpression) marker;
			exp.getItems().forEach(m -> compute(m, callAction, expAction));
			return expAction.apply(exp);
		}

		default:
			throw EvaluationUtils.forUnsupportedQueryFragment("marker", marker.getType());
		}
	}

	private final List<IqlMarker> raw = new ObjectArrayList<>();
	private final Reference2IntMap<IqlMarker> ids = new Reference2IntOpenHashMap<>();
	private final Reference2IntMap<IqlMarker> flagLut = new Reference2IntOpenHashMap<>();

	private static @Nullable IqlMarker _and(List<IqlMarker> elements) {
		if(elements.isEmpty()) {
			return null;
		}
		return elements.size()==1 ? elements.get(0) : IqlMarkerExpression.and(elements);
	}

	private static @Nullable IqlMarker _or(List<IqlMarker> elements) {
		if(elements.isEmpty()) {
			return null;
		}
		return elements.size()==1 ? elements.get(0) : IqlMarkerExpression.or(elements);
	}

	@VisibleForTesting
	int scan(IqlMarker marker) {
		return compute(marker,
				call -> {
					int flags = _flags(call.getName());
					flagLut.put(call, flags);
					return _int(flags);
				},
				exp -> {
					int flags = 0;
					for (IqlMarker m : exp.getItems()) {
						flags |= flagLut.getInt(m);
					}
					flagLut.put(exp, flags);
					return _int(flags);
				}).intValue() & ALL; // Filter out artifacts
	}

	/**
	 * Analyzes the given {@code marker} and transforms it if necessary.
	 * <p>
	 * If the marker is pure (i.e. it contains either only
	 * {@link GenerationMarker#isValidName(String) generation} marker
	 * instances or none of that type at all) it will be used as is and
	 * wrapped into a single instance of {@link MarkerSetup}.
	 * <p>
	 * If the marker is found to be non-pure, it will undergo the following
	 * transformation process:
	 * <ol>
	 * <li>Transform marker expressions in such a way that nested expressions are
	 * split off. This reorders elements such that elements of mixed nature are
	 * processed first and re-added, then pure generation markers and lastly pure
	 * regular markers.</li>
	 * <li>Find a nested disjunction that contains (directly or transitively through
	 * a pure element) generation markers and apply the distributive law on it. This is done
	 * back to front, so {@code (a | b | c) & (d | e)}, where {@code (a | b | c)} is the inner
	 * (nested) expression, will result in {@code (c & e) | (c & d) | ... | (a & e) & (a & d)}.</li>
	 * <li>Flatten any affected terms, i.e. hoist the elements of nested expressions
	 * of the same type. In other words: remove redundant bracketing.</li>
	 * <li>Repeat steps 2 and 3 until no nested disjunctions with generation markers remain.</li>
	 * <li>Now transform every next-to-top-level conjunction (or the entire expression in case the
	 * top-level connective is disjunctive) into a separate {@link MarkerSetup} instance.
	 * This will gather all the top-level expression into two groups, one for generation markers
	 * and one for regular types.</li>
	 * </ol>
	 *
	 *
	 * @param marker
	 * @return a series of {@link MarkerSetup} instances that represent a normalized
	 * equivalent of the given {@code marker}.
	 */
	public MarkerSetup[] apply(IqlMarker marker) {

		// Initial analysis
		int rootFlags = scan(marker);

		final MarkerSetup[] setups;

		// Simple options
		if(_pure(rootFlags)) {
			switch (rootFlags) {
			case GEN: setups = new MarkerSetup[] {new MarkerSetup(marker, null)}; break;
			case SEQ:
			case LVL: setups = new MarkerSetup[] {new MarkerSetup(null, marker)}; break;

			default:
				throw new InternalError("We messed up flag calculation");
			}
		} else {

			/*
			 * Transform marker expression into disjunctive normal form.
			 * Since we do _not_ have negation with markers, this is
			 * straightforward.
			 */

			// Convert to an easier to use term hierarchy
			Term root = toTerm(marker);
//			System.out.println("root: "+toString(root));
			// Make sure we start as flat as possible
			unwrap(root, true);
//			System.out.println("flattened: "+toString(root));
			// Now transform as much as possible (this might require a lot of passes internally)
			normalize(root);
//			System.out.println("normalized: "+toString(root));

			//TODO we can end up with multiple branches that start with the same generation markers
			/*
			 *  e.g.: (lvl | seq1 | seq2) & gen -> (lvl & gen) | (gen & (seq1 | seq2))
			 *
			 *  should not get optimized at all?
			 */

			setups = asSetups(root, root.type==OR);
		}

		// Cleanup state
		raw.clear();
		ids.clear();
		flagLut.clear();

		return setups;
	}

	/** Term is a direct pointer to a raw marker call or construct */
	final static int RAW = 0;
	/** Intermediate conjunctive term */
	final static int AND = 1;
	/** Intermediate disjunctive term */
	final static int OR = 2;

	final static int GEN = 1;
	final static int LVL = 2;
	final static int SEQ = 4;

	final static int ALL = GEN | LVL | SEQ;
	final static int NON_GEN = LVL | SEQ;

	private void append(IqlMarker marker, StringBuilder sb) {
		if(marker.getType()==IqlType.MARKER_CALL) {
			IqlMarkerCall call = (IqlMarkerCall)marker;
			sb.append(call.getName());
			if(call.getArgumentCount()>0) {
				sb.append('(');
				for (int i = 0; i < call.getArgumentCount(); i++) {
					if(i>0) sb.append(',');
					sb.append(call.getArgument(i));
				}
				sb.append(')');
			}
		} else {
			IqlMarkerExpression exp = (IqlMarkerExpression) marker;
			final String op = exp.getExpressionType()==MarkerExpressionType.CONJUNCTION ? " & " : " | ";
			final List<IqlMarker> elements = exp.getItems();
			sb.append('(');
			for (int i = 0; i < elements.size(); i++) {
				if(i>0) sb.append(op);
				append(elements.get(i), sb);
			}
			sb.append(')');
		}
	}

	private void append(Term term, StringBuilder sb) {
		if(term.type==RAW) {
			sb.append('[');
			append(raw.get(term.id), sb);
			sb.append(']');
		} else {
			final String op = term.type==AND ? " & " : " | ";
			final List<Term> elements = term.elements;
			sb.append('(');
			for (int i = 0; i < elements.size(); i++) {
				if(i>0) sb.append(op);
				append(elements.get(i), sb);
			}
			sb.append(')');
		}
	}

	public String toString(Term term) {
		StringBuilder sb = new StringBuilder();
		append(term, sb);
		return sb.toString();
	}

	static class Term {
		private int type;

		/** Term elements for binary operations */
		private @Nullable List<Term> elements;
		/** Id of marker call if type==RAW */
		private int id = UNSET_INT;
		/** Flags to indicate whether a term contains certain markers */
		private int flags = 0;

		Term(int type) { this.type = type; }

		void addFlag(int flag) { flags |= flag; }
		void removeFlag(int flag) { flags &= ~flag; }
		boolean isType(int type) { return this.type==type; }
		boolean isSet(int flag) { return (flags & flag) == flag; }
		boolean isPure() { return _pure(flags); }
		List<Term> elements() {
			if(elements==null) elements = new ObjectArrayList<>();
			return elements;
		}
		void add(Term term) {
			elements().add(term);
			addFlag(term.flags);
		}
		void replace(Term other) {
			flags = other.flags;
			type = other.type;
			id = other.id;
			elements.clear();
			if(other.elements!=null) {
				elements.addAll(other.elements);
			}
		}
		void destroy() {
			flags = 0;
			id = UNSET_INT;
			type = UNSET_INT;
			elements = null;
		}
	}

	/** Creates a term that points to a raw marker (call or construct) */
	private Term createRaw(IqlMarker marker) {
		Term term = new Term(RAW);
		final int id = raw.size();
		ids.put(marker, id);
		raw.add(marker);
		term.id = id;
		term.addFlag(flagLut.getInt(marker));
		assert _pure(term.flags) : "RAW terms must pe pure: "+term.flags;
		return term;
	}

	/** */
	private Term createProxy(int type, int flags, List<IqlMarker> elements) {
		IqlMarker marker = type==AND ? _and(elements) : _or(elements);
		flagLut.put(marker, flags);
		return createRaw(marker);
	}

	@VisibleForTesting
	Term toTerm(IqlMarker marker) {
		switch (marker.getType()) {
		case MARKER_CALL: {
			return createRaw(marker);
		}

		case MARKER_EXPRESSION: {
			IqlMarkerExpression exp = (IqlMarkerExpression) marker;

			int rootFlags = flagLut.getInt(exp);
			// Nice, can keep it as is
			if(_pure(rootFlags)) {
				return createRaw(exp);
			}

			// Not a pure construct, now start splitting

			boolean isDisjunction = exp.getExpressionType()==MarkerExpressionType.DISJUNCTION;
			final int type = isDisjunction ? OR : AND;

			List<IqlMarker> elements = exp.getItems();
			assert elements.size()>1 : "must have at least 2 elements in expression";
			List<IqlMarker> mixedElements = new ObjectArrayList<>();
			List<IqlMarker> pureGen = new ObjectArrayList<>();
			List<IqlMarker> pureReg = new ObjectArrayList<>();
			int regFlags = 0;

			for (int i = 0; i < elements.size(); i++) {
				IqlMarker element = elements.get(i);
				int flags = flagLut.getInt(element) & ALL;

				if(flags==GEN) {
					pureGen.add(element);
				} else if(_pure(flags)) {
					pureReg.add(element);
					regFlags |= flags;
				} else {
					mixedElements.add(element);
				}
			}

			Term term = new Term(type);

			mixedElements.stream()
				.map(this::toTerm)
				.forEach(term::add);

			if(!pureGen.isEmpty()) term.add(createProxy(type, GEN, pureGen));
			if(!pureReg.isEmpty()) term.add(createProxy(type, regFlags, pureReg));

			return term;
		}

		default:
			throw EvaluationUtils.forUnsupportedQueryFragment("marker", marker.getType());
		}
	}

	/** Check if flags do not contain a mix of generation and other markers */
	@VisibleForTesting
	static boolean _pure(int flags) {
		return Integer.bitCount(flags & ALL) == 1 || (flags & GEN) == 0;
	}

	@VisibleForTesting
	static int _flags(String callName) {
		int flags = 0;
		if(LevelMarker.isValidName(callName)) {
			flags |= LVL;
		} else if(GenerationMarker.isValidName(callName)) {
			flags |= GEN;
		} else if(HorizontalMarker.isValidName(callName)) {
			flags |= SEQ;
		}

		return flags;
	}

	/**
	 * Try to convert the given term to modified DNF and return {@code true}
	 * if at least one modification was performed.
	 *
	 * Preconditions:
	 * The given term must be flattened!
	 */
	@VisibleForTesting
	boolean normalize(Term term) {
		assert term.type!=RAW : "cannot normalize raw terms";
		List<Term> elements = term.elements;

		boolean result = false;
		// 0. ensure nested terms are normalized
		for (int i = 0; i < elements.size(); i++) {
			Term element = elements.get(i);
			if(element.type!=RAW) {
				result |= normalize(element);
			}
		}
		// Flatten structure if our direct elements changed
		if(result) {
			unwrap(term, false);
		}

		scan : for(;;) {
			assert elements.size()>1 : "must have at least 2 elements in root expression";
			for (int i = 0; i < elements.size(); i++) {
				Term element = elements.get(i);
				// Skip raw terms
				if(element.type==RAW) {
					continue;
				}

				// 1. find a nested disjunction that contains a generation marker
				if(element.type==OR && element.isSet(GEN)) {
					assert term.type==AND : "redundant disjunctive nesting detected";
					// 2. apply distributive property
					Term inner = elements.remove(i); // The disjunctive element we found
					Term outer = elements.remove(0); // Next best element in term
					term.add(distribute(inner, outer));
					// Flatten our current structure
					unwrap(term, false);

					// We modified term structure, so force another scan
					result = true;
					continue scan;
				}
			}
			// No changes made
			break;
		}
		// 3. flatten the structure again (we do this only for the part that we modified)

		return result;
	}

	private static int len(Term term) {
		return term.type==RAW ? 1 : term.elements.size();
	}

	private Term binary(int type, Term left, Term right) {
		Term t = new Term(type);
		t.add(left);
		t.add(right);
		unwrap(t, false);
		return t;
	}

	@VisibleForTesting
	Term distribute(Term inner, Term outer) {
		final int lenOut = len(outer), lenIn = len(inner);
		assert lenIn>1 : "inner term must be a proper expression";

		Term root = new Term(OR);
		if(lenOut==1) {
			/* Classic distributive situation:
			 *   ((A | B | C) & D)
			 * = ((A & D) | (B & D) | (C & D))
			 *
			 * We split this case off since we don't need to
			 * destroy the 'outer' term for this.
			 */
			for (int i = inner.elements.size()-1; i >= 0; i--) {
				root.add(binary(AND, inner.elements.get(i), outer));
			}
			inner.destroy();
		} else {
			// lenOut >= 2 AND lenIn >= 2
			List<Term> eOut = outer.elements;
			List<Term> eIn = inner.elements;

			// Generalized FOIL approach: include all binary combinations of elements from the two terms
			for (int i = eIn.size()-1; i >= 0; i--) {
				for (int j = eOut.size()-1; j >= 0; j--) {
					root.add(binary(AND, eIn.get(i), eOut.get(j)));
				}
			}

			inner.destroy();
			outer.destroy();
		}
		return root;
	}

	/** Remove any "brackets". Preserves flags from merged subtrees. */
	@VisibleForTesting
	void unwrap(Term term, boolean recursive) {
		assert term.type!=RAW : "cannot unwrap raw";
		// Hoist single children
		if(term.elements.size()==1) {
			Term child = term.elements.get(0);
			term.replace(child);
			child.destroy();

			if(recursive && term.type!=RAW) {
				unwrap(term, true);
			}

			return;
		}

		// Scan nested expressions of same type and move them up
		for (int i = 0; i < term.elements.size(); i++) {
			Term element = term.elements.get(i);
			if(recursive && element.type!=RAW) {
				unwrap(element, recursive);
			}
			if(element.type==term.type) {
				term.elements.remove(i);
				term.elements.addAll(i, element.elements);
				term.addFlag(element.flags);
				element.destroy();
			}
		}
	}

	@VisibleForTesting
	MarkerSetup[] asSetups(Term root, boolean split) {
		Collection<Term> terms = split ? root.elements : Collections.singleton(root);

		final MarkerSetup[] setups = new MarkerSetup[terms.size()];

		List<IqlMarker> genMarkers = new ObjectArrayList<>();
		List<IqlMarker> regMarkers = new ObjectArrayList<>();

		int idx = 0;

		for(Term term : terms) {
			assert term.type!=OR || (!split && term==root) : "cannot create marker setup from disjunctive term";

			// All the actual elements to collect
			Collection<Term> elements = term.elements;
			if(term.type==RAW) {
				elements = Collections.singleton(term);
			}

			// Split raw elements into 3 groups
			for (Term element : elements) {
				assert element.type==RAW : "Nested terms m ust be pointers to actual markers";
				assert element.isPure() : "No mixed content allowed in nested terms";

				IqlMarker marker = raw.get(element.id);
				if(element.flags==GEN) {
					genMarkers.add(marker);
				} else {
					regMarkers.add(marker);
				}
			}

			setups[idx++] = new MarkerSetup(_and(genMarkers), _and(regMarkers));

			genMarkers.clear();
			regMarkers.clear();
		}

		return setups;
	}
}