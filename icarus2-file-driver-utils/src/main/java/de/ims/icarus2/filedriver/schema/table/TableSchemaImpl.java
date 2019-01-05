/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.schema.table;

import static de.ims.icarus2.util.lang.Primitives._boolean;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Objects;

import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.manifest.standard.DefaultModifiableIdentity;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.lang.ClassUtils;

/**
 * @author Markus Gärtner
 *
 */
public class TableSchemaImpl extends DefaultModifiableIdentity<TableSchemaImpl> implements TableSchema {

	private String separator;
	private String groupId;
	private BlockSchema root;

	private int blockCount = -1;

	/**
	 * @see de.ims.icarus2.filedriver.schema.table.TableSchema#getTotalBlockSchemaCount()
	 */
	@Override
	public int getTotalBlockSchemaCount() {
		if(blockCount==-1) {
			MutableInteger counter = new MutableInteger(0);
			countBlockSchemas(getRootBlock(), counter);
			blockCount = counter.intValue();
		}
		return blockCount;
	}

	private static void countBlockSchemas(BlockSchema blockSchema, MutableInteger counter) {
		counter.incrementAndGet();

		blockSchema.forEachNestedBlock(b -> countBlockSchemas(b, counter));
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.table.TableSchema#getGroupId()
	 */
	@Override
	public String getGroupId() {
		return groupId;
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

	public TableSchemaImpl setRootBlock(BlockSchema root) {
		requireNonNull(root);

		this.root = root;

		return this;
	}

	public TableSchemaImpl setSeparator(String separator) {
		requireNonNull(separator);

		this.separator = separator;

		return this;
	}

	public void setGroupId(String groupId) {
		requireNonNull(groupId);

		this.groupId = groupId;
	}

	private static AttributeSchema[] EMPTY_ATTRIBUTES = {};
	private static BlockSchema[] EMPTY_BLOCKS = {};
	private static ColumnSchema[] EMPTY_COLUMNS = {};

	public static class BlockSchemaImpl implements BlockSchema {

		private String layerId;
		private String separator;
		private MemberSchema componentSchema;
		private AttributeSchema beginDelimiter, endDelimiter;
		private final List<AttributeSchema> attributes = new ArrayList<>();
		private final List<ColumnSchema> columns = new ArrayList<>();
		private ColumnSchema fallbackColumn;
		private final List<BlockSchema> nestedBlocks = new ArrayList<>();
		private String noEntryLabel;
		private Boolean columnOrderFixed;
		private final Options options = new Options();

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getLayerId()
		 */
		@Override
		public String getLayerId() {
			return layerId;
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

			BlockSchema[] result = EMPTY_BLOCKS;

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
			return columnOrderFixed==null ? DEFAULT_COLUMN_ORDER_FIXED : columnOrderFixed.booleanValue();
		}

		public BlockSchemaImpl setColumnOrderFixed(boolean columnOrderFixed) {
			this.columnOrderFixed = columnOrderFixed==DEFAULT_COLUMN_ORDER_FIXED ? null : Boolean.valueOf(columnOrderFixed);

			return this;
		}

		public BlockSchemaImpl setLayerId(String layerId) {
			requireNonNull(layerId);

			this.layerId = layerId;

			return this;
		}

		public BlockSchemaImpl setComponentSchema(MemberSchema componentSchema) {
			requireNonNull(componentSchema);

			this.componentSchema = componentSchema;

			return this;
		}

		public BlockSchemaImpl setBeginDelimiter(AttributeSchema beginDelimiter) {
			requireNonNull(beginDelimiter);

			this.beginDelimiter = beginDelimiter;

			return this;
		}

		public BlockSchemaImpl setEndDelimiter(AttributeSchema endDelimiter) {
			requireNonNull(endDelimiter);

			this.endDelimiter = endDelimiter;

			return this;
		}

		public BlockSchemaImpl setFallbackColumn(ColumnSchema fallbackColumn) {
			requireNonNull(fallbackColumn);

			this.fallbackColumn = fallbackColumn;

			return this;
		}

		public BlockSchemaImpl setNoEntryLabel(String noEntryLabel) {
			requireNonNull(noEntryLabel);

			this.noEntryLabel = noEntryLabel;

			return this;
		}

		public BlockSchemaImpl addAttribute(AttributeSchema attribute) {
			requireNonNull(attribute);

			attributes.add(attribute);

			return this;
		}

		public BlockSchemaImpl addColumn(ColumnSchema column) {
			requireNonNull(column);

			columns.add(column);

			return this;
		}

		public BlockSchemaImpl addColumns(ColumnSchema...columnSchemas) {
			requireNonNull(columnSchemas);

			Collections.addAll(columns, columnSchemas);

			return this;
		}

		public BlockSchemaImpl addBlock(BlockSchema block) {
			requireNonNull(block);

			nestedBlocks.add(block);

			return this;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getOptions()
		 */
		@Override
		public Options getOptions() {
			return options==null ? Options.NONE : options;
		}

		public BlockSchemaImpl addOption(String key, String value) {
			options.put(key, value);

			return this;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#getSeparator()
		 */
		@Override
		public String getSeparator() {
			return separator;
		}

		public BlockSchemaImpl setSeparator(String separator) {
			requireNonNull(separator);

			this.separator = separator;

			return this;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema#forEachNestedBlock(java.util.function.Consumer)
		 */
		@Override
		public void forEachNestedBlock(Consumer<? super BlockSchema> action) {
			if(nestedBlocks!=null) {
				nestedBlocks.forEach(action);
			}
		}
	}

	public static class MemberSchemaImpl implements MemberSchema {

		private MemberType memberType;
		private Boolean isReference;

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

		public MemberSchemaImpl setMemberType(MemberType memberType) {
			requireNonNull(memberType);

			this.memberType = memberType;

			return this;
		}

		public MemberSchemaImpl setIsReference(boolean isReference) {
			this.isReference = isReference==DEFAULT_IS_REFERENCE ? null : Boolean.valueOf(isReference);

			return this;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("Member@[memberType=%s, isReference=%b]",
					memberType, _boolean(isReference()));
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(memberType, isReference);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj==this) {
				return true;
			} else if(obj instanceof MemberSchema) {
				MemberSchema other = (MemberSchema)obj;
				return ClassUtils.equals(memberType, other.getMemberType())
						&& isReference()==other.isReference();
			}
			return false;
		}
	}

	public static class AttributeSchemaImpl implements AttributeSchema {

		private String pattern;
		private ResolverSchema resolver;
		private AttributeTarget target;

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

		public AttributeSchemaImpl setPattern(String pattern) {
			requireNonNull(pattern);

			this.pattern = pattern;

			return this;
		}

		public AttributeSchemaImpl setResolver(ResolverSchema resolver) {
			requireNonNull(resolver);

			this.resolver = resolver;

			return this;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeSchema#getTarget()
		 */
		@Override
		public AttributeTarget getTarget() {
			return target;
		}

		public AttributeSchemaImpl setTarget(AttributeTarget target) {
			requireNonNull(target);

			this.target = target;

			return this;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("Attribute@[pattern=%s, target=%s, resolver=%s]",
					pattern, target, resolver);
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(pattern, resolver, target);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj==this) {
				return true;
			} else if(obj instanceof AttributeSchema) {
				AttributeSchema other = (AttributeSchema) obj;
				return ClassUtils.equals(pattern, other.getPattern())
						&& ClassUtils.equals(target, other.getTarget())
						&& ClassUtils.equals(resolver, other.getResolver());
			}
			return false;
		}
	}

	public static class ColumnSchemaImpl implements ColumnSchema {

		private String name, layerId, annotationKey, noEntryLabel;
		private Boolean isIgnoreColumn;
		private ResolverSchema resolver;
		private final EnumMap<SubstituteType, SubstituteSchema> substitutes = new EnumMap<>(SubstituteType.class);

		public ColumnSchemaImpl(String name) {
			setName(name);
		}

		public ColumnSchemaImpl() {
			// no-op
		}

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
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema#hasSubstitutes()
		 */
		@Override
		public boolean hasSubstitutes() {
			return substitutes!=null && !substitutes.isEmpty();
		}

		public ColumnSchemaImpl setName(String name) {
			requireNonNull(name);

			this.name = name;

			return this;
		}

		public ColumnSchemaImpl setLayerId(String layerId) {
			requireNonNull(layerId);

			this.layerId = layerId;

			return this;
		}

		public ColumnSchemaImpl setAnnotationKey(String annotationKey) {
			requireNonNull(annotationKey);

			this.annotationKey = annotationKey;

			return this;
		}

		public ColumnSchemaImpl setNoEntryLabel(String noEntryLabel) {
			requireNonNull(noEntryLabel);

			this.noEntryLabel = noEntryLabel;

			return this;
		}

		public ColumnSchemaImpl setIsIgnoreColumn(boolean isIgnoreColumn) {

			this.isIgnoreColumn = isIgnoreColumn==DEFAULT_IGNORE_COLUMN ? null : Boolean.valueOf(isIgnoreColumn);

			return this;
		}

		public ColumnSchemaImpl setResolver(ResolverSchema resolver) {
			requireNonNull(resolver);

			this.resolver = resolver;

			return this;
		}

		public ColumnSchemaImpl addSubstitute(SubstituteSchema substitute) {
			requireNonNull(substitute);
			requireNonNull(substitute.getType());

			substitutes.put(substitute.getType(), substitute);

			return this;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema#getSubstitute(de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteType)
		 */
		@Override
		public SubstituteSchema getSubstitute(SubstituteType type) {
			return substitutes==null ? null : substitutes.get(type);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("Column@[name=%s, layerId=%s, annotationKey=%s, noEntryLabel=%s, ignore=%b, resolver=%s, substitutes=%s]",
					name, layerId, annotationKey, noEntryLabel, isIgnoreColumn, resolver, substitutes);
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(name, layerId, annotationKey, noEntryLabel, isIgnoreColumn, resolver, substitutes);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj==this) {
				return true;
			} else if(obj instanceof ColumnSchema) {
				ColumnSchema other = (ColumnSchema) obj;
				return ClassUtils.equals(name, other.getName())
						&& ClassUtils.equals(layerId, other.getLayerId())
						&& ClassUtils.equals(annotationKey, other.getAnnotationKey())
						&& ClassUtils.equals(noEntryLabel, other.getNoEntryLabel())
						&& ClassUtils.equals(resolver, other.getResolver())
						&& isIgnoreColumn()==other.isIgnoreColumn()
						&& ClassUtils.equals(getSubstitute(SubstituteType.ADDITION), other.getSubstitute(SubstituteType.ADDITION))
						&& ClassUtils.equals(getSubstitute(SubstituteType.REPLACEMENT), other.getSubstitute(SubstituteType.REPLACEMENT))
						&& ClassUtils.equals(getSubstitute(SubstituteType.TARGET), other.getSubstitute(SubstituteType.TARGET));
			}
			return false;
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

		public SubstituteSchemaImpl setType(SubstituteType type) {
			requireNonNull(type);

			this.type = type;

			return this;
		}

		public SubstituteSchemaImpl setMemberType(MemberType memberType) {
			requireNonNull(memberType);

			this.memberType = memberType;

			return this;
		}

		public SubstituteSchemaImpl setName(String name) {
			requireNonNull(name);

			this.name = name;

			return this;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("Substitute@[type=%s memberType=%s name=%s]",
					type, memberType, name);
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(type, memberType, name);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj==this) {
				return true;
			} else if(obj instanceof SubstituteSchema) {
				SubstituteSchema other = (SubstituteSchema) obj;
				return ClassUtils.equals(type, other.getType())
						&& ClassUtils.equals(memberType, other.getMemberType())
						&& ClassUtils.equals(name, other.getName());
			}
			return false;
		}
	}

	public static class ResolverSchemaImpl implements ResolverSchema {

		private String type;
		private final Options options = new Options();

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

		public ResolverSchemaImpl setType(String type) {
			requireNonNull(type);

			this.type = type;

			return this;
		}

		public ResolverSchemaImpl addOption(String key, String value) {
			options.put(key, value);

			return this;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("Resolver@[type=%s, options=%s]",
					type, options);
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(type, options);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj==this) {
				return true;
			} else if(obj instanceof ResolverSchema) {
				ResolverSchema other = (ResolverSchema) obj;
				return ClassUtils.equals(type, other.getType())
						&& ClassUtils.equals(options, other.getOptions());
			}
			return false;
		}
	}
}
