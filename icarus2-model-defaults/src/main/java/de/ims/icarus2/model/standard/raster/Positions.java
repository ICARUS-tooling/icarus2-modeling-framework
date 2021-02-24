/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.raster;

import java.util.Arrays;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * @author Markus Gärtner
 *
 */
public class Positions {

	public static Position create(long... values) {
		switch (values.length) {
		case 0:
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Empty values array - cannot create position");
		case 1:
			return new Position1D(values[0]);
		case 2:
			return new Position2D(values[0], values[1]);
		case 3:
			return new Position3D(values[0], values[1], values[2]);

		default:
			return new PositionND(values);
		}
	}

	@TestableImplementation(Position.class)
	public static class Position1D implements Position, Comparable<Position1D> {

		private final long x;

		public Position1D(long x) {
			if(x<0)
				throw new IllegalArgumentException("Value must not be negative: "+x); //$NON-NLS-1$

			this.x = x;
		}

		/**
		 * @return the x
		 */
		public long getX() {
			return x;
		}

		/**
		 * @see de.ims.icarus2.model.api.raster.Position#getDimensionality()
		 */
		@Override
		public int getDimensionality() {
			return 1;
		}

		/**
		 * @see de.ims.icarus2.model.api.raster.Position#getValue(int)
		 */
		@Override
		public long getValue(int dimension) {
			if(dimension!=0)
				throw new IllegalArgumentException("Invalid dimension: "+dimension); //$NON-NLS-1$
			return x;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return (int) x;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof Position1D) {
				return x==((Position1D)obj).x;
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "["+x+"]"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Position1D o) {
			return Long.compare(x, o.x);
		}

	}

	@TestableImplementation(Position.class)
	public static class Position2D implements Position {

		private final long x, y;

		public Position2D(long x, long y) {
			if(x<0)
				throw new IllegalArgumentException("X-Value must not be negative: "+x); //$NON-NLS-1$
			if(y<0)
				throw new IllegalArgumentException("Y-Value must not be negative: "+y); //$NON-NLS-1$

			this.x = x;
			this.y = y;
		}

		/**
		 * @return the x
		 */
		public long getX() {
			return x;
		}

		/**
		 * @return the y
		 */
		public long getY() {
			return y;
		}

		/**
		 * @see de.ims.icarus2.model.api.raster.Position#getDimensionality()
		 */
		@Override
		public int getDimensionality() {
			return 2;
		}

		/**
		 * @see de.ims.icarus2.model.api.raster.Position#getValue(int)
		 */
		@Override
		public long getValue(int dimension) {
			switch (dimension) {
			case 0: return x;
			case 1: return y;

			default:
				throw new IllegalArgumentException("Illegal dimension: "+dimension); //$NON-NLS-1$
			}
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return (int) (x+y+1);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof Position2D) {
				Position2D other = (Position2D) obj;
				return x==other.x && y==other.y;
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "["+x+","+y+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	@TestableImplementation(Position.class)
	public static class Position3D implements Position {

		private final long x, y, z;

		public Position3D(long x, long y, long z) {
			if(x<0)
				throw new IllegalArgumentException("X-Value must not be negative: "+x); //$NON-NLS-1$
			if(y<0)
				throw new IllegalArgumentException("Y-Value must not be negative: "+y); //$NON-NLS-1$
			if(z<0)
				throw new IllegalArgumentException("Z-Value must not be negative: "+z); //$NON-NLS-1$

			this.x = x;
			this.y = y;
			this.z = z;
		}

		/**
		 * @return the x
		 */
		public long getX() {
			return x;
		}

		/**
		 * @return the y
		 */
		public long getY() {
			return y;
		}

		/**
		 * @return the z
		 */
		public long getZ() {
			return z;
		}

		/**
		 * @see de.ims.icarus2.model.api.raster.Position#getDimensionality()
		 */
		@Override
		public int getDimensionality() {
			return 3;
		}

		/**
		 * @see de.ims.icarus2.model.api.raster.Position#getValue(int)
		 */
		@Override
		public long getValue(int dimension) {
			switch (dimension) {
			case 0: return x;
			case 1: return y;
			case 2: return z;

			default:
				throw new IllegalArgumentException("Illegal dimension: "+dimension); //$NON-NLS-1$
			}
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return (int) (x+y+z+1);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof Position3D) {
				Position3D other = (Position3D) obj;
				return x==other.x && y==other.y && z==other.z;
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "["+x+","+y+","+z+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}

	@TestableImplementation(Position.class)
	public static class PositionND implements Position {

		private final long[] vector;
		private int hash = -1;

		public PositionND(long[] vector) {
			if (vector == null)
				throw new NullPointerException("Invalid vector");

			if(vector.length==0)
				throw new IllegalArgumentException("Vector must not be empty");

			for(int i=0; i<vector.length; i++) {
				if(vector[i] < 0)
					throw new IllegalArgumentException("Vector contains negative elements: "+Arrays.toString(vector));
			}

			this.vector = vector;
		}

		/**
		 * @see de.ims.icarus2.model.api.raster.Position#getDimensionality()
		 */
		@Override
		public int getDimensionality() {
			return vector.length;
		}

		/**
		 * @see de.ims.icarus2.model.api.raster.Position#getValue(int)
		 */
		@Override
		public long getValue(int dimension) {
			return vector[dimension];
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			if(hash==-1) {
				hash = Math.abs(Arrays.hashCode(vector));
			}

			return hash;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof PositionND) {
				PositionND other = (PositionND) obj;
				return Arrays.equals(vector, other.vector);
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return Arrays.toString(vector);
		}
	}
}
