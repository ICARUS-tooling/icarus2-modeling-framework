/**
 *
 */
package de.ims.icarus2.model.standard.driver.virtual;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemListTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class DefaultItemListTest implements ItemListTest<DefaultItemList> {

	@Override
	public DefaultItemList createFilled(Item... items) {
		return new DefaultItemList(Arrays.asList(items));
	}

	@Override
	public Class<?> getTestTargetClass() {
		return DefaultItemList.class;
	}

	@Override
	public DefaultItemList createTestInstance(TestSettings settings) {
		return settings.process(new DefaultItemList());
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.virtual.DefaultItemList#DefaultItemList()}.
		 */
		@Test
		void testDefaultItemList() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.virtual.DefaultItemList#DefaultItemList(java.util.Collection)}.
		 */
		@Test
		void testDefaultItemListCollectionOfQextendsItem() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.virtual.DefaultItemList#DefaultItemList(int)}.
		 */
		@Test
		void testDefaultItemListInt() {
			fail("Not yet implemented"); // TODO
		}

	}

}
