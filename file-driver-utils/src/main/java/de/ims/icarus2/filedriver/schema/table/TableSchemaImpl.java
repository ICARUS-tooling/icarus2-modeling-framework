/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 */
package de.ims.icarus2.filedriver.schema.table;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.manifest.standard.DefaultModifiableIdentity;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public class TableSchemaImpl extends DefaultModifiableIdentity implements TableSchema {

	private String separator;
	private BlockSchema root;

	/**
	 * @see de.ims.icarus2.filedriver.schema.Schema#getSchemaTypeName()
	 */
	@Override
	public String getSchemaTypeName() {
		return SCHEMA_ID;
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.table.TableSchema#getRootBlock()
	 */
	@Override
	public BlockSchema getRootBlock() {
		return root;
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.table.TableSchema#getSeparator()
	 */
	@Override
	public String getSeparator() {
		return separator;
	}

	public void setRootBlock(BlockSchema root) {
		checkNotNull(root);

		this.root = root;
	}

	public void setSeparator(String separator) {
		checkNotNull(separator);

		this.separator = separator;
	}

	private static AttributeSchema[] EMPTY_ATTRIBUTES = {};
	private static BlockSchema[] EMPTY_BLOCKS = {};
	private static ColumnSchema[] EMPTY_COLUMNS = {};
	private static SubstituteSchema[] EMPTY_SUBSTITUTES = {};

	public static class BlockSchemaImpl implements BlockSchema {

		private String layerId;
		private MemberSchema containerSchema, componentSchema;
		private AttributeSchema beginDelimiter, endDelimiter;
		private List<AttributeSchema> attributes;
		private List<ColumnSchema> columns;
		private ColumnSchema fallbackColumn;
		private List<BlockSchema> nestedBlocks;
		private String noEntryLabel;
		private boolean columnOrderFixed;
		private Options options;

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getLayerId()
		 */
		@Override
		public String getLayerId() {
			return layerId;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getContainerSchema()
		 */
		@Override
		public MemberSchema getContainerSchema() {
			return containerSchema;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getComponentSchema()
		 */
		@Override
		public MemberSchema getComponentSchema() {
			return componentSchema;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getBeginDelimiter()
		 */
		@Override
		public AttributeSchema getBeginDelimiter() {
			return beginDelimiter;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getEndDelimiter()
		 */
		@Override
		public AttributeSchema getEndDelimiter() {
			return endDelimiter;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getAttributes()
		 */
		@Override
		public AttributeSchema[] getAttributes() {

			AttributeSchema[] result = null;

			if(attributes!=null) {
				result = attributes.toArray(EMPTY_ATTRIBUTES);
			}

			return result;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getColumns()
		 */
		@Override
		public ColumnSchema[] getColumns() {

			ColumnSchema[] result = null;

			if(columns!=null) {
				result = columns.toArray(EMPTY_COLUMNS);
			}

			return result;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getFallbackColumn()
		 */
		@Override
		public ColumnSchema getFallbackColumn() {
			return fallbackColumn;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getNestedBlocks()
		 */
		@Override
		public BlockSchema[] getNestedBlocks() {

			BlockSchema[] result = null;

			if(nestedBlocks!=null) {
				result = nestedBlocks.toArray(EMPTY_BLOCKS);
			}

			return result;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getNoEntryLabel()
		 */
		@Override
		public String getNoEntryLabel() {
			return noEntryLabel;
		}

		@Override
		public boolean isColumnOrderFixed() {
			return columnOrderFixed;
		}

		public void setColumnOrderFixed(boolean columnOrderFixed) {
			this.columnOrderFixed = columnOrderFixed;
		}

		public void setLayerId(String layerId) {
			checkNotNull(layerId);

			this.layerId = layerId;
		}

		public void setContainerSchema(MemberSchema containerSchema) {
			checkNotNull(containerSchema);

			this.containerSchema = containerSchema;
		}

		public void setComponentSchema(MemberSchema componentSchema) {
			checkNotNull(componentSchema);

			this.componentSchema = componentSchema;
		}

		public void setBeginDelimiter(AttributeSchema beginDelimiter) {
			checkNotNull(beginDelimiter);

			this.beginDelimiter = beginDelimiter;
		}

		public void setEndDelimiter(AttributeSchema endDelimiter) {
			checkNotNull(endDelimiter);

			this.endDelimiter = endDelimiter;
		}

		public void setFallbackColumn(ColumnSchema fallbackColumn) {
			checkNotNull(fallbackColumn);

			this.fallbackColumn = fallbackColumn;
		}

		public void setNoEntryLabel(String noEntryLabel) {
			checkNotNull(noEntryLabel);

			this.noEntryLabel = noEntryLabel;
		}

		public void addAttribute(AttributeSchema attribute) {
			checkNotNull(attribute);

			if(attributes==null) {
				attributes = new ArrayList<>();
			}

			attributes.add(attribute);
		}

		public void addColumn(ColumnSchema column) {
			checkNotNull(column);

			if(columns==null) {
				columns = new ArrayList<>();
			}

			columns.add(column);
		}

		public void addBlock(BlockSchema block) {
			checkNotNull(block);

			if(nestedBlocks==null) {
				nestedBlocks = new ArrayList<>();
			}

			nestedBlocks.add(block);
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getOptions()
		 */
		@Override
		public Options getOptions() {
			return options;
		}

		public void addOption(String key, String value) {
			if(options==null) {
				options = new Options();
			}

			options.put(key, value);
		}

	}

	public static class MemberSchemaImpl implements MemberSchema {

		private String layerId;
		private MemberType memberType;
		private Boolean isReference;

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.MemberSchema#getLayerId()
		 */
		@Override
		public String getLayerId() {
			return layerId;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.MemberSchema#getMemberType()
		 */
		@Override
		public MemberType getMemberType() {
			return memberType;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.MemberSchema#isReference()
		 */
		@Override
		public boolean isReference() {
			return isReference==null ? DEFAULT_IS_REFERENCE : isReference.booleanValue();
		}

		public void setLayerId(String layerId) {
			checkNotNull(layerId);

			this.layerId = layerId;
		}

		public void setMemberType(MemberType memberType) {
			checkNotNull(memberType);

			this.memberType = memberType;
		}

		public void setIsReference(boolean isReference) {
			this.isReference = Boolean.valueOf(isReference);
		}

	}

	public static class AttributeSchemaImpl implements AttributeSchema {

		private String pattern;
		private ResolverSchema resolver;

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeSchema#getPattern()
		 */
		@Override
		public String getPattern() {
			return pattern;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeSchema#getResolver()
		 */
		@Override
		public ResolverSchema getResolver() {
			return resolver;
		}

		public void setPattern(String pattern) {
			checkNotNull(pattern);

			this.pattern = pattern;
		}

		public void setResolver(ResolverSchema resolver) {
			checkNotNull(resolver);

			this.resolver = resolver;
		}

	}

	public static class ColumnSchemaImpl implements ColumnSchema {

		private String name, layerId, annotationKey, noEntryLabel;
		private Boolean isIgnoreColumn;
		private ResolverSchema resolver;
		private List<SubstituteSchema> substitutes;

		/**
		 * @see de.ims.icarus2.util.strings.NamedObject#getName()
		 */
		@Override
		public String getName() {
			return name;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema#getLayerId()
		 */
		@Override
		public String getLayerId() {
			return layerId;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema#getAnnotationKey()
		 */
		@Override
		public String getAnnotationKey() {
			return annotationKey;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema#getNoEntryLabel()
		 */
		@Override
		public String getNoEntryLabel() {
			return noEntryLabel;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema#isIgnoreColumn()
		 */
		@Override
		public boolean isIgnoreColumn() {
			return isIgnoreColumn==null ? DEFAULT_IGNORE_COLUMN : isIgnoreColumn.booleanValue();
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema#getResolver()
		 */
		@Override
		public ResolverSchema getResolver() {
			return resolver;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema#getSubstitutes()
		 */
		@Override
		public SubstituteSchema[] getSubstitutes() {

			SubstituteSchema[] result = null;

			if(substitutes!=null) {
				result = substitutes.toArray(EMPTY_SUBSTITUTES);
			}

			return result;
		}

		public void setName(String name) {
			checkNotNull(name);

			this.name = name;
		}

		public void setLayerId(String layerId) {
			checkNotNull(layerId);

			this.layerId = layerId;
		}

		public void setAnnotationKey(String annotationKey) {
			checkNotNull(annotationKey);

			this.annotationKey = annotationKey;
		}

		public void setNoEntryLabel(String noEntryLabel) {
			checkNotNull(noEntryLabel);

			this.noEntryLabel = noEntryLabel;
		}

		public void setIsIgnoreColumn(Boolean isIgnoreColumn) {
			checkNotNull(isIgnoreColumn);

			this.isIgnoreColumn = isIgnoreColumn;
		}

		public void setResolver(ResolverSchema resolver) {
			checkNotNull(resolver);

			this.resolver = resolver;
		}

		public void addSubstitute(SubstituteSchema substitute) {
			checkNotNull(substitute);

			if(substitutes==null) {
				substitutes = new ArrayList<>();
			}

			substitutes.add(substitute);
		}

	}

	public static class SubstituteSchemaImpl implements SubstituteSchema {

		private SubstituteType type;
		private MemberType memberType;
		private String name;

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteSchema#getType()
		 */
		@Override
		public SubstituteType getType() {
			return type;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteSchema#getName()
		 */
		@Override
		public String getName() {
			return name;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteSchema#getMemberType()
		 */
		@Override
		public MemberType getMemberType() {
			return memberType;
		}

		public void setType(SubstituteType type) {
			checkNotNull(type);

			this.type = type;
		}

		public void setMemberType(MemberType memberType) {
			checkNotNull(memberType);

			this.memberType = memberType;
		}

		public void setName(String name) {
			checkNotNull(name);

			this.name = name;
		}

	}

	public static class ResolverSchemaImpl implements ResolverSchema {

		private String type;
		private Options options;

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.ResolverSchema#getType()
		 */
		@Override
		public String getType() {
			return type;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.ResolverSchema#getOptions()
		 */
		@Override
		public Options getOptions() {
			return options;
		}

		public void setType(String type) {
			checkNotNull(type);

			this.type = type;
		}

		public void addOption(String key, String value) {
			if(options==null) {
				options = new Options();
			}

			options.put(key, value);
		}
	}
}
