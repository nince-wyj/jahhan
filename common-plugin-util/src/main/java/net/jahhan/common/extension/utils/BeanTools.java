package net.jahhan.common.extension.utils;

import net.jahhan.common.extension.utils.apache.convert.DateConverter;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class BeanTools {
    private static ConvertUtilsBean convertUtils = BeanUtilsBean.getInstance().getConvertUtils();

    static {
        convertUtils.register(new DateConverter(), Date.class);
        convertUtils.register(new DateConverter(), java.sql.Date.class);
    }

    public static <S, D> D convertType(Object obj, Class<S> srcClass, Class<D> destClass) {
        if (srcClass.equals(destClass)) {
            return (D) JsonUtil.copyObject(obj);
        }
        Converter converter = convertUtils.lookup(srcClass, destClass);
        return converter.convert(destClass, obj);
    }

    public static Map<String, Object> toMap(Object model) {
        Map m = new LinkedHashMap();
        List<Field> fieldList = getAllClassField(model.getClass());
        copyFieldValueToMap(m, model, fieldList);

        return m;
    }

    private static void copyFieldValueToMap(Map destMap, Object src, List<Field> fields) {
        Method getMethod = null;
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isFinal(field.getModifiers())) {// 跳过final属性不能赋值
                continue;
            }
            try {
                field.setAccessible(true);
                getMethod = getGetMethod(src.getClass(), field.getName());
                if (getMethod == null) {// 有get方法优先调用get方法获取值，防止方法重写取不到正确的值
                    destMap.put(field.getName(), field.get(src));
                } else {
                    destMap.put(field.getName(), getMethod.invoke(src));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public static void copyFromMap(Object dest, Map src) {
        List<Field> fieldList = getAllClassField(dest.getClass());
        setFieldValueFromMap(dest, fieldList, src);
    }

    private static void setFieldValueFromMap(Object dest, List<Field> fields, Map src) {
        Method setMethod = null;
        Object data = null;
        Map tempMap = null;
        try {
            tempMap = src.getClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        tempMap.putAll(src);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String name = field.getName();
                Object value = src.get(name);
                if (value != null && !java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                    setMethod = getSetMethod(dest.getClass(), name, value.getClass());
                    data = null;
                    if (value != null) {
                        data = convertType(value, value.getClass(), field.getType());
                    }

                    if (setMethod == null) {
                        field.set(dest, data);
                    } else {
                        setMethod.invoke(dest, data);
                    }
                    tempMap.remove(name);
                }
            } catch (Exception e) {
            }
        }

        if (tempMap != null) {// 处理目标类没有字段，只有set的方法的问题
            Set<Map.Entry> set = tempMap.entrySet();
            String name = null;
            for (Map.Entry entry : set) {
                name = (String) entry.getKey();
                setMethod = getSetMethod(dest.getClass(), name,
                        entry.getValue() == null ? null : entry.getValue().getClass());
                if (setMethod != null) {
                    Class[] ptypes = setMethod.getParameterTypes();
                    if (ptypes != null && ptypes.length == 1) {// 说明是set属性值得方法
                        data = null;
                        if (entry.getValue() != null) {
                            data = convertType(entry.getValue(), entry.getValue().getClass(), ptypes[0]);
                        }

                        try {
                            setMethod.invoke(dest, data);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    }

    /**
     * 获取类所有字段（包括父类）
     *
     * @param clazz
     * @return
     */
    public static List<Field> getAllClassField(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
        Class superClazz = clazz.getSuperclass();
        while (superClazz != null) {
            fieldList.addAll(Arrays.asList(superClazz.getDeclaredFields()));
            superClazz = superClazz.getSuperclass();
        }

        return fieldList;
    }

    @SuppressWarnings("rawtypes")
    public static void copyBean(Object dest, Object src) {
        Map srcMap = null;
        if (src instanceof Map) {
            srcMap = (Map) src;
        } else {
            srcMap = toMap(src);
        }
        copyFromMap(dest, srcMap);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] values(Class<T> c) {
        try {
            return (T[]) c.getMethod("values").invoke(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] objectToData(Object obj) {
        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bOut);
            out.writeObject(obj);
            out.close();
            bOut.close();
            return bOut.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    /**
     * java反射bean的get方法
     *
     * @param objectClass
     * @param fieldName
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Method getGetMethod(Class objectClass, String fieldName) {
        StringBuffer sb = new StringBuffer();
        sb.append("get");
        sb.append(fieldName.substring(0, 1).toUpperCase());
        sb.append(fieldName.substring(1));
        try {
            return objectClass.getMethod(sb.toString());
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * java反射bean的set方法
     *
     * @param objectClass
     * @param fieldName
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Method getSetMethod(Class objectClass, String fieldName, Class valueType) {
        Method method = null;
        StringBuffer sb = new StringBuffer();
        sb.append("set");
        sb.append(fieldName.substring(0, 1).toUpperCase());
        sb.append(fieldName.substring(1));

        try {
            Class[] parameterTypes = new Class[]{valueType};
            if (valueType == null) {// 不知道值是什么类型的话，看看没有相应的字段的类型或相应get的方法的类型
                try {
                    Field field = objectClass.getDeclaredField(fieldName);
                    parameterTypes[0] = field.getType();
                } catch (NoSuchFieldException e) {
                    Method getMethod = getGetMethod(objectClass, fieldName);
                    if (getMethod != null) {// 不一定有字段，可能只有set和get方法
                        parameterTypes[0] = getMethod.getReturnType();
                    } else {// 没有相应的字段，又没有相应的get方法
                        throw e;
                    }
                }
            }
            method = objectClass.getMethod(sb.toString(), parameterTypes);

            return method;

        } catch (NoSuchMethodException me) {
        } catch (NoSuchFieldException me) {
        }

        if (method == null && valueType != null && isWrapClass(valueType)) {
            try {
                if (valueType.equals(Integer.class)) {
                    method = objectClass.getMethod(sb.toString(), int.class);
                } else if (valueType.equals(Long.class)) {
                    method = objectClass.getMethod(sb.toString(), long.class);
                } else if (valueType.equals(Double.class)) {
                    method = objectClass.getMethod(sb.toString(), double.class);
                } else if (valueType.equals(Float.class)) {
                    method = objectClass.getMethod(sb.toString(), float.class);
                } else if (valueType.equals(Boolean.class)) {
                    method = objectClass.getMethod(sb.toString(), boolean.class);
                } else if (valueType.equals(Character.class)) {
                    method = objectClass.getMethod(sb.toString(), char.class);
                } else if (valueType.equals(Byte.class)) {
                    method = objectClass.getMethod(sb.toString(), byte.class);
                } else if (valueType.equals(Short.class)) {
                    method = objectClass.getMethod(sb.toString(), short.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return method;
    }

    /**
     * 执行set方法
     *
     * @param o         执行对象
     * @param fieldName 属性
     * @param value     值
     */
    public static void invokeSet(Object o, String fieldName, Object value) {
        Method method = getSetMethod(o.getClass(), fieldName, value == null ? null : value.getClass());
        try {
            method.invoke(o, new Object[]{value});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行get方法
     *
     * @param o         执行对象
     * @param fieldName 属性
     */
    public static Object invokeGet(Object o, String fieldName) {
        Method method = getGetMethod(o.getClass(), fieldName);
        try {
            return method.invoke(o, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 是否是基本类型的包装类型
     */
    public static boolean isWrapClass(Class clz) {
        try {
            return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }
}