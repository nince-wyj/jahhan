package net.jahhan.annotation;

import java.io.Serializable;
import java.lang.annotation.Annotation;

class GroupImpl implements Group, Serializable {

	private final String value;

	public GroupImpl(String value) {
		if (null == value) {
			throw new NullPointerException("group");
		}
		this.value = value;
	}

	public String value() {
		return this.value;
	}

	public int hashCode() {
		// This is specified in java.lang.Annotation.
		return (127 * "value".hashCode()) ^ value.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof Group)) {
			return false;
		}

		Group other = (Group) o;
		return value.equals(other.value());
	}

	public String toString() {
		return "@" + Group.class.getName() + "(value=" + value + ")";
	}

	public Class<? extends Annotation> annotationType() {
		return Group.class;
	}

	private static final long serialVersionUID = 0;
}
