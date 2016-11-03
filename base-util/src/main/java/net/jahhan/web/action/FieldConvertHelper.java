package net.jahhan.web.action;

import java.awt.geom.Arc2D.Double;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import net.jahhan.constant.enumeration.FieldTypeEnum;

/**
 * @author nince
 */
@Singleton
public class FieldConvertHelper {

	private final Map<Class<?>, FieldTypeEnum> fieldTypeMap = new HashMap<>(19, 1);

	public FieldConvertHelper() {
		fieldTypeMap.put(Byte.class, FieldTypeEnum.TYINT);
		fieldTypeMap.put(byte.class, FieldTypeEnum.TYINT);
		fieldTypeMap.put(Integer.class, FieldTypeEnum.INT);
		fieldTypeMap.put(int.class, FieldTypeEnum.INT);
		fieldTypeMap.put(Short.class, FieldTypeEnum.SMALL_INT);
		fieldTypeMap.put(short.class, FieldTypeEnum.SMALL_INT);
		fieldTypeMap.put(Long.class, FieldTypeEnum.BIG_INT);
		fieldTypeMap.put(long.class, FieldTypeEnum.BIG_INT);
		fieldTypeMap.put(Boolean.class, FieldTypeEnum.BOOLEAN);
		fieldTypeMap.put(boolean.class, FieldTypeEnum.BOOLEAN);
		fieldTypeMap.put(Double.class, FieldTypeEnum.DOUBLE);
		fieldTypeMap.put(double.class, FieldTypeEnum.DOUBLE);
		fieldTypeMap.put(String.class, FieldTypeEnum.CHAR65535);
		fieldTypeMap.put(Date.class, FieldTypeEnum.DATETIME_OR_EMPTY);
		fieldTypeMap.put(Timestamp.class, FieldTypeEnum.DATETIME_OR_EMPTY);
		fieldTypeMap.put(java.sql.Date.class, FieldTypeEnum.DATE_OR_EMPTY);
		fieldTypeMap.put(List.class, FieldTypeEnum.LIST);
		fieldTypeMap.put(Map.class, FieldTypeEnum.MAP);
		fieldTypeMap.put(BigDecimal.class, FieldTypeEnum.BIGDECIMAL);
	}

	public FieldTypeEnum getFieldType(Class<?> clazz) {
		return fieldTypeMap.get(clazz);
	}
}
