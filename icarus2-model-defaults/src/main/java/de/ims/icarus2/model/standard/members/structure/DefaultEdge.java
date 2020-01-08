/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.structure;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.standard.members.item.DefaultItem;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.Recyclable;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Reference;

/**
 * @author Markus Gärtner
 *
 */
@Assessable
@TestableImplementation(Edge.class)
public class DefaultEdge extends DefaultItem implements Edge, Recyclable {

	@Reference
	private Item source;
	@Reference
	private Item target;

	public DefaultEdge() {
		// no-op
	}

	public DefaultEdge(Structure structure) {
		setStructure(structure);
	}

	public DefaultEdge(Structure structure, Item source, Item target) {
		setStructure(structure);
		setSource(requireNonNull(source));
		setTarget(requireNonNull(target));
	}

	@Override
	public Structure getStructure() {
		return getContainer();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.item.DefaultItem#getContainer()
	 */
	@Override
	public Structure getContainer() {
		return (Structure) super.getContainer();
	}

	@Override
	public Item getTerminal(boolean isSource) {
		return isSource ? source : target;
	}

	@Override
	public boolean isLoop() {
		return source!=null && source==target;
	}

	@Override
	public void setTerminal(Item item, boolean isSource) {
		if(isSource) {
			source = item;
		} else {
			target = item;
		}
	}

	@Override
	public void setLocked(boolean locked) {
		// no-op
	}

	@Override
	public void setId(long id) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Edges cannot have an id asigned");
	}

	@Override
	public void setContainer(Container container) {
		super.setContainer(container);
	}

	public void setStructure(Structure structure) {
		setContainer(structure);
	}

	/**
	 * @param source the source to set
	 */
	@Override
	public void setSource(@Nullable Item source) {
		this.source = source;
	}

	/**
	 * @param target the target to set
	 */
	@Override
	public void setTarget(@Nullable Item target) {
		this.target = target;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return (source==null || target==null) ? IcarusUtils.UNSET_LONG
				: Math.min(source.getBeginOffset(), target.getBeginOffset());
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return (source==null || target==null) ? IcarusUtils.UNSET_LONG
				: Math.max(source.getEndOffset(), target.getEndOffset());
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.EDGE;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Edge#getSource()
	 */
	@Override
	public Item getSource() {
		return source;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Edge#getTarget()
	 */
	@Override
	public Item getTarget() {
		return target;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
	 */
	@Override
	public long getIndex() {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isAlive()
	 */
	@Override
	public boolean isAlive() {
		return super.isAlive() && source!=null && target!=null;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isLocked()
	 */
	@Override
	public boolean isLocked() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return super.isDirty() || source==null || target==null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();
		source = target = null;
	}

	/**
	 * Changes the revival behavior such that it no longer requires a valid
	 * {@link #getIndex() index} to be reported.
	 *
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return getContainer()!=null && source!=null && target!=null;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.item.DefaultItem#toString()
	 */
	@Override
	public String toString() {
		return super.toString()+"<"+(source==null ? "??" : source)+","+(target==null ? "??" : target)+">";
	}
}
