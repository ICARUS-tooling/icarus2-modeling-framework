/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.schema.tabular;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.FileDriverUtils;
import de.ims.icarus2.filedriver.schema.Schema;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.LazyStore;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.strings.NamedObject;
import de.ims.icarus2.util.strings.StringResource;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

/**
 * @author Markus Gärtner
 *
 */
public interface TableSchema extends Schema {

	public static final String SCHEMA_TYPE_ID = FileDriverUtils.SCHEMA_PROPERTY_PREFIX+".tabular";

	public static final String SEPARATOR_TAB = "TAB";
	public static final String SEPARATOR_SPACE = "SPACE";
	public static final String SEPARATOR_WHITESPACE = "WHITESPACE";
	public static final String SEPARATOR_WHITESPACES = "WHITESPACES";

	/**
	 * @see de.ims.icarus2.filedriver.schema.Schema#getSchemaTypeName()
	 */
	@Override
	default String getSchemaTypeName() {
		return SCHEMA_TYPE_ID;
	}

	/**
	 * Returns the total number of all (recursively nested) {@link BlockSchema} objects
	 * declared within this schema.
	 *
	 * @return
	 */
	int getTotalBlockSchemaCount();

	BlockSchema getRootBlock();

	/**
	 * Returns the global column separator.
	 * This can be overridden on the {@link BlockSchema block} level.
	 *
	 * @return
	 */
	String getSeparator();

	/**
	 * Returns the {@link LayerGroupManifest#getId() id} of the {@link LayerGroup} top
	 * level members created via this schema should be placed in.
	 * <p>
	 * The returned value is allowed to either be a simple identifier only containing
	 * the group's {@link LayerGroupManifest#getId() id} or a complex one that denotes
	 * a {@link ModelUtils#g}
	 * TODO
	 *
	 * @return
	 */
	String getGroupId();

	/**
	 * Defines a schema for one hierarchical level inside a tabular file format.
	 * The content of a "block" of data consists of a series of rows each of which
	 * will be turned into one {@link Item} residing within the {@link Container}
	 * of the block.
	 *
	 * The array of {@link ColumnSchema}s returned by {@link #getColumns()} defines
	 * the actual annotation content of each item.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface BlockSchema {

		public static final boolean DEFAULT_COLUMN_ORDER_FIXED = false;

		Options getOptions();

		/**
		 * Returns the {@link LayerManifest#getId() layer-id} of the {@link ItemLayer}
		 * that containers created for this block should be placed in.
		 *
		 * @return
		 */
		String getLayerId();

		/**
		 * Returns the ids of additional {@link LayerGroup layer groups} in the
		 * context besides the group which hosts the {@link #getLayerId() primary layer}
		 * of this block.
		 */
		String[] getExternalGroupIds();

		/**
		 * Specification of nested elements.
		 *
		 * @return
		 */
		MemberSchema getComponentSchema();

		/**
		 * Attribute that signals the beginning of a physical block of data that
		 * represents content of this block.
		 * Returns {@code null} for simple formats where the beginning of valid
		 * data is simply the first non-empty line of text.
		 *
		 * @return
		 */
		AttributeSchema getBeginDelimiter();

		/**
		 * Attribute that signals the end of a physical block of data that
		 * represents content of this block.
		 *
		 * @return
		 */
		AttributeSchema getEndDelimiter();

		/**
		 * Returns the separator to be used for columns within this block.
		 * This overrides the {@link TableSchema#getSeparator() global definition}
		 * of the surrounding {@link TableSchema} if present.
		 *
		 * @return
		 */
		String getSeparator();

		/**
		 * Free-form attributes like comments and the like
		 *
		 * @return
		 */
		AttributeSchema[] getAttributes();

		int getRequiredColumns();

		/**
		 *
		 * @return
		 */
		ColumnSchema[] getColumns();

		boolean isColumnOrderFixed();

		/**
		 * If a format can have a variable number of columns, this method returns
		 * the column schema for excess columns that are handled by the regular
		 * {@link #getColumns() column definition}.
		 *
		 * @return
		 */
		ColumnSchema getFallbackColumn();

		/**
		 * Nested blocks for complex hierarchical formats (like CoNLL 2012 Shared-Task)
		 *
		 * @return
		 */
		BlockSchema[] getNestedBlocks();

		/**
		 * Returns the globally used {@code String} label that indicates an "empty"
		 * annotation.
		 *
		 * @return
		 */
		String getNoEntryLabel();

		void forEachNestedBlock(Consumer<? super BlockSchema> action);
	}

	public interface MemberSchema {

		public static final boolean DEFAULT_IS_REFERENCE = false;

		/**
		 * Type of the member, if different from what is defined in the respective
		 * {@link MemberManifest}.
		 *
		 * @return
		 */
		MemberType getMemberType();

		/**
		 * If {@code true} the converter should not create new elements but use the
		 * supplied data to reference existing ones.
		 */
		boolean isReference();
	}

	public enum AttributeTarget implements StringResource {
//		CURRENT_BLOCK,
//		PARENT_BLOCK,
//		ROOT_BLOCK,
		NEXT_ITEM("nextItem"),
		PREVIOUS_ITEM("previousItem"),
		;

		private final String label;

		private AttributeTarget(String label) {
			this.label = requireNonNull(label);
		}

		/**
		 * @see de.ims.icarus2.util.strings.StringResource#getStringValue()
		 */
		@Override
		public String getStringValue() {
			return label;
		}

		private static Map<String, AttributeTarget> map = new Object2ObjectArrayMap<>();
		static {
			for (AttributeTarget target : values()) {
				map.put(target.label, target);
			}
		}

		public static AttributeTarget parseAttributeTarget(String s) {
			AttributeTarget target = map.get(s);
			if(target==null)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Unknown attribute target: "+s);
			return target;
		}
	}

	public enum PatternType implements StringResource {
		/** Use pattern as input for a {@link Pattern} instance */
		REGEX("regex"),
		/** Run a simple {@link String#startsWith(String)} call */
		PLAIN("plain"),
		/** Lookup a predefined delimiter by name */
		NAME("name"),
		;

		private final String label;

		private PatternType(String label) {
			this.label = label;
		}

		/**
		 * @see de.ims.icarus2.util.strings.StringResource#getStringValue()
		 */
		@Override
		public String getStringValue() {
			return label;
		}

		private static Map<String, PatternType> map = new Object2ObjectArrayMap<>();
		static {
			for (PatternType target : values()) {
				map.put(target.label, target);
			}
		}

		public static PatternType parsePatternType(String s) {
			PatternType type = map.get(s);
			if(type==null)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Unknown pattern type: "+s);
			return type;
		}
	}

	public interface AttributeSchema {

		public static final String DELIMITER_EMPTY_LINE = "EMPTY_LINE";
		public static final String DELIMITER_EMPTY_LINES = "EMPTY_LINES";

		public static final PatternType DEFAULT_TYPE = PatternType.PLAIN;
		public static final boolean DEFAULT_SHARED = false;

		AttributeTarget getTarget();

		/**
		 * Pattern to be used for matching lines of text against this schema.
		 * <p>
		 * If the return value is any of the predefined {@code DELIMITER_} constants
		 * available in this interface, than it should not be used directly as a
		 * regular expression.
		 *
		 * @return
		 */
		String getPattern();

		/**
		 * Signals whether the returned value should be treated as regex, plai text or lookup name.
		 * @return
		 */
		PatternType getType();

		/**
		 * If the line matched by the {@link #getPattern() pattern} of this attribute
		 * also contains important data then this method returns the resolver to pass
		 * the data to.
		 *
		 * @return
		 */
		ResolverSchema getResolver();

		/**
		 * If {@code true} content matched by this attribute can be effectively shared between
		 * multiple attributes or delimiters. If {@code false} any content matched for this
		 * attribute must be immediately consumed!
		 * <p>
		 * For instance this supports situations where a nested {@link BlockSchema} can share an
		 * {@link BlockSchema#getEndDelimiter() end delimiter} with its host.
		 * @return
		 */
		boolean isShared();
	}

	public interface ColumnSchema extends NamedObject {

		public static final boolean DEFAULT_IGNORE_COLUMN = false;

		/**
		 * Id of the layer content of this column should be stored in.
		 * Note that this id can point to various layer types depending
		 * on whether the optional {@link #getAnnotationKey() annotation key}
		 * has also been set or if the content represents structural information.
		 *
		 * @return
		 */
		String getLayerId();

		String getAnnotationKey();

		/**
		 * Returns the {@code String} label that indicates an empty annotation
		 * entry. Note that the return value of this method overrides the
		 * global setting accessible via {@link BlockSchema#getNoEntryLabel()}
		 * of the surrounding block.
		 *
		 * @return
		 *
		 * @see BlockSchema#getNoEntryLabel()
		 */
		String getNoEntryLabel();

		/**
		 * Flag to signal that the column should not be processed
		 * @return
		 */
		boolean isIgnoreColumn();

		/**
		 * Optional resolver that replaces the default handling process of simply
		 * reading the content of a column and assigning it to the designated
		 * annotation layer.
		 *
		 * @return
		 */
		ResolverSchema getResolver();

		/**
		 * Returns the substitute declaration for the given type if present.
		 *
		 * @return
		 */
		SubstituteSchema getSubstitute(SubstituteType type);

		boolean hasSubstitutes();

		/**
		 * Allows to override the index from which this column should actually
		 * draw input data. If this field is not set (i.e. {@code -1} is returned)
		 * then the position within the list of column definitions is used as index!
		 * @return
		 */
		int getColumnIndex();
	}

	/**
	 * Defines possible substitute actions a column can perform.
	 * <p>
	 * Note that although a column can declare multiple substitutes,
	 * not all arbitrary combinations are possible. The following list
	 * shows legal combinations:
	 *
	 * TODO
	 *
	 * @author Markus Gärtner
	 *
	 */
	public enum SubstituteType implements StringResource {

		/**
		 * Replaces the slot of the column with a new member
		 */
		REPLACEMENT("replacement"),

		/**
		 * Adds a new member to the loading process and makes
		 * it addressable by name
		 */
		ADDITION("addition"),

		/**
		 * Redirect annotation target to named member
		 */
		TARGET("target"),
		;

		private final String xml;

		private SubstituteType(String xml) {
			this.xml = requireNonNull(xml);
		}

		private static final LazyStore<SubstituteType, String> store
			= LazyStore.forStringResource(SubstituteType.class, true);

		public static SubstituteType parseSubstituteType(String s) {
			return store.lookup(s);
		}

		/**
		 * @see de.ims.icarus2.util.strings.StringResource#getStringValue()
		 */
		@Override
		public String getStringValue() {
			return xml;
		}
	}

	/**
	 * Allows a column to declare a foreign target for its annotations or to
	 * inject new addressable members into the loading process.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface SubstituteSchema {

		/**
		 *
		 *
		 * @return
		 */
		SubstituteType getType();

		/**
		 * Returns the name of the new member or the target this schema relates to.
		 *
		 * @return
		 */
		String getName();

		/**
		 * If injecting a new member this method returns its designated type
		 * otherwise it should return {@code null}.
		 *
		 * @return
		 */
		MemberType getMemberType();
	}

	public interface ResolverSchema {

		/**
		 * Either returns a known id of a common (simple) resolver or the fully qualified
		 * class name of an actual implementation which must have a public no-argument constructor.
		 *
		 * @return
		 */
		String getType();

		/**
		 * Optional configuration which will be passed on to the resolver implementation
		 *
		 * @return
		 */
		Options getOptions();
	}
}
