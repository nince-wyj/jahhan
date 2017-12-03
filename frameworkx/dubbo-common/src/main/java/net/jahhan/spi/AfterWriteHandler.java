package net.jahhan.spi;

import net.jahhan.common.extension.annotation.SPI;
import net.jahhan.exception.JahhanException;

@SPI
public interface AfterWriteHandler {

	public void exec() throws JahhanException;

}