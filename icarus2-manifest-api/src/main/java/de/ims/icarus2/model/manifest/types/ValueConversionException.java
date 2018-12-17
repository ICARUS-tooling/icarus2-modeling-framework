/**
 *
 */
package de.ims.icarus2.model.manifest.types;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.model.manifest.ManifestErrorCode;

/**
 * Special exception class for the {@link ManifestErrorCode#MANIFEST_TYPE_CAST}
 * error type.
 * <p>
 * This is a <i>checked exception</i> class.
 *
 * @author Markus Gärtner
 *
 */
public class ValueConversionException extends IcarusApiException {
	private static final long serialVersionUID = 9078565278093991307L;

	private final ValueType type;
	private final Object data;
	private final boolean serialization;

	/**
	 * @param type
	 * @param data
	 * @param serialization
	 */
	public ValueConversionException(String message, Throwable cause,
			ValueType type, Object data, boolean serialization) {
		super(ManifestErrorCode.MANIFEST_TYPE_CAST, message, cause);
		this.type = requireNonNull(type);
		this.data = requireNonNull(data);
		this.serialization = serialization;
	}

	public ValueType getType() {
		return type;
	}

	/**
	 * Returns either the object to be serialized or the {@link CharSequence} to be parsed.
	 * <p>
	 * Which of the two can be expected is dendent on the return value of {@link #isSerialization()}!
	 *
	 * @return
	 */
	public Object getData() {
		return data;
	}

	public boolean isSerialization() {
		return serialization;
	}
}
