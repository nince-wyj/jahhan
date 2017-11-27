//package net.jahhan.web.action;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.inject.Singleton;
//
//import net.jahhan.constant.enumeration.FieldTypeEnum;
//import net.jahhan.web.action.fieldtypehandler.BigDecimalFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.BigIntFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.BooleanFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.Char1024FieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.Char11FieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.Char128FieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.Char2048FieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.Char24FieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.Char32FieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.Char36FieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.Char512FieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.Char64FieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.Char65535FieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.Char8FieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.DateFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.DateOrEmptyFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.DateTimeFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.DateTimeOrEmptyFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.DoubleFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.IntFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.ListFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.MapFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.ObjectFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.SmallIntFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.TimeFieldTypeHandler;
//import net.jahhan.web.action.fieldtypehandler.TinyIntFieldTypeHandler;
//
///**
// * 字段校验处理器获取帮助类
// */
//@Singleton
//public class FieldTypeHelper {
//
//	private final Map<FieldTypeEnum, FieldTypeHandler> fieldTypeMap = new HashMap<FieldTypeEnum, FieldTypeHandler>(64,
//			1);
//
//	public FieldTypeHelper() {
//		fieldTypeMap.put(FieldTypeEnum.TYINT, new TinyIntFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.INT, new IntFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.SMALL_INT, new SmallIntFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.BIG_INT, new BigIntFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.BOOLEAN, new BooleanFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.DOUBLE, new DoubleFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.CHAR8, new Char8FieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.CHAR11, new Char11FieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.CHAR24, new Char24FieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.CHAR32, new Char32FieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.CHAR36, new Char36FieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.CHAR64, new Char64FieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.CHAR128, new Char128FieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.CHAR512, new Char512FieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.CHAR1024, new Char1024FieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.CHAR2048, new Char2048FieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.CHAR65535, new Char65535FieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.DATE, new DateFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.DATETIME, new DateTimeFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.DATE_OR_EMPTY, new DateOrEmptyFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.DATETIME_OR_EMPTY, new DateTimeOrEmptyFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.TIME, new TimeFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.LIST, new ListFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.MAP, new MapFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.OBJECT, new ObjectFieldTypeHandler());
//		fieldTypeMap.put(FieldTypeEnum.BIGDECIMAL, new BigDecimalFieldTypeHandler());
//	}
//
//	public FieldTypeHandler getFieldTypeHandler(FieldTypeEnum fieldTypeEnum) {
//		return fieldTypeMap.get(fieldTypeEnum);
//	}
//}
