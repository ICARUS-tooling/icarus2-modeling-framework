/**
 *
 */
package de.ims.icarus2.query.api.engine.matcher.mark;

import static de.ims.icarus2.util.IcarusUtils.DO_NOTHING;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.query.api.exp.EvaluationUtils;
import de.ims.icarus2.query.api.iql.IqlMarker;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerExpression;
import de.ims.icarus2.query.api.iql.IqlMarker.MarkerExpressionType;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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

	private int markerCount, genMarkerCount, levelMarkerCount;
	private boolean hasDisjunction;
	/** Disjunctive sections of the marker construct */
	private final List<MarkerSetup> setups = new ObjectArrayList<>();

	public List<MarkerSetup> getSetups() { return CollectionUtils.unmodifiableListProxy(setups); }

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

	private void checkMarker(IqlMarker marker) {
		Consumer<? super IqlMarkerCall> callAction = call -> {
			String name = call.getName();
			if(LevelMarker.isValidName(name)) {
				levelMarkerCount++;
			} else if(GenerationMarker.isValidName(name)) {
				genMarkerCount++;
			}
			markerCount++;
		};
		Consumer<? super IqlMarkerExpression> expAction = exp -> {
			if(exp.getExpressionType()==MarkerExpressionType.DISJUNCTION) {
				hasDisjunction = true;
			}
		};

		traverse(marker, callAction, expAction);
	}

	public void reset() {
		markerCount = genMarkerCount = 0;
		hasDisjunction = false;
		setups.clear();
	}

	private static @Nullable IqlMarker _and(List<IqlMarkerCall> calls) {
		if(calls.isEmpty()) {
			return null;
		}
		return calls.size()==1 ? calls.get(0) : IqlMarkerExpression.and(calls);
	}

	public void apply(IqlMarker marker) {
		reset();
		checkMarker(marker);

		assert markerCount>0 : "Empty marker construct";

		// No generation or level markers
		if(genMarkerCount==0 && levelMarkerCount==0) {
			setups.add(new MarkerTransform.MarkerSetup(null, marker, null));
			return;
		}
		// Only generation markers
		else if(markerCount==genMarkerCount && levelMarkerCount==0) {
			setups.add(new MarkerTransform.MarkerSetup(marker, null, null));
			return;
		}
		// Only level markers
		else if(markerCount==levelMarkerCount && genMarkerCount==0) {
			setups.add(new MarkerTransform.MarkerSetup(null, null, marker));
			return;
		}
		// Fully conjunctive mix
		else if(!hasDisjunction) {
			List<IqlMarkerCall> genMarkers = new ObjectArrayList<>();
			List<IqlMarkerCall> horMarkers = new ObjectArrayList<>();
			List<IqlMarkerCall> levelMarkers = new ObjectArrayList<>();
			traverse(marker, call -> {
				String name = call.getName();
				if(LevelMarker.isValidName(name)) {
					levelMarkers.add(call);
				} else if(GenerationMarker.isValidName(name)) {
					genMarkers.add(call);
				} else {
					horMarkers.add(call);
				}
			}, DO_NOTHING());

			setups.add(new MarkerTransform.MarkerSetup(_and(genMarkers), _and(horMarkers), _and(levelMarkers)));
		}
		//TODO split marker construct and assign horizontalMarker and generationMarker sections
		else {
			/*
			 * Transform marker expression into disjunctive normal form.
			 * Since we do _not_ have negation with markers, this is
			 * straightforward:
			 */

			throw new UnsupportedOperationException("not implemented yet");
		}
	}
}