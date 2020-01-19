/**
 *
 */
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Markus Gärtner
 *
 */
public abstract class IqlReference extends IqlUnique {

	/**
	 * The basic identifier to be used for the variable without the type-specific
	 * prefix, such as '$' or '@'.
	 */
	@JsonProperty(IqlProperties.NAME)
	public String name;

	public abstract ReferenceType getReferenceType();

	public static class IqlVariable extends IqlReference {

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlReference#getReferenceType()
		 */
		@Override
		public ReferenceType getReferenceType() {
			return ReferenceType.VARIABLE;
		}

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
		 */
		@Override
		public IqlType getType() {
			return IqlType.VARIABLE;
		}

	}

	public static class IqlMember extends IqlReference {

		@JsonProperty(IqlProperties.MEMBER_TYPE)
		public MemberType memberType;

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlReference#getReferenceType()
		 */
		@Override
		public ReferenceType getReferenceType() {
			return ReferenceType.MEMBER;
		}

		/**
		 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
		 */
		@Override
		public IqlType getType() {
			return IqlType.MEMBER;
		}

	}

	public enum ReferenceType {

		MEMBER("member", '$'),
		VARIABLE("variable", '@'),
		;

		private final String label;
		private final char prefix;

		private ReferenceType(String label, char prefix) {
			this.label = label;
			this.prefix = prefix;
		}

		public char getPrefix() {
			return prefix;
		}

		@JsonValue
		public String getLabel() {
			return label;
		}
	}

	public enum MemberType {

		ITEM,
		EDGE,
		FRAGMENT,
		CONTAINER,
		STRUCTURE,
		;
	}
}
