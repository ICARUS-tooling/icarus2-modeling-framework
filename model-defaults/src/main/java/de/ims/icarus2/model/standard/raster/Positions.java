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
 *
 */
package de.ims.icarus2.model.standard.raster;

import java.util.Arrays;

import de.ims.icarus2.model.api.raster.Position;

/**
 * @author Markus Gärtner
 *
 */
public class Positions {

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
			// FIXME consider int overflow!
			return (int)(x-o.x);
		}

	}

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
