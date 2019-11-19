/**
 *
 */
package de.ims.icarus2.filedriver.schema.resolve.common;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItems;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getTestValues;
import static de.ims.icarus2.test.util.Pair.pair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.Converter.ReadMode;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.filedriver.schema.resolve.ResolverOptions;
import de.ims.icarus2.filedriver.schema.resolve.ResolverTest;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationFlag;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
class PropertyListResolverTest implements ResolverTest<PropertyListResolver> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return PropertyListResolver.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public PropertyListResolver createTestInstance(TestSettings settings) {
		return settings.process(new PropertyListResolver());
	}

	@SafeVarargs
	@SuppressWarnings("boxing")
	private final Options createOptions(boolean allowUnknownKeys,
			Pair<String, ValueType>...keys) {
		AnnotationLayerManifest manifest = mock(AnnotationLayerManifest.class);
		when(manifest.isAnnotationFlagSet(AnnotationFlag.UNKNOWN_KEYS)).thenReturn(allowUnknownKeys);

		if(keys.length>0) {
			Set<String> availableKeys = new HashSet<>();
			for(Pair<String, ValueType> entry : keys) {
				assertThat(availableKeys.add(entry.first)).isTrue();
				AnnotationManifest am = mock(AnnotationManifest.class);
				when(am.getKey()).thenReturn(Optional.of(entry.first));
				when(am.getValueType()).thenReturn(entry.second);
				when(manifest.getAnnotationManifest(entry.first)).thenReturn(Optional.of(am));
			}
			when(manifest.getAvailableKeys()).thenReturn(availableKeys);
		} else {
			when(manifest.getAvailableKeys()).thenReturn(Collections.emptySet());
		}

		AnnotationStorage storage = mock(AnnotationStorage.class);

		AnnotationLayer layer = mock(AnnotationLayer.class);
		when(layer.getManifest()).thenReturn(manifest);
		when(layer.getAnnotationStorage()).thenReturn(storage);

		return Options.of(ResolverOptions.LAYER, layer);
	}

	@Override
	public Options defaultOptions() {
		return createOptions(false);
	}

	@TestFactory
	Stream<DynamicNode> testProcess() {
		return ValueType.serializableValueTypes().stream().map(type -> dynamicTest(type.getName(), () -> {
			PropertyListResolver resolver = create();
			String key = "key1";

			resolver.prepareForReading(mock(Converter.class), ReadMode.SCAN, mock(Function.class),
					createOptions(false, pair(key, type)));

			Object[] values = getTestValues(type);
			Item[] items = mockItems(values.length);
			AnnotationStorage storage = resolver.getAnnotationLayer().getAnnotationStorage();

			for (int i = 0; i < items.length; i++) {
				String data = key+resolver.getAssignmentSymbol()+type.toChars(values[i]);
				ResolverContext context = mock(ResolverContext.class);
				Item item = items[i];
				when(context.currentItem()).thenReturn(item);
				when(context.rawData()).thenReturn(data);
				assertThat(resolver.process(context)).isSameAs(item);

				if(type==ValueType.STRING)
					verify(storage).setValue(eq(item), eq(key), eq(values[i]));
			}

			resolver.close();
		}));
	}

	@Test
	void testUnknownKey() throws Exception {
		PropertyListResolver resolver = create();

		resolver.prepareForReading(mock(Converter.class), ReadMode.SCAN, mock(Function.class),
				createOptions(true));

		String key = "key1";
		String value = "value1234";
		Item item = mockItem();
		AnnotationStorage storage = resolver.getAnnotationLayer().getAnnotationStorage();

		ResolverContext context = mock(ResolverContext.class);
		when(context.rawData()).thenReturn(key+resolver.getAssignmentSymbol()+value);
		when(context.currentItem()).thenReturn(item);

		resolver.process(context);

		verify(storage).setValue(item, key, value);

		resolver.close();
	}

	@Test
	void testBatchAssignments() throws Exception {
		PropertyListResolver resolver = create();

		resolver.prepareForReading(mock(Converter.class), ReadMode.SCAN, mock(Function.class),
				createOptions(true));

		String[] keys = {"key1", "key2"};
		String[] values = {"value1234", "dhaghjagfzug"};
		Item item = mockItem();
		AnnotationStorage storage = resolver.getAnnotationLayer().getAnnotationStorage();

		ResolverContext context = mock(ResolverContext.class);
		when(context.rawData()).thenReturn(
				keys[0]+resolver.getAssignmentSymbol()+values[0]
				+ resolver.getSeparator()
				+ keys[1]+resolver.getAssignmentSymbol()+values[1]);
		when(context.currentItem()).thenReturn(item);

		resolver.process(context);

		for (int i = 0; i < values.length; i++) {
			verify(storage).setValue(item, keys[i], values[i]);
		}

		resolver.close();
	}

	@Test
	void testMissingAssignmentSymbol() {
		PropertyListResolver resolver = create();

		resolver.prepareForReading(mock(Converter.class), ReadMode.SCAN, mock(Function.class),
				createOptions(true));

		ResolverContext context = mock(ResolverContext.class);
		when(context.rawData()).thenReturn("test123");
		assertModelException(ModelErrorCode.DRIVER_INVALID_CONTENT,
				() -> resolver.process(context));

		resolver.close();
	}

	@Test
	void testMissingLayer() {
		PropertyListResolver resolver = create();

		assertModelException(GlobalErrorCode.INVALID_INPUT,
				() -> resolver.prepareForReading(mock(Converter.class),
						ReadMode.SCAN, mock(Function.class),
						Options.none()));
	}

	@Test
	void testNoHandler() {
		PropertyListResolver resolver = create();

		resolver.prepareForReading(mock(Converter.class), ReadMode.SCAN, mock(Function.class),
				createOptions(false));

		ResolverContext context = mock(ResolverContext.class);
		when(context.rawData()).thenReturn("key"+resolver.getAssignmentSymbol()+"value");
		assertModelException(ModelErrorCode.DRIVER_INVALID_CONTENT,
				() -> resolver.process(context));

		resolver.close();
	}
}
