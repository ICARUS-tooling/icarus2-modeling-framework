/**
 *
 */
package de.ims.icarus2.query.api.iql;

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
public abstract class IqlUnique implements IqlQueryElement {

	@JsonProperty(IqlConstants.ID_PROPERTY)
	public String id;
}
