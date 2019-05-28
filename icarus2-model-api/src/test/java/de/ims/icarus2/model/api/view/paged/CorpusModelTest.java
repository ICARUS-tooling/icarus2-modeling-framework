/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package de.ims.icarus2.model.api.view.paged;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockPosition;
import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;
import static de.ims.icarus2.model.api.ModelTestUtils.mockUsableContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockUsableEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockUsableFragment;
import static de.ims.icarus2.model.api.ModelTestUtils.mockUsableItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockUsableStructure;
import static de.ims.icarus2.model.api.ModelTestUtils.range;
import static de.ims.icarus2.model.api.ModelTestUtils.stubEdgeCount;
import static de.ims.icarus2.model.api.ModelTestUtils.stubFlags;
import static de.ims.icarus2.model.api.ModelTestUtils.stubItemCount;
import static de.ims.icarus2.model.api.ModelTestUtils.stubOffsets;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.wrap;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.MANIFEST_FACTORY;
import static de.ims.icarus2.test.TestUtils.DO_NOTHING;
import static de.ims.icarus2.test.TestUtils.assertCollectionNotEmpty;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.IcarusUtils.DO_NOTHING;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.edit.change.AtomicChange;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ManifestBuilder;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.ChangeableTest;
import de.ims.icarus2.util.Mutable.MutableObject;
import de.ims.icarus2.util.PartTest;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
public interface CorpusModelTest<M extends CorpusModel>
		extends PartTest<PagedCorpusView, M>, ChangeableTest<M> {

	/**
	 * Returns the non-empty set of {@link AccessMode}s that the
	 * model implementation under test is expected to support.
	 */
	Set<AccessMode> getSupportedAccessModes();

	/**
	 * @see de.ims.icarus2.util.PartTest#createEnvironment()
	 */
	@Provider
	@Override
	default PagedCorpusView createEnvironment() {
		ItemLayerManager itemLayerManager = mock(ItemLayerManager.class);
		IndexSet[] indices = wrap(range(0, 9));

		return createView(AccessMode.READ_WRITE, itemLayerManager, 10, indices);
	}

	@Provider
	PagedCorpusView createView(AccessMode accessMode, ItemLayerManager itemLayerManager,
			int pageSize, IndexSet...indices);

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Provider
	@Override
	default M createTestInstance(TestSettings settings) {
		return settings.process(createModel(createEnvironment(),
				mock(ItemLayerManager.class), DO_NOTHING()));
	}

	@Provider
	M createModel(PagedCorpusView view, ItemLayerManager itemLayerManager,
			Consumer<AtomicChange> changeHandler);

	/**
	 * Creates a flat corpus with the following layers:
	 * <table border="1">
	 * <tr><th>Name</th><th>Type</th><th>Base-Layer(s)</th><th>Boundary-Layer(s)</th></tr>
	 * <tr><td>token</td><td>{@link ItemLayer}</td><td>-</td><td>-</td></tr>
	 * <tr><td>sentence</td><td>{@link ItemLayer}</td><td>token</td><td>-</td></tr>
	 * <tr><td>structure</td><td>{@link StructureLayer}</td><td>token</td><td>sentence</td></tr>
	 * <tr><td>annotation</td><td>{@link AnnotationLayer}</td><td>token</td><td></td></tr>
	 * </table>
	 *
	 * The {@code annotation} layer contains an {@link AnnotationManifest} for every of
	 * the basic {@link ValueType value types}.
	 * @return
	 */
	default CorpusManifest createDefaultCorpusManifest() {

		try(ManifestBuilder builder = new ManifestBuilder(MANIFEST_FACTORY)) {
			return builder.create(CorpusManifest.class, "corpus")
					.addRootContextManifest(builder.create(ContextManifest.class, "context", "corpus")
							.setPrimaryLayerId("token")
							.setDriverManifest(builder.create(DriverManifest.class, "driver", "context")
									.setImplementationManifest(builder.live(Driver.class)))
							.addLayerGroup(builder.create(LayerGroupManifest.class, "group", "context")
									.setPrimaryLayerId("token")
									.setIndependent(true)
									// Logical structure
									.addLayerManifest(builder.create(ItemLayerManifest.class, "token", "group"))
									.addLayerManifest(builder.create(ItemLayerManifest.class, "ref", "group"))
									.addLayerManifest(builder.create(ItemLayerManifest.class, "sentence", "group")
											.addBaseLayerId("token", DO_NOTHING)
											.setContainerHierarchy(builder.containers()
													.add(builder.createInternal(ContainerManifest.class, "sentence"))))
									// Structure
									.addLayerManifest(builder.create(StructureLayerManifest.class, "structure", "group")
											.addBaseLayerId("token", DO_NOTHING)
											.setBoundaryLayerId("sentence", DO_NOTHING)
											.setContainerHierarchy(builder.containers()
													.add(builder.createInternal(StructureManifest.class, "structure"))))
									// Annotations
									.addLayerManifest(builder.create(AnnotationLayerManifest.class, "annotation", "group")
											.addBaseLayerId("token", DO_NOTHING)
											.addReferenceLayerId("ref", DO_NOTHING)
											.setDefaultKey("string")
											// String
											.addAnnotationManifest(builder.create(AnnotationManifest.class)
													.setKey("string").setValueType(ValueType.STRING))
											// Integer
											.addAnnotationManifest(builder.create(AnnotationManifest.class)
													.setKey("integet").setValueType(ValueType.INTEGER))
											// Long
											.addAnnotationManifest(builder.create(AnnotationManifest.class)
													.setKey("long").setValueType(ValueType.LONG))
											// Float
											.addAnnotationManifest(builder.create(AnnotationManifest.class)
													.setKey("float").setValueType(ValueType.FLOAT))
											// Double
											.addAnnotationManifest(builder.create(AnnotationManifest.class)
													.setKey("double").setValueType(ValueType.DOUBLE))
											// Boolean
											.addAnnotationManifest(builder.create(AnnotationManifest.class)
													.setKey("boolean").setValueType(ValueType.BOOLEAN))
											// Custom
											.addAnnotationManifest(builder.create(AnnotationManifest.class)
													.setKey("object").setValueType(ValueType.CUSTOM))
											// Ref
											.addAnnotationManifest(builder.create(AnnotationManifest.class)
													.setKey("ref").setValueType(ValueType.REF))

											)));
		}
	}

	/**
	 * Constructs a mock of {@link CorpusManager} that allows testing creation of live corpora
	 * based on the specified {@link CorpusManifest}.
	 *
	 * @param corpusManifest
	 * @return
	 */
	@SuppressWarnings("boxing")
	default CorpusManager createDefaultCorpusManager(CorpusManifest corpusManifest) {

		CorpusManager corpusManager = mock(CorpusManager.class);
		when(corpusManager.isCorpusConnected(eq(corpusManifest))).thenReturn(Boolean.TRUE);
		when(corpusManager.isCorpusEnabled(eq(corpusManifest))).thenReturn(Boolean.TRUE);
		when(corpusManager.getImplementationClassLoader(any())).thenReturn(getClass().getClassLoader());

		return corpusManager;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isModelEditable()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsModelEditable() {
		Set<AccessMode> supportedModes = getSupportedAccessModes();
		assertCollectionNotEmpty(supportedModes);

		return supportedModes.stream()
				.map(accessMode -> dynamicTest(accessMode.name(),
						() -> {
							ItemLayerManager itemLayerManager = mock(ItemLayerManager.class);
							M model = createModel(createView(accessMode,
									itemLayerManager, 10), itemLayerManager, DO_NOTHING());
							assertEquals(Boolean.valueOf(accessMode.isWrite()),
									Boolean.valueOf(model.isModelEditable()));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isModelComplete()}.
	 */
	@SuppressWarnings("resource")
	@TestFactory
	default List<DynamicTest> testIsModelComplete() {
		return Arrays.asList(
				dynamicTest("pageSize<size", () -> {
					ItemLayerManager itemLayerManager = mock(ItemLayerManager.class);
					PagedCorpusView view = createView(AccessMode.READ_WRITE,
							itemLayerManager, 10, range(0, 19));
					M model = createModel(view, itemLayerManager, DO_NOTHING());
					assertFalse(model.isModelComplete());
				}),
				dynamicTest("pageSize==size", () -> {
					ItemLayerManager itemLayerManager = mock(ItemLayerManager.class);
					PagedCorpusView view = createView(AccessMode.READ_WRITE,
							itemLayerManager, 10, range(0, 9));
					M model = createModel(view, itemLayerManager, DO_NOTHING());
					assertTrue(model.isModelComplete());
				}),
				dynamicTest("pageSize>size", () -> {
					ItemLayerManager itemLayerManager = mock(ItemLayerManager.class);
					PagedCorpusView view = createView(AccessMode.READ_WRITE,
							itemLayerManager, 20, range(0, 9));
					M model = createModel(view, itemLayerManager, DO_NOTHING());
					assertTrue(model.isModelComplete());
				})
		);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isModelActive()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testIsModelActive() {
		@SuppressWarnings("resource")
		PagedCorpusView view = mock(PagedCorpusView.class);
		when(view.getAccessMode()).thenReturn(AccessMode.READ_WRITE);
		Corpus corpus = mock(Corpus.class); // needed for listener registration
		when(view.getCorpus()).thenReturn(corpus);

		M model = createModel(view, mock(ItemLayerManager.class), DO_NOTHING());

		when(view.isActive()).thenReturn(Boolean.FALSE);
		assertEquals(view.isActive(), model.isModelActive());

		when(view.isActive()).thenReturn(Boolean.TRUE);
		assertEquals(view.isActive(), model.isModelActive());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getCorpus()}.
	 */
	@Test
	default void testGetCorpus() {
		try(PagedCorpusView view = createEnvironment()) {
			M model = createModel(view, mock(ItemLayerManager.class), DO_NOTHING());
			assertSame(view.getCorpus(), model.getCorpus());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getView()}.
	 */
	@Test
	default void testGetView() {
		try(PagedCorpusView view = createEnvironment()) {
			M model = createModel(view, mock(ItemLayerManager.class), DO_NOTHING());
			assertSame(view, model.getView());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getMemberType(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetMemberType() {
		M model = create();

		return Stream.of(MemberType.values())
				.map(type -> dynamicTest(type.name(), () -> {
					CorpusMember member = mock(CorpusMember.class);
					when(member.getMemberType()).thenReturn(type);
					// No reason to test for exceptions, cause accessing member type is always allowed
					assertSame(type, model.getMemberType(member));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isItem(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsItem() {
		M model = create();

		return Stream.of(MemberType.values())
				.map(type -> dynamicTest(type.name(), () -> {
					CorpusMember member = mock(CorpusMember.class);
					when(member.getMemberType()).thenReturn(type);

					if(type==MemberType.ITEM) {
						assertTrue(model.isItem(member));
					} else {
						assertFalse(model.isItem(member));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isContainer(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsContainer() {
		M model = create();

		return Stream.of(MemberType.values())
				.map(type -> dynamicTest(type.name(), () -> {
					CorpusMember member = mock(CorpusMember.class);
					when(member.getMemberType()).thenReturn(type);

					if(type==MemberType.CONTAINER) {
						assertTrue(model.isContainer(member));
					} else {
						assertFalse(model.isContainer(member));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isStructure(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsStructure() {
		M model = create();

		return Stream.of(MemberType.values())
				.map(type -> dynamicTest(type.name(), () -> {
					CorpusMember member = mock(CorpusMember.class);
					when(member.getMemberType()).thenReturn(type);

					if(type==MemberType.STRUCTURE) {
						assertTrue(model.isStructure(member));
					} else {
						assertFalse(model.isStructure(member));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isEdge(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsEdge() {
		M model = create();

		return Stream.of(MemberType.values())
				.map(type -> dynamicTest(type.name(), () -> {
					CorpusMember member = mock(CorpusMember.class);
					when(member.getMemberType()).thenReturn(type);

					if(type==MemberType.EDGE) {
						assertTrue(model.isEdge(member));
					} else {
						assertFalse(model.isEdge(member));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isFragment(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsFragment() {
		M model = create();

		return Stream.of(MemberType.values())
				.map(type -> dynamicTest(type.name(), () -> {
					CorpusMember member = mock(CorpusMember.class);
					when(member.getMemberType()).thenReturn(type);

					if(type==MemberType.FRAGMENT) {
						assertTrue(model.isFragment(member));
					} else {
						assertFalse(model.isFragment(member));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isLayer(de.ims.icarus2.model.api.members.CorpusMember)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsLayer() {
		M model = create();

		return Stream.of(MemberType.values())
				.map(type -> dynamicTest(type.name(), () -> {
					CorpusMember member = mock(CorpusMember.class);
					when(member.getMemberType()).thenReturn(type);

					if(type==MemberType.LAYER) {
						assertTrue(model.isLayer(member));
					} else {
						assertFalse(model.isLayer(member));
					}
				}));
	}

	class ModelTest<E> {
			private final CorpusModelTest<?> source;

			private final List<DynamicTest> tests = new ArrayList<>();

			private final Class<E> argumentClass;

			ModelTest(CorpusModelTest<?> source, Class<E> argumentClass) {
				this.source = requireNonNull(source);
				this.argumentClass = requireNonNull(argumentClass);
			}

			public List<DynamicTest> tests() {
				return tests;
			}

			// Model construction

			private CorpusModel model;
			private PagedCorpusView view;
			private ItemLayerManager itemLayerManager;
			private AccessMode accessMode;
			private int pageSize;
			private IndexSet[] indices;
			private Consumer<AtomicChange> changeHandler;

			private boolean preventLoad;

			ModelTest<E> changeHandler(Consumer<AtomicChange> changeHandler) {
				this.changeHandler = requireNonNull(changeHandler);
				return this;
			}

			ModelTest<E> itemLayerManager(ItemLayerManager itemLayerManager) {
				this.itemLayerManager = requireNonNull(itemLayerManager);
				return this;
			}

			ModelTest<E> view(PagedCorpusView view) {
				this.view = requireNonNull(view);
				return this;
			}

			ModelTest<E> accessMode(AccessMode accessMode) {
				this.accessMode = requireNonNull(accessMode);
				return this;
			}

			ModelTest<E> model(CorpusModel model) {
				this.model = requireNonNull(model);
				return this;
			}

			ModelTest<E> pageSize(int pageSize) {
				this.pageSize = pageSize;
				return this;
			}

			ModelTest<E> indices(IndexSet...indices) {
				this.indices = requireNonNull(indices);
				return this;
			}

			private Consumer<ModelTest<E>> setup;

			ModelTest<E> setup(Consumer<ModelTest<E>> setup) {
				this.setup = requireNonNull(setup);
				return this;
			}

			private void setup() {
				// Allow client code to initialize first
				if(setup!=null) {
					setup.accept(this);
				}
				// Now fill everything with default values if required
				if(changeHandler==null) {
					changeHandler = DO_NOTHING();
				}
				if(indices==null) {
					indices = wrap(range(0, 9));
				}
				if(accessMode==null) {
					accessMode = AccessMode.READ_WRITE;
				}
				if(pageSize==UNSET_INT) {
					pageSize = 10;
				}
				if(itemLayerManager==null) {
					itemLayerManager = mock(ItemLayerManager.class);
				}
				if(view==null) {
					view = source.createView(accessMode, itemLayerManager, pageSize, indices);
				}
				if(model==null) {
					model = source.createModel(view, itemLayerManager, changeHandler);
				}
			}

			private void reset() {
				changeHandler = null;
				itemLayerManager = null;
				view = null;
				model = null;
				indices = null;
				accessMode = null;
				pageSize = UNSET_INT;
				preventLoad = false;
			}

			ItemLayerManager itemLayerManager() {
				assertNotNull(itemLayerManager);
				return itemLayerManager;
			}

			PagedCorpusView view() {
				assertNotNull(view);
				return view;
			}

			CorpusModel model() {
				assertNotNull(model);
				return model;
			}

			// test construction

			private BiConsumer<ModelTest<E>, E> action;

			ModelTest<E> action(BiConsumer<ModelTest<E>, E> action) {
				assertNull(this.action);
				this.action = requireNonNull(action);
				return this;
			}

//			ModelTest<E> argumentClass(Class<? extends E> argumentClass) {
//				assertNull(this.argumentClass);
//				this.argumentClass = requireNonNull(argumentClass);
//				return this;
//			}

			// Helpers

			void loadPage() {
				if(!preventLoad) {
					try {
						view.getPageControl().load();
					} catch (InterruptedException e) {
						throw new AssertionError("Failed to load page", e);
					}
				}
			}

			// Actual tests

			private void checkAction() {
				assertNotNull(action);
			}

			@SuppressWarnings("unchecked")
			private <T> T mockArgument() {
				return (T) mock(argumentClass);
			}

			ModelTest<E> testSuccess(Function<ModelTest<E>, E> valueGen) {
				checkAction();
				tests.add(dynamicTest("success", () -> {
							reset();
							setup();
							action.accept(this, valueGen.apply(this));
						}));
				return this;
			}

			ModelTest<E> testSuccess(Class<? extends E> valClass) {
				return testSuccess(t -> mock(valClass));
			}

			ModelTest<E> testSuccess(E value) {
				return testSuccess(t -> value);
			}

			ModelTest<E> testForeignLayerError() {
				checkAction();
				tests.add(dynamicTest("foreign layer [ME]", () -> {
					reset();
					setup();
					assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
							() -> action.accept(this, (E) mock(argumentClass)));
				}));
				return this;
			}

			ModelTest<E> testNotPrimaryLayerError() {
				checkAction();
				tests.add(dynamicTest("not primary layer [ME]", () -> {
					reset();
					setup();
					assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
							() -> action.accept(this, (E) mock(argumentClass)));
				}));
				return this;
			}

			ModelTest<E> testNotReadableError() {
				checkAction();
				tests.add(dynamicTest("not readable [ME]", () -> {
					reset();
					accessMode = AccessMode.WRITE;
					setup();
					assertModelException(ModelErrorCode.MODEL_WRITE_ONLY,
							() -> action.accept(this, (E) mock(argumentClass)));
				}));
				return this;
			}

			ModelTest<E> testNotWritableError() {
				checkAction();
				tests.add(dynamicTest("not writable [ME]", () -> {
					reset();
					accessMode = AccessMode.READ;
					setup();
					assertModelException(ModelErrorCode.MODEL_READ_ONLY,
							() -> action.accept(this, (E) mock(argumentClass)));
				}));
				return this;
			}

			ModelTest<E> testViewEmptyError() {
				checkAction();
				tests.add(dynamicTest("not readable [ME]", () -> {
					reset();
					preventLoad = true;
					setup();
					assertModelException(ModelErrorCode.VIEW_EMPTY,
							() -> action.accept(this, (E) mock(argumentClass)));
				}));
				return this;
			}

			@SuppressWarnings({ "unchecked" })
			ModelTest<E> testDeadItemError() {
				checkAction();
				tests.add(dynamicTest("dead [ME]", () -> {
					reset();
					preventLoad = true;
					setup();
					Item item = stubFlags(mockArgument(), false, true, true);
					assertModelException(ModelErrorCode.MODEL_CORRUPTED_STATE,
							() -> action.accept(this, (E) item));
				}));
				return this;
			}

			@SuppressWarnings({ "unchecked" })
			ModelTest<E> testDirtyItemError() {
				checkAction();
				tests.add(dynamicTest("dirty [ME]", () -> {
					reset();
					preventLoad = true;
					setup();
					Item item = stubFlags(mockArgument(), true, false, true);
					assertModelException(ModelErrorCode.MODEL_CORRUPTED_STATE,
							() -> action.accept(this, (E) item));
				}));
				return this;
			}

			@SuppressWarnings({ "unchecked" })
			ModelTest<E> testLockedItemError() {
				checkAction();
				tests.add(dynamicTest("locked [ME]", () -> {
					reset();
					preventLoad = true;
					setup();
					Item item = stubFlags(mockArgument(), true, true, false);
					assertModelException(ModelErrorCode.MODEL_CORRUPTED_STATE,
							() -> action.accept(this, (E) item));
				}));
				return this;
			}
		}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getSize(de.ims.icarus2.model.api.layer.ItemLayer)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetSize() {
		return new ModelTest<>(this, ItemLayer.class)
				.action((t, layer) -> {
					when(t.itemLayerManager().getItemCount(layer)).thenReturn(Long.valueOf(10));
					assertEquals(10, t.model().getSize(layer));
					verify(t.itemLayerManager()).getItemCount(layer);
				})
				.testSuccess(t -> t.view().getScope().getPrimaryLayer())
				.testForeignLayerError()
				.testNotReadableError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getRootContainer(de.ims.icarus2.model.api.layer.ItemLayer)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetRootContainerItemLayer() {
		return new ModelTest<>(this, ItemLayer.class)
				.action((t, layer) -> {
					t.loadPage();
					assertNotNull(t.model().getRootContainer(layer));
				})
				.testSuccess(t -> t.view().getScope().getPrimaryLayer())
				.testForeignLayerError()
				.testNotPrimaryLayerError()
				.testViewEmptyError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getRootContainer()}.
	 */
	@Test
	default void testGetRootContainer() throws Exception {
		M model = create();
		ItemLayer primaryLayer = model.getView().getScope().getPrimaryLayer();
		model.getView().getPageControl().load();
		assertNotNull(model.getRootContainer());
		assertSame(model.getRootContainer(primaryLayer), model.getRootContainer());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getContainer(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetContainer() {
		Container container = mock(Container.class);
		Item item = mockUsableItem();
		when(item.getContainer()).thenReturn(container);

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					assertSame(container, t.model().getContainer(i));
					verify(item).getContainer();
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getLayer(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetLayer() {
		ItemLayer layer = mock(ItemLayer.class);
		Item item = mockUsableItem();
		when(item.getLayer()).thenReturn(layer);

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					assertSame(layer, t.model().getLayer(i));
					verify(item).getLayer();
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getIndex(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetIndex() {
		long index = random(0, Long.MAX_VALUE);
		Item item = mockUsableItem();
		when(item.getIndex()).thenReturn(index);

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					assertEquals(index, t.model().getIndex(i));
					verify(item).getIndex();
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getBeginOffset(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetBeginOffset() {
		long offset = random(0, Long.MAX_VALUE);
		Item item = mockUsableItem();
		when(item.getBeginOffset()).thenReturn(offset);

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					assertEquals(offset, t.model().getBeginOffset(i));
					verify(item).getBeginOffset();
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getEndOffset(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetEndOffset() {
		long offset = random(0, Long.MAX_VALUE);
		Item item = mockUsableItem();
		when(item.getEndOffset()).thenReturn(offset);

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					assertEquals(offset, t.model().getEndOffset(i));
					verify(item).getEndOffset();
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isVirtual(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default List<DynamicTest> testIsVirtual() {
		Item item = mockUsableItem();

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					stubOffsets(i, 0, 0);
					assertFalse(t.model().isVirtual(i));

					verify(item).getBeginOffset();
					verify(item).getEndOffset();

					// Now check different constellations
					stubOffsets(i, UNSET_LONG, 0);
					assertTrue(t.model().isVirtual(i));

					stubOffsets(i, 0, UNSET_LONG);
					assertTrue(t.model().isVirtual(i));

					stubOffsets(i, UNSET_LONG, UNSET_LONG);
					assertTrue(t.model().isVirtual(i));
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getContainerType(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetContainerType() {
		ContainerType type = random(ContainerType.values());
		Container container = mockUsableContainer();
		when(container.getContainerType()).thenReturn(type);

		return new ModelTest<>(this, Container.class)
				.action((t, c) -> {
					assertSame(type, t.model().getContainerType(c));
					verify(container).getContainerType();
				})
				.testSuccess(container)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getBaseContainers(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetBaseContainers() {
		DataSet<Container> containers = mock(DataSet.class);
		Container container = mockUsableContainer();
		when(container.getBaseContainers()).thenReturn(containers);

		return new ModelTest<>(this, Container.class)
				.action((t, c) -> {
					assertSame(containers, t.model().getBaseContainers(c));
					verify(container).getBaseContainers();
				})
				.testSuccess(container)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getBoundaryContainer(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetBoundaryContainer() {
		Container boundary = mockContainer();
		Container container = mockUsableContainer();
		when(container.getBoundaryContainer()).thenReturn(boundary);

		return new ModelTest<>(this, Container.class)
				.action((t, c) -> {
					assertSame(boundary, t.model().getBoundaryContainer(c));
					verify(container).getBoundaryContainer();
				})
				.testSuccess(container)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getItemCount(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetItemCount() {
		long count = random(0, Long.MAX_VALUE);
		Container container = mockUsableContainer();
		when(container.getItemCount()).thenReturn(count);

		return new ModelTest<>(this, Container.class)
				.action((t, c) -> {
					assertEquals(count, t.model().getItemCount(c));
					verify(container).getItemCount();
				})
				.testSuccess(container)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetItemAt() {
		Item item = mockItem();
		long index = random(0, Long.MAX_VALUE);
		Container container = mockUsableContainer();
		when(container.getItemAt(eq(index))).thenReturn(item);

		return new ModelTest<>(this, Container.class)
				.action((t, c) -> {
					assertSame(item, t.model().getItemAt(c, index));
					verify(container).getItemAt(eq(index));
				})
				.testSuccess(container)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testIndexOfItem() {
		Item item = mockItem();
		long index = random(0, Long.MAX_VALUE);
		Container container = mockUsableContainer();
		when(container.indexOfItem(eq(item))).thenReturn(index);

		return new ModelTest<>(this, Container.class)
				.action((t, c) -> {
					assertEquals(index, t.model().indexOfItem(c, item));
					verify(container).indexOfItem(eq(item));
				})
				.testSuccess(container)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#containsItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testContainsItem() {
		Item item = mockItem();
		Container container = mockUsableContainer();
		when(container.containsItem(item)).thenReturn(Boolean.TRUE);

		return new ModelTest<>(this, Container.class)
				.action((t, c) -> {
					assertTrue(t.model().containsItem(c, item));
					verify(container).containsItem(eq(item));
				})
				.testSuccess(container)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#addItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default List<DynamicTest> testAddItemContainerItem() {
		Item item = mockItem();
		Container container = mockUsableContainer();
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Container.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, c) -> {
					t.model().addItem(c, item);
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(container).addItem(0L, item); // addItem(item) redirects to addItem(index, item)
				})
				.testSuccess(container)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default List<DynamicTest> testAddItemContainerLongItem() {
		Item item = mockItem();
		long size = random(0, Long.MAX_VALUE);
		long index = random(0, size);
		Container container = mockUsableContainer();
		stubItemCount(container, size);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Container.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, c) -> {
					t.model().addItem(c, index, item);
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(container).addItem(index, item);
				})
				.testSuccess(container)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
	 */
	@TestFactory
	default List<DynamicTest> testAddItems() {
		long itemCount = random(0, Long.MAX_VALUE/2);
		long index = random(0, itemCount);
		long size = random(1, Long.MAX_VALUE-itemCount);
		DataSequence<Item> items = mockSequence(size);
		Container container = mockUsableContainer();
		stubItemCount(container, itemCount);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Container.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, c) -> {
					t.model().addItems(c, index, items);
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(container).addItems(index, items);
				})
				.testSuccess(container)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#removeItem(de.ims.icarus2.model.api.members.container.Container, long)}.
	 */
	@TestFactory
	default List<DynamicTest> testRemoveItemContainerLong() {
		long itemCount = random(10, Long.MAX_VALUE/2);
		long index = random(0, itemCount);
		Container container = mockUsableContainer();
		stubItemCount(container, itemCount);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Container.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, c) -> {
					t.model().removeItem(c, index);
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(container).removeItem(index);
				})
				.testSuccess(container)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#removeItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testRemoveItemContainerItem() {
		long itemCount = random(10, Long.MAX_VALUE/2);
		long index = random(0, itemCount);
		Item item = mockItem();
		Container container = mockUsableContainer();
		when(container.indexOfItem(item)).thenReturn(index);
		stubItemCount(container, itemCount);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Container.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, c) -> {
					t.model().removeItem(c, item);
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(container).removeItem(index); // item-based removal redirects to index-based
				})
				.testSuccess(container)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
	 */
	@SuppressWarnings("unchecked")
	@TestFactory
	default List<DynamicTest> testRemoveItems() {
		long itemCount = random(10, Long.MAX_VALUE/2);
		long index0 = random(0, itemCount/2);
		long index1 = random(index0, itemCount);
		Container container = mockUsableContainer();
		stubItemCount(container, itemCount);
		DataSequence<Item> items = mockSequence(index1-index0+1);
		when((DataSequence<Item>)container.removeItems(index0, index1)).thenReturn(items);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Container.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, c) -> {
					t.model().removeItems(c, index0, index1);
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(container).removeItems(index0, index1);
				})
				.testSuccess(container)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
	 */
	@TestFactory
	default List<DynamicTest> testSwapItems() {
		long itemCount = random(10, Long.MAX_VALUE/2);
		long index0 = random(0, itemCount/2);
		long index1 = random(index0, itemCount);
		Container container = mockUsableContainer();
		stubItemCount(container, itemCount);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Container.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, c) -> {
					t.model().swapItems(c, index0, index1);
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(container).swapItems(index0, index1);
				})
				.testSuccess(container)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getStructureType(de.ims.icarus2.model.api.members.structure.Structure)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetStructureType() {
		StructureType type = random(StructureType.values());
		Structure structure = mockUsableStructure();
		when(structure.getStructureType()).thenReturn(type);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertSame(type, t.model().getStructureType(s));
					verify(structure).getStructureType();
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetEdgeCountStructure() {
		long count = random(0, Long.MAX_VALUE);
		Structure structure = mockUsableStructure();
		when(structure.getEdgeCount()).thenReturn(count);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertEquals(count, t.model().getEdgeCount(s));
					verify(structure).getEdgeCount();
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, long)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetEdgeAtStructureLong() {
		Edge edge = mockEdge();
		long index = random(0, Long.MAX_VALUE);
		Structure structure = mockUsableStructure();
		when(structure.getEdgeAt(eq(index))).thenReturn(edge);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertSame(edge, t.model().getEdgeAt(s, index));
					verify(structure).getEdgeAt(eq(index));
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#indexOfEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testIndexOfEdge() {
		Edge edge = mockEdge();
		long index = random(0, Long.MAX_VALUE);
		Structure structure = mockUsableStructure();
		when(structure.indexOfEdge(eq(edge))).thenReturn(index);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertEquals(index, t.model().indexOfEdge(s, edge));
					verify(structure).indexOfEdge(eq(edge));
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#containsEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testContainsEdge() {
		Edge edge = mockEdge();
		Structure structure = mockUsableStructure();
		when(structure.containsEdge(edge)).thenReturn(Boolean.TRUE);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertTrue(t.model().containsEdge(s, edge));
					verify(structure).containsEdge(edge);
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetEdgeCountStructureItem() {
		long count = random(0, Long.MAX_VALUE);
		Item item = mockItem();
		Structure structure = mockUsableStructure();
		when(structure.getEdgeCount(item)).thenReturn(count);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertEquals(count, t.model().getEdgeCount(s, item));
					verify(structure).getEdgeCount(item);
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getEdgeCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, boolean)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetEdgeCountStructureItemBoolean() {
		long count = random(0, Long.MAX_VALUE);
		boolean isSource = random().nextBoolean();
		Item item = mockItem();
		Structure structure = mockUsableStructure();
		when(structure.getEdgeCount(item, isSource)).thenReturn(count);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertEquals(count, t.model().getEdgeCount(s, item, isSource));
					verify(structure).getEdgeCount(item, isSource);
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getEdgeAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long, boolean)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetEdgeAtStructureItemLongBoolean() {
		long index = random(0, Long.MAX_VALUE);
		boolean isSource = random().nextBoolean();
		Edge edge = mockEdge();
		Item item = mockItem();
		Structure structure = mockUsableStructure();
		when(structure.getEdgeAt(item, index, isSource)).thenReturn(edge);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertSame(edge, t.model().getEdgeAt(s, item, index, isSource));
					verify(structure).getEdgeAt(item, index, isSource);
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getParent(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetParent() {
		Item item = mockItem();
		Item parent = mockItem();
		Structure structure = mockUsableStructure();
		when(structure.getParent(item)).thenReturn(parent);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertSame(parent, t.model().getParent(s, item));
					verify(structure).getParent(item);
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#indexOfChild(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testIndexOfChild() {
		long index = random(0, Long.MAX_VALUE);
		Item item = mockItem();
		Structure structure = mockUsableStructure();
		when(structure.indexOfChild(item)).thenReturn(index);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertEquals(index, t.model().indexOfChild(s, item));
					verify(structure).indexOfChild(item);
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getSiblingAt(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, long)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetSiblingAt() {
		long index = random(0, Long.MAX_VALUE);
		Item item = mockItem();
		Item sibling = mockItem();
		Structure structure = mockUsableStructure();
		when(structure.getSiblingAt(item, index)).thenReturn(sibling);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertSame(sibling, t.model().getSiblingAt(s, item, index));
					verify(structure).getSiblingAt(item, index);
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getHeight(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetHeight() {
		long height = random(0, Long.MAX_VALUE);
		Item item = mockItem();
		Structure structure = mockUsableStructure();
		when(structure.getHeight(item)).thenReturn(height);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertEquals(height, t.model().getHeight(s, item));
					verify(structure).getHeight(item);
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getDepth(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetDepth() {
		long depth = random(0, Long.MAX_VALUE);
		Item item = mockItem();
		Structure structure = mockUsableStructure();
		when(structure.getDepth(item)).thenReturn(depth);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertEquals(depth, t.model().getDepth(s, item));
					verify(structure).getDepth(item);
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getDescendantCount(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetDescendantCount() {
		long count = random(0, Long.MAX_VALUE);
		Item item = mockItem();
		Structure structure = mockUsableStructure();
		when(structure.getDescendantCount(item)).thenReturn(count);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertEquals(count, t.model().getDescendantCount(s, item));
					verify(structure).getDescendantCount(item);
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getVirtualRoot(de.ims.icarus2.model.api.members.structure.Structure)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetVirtualRoot() {
		Item root = mockItem();
		Structure structure = mockUsableStructure();
		when(structure.getVirtualRoot()).thenReturn(root);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertEquals(root, t.model().getVirtualRoot(s));
					verify(structure).getVirtualRoot();
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#isRoot(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testIsRoot() {
		Item root = mockItem();
		Structure structure = mockUsableStructure();
		when(structure.isRoot(root)).thenReturn(Boolean.TRUE);

		return new ModelTest<>(this, Structure.class)
				.action((t, s) -> {
					assertTrue(t.model().isRoot(s, root));
					verify(structure).isRoot(root);
				})
				.testSuccess(structure)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#addEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@TestFactory
	default List<DynamicTest> testAddEdgeStructureEdge() {
		Edge edge = mockEdge();
		Structure structure = mockUsableStructure();
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Structure.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, s) -> {
					t.model().addEdge(s, edge);
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(structure).addEdge(0L, edge); // addEdge(item) redirects to addEdge(index, item)
				})
				.testSuccess(structure)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#addEdge(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@TestFactory
	default List<DynamicTest> testAddEdgeStructureLongEdge() {
		Edge edge = mockEdge();
		long edgeCount = random(0, Long.MAX_VALUE);
		long index = random(0, edgeCount);
		Structure structure = mockUsableStructure();
		stubEdgeCount(structure, edgeCount);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Structure.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, s) -> {
					t.model().addEdge(s, index, edge);
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(structure).addEdge(index, edge);
				})
				.testSuccess(structure)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#addEdges(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
	 */
	@TestFactory
	default List<DynamicTest> testAddEdges() {
		long edgeCount = random(0, Long.MAX_VALUE/2);
		long index = random(0, edgeCount);
		long size = random(1, Long.MAX_VALUE-edgeCount);
		DataSequence<Edge> edges = mockSequence(size);
		Structure structure = mockUsableStructure();
		stubEdgeCount(structure, edgeCount);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Structure.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, s) -> {
					t.model().addEdges(s, index, edges);
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(structure).addEdges(index, edges);
				})
				.testSuccess(structure)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#removeEdge(de.ims.icarus2.model.api.members.structure.Structure, long)}.
	 */
	@TestFactory
	default List<DynamicTest> testRemoveEdgeStructureLong() {
		long edgeCount = random(10, Long.MAX_VALUE);
		long index = random(0, edgeCount);
		Edge edge = mockEdge();
		Structure structure = mockUsableStructure();
		stubEdgeCount(structure, edgeCount);
		when(structure.getEdgeAt(index)).thenReturn(edge);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Structure.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, s) -> {
					assertSame(edge, t.model().removeEdge(s, index));
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(structure).removeEdge(index);
				})
				.testSuccess(structure)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#removeEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testRemoveEdgeStructureEdge() {
		long edgeCount = random(10, Long.MAX_VALUE);
		long index = random(0, edgeCount);
		Edge edge = mockEdge();
		Structure structure = mockUsableStructure();
		stubEdgeCount(structure, edgeCount);
		when(structure.indexOfEdge(edge)).thenReturn(index);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Structure.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, s) -> {
					assertTrue(t.model().removeEdge(s, edge));
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(structure).removeEdge(index); // expected to delegate
				})
				.testSuccess(structure)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#removeEdges(de.ims.icarus2.model.api.members.structure.Structure, long, long)}.
	 */
	@SuppressWarnings("unchecked")
	@TestFactory
	default List<DynamicTest> testRemoveEdges() {
		long edgeCount = random(10, Long.MAX_VALUE);
		long index0 = random(0, edgeCount/2);
		long index1 = random(index0, edgeCount);
		DataSequence<Edge> edges = mockSequence(index1-index0+1);
		Structure structure = mockUsableStructure();
		stubEdgeCount(structure, edgeCount);
		when((DataSequence<Edge>)structure.removeEdges(index0, index1)).thenReturn(edges);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Structure.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, s) -> {
					t.model().removeEdges(s, index0, index1);
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(structure).removeEdges(index0, index1);
				})
				.testSuccess(structure)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#swapEdges(de.ims.icarus2.model.api.members.structure.Structure, long, long)}.
	 */
	@TestFactory
	default List<DynamicTest> testSwapEdges() {
		long edgeCount = random(10, Long.MAX_VALUE);
		long index0 = random(0, edgeCount/2);
		long index1 = random(index0, edgeCount);
		Structure structure = mockUsableStructure();
		stubEdgeCount(structure, edgeCount);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Structure.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, s) -> {
					t.model().swapEdges(s, index0, index1);
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(structure).swapEdges(index0, index1);
				})
				.testSuccess(structure)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setTerminal(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)}.
	 */
	@TestFactory
	default List<DynamicTest> testSetTerminal() {
		Structure structure = mockUsableStructure();
		Edge edge = mockEdge(structure);
		Item terminal = mockItem();
		boolean isSource = random().nextBoolean();
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Structure.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, s) -> {
					t.model().setTerminal(s, edge, terminal, isSource);
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(structure).setTerminal(edge, terminal, isSource);
				})
				.testSuccess(structure)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getStructure(de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetStructure() {
		Structure structure = mockStructure();
		Edge edge = mockUsableEdge();
		when(edge.getStructure()).thenReturn(structure);

		return new ModelTest<>(this, Edge.class)
				.action((t, e) -> {
					assertSame(structure, t.model().getStructure(e));
					verify(edge).getStructure();
				})
				.testSuccess(edge)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getSource(de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetSource() {
		Item source = mockItem();
		Edge edge = mockUsableEdge();
		when(edge.getSource()).thenReturn(source);

		return new ModelTest<>(this, Edge.class)
				.action((t, e) -> {
					assertSame(source, t.model().getSource(e));
					verify(edge).getSource();
				})
				.testSuccess(edge)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getTarget(de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetTarget() {
		Item target = mockItem();
		Edge edge = mockUsableEdge();
		when(edge.getTarget()).thenReturn(target);

		return new ModelTest<>(this, Edge.class)
				.action((t, e) -> {
					assertSame(target, t.model().getTarget(e));
					verify(edge).getTarget();
				})
				.testSuccess(edge)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getItem(de.ims.icarus2.model.api.members.item.Fragment)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetItemFragment() {
		Item item = mockItem();
		Fragment fragment = mockUsableFragment();
		when(fragment.getItem()).thenReturn(item);

		return new ModelTest<>(this, Fragment.class)
				.action((t, f) -> {
					assertSame(item, t.model().getItem(f));
					verify(fragment).getItem();
				})
				.testSuccess(fragment)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getFragmentBegin(de.ims.icarus2.model.api.members.item.Fragment)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetFragmentBegin() {
		Position position = mock(Position.class);
		Fragment fragment = mockUsableFragment();
		when(fragment.getFragmentBegin()).thenReturn(position);

		return new ModelTest<>(this, Fragment.class)
				.action((t, f) -> {
					assertSame(position, t.model().getFragmentBegin(f));
					verify(fragment).getFragmentBegin();
				})
				.testSuccess(fragment)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getFragmentEnd(de.ims.icarus2.model.api.members.item.Fragment)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetFragmentEnd() {
		Position position = mock(Position.class);
		Fragment fragment = mockUsableFragment();
		when(fragment.getFragmentEnd()).thenReturn(position);

		return new ModelTest<>(this, Fragment.class)
				.action((t, f) -> {
					assertSame(position, t.model().getFragmentEnd(f));
					verify(fragment).getFragmentEnd();
				})
				.testSuccess(fragment)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setFragmentBegin(de.ims.icarus2.model.api.members.item.Fragment, de.ims.icarus2.model.api.raster.Position)}.
	 */
	@TestFactory
	default List<DynamicTest> testSetFragmentBegin() {
		Fragment fragment = mockUsableFragment();
		Position oldPosition = mockPosition(0);
		Position newPosition = mockPosition(1);
		when(fragment.getFragmentBegin()).thenReturn(oldPosition);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Fragment.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, f) -> {
					assertSame(oldPosition, t.model().setFragmentBegin(f, newPosition));
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(fragment).setFragmentBegin(newPosition);
				})
				.testSuccess(fragment)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setFragmentEnd(de.ims.icarus2.model.api.members.item.Fragment, de.ims.icarus2.model.api.raster.Position)}.
	 */
	@TestFactory
	default List<DynamicTest> testSetFragmentEnd() {
		Fragment fragment = mockUsableFragment();
		Position oldPosition = mockPosition(0);
		Position newPosition = mockPosition(1);
		when(fragment.getFragmentEnd()).thenReturn(oldPosition);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Fragment.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, f) -> {
					assertSame(oldPosition, t.model().setFragmentEnd(f, newPosition));
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(fragment).setFragmentEnd(newPosition);
				})
				.testSuccess(fragment)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#collectKeys(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.util.function.Consumer)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testCollectKeys() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		Consumer<String> action = mock(Consumer.class);
		when(storage.collectKeys(item, action)).thenReturn(Boolean.TRUE);

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					assertTrue(t.model().collectKeys(layer, i, action));
					verify(storage).collectKeys(item, action);
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@TestFactory
	default List<DynamicTest> testGetValue() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		Object value = mock(Object.class);
		String key = "key";
		when(storage.getValue(item, key)).thenReturn(value);

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					assertSame(value, t.model().getValue(layer, i, key));
					verify(storage).getValue(item, key);
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getIntegerValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetIntegerValue() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		int value = random(0, Integer.MAX_VALUE);
		String key = "key";
		when(storage.getIntegerValue(item, key)).thenReturn(value);

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					assertEquals(value, t.model().getIntegerValue(layer, i, key));
					verify(storage).getIntegerValue(item, key);
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getLongValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetLongValue() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		long value = random(0, Long.MAX_VALUE);
		String key = "key";
		when(storage.getLongValue(item, key)).thenReturn(value);

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					assertEquals(value, t.model().getLongValue(layer, i, key));
					verify(storage).getLongValue(item, key);
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getFloatValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetFloatValue() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		float value = random().nextFloat();
		String key = "key";
		when(storage.getFloatValue(item, key)).thenReturn(value);

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					assertEquals(value, t.model().getFloatValue(layer, i, key));
					verify(storage).getFloatValue(item, key);
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getDoubleValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetDoubleValue() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		double value = random().nextDouble();
		String key = "key";
		when(storage.getDoubleValue(item, key)).thenReturn(value);

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					assertEquals(value, t.model().getDoubleValue(layer, i, key));
					verify(storage).getDoubleValue(item, key);
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#getBooleanValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testGetBooleanValue() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		boolean value = random().nextBoolean();
		String key = "key";
		when(storage.getBooleanValue(item, key)).thenReturn(value);

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					assertEquals(value, t.model().getBooleanValue(layer, i, key));
					verify(storage).getBooleanValue(item, key);
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)}.
	 */
	@TestFactory
	default List<DynamicTest> testSetValue() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		Object oldValue = mock(Object.class);
		Object newValue = mock(Object.class);
		String key = "key";
		when(storage.getValue(item, key)).thenReturn(oldValue);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Item.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, i) -> {
					assertSame(oldValue, t.model().setValue(layer, i, key, newValue));
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(storage).setValue(item, key, newValue);
				})
				.testSuccess(item)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setIntegerValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String, int)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testSetIntegerValue() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		int oldValue = random().nextInt();
		int newValue = random().nextInt();
		String key = "key";
		when(storage.getIntegerValue(item, key)).thenReturn(oldValue);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Item.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, i) -> {
					assertEquals(oldValue, t.model().setIntegerValue(layer, i, key, newValue));
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(storage).setIntegerValue(item, key, newValue);
				})
				.testSuccess(item)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setLongValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String, long)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testSetLongValue() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		long oldValue = random().nextLong();
		long newValue = random().nextLong();
		String key = "key";
		when(storage.getLongValue(item, key)).thenReturn(oldValue);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Item.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, i) -> {
					assertEquals(oldValue, t.model().setLongValue(layer, i, key, newValue));
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(storage).setLongValue(item, key, newValue);
				})
				.testSuccess(item)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setFloatValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String, float)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testSetFloatValue() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		float oldValue = random().nextFloat();
		float newValue = random().nextFloat();
		String key = "key";
		when(storage.getFloatValue(item, key)).thenReturn(oldValue);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Item.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, i) -> {
					assertEquals(oldValue, t.model().setFloatValue(layer, i, key, newValue));
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(storage).setFloatValue(item, key, newValue);
				})
				.testSuccess(item)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setDoubleValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String, double)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testSetDoubleValue() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		double oldValue = random().nextDouble();
		double newValue = random().nextDouble();
		String key = "key";
		when(storage.getDoubleValue(item, key)).thenReturn(oldValue);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Item.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, i) -> {
					assertEquals(oldValue, t.model().setDoubleValue(layer, i, key, newValue));
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(storage).setDoubleValue(item, key, newValue);
				})
				.testSuccess(item)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#setBooleanValue(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item, java.lang.String, boolean)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testSetBooleanValue() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		boolean oldValue = random().nextBoolean();
		boolean newValue = random().nextBoolean();
		String key = "key";
		when(storage.getBooleanValue(item, key)).thenReturn(oldValue);
		MutableObject<AtomicChange> change = new MutableObject<>();

		return new ModelTest<>(this, Item.class)
				.setup(t -> t.changeHandler(change::set))
				.action((t, i) -> {
					assertEquals(oldValue, t.model().setBooleanValue(layer, i, key, newValue));
					assertFalse(change.isEmpty());
					change.get().execute();
					verify(storage).setBooleanValue(item, key, newValue);
				})
				.testSuccess(item)
				.testNotWritableError()
				.testDeadItemError()
				.testLockedItemError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#hasAnnotations(de.ims.icarus2.model.api.layer.AnnotationLayer)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testHasAnnotationsAnnotationLayer() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		when(storage.hasAnnotations()).thenReturn(Boolean.TRUE);

		return new ModelTest<>(this, AnnotationLayer.class)
				.action((t, l) -> {
					assertTrue(t.model().hasAnnotations(l));
					verify(storage).hasAnnotations();
				})
				.testSuccess(layer)
				.testNotReadableError()
				.tests();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.CorpusModel#hasAnnotations(de.ims.icarus2.model.api.layer.AnnotationLayer, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default List<DynamicTest> testHasAnnotationsAnnotationLayerItem() {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		when(layer.getAnnotationStorage()).thenReturn(storage);
		Item item = mockUsableItem();
		when(storage.hasAnnotations(item)).thenReturn(Boolean.TRUE);

		return new ModelTest<>(this, Item.class)
				.action((t, i) -> {
					assertTrue(t.model().hasAnnotations(layer, i));
					verify(storage).hasAnnotations(item);
				})
				.testSuccess(item)
				.testNotReadableError()
				.testDeadItemError()
				.testDirtyItemError()
				.tests();
	}
}
