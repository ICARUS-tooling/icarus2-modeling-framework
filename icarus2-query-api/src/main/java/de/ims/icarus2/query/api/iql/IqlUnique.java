/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * @author Markus Gärtner
 *
 */
@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = IqlConstants.ID_PROPERTY
)
public abstract class IqlUnique extends AbstractIqlQueryElement {

	@JsonProperty(IqlConstants.ID_PROPERTY)
	private String id;

	public String getId() { return id; }

	public void setId(String id) { this.id = checkNotEmpty(id); }

	/**
	 * @see de.ims.icarus2.query.api.iql.AbstractIqlQueryElement#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(id, IqlConstants.ID_PROPERTY);
	}
}
