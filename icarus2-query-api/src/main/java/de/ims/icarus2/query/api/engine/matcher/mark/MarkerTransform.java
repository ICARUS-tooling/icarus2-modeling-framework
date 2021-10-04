/**
 *
 */
package de.ims.icarus2.query.api.engine.matcher.mark;

import static de.ims.icarus2.util.IcarusUtils.DO_NOTHING;
import static de.ims.icarus2.util.IcarusUtils.UNSET_BYTE;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.list;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.query.api.exp.EvaluationUtils;
import de.ims.icarus2.query.api.iql.IqlMarker;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerExpression;
import de.ims.icarus2.query.api.iql.IqlMarker.MarkerExpressionType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

public class MarkerTransform {
	public static class MarkerSetup {
		public final IqlMarker horizontalMarker, generationMarker, levelMarker;

		public MarkerSetup(IqlMarker generationMarker, IqlMarker horizontalMarker,
				IqlMarker levelMarker) {
			this.horizontalMarker = horizontalMarker;
			this.generationMarker = generationMarker;
			this.levelMarker = levelMarker;
		}
	}

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
			expAction.accept(exp);
			exp.getItems().forEach(m -> traverse(m, callAction, expAction));
		} break;

		default:
			throw EvaluationUtils.forUnsupportedQueryFragment("marker", marker.getType());
		}
	}

	private final List<IqlMarkerCall> calls = new ObjectArrayList<>();
	private final Reference2IntMap<IqlMarkerCall> callIds = new Reference2IntOpenHashMap<>();
	private boolean hasDisjunction, hasGen, hasLvl, hasSeq;

	private static @Nullable IqlMarker _and(List<IqlMarkerCall> calls) {
		if(calls.isEmpty()) {
			return null;
		}
		return calls.size()==1 ? calls.get(0) : IqlMarkerExpression.and(calls);
	}

	public List<MarkerSetup> apply(IqlMarker marker) {

		traverse(marker, call -> {
			String name = call.getName();
			if(LevelMarker.isValidName(name)) {
				hasLvl = true;
			} else if(GenerationMarker.isValidName(name)) {
				hasGen = true;
			} else {
				hasSeq = true;
			}
		}, DO_NOTHING());

		final List<MarkerSetup> setups;

		// Simple options
		if(hasGen && !hasLvl && !hasSeq) {
			setups = list(new MarkerSetup(marker, null, null));
		} else if(hasSeq && !hasGen && !hasLvl) {
			setups = list(new MarkerSetup(null, marker, null));
		} else if(hasLvl && !hasGen && !hasSeq) {
			setups = list(new MarkerSetup(null, null, marker));
		} else {

			/*
			 * Transform marker expression into disjunctive normal form.
			 * Since we do _not_ have negation with markers, this is
			 * straightforward.
			 */

			// Convert to an easier to use term hierarchy
			Term root = toTerm(marker);
			// If required, actually transform the expression
			final boolean split = hasDisjunction && hasGen;
			if(split) {
				root = toDNF(root);
				assert root.type==OR : "DNF conversion failed, root term is no disjunction";
			}
			// Finally generate a dedicated setup per disjunctive branch
			setups = asSetups(root, split);
		}

		// Cleanup state
		calls.clear();
		callIds.clear();
		hasDisjunction = false;
		hasGen = hasLvl = hasSeq = false;

		return setups;
	}

	private final static byte CALL = 0;
	private final static byte AND = 1;
	private final static byte OR = 2;

	private final static byte GEN = 0;
	private final static byte LVL = 1;
	private final static byte SEQ = 2;

	private static class Term {
		private final byte type;
		/** Actual elements to be joined by boolean connective if type is AND or OR */
		private List<Term> elements;
		/** Id of marker call if type==CALL */
		private int callId = UNSET_INT;
		/** Type of marker call, if term type is CALL */
		private byte callType = UNSET_BYTE;

		Term(byte type) { this.type = type; }

		private List<Term> elements () {
			if(elements == null) elements = new ObjectArrayList<>();
			return elements;
		}
	}

	private Term toTerm(IqlMarker marker) {
		switch (marker.getType()) {
		case MARKER_CALL: {
			IqlMarkerCall call = (IqlMarkerCall) marker;
			final int callId = calls.size();
			callIds.put(call, callId);
			calls.add(call);
			Term term = new Term(CALL);
			term.callId = callId;

			String name = call.getName();
			if(LevelMarker.isValidName(name)) {
				term.callType = LVL;
			} else if(GenerationMarker.isValidName(name)) {
				term.callType = GEN;
			} else {
				term.callType = SEQ;
			}
			return term;
		}

		case MARKER_EXPRESSION: {
			IqlMarkerExpression exp = (IqlMarkerExpression) marker;
			boolean isDisjunction = exp.getExpressionType()==MarkerExpressionType.DISJUNCTION;
			Term term = new Term(isDisjunction ? OR : AND);
			exp.getItems().stream()
					.map(this::toTerm)
					.forEach(term.elements()::add);
			hasDisjunction |= isDisjunction;
			return term;
		}

		default:
			throw EvaluationUtils.forUnsupportedQueryFragment("marker", marker.getType());
		}
	}

	private Term toDNF(Term term) {
		if(term.type==CALL) {
			return term;
		}

		//TODO

		throw new UnsupportedOperationException();
	}

	private List<MarkerSetup> asSetups(Term root, boolean split) {
		Collection<Term> terms = split ? root.elements : Collections.singleton(root);

		final List<MarkerSetup> setups = new ObjectArrayList<>();

		List<IqlMarkerCall> genMarkers = new ObjectArrayList<>();
		List<IqlMarkerCall> seqMarkers = new ObjectArrayList<>();
		List<IqlMarkerCall> lvlMarkers = new ObjectArrayList<>();

		for(Term term : terms) {
			assert term.type!=OR || (!split && term==root) : "cannot create marker setup from disjunctive term";

			Collection<Term> elements = term.elements;

			if(term.type==CALL) {
				elements = Collections.singleton(term);
			}

			for (Term element : elements) {
				IqlMarkerCall call = calls.get(element.callId);
				switch (element.callType) {
				case GEN: genMarkers.add(call); break;
				case LVL: lvlMarkers.add(call); break;
				case SEQ: seqMarkers.add(call); break;

				default:
					break;
				}
			}

			setups.add(new MarkerSetup(_and(genMarkers), _and(seqMarkers), _and(lvlMarkers)));

			genMarkers.clear();
			lvlMarkers.clear();
			seqMarkers.clear();
		}

		return setups;
	}
}