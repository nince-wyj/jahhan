//package net.jahhan.web.action;
//
//import net.jahhan.constant.enumeration.FieldTypeEnum;
//import net.jahhan.context.BaseContext;
//
///**
// * @author nince
// */
//public class FieldHandlerHolder  {
//
//    private String fieldName;
//
//    private String defaultValue;
//
//    private FieldTypeEnum fieldTypeEnum;
//
//    public FieldHandlerHolder(String fieldName, FieldTypeEnum fieldTypeEnum, String defaultValue) {
//        this.fieldName = fieldName;
//        this.defaultValue = defaultValue;
//        this.fieldTypeEnum = fieldTypeEnum;
//    }
//
//    public FieldHandlerHolder(String fieldName, FieldTypeEnum fieldTypeEnum) {
//        this.fieldName = fieldName;
//        this.fieldTypeEnum = fieldTypeEnum;
//    }
//
//    public String getFieldName() {
//        return this.fieldName;
//    }
//
//    public FieldTypeHandler getFieldTypeHandler() {
//        return BaseContext.CTX.getFieldTypeHandler(fieldTypeEnum);
//    }
//
//    public String getDefaultValue() {
//        return this.defaultValue;
//    }
//}
