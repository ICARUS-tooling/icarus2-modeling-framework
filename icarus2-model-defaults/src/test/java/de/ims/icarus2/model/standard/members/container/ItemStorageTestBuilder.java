/**
 *
 */
package de.ims.icarus2.model.standard.members.container;

import java.util.function.Supplier;

import de.ims.icarus2.test.annotations.TestBuilder;

/**
 * @author Markus Gärtner
 *
 */
@TestBuilder(ItemStorage.class)
public class ItemStorageTestBuilder<S extends ItemStorage> {

	/** Used to produce fresh storage object for every test invocation */
	private Supplier<S> storageGen;

	private boolean requireHandler = false;
}
