package net.jahhan.annotation;

import com.google.inject.name.Named;

public class Groups {
	private Groups() {}

	  /**
	   * Creates a {@link Named} annotation with {@code name} as the value.
	   */
	  public static Group group(String name) {
	    return new GroupImpl(name);
	  }
}
