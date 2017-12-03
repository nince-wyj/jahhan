package net.jahhan.init.initer;

import java.util.Set;

import com.frameworkx.common.extension.utils.ExtensionExtendUtil;

import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.spi.RemotingContainer;

@InitAnnocation(isLazy = false, initSequence = 3000)
public class RemotingContainerStart implements BootstrapInit {

	@Override
	public void execute() {
		Set<String> supportedExtensions = ExtensionExtendUtil.getSupportedExtensions(RemotingContainer.class);
		if (null != supportedExtensions) {
			for (String extension : supportedExtensions) {
				RemotingContainer remotingContainer = ExtensionExtendUtil.getExtension(RemotingContainer.class,
						extension);
				remotingContainer.start();
			}
		}

	}
}
