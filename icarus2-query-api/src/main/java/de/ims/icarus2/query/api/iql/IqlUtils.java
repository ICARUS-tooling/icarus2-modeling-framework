/**
 *
 */
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

/**
 * @author Markus Gärtner
 *
 */
public final class IqlUtils {

	public static ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jdk8Module()); // Properly handles java.util.Optional
		//TODO apply default configuration for IQL
		return mapper;
	}
}
