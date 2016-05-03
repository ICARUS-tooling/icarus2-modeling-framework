/**
 *
 */
package de.ims.icarus2.util;


/**
 * @author Markus Gärtner
 *
 */
public class MutablePrimitives {

	public interface Primitive extends Wrapper<Object>, Cloneable {
		int intValue();
		long longValue();
		float floatValue();
		double doubleValue();
		short shortValue();
		byte byteValue();
		boolean booleanValue();

		Primitive clone();
	}

	public interface MutablePrimitive extends Primitive {
		void setInt(int value);
		void setLong(long value);
		void setFloat(float value);
		void setDouble(double value);
		void setShort(short value);
		void setByte(byte value);
		void setBoolean(boolean value);

		/**
		 * Assumes {@code wrapper} to be one of the default wrapper
		 * classes like {@link Integer}. It is the responsibility
		 * of the implementing class to cast to the correct wrapper
		 * and retrieve the primitive value.
		 *
		 * @param value
		 */
		void fromWrapper(Object wrapper);

		@Override
		MutablePrimitive clone();
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: MutablePrimitives.java 443 2016-01-11 11:31:11Z mcgaerty $
	 *
	 */
	public static final class MutableBoolean implements MutablePrimitive {
		private boolean value;

		public MutableBoolean(boolean value) {
			this.value = value;
		}

		public MutableBoolean() {
			this(false);
		}

		@Override
		public boolean booleanValue() {
			return value;
		}

		@Override
		public void setBoolean(boolean value) {
			this.value = value;
		}

		public boolean compareAndSet(boolean value, boolean expected) {
			boolean result = false;

			if(this.value==expected) {
				this.value = value;
				result = true;
			}

			return result;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#intValue()
		 */
		@Override
		public int intValue() {
			return value ? 1 : 0;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#longValue()
		 */
		@Override
		public long longValue() {
			return value ? 1L : 0L;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#floatValue()
		 */
		@Override
		public float floatValue() {
			return value ? 1F : 0F;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#doubleValue()
		 */
		@Override
		public double doubleValue() {
			return value ? 1D : 0D;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#shortValue()
		 */
		@Override
		public short shortValue() {
			return (short) intValue();
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#byteValue()
		 */
		@Override
		public byte byteValue() {
			return (byte) intValue();
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setInt(int)
		 */
		@Override
		public void setInt(int value) {
			this.value = value!=0;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setLong(long)
		 */
		@Override
		public void setLong(long value) {
			this.value = value!=0L;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setFloat(float)
		 */
		@Override
		public void setFloat(float value) {
			this.value = Float.compare(0F, value)!=0;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setDouble(double)
		 */
		@Override
		public void setDouble(double value) {
			this.value = Double.compare(0D, value)!=0;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setShort(short)
		 */
		@Override
		public void setShort(short value) {
			setInt(value);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setByte(byte)
		 */
		@Override
		public void setByte(byte value) {
			setInt(value);
		}

		@Override
		public MutableBoolean clone() {
			return new MutableBoolean(value);
		}

		@Override
		public int hashCode() {
			return value ? 1 : 0;
		}

		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof MutableBoolean) {
				return value==((MutableBoolean)obj).value;
			}
			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		/**
		 * @see de.ims.icarus2.util.Wrapper#get()
		 */
		@Override
		public Object get() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#fromWrapper(java.lang.Object)
		 */
		@Override
		public void fromWrapper(Object wrapper) {
			value = ((Boolean)wrapper).booleanValue();
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: MutablePrimitives.java 443 2016-01-11 11:31:11Z mcgaerty $
	 *
	 */
	public static final class MutableInteger implements MutablePrimitive {
		private int value;

		public MutableInteger(int value) {
			this.value = value;
		}

		public MutableInteger() {
			this(0);
		}

		@Override
		public int intValue() {
			return value;
		}

		@Override
		public void setInt(int value) {
			this.value = value;
		}

		public int increment() {
			value++;
			return value;
		}

		public int increment(int delta) {
			value += delta;
			return value;
		}

		public int decrement() {
			value--;
			return value;
		}

		public int decrement(int delta) {
			value -= delta;
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#longValue()
		 */
		@Override
		public long longValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#floatValue()
		 */
		@Override
		public float floatValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#doubleValue()
		 */
		@Override
		public double doubleValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#shortValue()
		 */
		@Override
		public short shortValue() {
			return (short) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#byteValue()
		 */
		@Override
		public byte byteValue() {
			return (byte) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#booleanValue()
		 */
		@Override
		public boolean booleanValue() {
			return value!=0;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setLong(long)
		 */
		@Override
		public void setLong(long value) {
			this.value = (int) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setFloat(float)
		 */
		@Override
		public void setFloat(float value) {
			this.value = (int) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setDouble(double)
		 */
		@Override
		public void setDouble(double value) {
			this.value = (int) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setShort(short)
		 */
		@Override
		public void setShort(short value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setByte(byte)
		 */
		@Override
		public void setByte(byte value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setBoolean(boolean)
		 */
		@Override
		public void setBoolean(boolean value) {
			this.value = value ? 1 : 0;
		}

		@Override
		public MutableInteger clone() {
			return new MutableInteger(value);
		}

		@Override
		public int hashCode() {
			return value;
		}

		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof MutableInteger) {
				return value==((MutableInteger)obj).value;
			}
			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		/**
		 * @see de.ims.icarus2.util.Wrapper#get()
		 */
		@Override
		public Object get() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#fromWrapper(java.lang.Object)
		 */
		@Override
		public void fromWrapper(Object wrapper) {
			value = ((Number)wrapper).intValue();
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: MutablePrimitives.java 443 2016-01-11 11:31:11Z mcgaerty $
	 *
	 */
	public static final class MutableFloat implements MutablePrimitive {
		private float value;

		public MutableFloat(float value) {
			this.value = value;
		}

		public MutableFloat() {
			this(0F);
		}

		@Override
		public float floatValue() {
			return value;
		}

		@Override
		public void setFloat(float value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#intValue()
		 */
		@Override
		public int intValue() {
			return (int) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#longValue()
		 */
		@Override
		public long longValue() {
			return (long) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#doubleValue()
		 */
		@Override
		public double doubleValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#shortValue()
		 */
		@Override
		public short shortValue() {
			return (short) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#byteValue()
		 */
		@Override
		public byte byteValue() {
			return (byte) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#booleanValue()
		 */
		@Override
		public boolean booleanValue() {
			return Float.compare(0F, value)!=0;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setInt(int)
		 */
		@Override
		public void setInt(int value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setLong(long)
		 */
		@Override
		public void setLong(long value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setDouble(double)
		 */
		@Override
		public void setDouble(double value) {
			this.value = (float) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setShort(short)
		 */
		@Override
		public void setShort(short value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setByte(byte)
		 */
		@Override
		public void setByte(byte value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setBoolean(boolean)
		 */
		@Override
		public void setBoolean(boolean value) {
			this.value = value ? 1F : 0F;
		}

		@Override
		public MutableFloat clone() {
			return new MutableFloat(value);
		}

		@Override
		public int hashCode() {
			return (int) value;
		}

		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof MutableFloat) {
				return Float.compare(value, ((MutableFloat)obj).value)==0;
			}
			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		/**
		 * @see de.ims.icarus2.util.Wrapper#get()
		 */
		@Override
		public Object get() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#fromWrapper(java.lang.Object)
		 */
		@Override
		public void fromWrapper(Object wrapper) {
			value = ((Number)wrapper).floatValue();
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: MutablePrimitives.java 443 2016-01-11 11:31:11Z mcgaerty $
	 *
	 */
	public static final class MutableDouble implements MutablePrimitive {
		private double value;

		public MutableDouble(double value) {
			this.value = value;
		}

		public MutableDouble() {
			this(0D);
		}

		@Override
		public double doubleValue() {
			return value;
		}

		@Override
		public void setDouble(double value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#intValue()
		 */
		@Override
		public int intValue() {
			return (int) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#longValue()
		 */
		@Override
		public long longValue() {
			return (long) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#floatValue()
		 */
		@Override
		public float floatValue() {
			return (float) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#shortValue()
		 */
		@Override
		public short shortValue() {
			return (short) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#byteValue()
		 */
		@Override
		public byte byteValue() {
			return (byte) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#booleanValue()
		 */
		@Override
		public boolean booleanValue() {
			return Double.compare(0D, value)!=0;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setInt(int)
		 */
		@Override
		public void setInt(int value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setLong(long)
		 */
		@Override
		public void setLong(long value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setFloat(float)
		 */
		@Override
		public void setFloat(float value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setShort(short)
		 */
		@Override
		public void setShort(short value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setByte(byte)
		 */
		@Override
		public void setByte(byte value) {
			this.value = value;

		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setBoolean(boolean)
		 */
		@Override
		public void setBoolean(boolean value) {
			this.value = value ? 1D : 0D;
		}

		@Override
		public MutableDouble clone() {
			return new MutableDouble(value);
		}

		@Override
		public int hashCode() {
			return (int) value;
		}

		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof MutableDouble) {
				return Double.compare(value, ((MutableDouble)obj).value)==0;
			}
			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		/**
		 * @see de.ims.icarus2.util.Wrapper#get()
		 */
		@Override
		public Object get() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#fromWrapper(java.lang.Object)
		 */
		@Override
		public void fromWrapper(Object wrapper) {
			value = ((Number)wrapper).doubleValue();
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: MutablePrimitives.java 443 2016-01-11 11:31:11Z mcgaerty $
	 *
	 */
	public static final class MutableLong implements MutablePrimitive {
		private long value;

		public MutableLong(long value) {
			this.value = value;
		}

		public MutableLong() {
			this(0L);
		}

		@Override
		public long longValue() {
			return value;
		}

		@Override
		public void setLong(long value) {
			this.value = value;
		}

		public long increment() {
			value++;
			return value;
		}

		public long increment(long delta) {
			value += delta;
			return value;
		}

		public long decrement() {
			value--;
			return value;
		}

		public long decrement(long delta) {
			value -= delta;
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#intValue()
		 */
		@Override
		public int intValue() {
			return (int) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#floatValue()
		 */
		@Override
		public float floatValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#doubleValue()
		 */
		@Override
		public double doubleValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#shortValue()
		 */
		@Override
		public short shortValue() {
			return (short) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#byteValue()
		 */
		@Override
		public byte byteValue() {
			return (byte) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#booleanValue()
		 */
		@Override
		public boolean booleanValue() {
			return value!=0L;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setInt(int)
		 */
		@Override
		public void setInt(int value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setFloat(float)
		 */
		@Override
		public void setFloat(float value) {
			this.value = (long) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setDouble(double)
		 */
		@Override
		public void setDouble(double value) {
			this.value = (long) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setShort(short)
		 */
		@Override
		public void setShort(short value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setByte(byte)
		 */
		@Override
		public void setByte(byte value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setBoolean(boolean)
		 */
		@Override
		public void setBoolean(boolean value) {
			this.value = value ? 1L : 0L;
		}

		@Override
		public MutableLong clone() {
			return new MutableLong(value);
		}

		@Override
		public int hashCode() {
			return (int) value;
		}

		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof MutableLong) {
				return value==((MutableLong)obj).value;
			}
			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		/**
		 * @see de.ims.icarus2.util.Wrapper#get()
		 */
		@Override
		public Object get() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#fromWrapper(java.lang.Object)
		 */
		@Override
		public void fromWrapper(Object wrapper) {
			value = ((Number)wrapper).longValue();
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: MutablePrimitives.java 443 2016-01-11 11:31:11Z mcgaerty $
	 *
	 */
	public static final class MutableShort implements MutablePrimitive {
		private short value;

		public MutableShort(short value) {
			this.value = value;
		}

		public MutableShort() {
			this((short) 0);
		}

		@Override
		public short shortValue() {
			return value;
		}

		@Override
		public void setShort(short value) {
			this.value = value;
		}

		public short increment() {
			value++;
			return value;
		}

		public short increment(short delta) {
			value += delta;
			return value;
		}

		public short decrement() {
			value--;
			return value;
		}

		public short decrement(short delta) {
			value -= delta;
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#intValue()
		 */
		@Override
		public int intValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#longValue()
		 */
		@Override
		public long longValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#floatValue()
		 */
		@Override
		public float floatValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#doubleValue()
		 */
		@Override
		public double doubleValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#byteValue()
		 */
		@Override
		public byte byteValue() {
			return (byte) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#booleanValue()
		 */
		@Override
		public boolean booleanValue() {
			return value!=(short)0;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setInt(int)
		 */
		@Override
		public void setInt(int value) {
			this.value = (short) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setLong(long)
		 */
		@Override
		public void setLong(long value) {
			this.value = (short) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setFloat(float)
		 */
		@Override
		public void setFloat(float value) {
			this.value = (short) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setDouble(double)
		 */
		@Override
		public void setDouble(double value) {
			this.value = (short) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setByte(byte)
		 */
		@Override
		public void setByte(byte value) {
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setBoolean(boolean)
		 */
		@Override
		public void setBoolean(boolean value) {
			this.value = (short) (value ? 1 : 0);
		}

		@Override
		public MutableShort clone() {
			return new MutableShort(value);
		}

		@Override
		public int hashCode() {
			return value;
		}

		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof MutableShort) {
				return value==((MutableShort)obj).value;
			}
			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		/**
		 * @see de.ims.icarus2.util.Wrapper#get()
		 */
		@Override
		public Object get() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#fromWrapper(java.lang.Object)
		 */
		@Override
		public void fromWrapper(Object wrapper) {
			value = ((Number)wrapper).shortValue();
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: MutablePrimitives.java 443 2016-01-11 11:31:11Z mcgaerty $
	 *
	 */
	public static final class MutableChar implements MutablePrimitive {
		private char value;

		public MutableChar(char value) {
			this.value = value;
		}

		public MutableChar() {
			this((char) 0);
		}

		@Override
		public short shortValue() {
			return (short) value;
		}

		@Override
		public void setShort(short value) {
			this.value = (char) value;
		}

		public char increment() {
			value++;
			return value;
		}

		public char increment(char delta) {
			value += delta;
			return value;
		}

		public char decrement() {
			value--;
			return value;
		}

		public char decrement(char delta) {
			value -= delta;
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#intValue()
		 */
		@Override
		public int intValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#longValue()
		 */
		@Override
		public long longValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#floatValue()
		 */
		@Override
		public float floatValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#doubleValue()
		 */
		@Override
		public double doubleValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#byteValue()
		 */
		@Override
		public byte byteValue() {
			return (byte) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#booleanValue()
		 */
		@Override
		public boolean booleanValue() {
			return value!=(short)0;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setInt(int)
		 */
		@Override
		public void setInt(int value) {
			this.value = (char) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setLong(long)
		 */
		@Override
		public void setLong(long value) {
			this.value = (char) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setFloat(float)
		 */
		@Override
		public void setFloat(float value) {
			this.value = (char) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setDouble(double)
		 */
		@Override
		public void setDouble(double value) {
			this.value = (char) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setByte(byte)
		 */
		@Override
		public void setByte(byte value) {
			this.value = (char) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setBoolean(boolean)
		 */
		@Override
		public void setBoolean(boolean value) {
			this.value = (char) (value ? 1 : 0);
		}

		@Override
		public MutableChar clone() {
			return new MutableChar(value);
		}

		@Override
		public int hashCode() {
			return value;
		}

		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof MutableChar) {
				return value==((MutableChar)obj).value;
			}
			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		/**
		 * @see de.ims.icarus2.util.Wrapper#get()
		 */
		@Override
		public Object get() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#fromWrapper(java.lang.Object)
		 */
		@Override
		public void fromWrapper(Object wrapper) {
			value = (char) ((Number)wrapper).longValue();
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: MutablePrimitives.java 443 2016-01-11 11:31:11Z mcgaerty $
	 *
	 */
	public static final class MutableByte implements MutablePrimitive {
		private byte value;

		public MutableByte(byte value) {
			this.value = value;
		}

		public MutableByte() {
			this((byte) 0);
		}

		@Override
		public byte byteValue() {
			return value;
		}

		@Override
		public void setByte(byte value) {
			this.value = value;
		}

		public byte increment() {
			value++;
			return value;
		}

		public byte increment(byte delta) {
			value += delta;
			return value;
		}

		public byte decrement() {
			value--;
			return value;
		}

		public byte decrement(byte delta) {
			value -= delta;
			return value;
		}

		public byte and(byte delta) {
			value &= delta;
			return value;
		}

		public byte nand(byte b) {
			value &= ~b;
			return value;
		}

		public byte or(byte b) {
			value |= b;
			return value;
		}

		public byte nor(byte b) {
			value |= ~b;
			return value;
		}

		public byte xor(byte b) {
			value ^= b;
			return value;
		}

		public byte not() {
			value = (byte) ~value;
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#intValue()
		 */
		@Override
		public int intValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#longValue()
		 */
		@Override
		public long longValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#floatValue()
		 */
		@Override
		public float floatValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#doubleValue()
		 */
		@Override
		public double doubleValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#shortValue()
		 */
		@Override
		public short shortValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#booleanValue()
		 */
		@Override
		public boolean booleanValue() {
			return value!=(byte)0;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setInt(int)
		 */
		@Override
		public void setInt(int value) {
			this.value = (byte) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setLong(long)
		 */
		@Override
		public void setLong(long value) {
			this.value = (byte) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setFloat(float)
		 */
		@Override
		public void setFloat(float value) {
			this.value = (byte) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setDouble(double)
		 */
		@Override
		public void setDouble(double value) {
			this.value = (byte) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setShort(short)
		 */
		@Override
		public void setShort(short value) {
			this.value = (byte) value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setBoolean(boolean)
		 */
		@Override
		public void setBoolean(boolean value) {
			this.value = (byte) (value ? 1 : 0);
		}

		@Override
		public MutableByte clone() {
			return new MutableByte(value);
		}

		@Override
		public int hashCode() {
			return value;
		}

		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof MutableByte) {
				return value==((MutableByte)obj).value;
			}
			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		/**
		 * @see de.ims.icarus2.util.Wrapper#get()
		 */
		@Override
		public Object get() {
			return value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#fromWrapper(java.lang.Object)
		 */
		@Override
		public void fromWrapper(Object wrapper) {
			value = ((Number)wrapper).byteValue();
		}
	}

	public static class GenericMutablePrimitive implements MutablePrimitive {

		private double storage = 0D;

		public GenericMutablePrimitive() {
			// no-op
		}

		public GenericMutablePrimitive(double value) {
			setDouble(value);
		}

		public GenericMutablePrimitive(float value) {
			setFloat(value);
		}

		public GenericMutablePrimitive(int value) {
			setInt(value);
		}

		public GenericMutablePrimitive(long value) {
			setLong(value);
		}

		public GenericMutablePrimitive(short value) {
			setShort(value);
		}

		public GenericMutablePrimitive(byte value) {
			setByte(value);
		}

		public GenericMutablePrimitive(boolean value) {
			setBoolean(value);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#intValue()
		 */
		@Override
		public int intValue() {
			return (int)longValue();
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#longValue()
		 */
		@Override
		public long longValue() {
			return Double.doubleToRawLongBits(storage);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#floatValue()
		 */
		@Override
		public float floatValue() {
			return (float) storage;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#doubleValue()
		 */
		@Override
		public double doubleValue() {
			return storage;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#shortValue()
		 */
		@Override
		public short shortValue() {
			return (short)longValue();
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#byteValue()
		 */
		@Override
		public byte byteValue() {
			return (byte) longValue();
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#booleanValue()
		 */
		@Override
		public boolean booleanValue() {
			return longValue()!=0L;
		}

		/**
		 * @see de.ims.icarus2.util.Wrapper#get()
		 */
		@Override
		public Object get() {
			return storage;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setInt(int)
		 */
		@Override
		public void setInt(int value) {
			setLong(value);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setLong(long)
		 */
		@Override
		public void setLong(long value) {
			storage = Double.longBitsToDouble(value);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setFloat(float)
		 */
		@Override
		public void setFloat(float value) {
			storage = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setDouble(double)
		 */
		@Override
		public void setDouble(double value) {
			storage = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setShort(short)
		 */
		@Override
		public void setShort(short value) {
			setLong(value);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setByte(byte)
		 */
		@Override
		public void setByte(byte value) {
			setLong(value);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setBoolean(boolean)
		 */
		@Override
		public void setBoolean(boolean value) {
			setLong(value ? 1L : 0L);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#fromWrapper(java.lang.Object)
		 */
		@Override
		public void fromWrapper(Object wrapper) {
			if(wrapper instanceof Boolean) {
				setBoolean(((Boolean)wrapper).booleanValue());
			} else {
				setDouble(((Number)wrapper).doubleValue());
			}
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#clone()
		 */
		@Override
		public MutablePrimitive clone() {
			return new GenericMutablePrimitive(storage);
		}

		@Override
		public int hashCode() {
			return intValue();
		}

		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof GenericMutablePrimitive) {
				return Double.compare(storage, ((GenericMutablePrimitive)obj).storage)==0;
			}
			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(intValue());
		}

	}

	public static class GenericTypeAwareMutablePrimitive implements MutablePrimitive {

		private long storage = 0L;
		private byte type = NULL;

		public static final byte NULL = 0x0;
		public static final byte BOOLEAN = 0x1;
		public static final byte BYTE = 0x2;
		public static final byte SHORT = 0x3;
		public static final byte INTEGER = 0x4;
		public static final byte LONG = 0x5;
		public static final byte FLOAT = 0x6;
		public static final byte DOUBLE = 0x7;

		public GenericTypeAwareMutablePrimitive() {
			// no-op
		}

		public GenericTypeAwareMutablePrimitive(double value) {
			setDouble(value);
		}

		public GenericTypeAwareMutablePrimitive(float value) {
			setFloat(value);
		}

		public GenericTypeAwareMutablePrimitive(int value) {
			setInt(value);
		}

		public GenericTypeAwareMutablePrimitive(long value) {
			setLong(value);
		}

		public GenericTypeAwareMutablePrimitive(short value) {
			setShort(value);
		}

		public GenericTypeAwareMutablePrimitive(byte value) {
			setByte(value);
		}

		public GenericTypeAwareMutablePrimitive(boolean value) {
			setBoolean(value);
		}


		public GenericTypeAwareMutablePrimitive(long value, byte type) {
			setLong(value);
			setType(type);
		}

		public void setType(byte type) {
			if(type<NULL || type>DOUBLE)
				throw new IllegalArgumentException("Not a valid type constant: "+type);

			this.type = type;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#intValue()
		 */
		@Override
		public int intValue() {
			return (int)longValue();
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#longValue()
		 */
		@Override
		public long longValue() {
			return storage;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#floatValue()
		 */
		@Override
		public float floatValue() {
			return (float) doubleValue();
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#doubleValue()
		 */
		@Override
		public double doubleValue() {
			return Double.longBitsToDouble(storage);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#shortValue()
		 */
		@Override
		public short shortValue() {
			return (short)longValue();
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#byteValue()
		 */
		@Override
		public byte byteValue() {
			return (byte) longValue();
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.Primitive#booleanValue()
		 */
		@Override
		public boolean booleanValue() {
			return longValue()!=0L;
		}

		/**
		 * @see de.ims.icarus2.util.Wrapper#get()
		 */
		@Override
		public Object get() {
			switch (type) {
			case BOOLEAN: return booleanValue();
			case BYTE: return byteValue();
			case SHORT: return shortValue();
			case INTEGER: return intValue();
			case LONG: return longValue();
			case FLOAT: return floatValue();
			case DOUBLE: return doubleValue();

			default:
				return null;
			}
		}

		public void setNull() {
			storage = 0L;
			setType(NULL);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setInt(int)
		 */
		@Override
		public void setInt(int value) {
			setLong0(value);
			setType(INTEGER);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setLong(long)
		 */
		@Override
		public void setLong(long value) {
			setLong0(value);
			setType(LONG);
		}

		private void setLong0(long value) {
			storage = value;
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setFloat(float)
		 */
		@Override
		public void setFloat(float value) {
			setDoubleg0(value);
			setType(FLOAT);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setDouble(double)
		 */
		@Override
		public void setDouble(double value) {
			setDoubleg0(value);
			setType(DOUBLE);
		}

		private void setDoubleg0(double value) {
			storage = Double.doubleToRawLongBits(value);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setShort(short)
		 */
		@Override
		public void setShort(short value) {
			setLong0(value);
			setType(SHORT);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setByte(byte)
		 */
		@Override
		public void setByte(byte value) {
			setLong0(value);
			setType(BYTE);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#setBoolean(boolean)
		 */
		@Override
		public void setBoolean(boolean value) {
			setLong0(value ? 1L : 0L);
			setType(BOOLEAN);
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#fromWrapper(java.lang.Object)
		 */
		@Override
		public void fromWrapper(Object wrapper) {
			if(wrapper==null) {
				setNull();
			} else if(wrapper instanceof Boolean) {
				setBoolean(((Boolean)wrapper).booleanValue());
			} else {
				switch (wrapper.getClass().getSimpleName()) {
				case "Byte":
					setByte(((Byte)wrapper).byteValue());
					break;

				case "Short":
					setShort(((Short)wrapper).shortValue());
					break;

				case "Integer":
					setInt(((Integer)wrapper).intValue());
					break;

				case "Character":
					setLong(((Character)wrapper).charValue());
					break;

				case "Long":
					setLong(((Long)wrapper).longValue());
					break;

				case "Float":
					setFloat(((Float)wrapper).floatValue());
					break;

				case "Double":
					setDouble(((Double)wrapper).doubleValue());
					break;

				default:
					setNull();
					break;
				}
			}
		}

		/**
		 * @see de.ims.icarus2.util.MutablePrimitives.MutablePrimitive#clone()
		 */
		@Override
		public MutablePrimitive clone() {
			return new GenericTypeAwareMutablePrimitive(storage);
		}

		@Override
		public int hashCode() {
			return intValue();
		}

		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof GenericTypeAwareMutablePrimitive) {
				return storage==((GenericTypeAwareMutablePrimitive)obj).storage;
			}
			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(storage);
		}

	}
}
