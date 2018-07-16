/**
 *
 */
package de.ims.icarus2;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Proxy class that only holds the map for looking up error codes.
 *
 * @author Markus
 *
 */
final class ErrorCodeLookup {


	static final Int2ObjectMap<ErrorCode> map = new Int2ObjectOpenHashMap<>();
}
