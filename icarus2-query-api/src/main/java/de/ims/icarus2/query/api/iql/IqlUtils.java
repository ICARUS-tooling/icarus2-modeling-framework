/**
 *
 */
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Markus Gärtner
 *
 */
public final class IqlUtils {

	public static ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		//TODO apply default configuration for IQL
		return mapper;
	}
}
