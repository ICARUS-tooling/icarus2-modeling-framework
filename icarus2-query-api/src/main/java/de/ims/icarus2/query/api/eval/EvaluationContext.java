/**
 *
 */
package de.ims.icarus2.query.api.eval;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class EvaluationContext implements Namespace {

	/** Maps the usable raw names or aliases to corpus entries. */
	private final Map<String, NsEntry> corpora = new Object2ObjectOpenHashMap<>();

	/** Maps the usable raw names or aliases to layer entries. */
	private final Map<String, NsEntry> layers = new Object2ObjectOpenHashMap<>();

}
