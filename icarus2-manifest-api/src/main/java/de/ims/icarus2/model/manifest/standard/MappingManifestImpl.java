/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.standard.Links.MemoryLink;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.lang.ClassUtils;

/**
 * @author Markus Gärtner
 *
 */
public class MappingManifestImpl extends AbstractLockable implements MappingManifest {

	private Coverage coverage;
	private Relation relation;

	private MappingLink inverse;

	private String sourceLayerId;
	private String targetLayerId;

	private String id;

	private final DriverManifest driverManifest;

	public MappingManifestImpl(DriverManifest driverManifest) {
		requireNonNull(driverManifest);

		this.driverManifest = driverManifest;
	}


	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(coverage, relation, sourceLayerId, targetLayerId);
	}


	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof MappingManifest) {
			MappingManifest other = (MappingManifest) obj;
			return ClassUtils.equals(coverage, other.getCoverage())
					&& ClassUtils.equals(relation, other.getRelation())
					&& ClassUtils.equals(sourceLayerId, other.getSourceLayerId())
					&& ClassUtils.equals(targetLayerId, other.getTargetLayerId());
		}

		return false;
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MappingManifest@[") //$NON-NLS-1$
		.append(sourceLayerId).append(',')
		.append(targetLayerId).append(',')
		.append(relation).append(',')
		.append(coverage).append(']');

		return sb.toString();
	}


	/**
	 * @see de.ims.icarus2.model.manifest.api.MappingManifest#getHost()
	 */
	@Override
	public DriverManifest getHost() {
		return driverManifest;
	}


	/**
	 * @see de.ims.icarus2.model.manifest.api.MappingManifest#getSourceLayerId()
	 */
	@Override
	public String getSourceLayerId() {
		return sourceLayerId;
	}


	/**
	 * @see de.ims.icarus2.model.manifest.api.MappingManifest#getTargetLayerId()
	 */
	@Override
	public String getTargetLayerId() {
		return targetLayerId;
	}


	/**
	 * @see de.ims.icarus2.model.manifest.api.MappingManifest#getRelation()
	 */
	@Override
	public Relation getRelation() {
		return relation;
	}


	/**
	 * @see de.ims.icarus2.model.manifest.api.MappingManifest#getCoverage()
	 */
	@Override
	public Coverage getCoverage() {
		return coverage;
	}

	@Override
	public MappingManifest getInverse() {
		return inverse==null ? null : inverse.get();
	}

	@Override
	public void setInverseId(String inverseId) {
		checkNotLocked();

		setInverseId0(inverseId);
	}

	protected void setInverseId0(String inverseId) {
		requireNonNull(inverseId);

		inverse = new MappingLink(inverseId);
	}

	/**
	 * @param coverage the coverage to set
	 */
	@Override
	public void setCoverage(Coverage coverage) {
		checkNotLocked();

		setCoverage0(coverage);
	}

	protected void setCoverage0(Coverage coverage) {
		requireNonNull(coverage);

		this.coverage = coverage;
	}


	/**
	 * @param relation the relation to set
	 */
	@Override
	public void setRelation(Relation relation) {
		checkNotLocked();

		setRelation0(relation);
	}

	protected void setRelation0(Relation relation) {
		requireNonNull(relation);

		this.relation = relation;
	}


	/**
	 * @param sourceLayerId the sourceLayerId to set
	 */
	@Override
	public void setSourceLayerId(String sourceLayerId) {
		checkNotLocked();

		setSourceLayerId0(sourceLayerId);
	}

	protected void setSourceLayerId0(String sourceLayerId) {
		requireNonNull(sourceLayerId);

		ManifestUtils.checkId(sourceLayerId);

		this.sourceLayerId = sourceLayerId;
	}


	/**
	 * @param targetLayerId the targetLayerId to set
	 */
	@Override
	public void setTargetLayerId(String targetLayerId) {
		checkNotLocked();

		setTargetLayerId0(targetLayerId);
	}

	protected void setTargetLayerId0(String targetLayerId) {
		requireNonNull(targetLayerId);

		ManifestUtils.checkId(targetLayerId);

		this.targetLayerId = targetLayerId;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		checkNotLocked();

		setId0(id);
	}

	protected void setId0(String id) {
		requireNonNull(id);

		ManifestUtils.checkId(id);

		this.id = id;
	}

	private class MappingLink extends MemoryLink<MappingManifest> {

		public MappingLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#resolve()
		 */
		@Override
		protected MappingManifest resolve() {
			return getDriverManifest().getMappingManifest(getId());
		}

	}
}
