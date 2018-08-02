/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import java.util.function.Function;

import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;

/**
 * @author Markus Gärtner
 *
 */
class RelationTest implements StringResourceTest<Relation> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createStringResources()
	 */
	@Override
	public Relation[] createStringResources() {
		return Relation.values();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createParser()
	 */
	@Override
	public Function<String, Relation> createParser() {
		return Relation::parseRelation;
	}

}
