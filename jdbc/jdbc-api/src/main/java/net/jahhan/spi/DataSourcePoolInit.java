package net.jahhan.spi;

import java.beans.PropertyVetoException;
import java.util.Properties;

import net.jahhan.common.extension.annotation.SPI;

@SPI("c3p0")
public interface DataSourcePoolInit {
	void initDataSource(String dataSource, Properties property) throws PropertyVetoException;

	public void init();
}
