package net.jahhan.spi;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperClient;
import com.frameworkx.annotation.Adaptive;

import net.jahhan.common.extension.annotation.SPI;

@SPI("zkclient")
public interface ZookeeperTransporter {

	@Adaptive({Constants.CLIENT_KEY, Constants.TRANSPORTER_KEY})
	ZookeeperClient connect(URL url);

}
