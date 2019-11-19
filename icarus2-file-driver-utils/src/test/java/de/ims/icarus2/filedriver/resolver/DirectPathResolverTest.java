/**
 *
 */
package de.ims.icarus2.filedriver.resolver;

import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.io.ResourcePath;
import de.ims.icarus2.model.manifest.api.LocationType;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class DirectPathResolverTest implements ApiGuardedTest<DirectPathResolver> {

	//TODO add tests for the forManifest() method

	@Override
	public Class<?> getTestTargetClass() {
		return DirectPathResolver.class;
	}

	@Override
	public DirectPathResolver createTestInstance(TestSettings settings) {
		return settings.process(new DirectPathResolver("test"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.resolver.DirectPathResolver#DirectPathResolver(java.util.List)}.
	 */
	@Test
	void testDirectPathResolverListOfString() {
		List<String> paths = list("path1", "path2", "path3");
		DirectPathResolver resolver = new DirectPathResolver(paths);
		assertThat(resolver.getPathCount()).isEqualTo(paths.size());
		for (int i = 0; i < paths.size(); i++) {
			ResourcePath path = resolver.getPath(i);
			assertThat(path).isNotNull();
			assertThat(path.getType()).isSameAs(LocationType.LOCAL);
			assertThat(path.getPath()).isEqualTo(paths.get(i));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.resolver.DirectPathResolver#DirectPathResolver(java.lang.String[])}.
	 */
	@Test
	void testDirectPathResolverStringArray() {
		String[] paths = {"path1", "path2", "path3"};
		DirectPathResolver resolver = new DirectPathResolver(paths);
		assertThat(resolver.getPathCount()).isEqualTo(paths.length);
		for (int i = 0; i < paths.length; i++) {
			ResourcePath path = resolver.getPath(i);
			assertThat(path).isNotNull();
			assertThat(path.getType()).isSameAs(LocationType.LOCAL);
			assertThat(path.getPath()).isEqualTo(paths[i]);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.resolver.DirectPathResolver#close()}.
	 */
	@Test
	void testClose() {
		create().close();
	}

}
