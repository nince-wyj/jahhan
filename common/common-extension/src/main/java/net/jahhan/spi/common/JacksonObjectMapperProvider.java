package net.jahhan.spi.common;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.jahhan.common.extension.annotation.SPI;

/**
 * Created by dylan on 11/12/14.
 */
@SPI("jackson")
public interface JacksonObjectMapperProvider {
    public ObjectMapper getObjectMapper();
}
