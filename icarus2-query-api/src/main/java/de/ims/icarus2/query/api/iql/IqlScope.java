/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class IqlScope extends IqlAliasedReference {

	/**
	 * Indicates that the primary layer of this scope is meant to be
	 * used as a primary layer in the query result.
	 */
	@JsonProperty(IqlProperties.PRIMARY)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean primary = false;

	/**
	 * Defines the members of this scope.
	 */
	@JsonProperty(IqlProperties.LAYERS)
	private List<IqlLayer> layers = new ArrayList<>();

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.SCOPE;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlAliasedReference#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkCollectionNotEmpty(layers, IqlProperties.LAYERS);
	}

	public boolean isPrimary() { return primary; }

	public List<IqlLayer> getLayers() { return CollectionUtils.unmodifiableListProxy(layers); }

	public void setPrimary(boolean primary) { this.primary = primary; }

	public void addLayer(IqlLayer layer) { layers.add(requireNonNull(layer)); }

	public void forEachLayer(Consumer<? super IqlLayer> action) { layers.forEach(action); }
}
