package com.frameworkx.annotation;

import java.io.Serializable;
import java.lang.annotation.Annotation;

class AdaptiveImpl implements Adaptive, Serializable {

	private String[] value = {};

	public String[] value() {
		return this.value;
	}

	public AdaptiveImpl(String[] value) {
		this.value = value;
	}

	public int hashCode() {
		// This is specified in java.lang.Annotation.
		return 127 * "value".hashCode() ^ value.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof Adaptive)) {
			return false;
		}
		Adaptive other = (Adaptive) o;
		if (null == value && null == other.value()) {
			return true;
		}
		if (null == value) {
			return false;
		}
		return value.equals(other.value());
	}

	public String toString() {
		return "@" + Adaptive.class.getName();
	}

	public Class<? extends Annotation> annotationType() {
		return Adaptive.class;
	}

	private static final long serialVersionUID = 0;
}
