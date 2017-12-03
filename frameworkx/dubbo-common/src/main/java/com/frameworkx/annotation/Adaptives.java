package com.frameworkx.annotation;

import com.google.inject.name.Named;

public class Adaptives {
	private Adaptives() {
	}

	/**
	 * Creates a {@link Named} annotation with {@code name} as the value.
	 */
	public static Adaptive adaptive(String[] value) {
		return new AdaptiveImpl(value);
	}
}
