/**
 *
 */
package de.ims.icarus2.filedriver.schema.resolve;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.Converter.ReadMode;
import de.ims.icarus2.filedriver.schema.tabular.TableConverter.InputResolverContext;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.model.api.driver.indices.standard.MutableSingletonIndexSet;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.WritableMapping;
import de.ims.icarus2.model.api.members.item.Item;

public class MappingHandler implements BatchResolver {

		private final WritableMapping mapping, reverseMapping;
		private MappingWriter writer, reverseWriter;
		private final IndexBuffer targetIndices;
		private final MutableSingletonIndexSet sourceIndex;

		public MappingHandler(WritableMapping mapping, WritableMapping reverseMapping) {
			this.mapping = mapping;
			this.reverseMapping = reverseMapping;
			targetIndices = new IndexBuffer(1024); //TODO better starting size?
			sourceIndex = new MutableSingletonIndexSet();
		}

		public void prepareForReading(Converter converter, ReadMode mode, InputResolverContext context) {
			if(mapping!=null) {
				writer = mapping.newWriter();
				writer.begin();
			}
			if(reverseMapping!=null) {
				reverseWriter = reverseMapping.newWriter();
				reverseWriter.begin();
			}
		}

		@Override
		public Item process(ResolverContext context) throws IcarusApiException {
			long targetIndex = context.currentItem().getIndex();
			targetIndices.add(targetIndex);
//			System.out.printf("adding target index: %d%n",_long(targetIndex));
			return null;
		}

		@Override
		public void beginBatch(ResolverContext context) {
			sourceIndex.setIndex(context.currentContainer().getIndex());
//			System.out.printf("assigned source index: %d%n",_long(sourceIndex));
		}

		@Override
		public void endBatch(ResolverContext context) {
//			System.out.printf("mapping %s to %s%n",this, buffer);
			if(reverseWriter!=null) {
				reverseWriter.map(targetIndices, sourceIndex);
			}
			if(writer!=null) {
				writer.map(sourceIndex, targetIndices);
			}
			targetIndices.clear();
		}

		@Override
		public String toString() {
			return String.valueOf(sourceIndex.getIndex());
		}

		@Override
		public void complete() {
			if(reverseWriter!=null) {
				reverseWriter.end();
			}
			if(writer!=null) {
				writer.end();
			}
		}

		@Override
		public void close() {
			if(reverseWriter!=null) {
				reverseWriter.close();
				reverseWriter = null;
			}
			if(writer!=null) {
				writer.close();
				writer = null;
			}
		}
	}