/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.jahhan.spi.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.jahhan.com.alibaba.dubbo.common.serialize.ObjectInput;
import net.jahhan.com.alibaba.dubbo.common.serialize.ObjectOutput;
import net.jahhan.common.extension.annotation.SPI;

/**
 * Serialization. (SPI, Singleton, ThreadSafe)
 * 
 * @author ding.lid
 * @author william.liangf
 */
@SPI("kryo")
public interface Serialization {

	/**
	 * get content type id
	 * 
	 * @return content type id
	 */
	byte getContentTypeId();

	/**
	 * get content type
	 * 
	 * @return content type
	 */
	String getContentType();

	/**
	 * create serializer
	 * 
	 * @param output
	 * @return serializer
	 * @throws IOException
	 */
	ObjectOutput serialize(OutputStream output) throws IOException;

	/**
	 * create deserializer
	 * 
	 * @param input
	 * @return deserializer
	 * @throws IOException
	 */
	ObjectInput deserialize(InputStream input) throws IOException;

}