/**
 *
 */
package de.ims.icarus2.query.api.engine.ext;

import javax.annotation.Nullable;

import de.ims.icarus2.query.api.eval.Environment;

/**
 * @author Markus Gärtner
 *
 */
public interface EngineConfigurator {

	/**
	 * Set or override the specified property with a new value.
	 * Using a {@code null} value will remove the property from
	 * the configuration.
	 *
	 * @param key
	 * @param value
	 */
	EngineConfigurator setProperty(String key, @Nullable Object value);

	/**
	 * Switch on or off the specified switch.
	 * @param name
	 * @param active
	 */
	EngineConfigurator setSwitch(String name, boolean active);

	/**
	 * Adds a new environment.
	 *
	 * @param environment
	 */
	EngineConfigurator registerEnvironment(Environment environment);
}
