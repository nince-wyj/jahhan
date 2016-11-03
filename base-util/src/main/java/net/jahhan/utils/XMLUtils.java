package net.jahhan.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLUtils {
	private static Logger logger = LoggerFactory.getLogger(XMLUtils.class);

	public static String objectToXML(Object obj) {

		String result = null;

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			JAXBContext context = JAXBContext.newInstance(obj.getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);
			marshaller.marshal(obj, baos);
			result = baos.toString("utf-8");
			;
		} catch (Exception e) {
			logger.error("ObjectToXML", e);
		}
		return result;
	}

	public static String mapToXml(Map<String, Object> dataMap) {
		synchronized (XMLUtils.class) {
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append("<root>");
			Set<String> objSet = dataMap.keySet();
			for (String key : objSet) {
				if (key == null) {
					continue;
				}
				strBuilder.append("<").append(key).append(">");
				Object value = dataMap.get(key);
				strBuilder.append(coverter(value));
				strBuilder.append("</").append(key).append(">\n");
			}
			strBuilder.append("</root>");
			return strBuilder.toString();
		}
	}

	private static String coverter(Object object) {
		if (object instanceof Object[]) {
			return coverter((Object[]) object);
		}
		if (object instanceof Collection) {
			return coverter((Collection<?>) object);
		}
		StringBuilder strBuilder = new StringBuilder();
		if (isObject(object)) {
			Class<? extends Object> clz = object.getClass();
			Field[] fields = clz.getDeclaredFields();

			for (Field field : fields) {
				if (field == null) {
					continue;
				}
				field.setAccessible(true);
				String fieldName = field.getName();
				Object value = null;
				try {
					value = field.get(object);
				} catch (IllegalArgumentException e) {
					continue;
				} catch (IllegalAccessException e) {
					continue;
				}
				if (value == null) {
					continue;
				}
				strBuilder.append("<").append(fieldName).append(" className=\"").append(value.getClass().getName())
						.append("\">\n");
				if (isObject(value)) {
					strBuilder.append(coverter(value));
				} else {
					strBuilder.append(value.toString());
				}
				strBuilder.append("</").append(fieldName).append(">\n");
			}
		} else if (object == null) {
			strBuilder.append("null");
		} else {
			strBuilder.append(object.toString());
		}
		return strBuilder.toString();
	}

	private static boolean isObject(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof String) {
			return false;
		}
		if (obj instanceof Integer) {
			return false;
		}
		if (obj instanceof Double) {
			return false;
		}
		if (obj instanceof Float) {
			return false;
		}
		if (obj instanceof Byte) {
			return false;
		}
		if (obj instanceof Long) {
			return false;
		}
		if (obj instanceof Character) {
			return false;
		}
		if (obj instanceof Short) {
			return false;
		}
		if (obj instanceof Boolean) {
			return false;
		}
		return true;
	}

	public static <T> T XMLToObject(Class<T> c, String xmlStr) {
		return XMLToObject(c, new ByteArrayInputStream(xmlStr.getBytes()));
	}

	@SuppressWarnings("unchecked")
	public static <T> T XMLToObject(Class<T> c, InputStream is) {
		try {
			JAXBContext context = JAXBContext.newInstance(c);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			return (T) unmarshaller.unmarshal(is);
		} catch (Exception e) {
			logger.error("XMLToObject", e);
		}
		return null;
	}

	public static Map<String, String> XMLToMap(InputStream is)
			throws ParserConfigurationException, SAXException, IOException {
		Map<String, String> map = new HashMap<>();
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(is);
		Element root = document.getDocumentElement();
		NodeList list = root.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			map.put(node.getNodeName(), node.getTextContent());
		}
		return map;
	}

	public static Map<String, String> XMLToMap(String xmlStr, String encode) {
		Map<String, String> map = new HashMap<>();
		try {
			byte bytes[] = xmlStr.getBytes(encode);
			try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
				map = XMLToMap(in);
			} catch (Exception e) {
				logger.error("XMLToMap", e);
			}
		} catch (Exception e) {
			logger.error("XMLToMap", e);
		}
		return map;
	}

	public static Map<String, String> XMLToMap(String xmlStr) {
		return XMLToMap(xmlStr, "utf-8");
	}

	public static <T> XMLCodec<T> createXMLCodec(Class<T> clazz) {
		return new XMLCodec<T>(clazz);
	}

	public static class XMLCodec<T> {
		Marshaller marshaller;
		Unmarshaller unmarshaller;

		public XMLCodec(Class<T> clazz) {
			try {
				JAXBContext context = JAXBContext.newInstance(clazz);
				marshaller = context.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);

				unmarshaller = context.createUnmarshaller();
			} catch (Exception e) {
				logger.error("XMLCodec", e);
			}
		}

		@SuppressWarnings("unchecked")
		public T XMLToObject(InputStream is) {
			try {
				return (T) unmarshaller.unmarshal(is);
			} catch (Exception e) {
				logger.error("XMLToObject", e);
			}
			return null;
		}

		public T XMLToObject(String xmlStr) {
			try {
				return XMLToObject(new ByteArrayInputStream(xmlStr.getBytes("UTF-8")));
			} catch (Exception e) {
				logger.error("XMLToObject", e);
			}
			return null;
		}

		public String ObjectToXML(T obj) {
			String result = null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				marshaller.marshal(obj, baos);
				result = new String(baos.toByteArray(), "UTF-8");
			} catch (Exception e) {
				logger.error("ObjectToXML", e);
			}
			return result;
		}
	}

}