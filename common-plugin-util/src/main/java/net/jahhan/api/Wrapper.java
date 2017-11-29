package net.jahhan.api;

import lombok.Setter;
import net.jahhan.common.extension.utils.ExtensionUtil;

public abstract class Wrapper<T> {
	protected T wrapper;
	@Setter
	private String extName;
	@Setter
	private Class<T> intefaceClass;

	public void initExtension() {
		String ext = extName;
		if (extName.contains("wrapper")) {
			wrapper = (T) ExtensionUtil.getExtension(intefaceClass, "wrapper$" + ext);
		} else {
			wrapper = (T) ExtensionUtil.getExtensionDirect(intefaceClass, ext);
		}
	}

}