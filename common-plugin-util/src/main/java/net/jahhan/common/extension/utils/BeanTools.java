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
                    setMethod = getSetMethod(dest.getClass(), name);
                    data = convertType(value, value.getClass(), field.getType());
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
                setMethod = getSetMethod(dest.getClass(), name);
                if (setMethod != null) {
                    Class[] ptypes = setMethod.getParameterTypes();
                    if (ptypes != null && ptypes.length == 1) {// 说明是set属性值得方法
                        data = convertType(entry.getValue(), entry.getValue().getClass(), ptypes[0]);
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
    public static Method getSetMethod(Class objectClass, String fieldName) {
        try {
            Method getMethod = getGetMethod(objectClass, fieldName);
            Class[] parameterTypes = new Class[1];
            if (getMethod != null) {// 不一定有字段，可能只有set和get方法
                // Class returnType= getMethod.getReturnType();
                parameterTypes[0] = getMethod.getReturnType();
            } else {
                Field field = objectClass.getDeclaredField(fieldName);
                parameterTypes[0] = field.getType();
            }

            StringBuffer sb = new StringBuffer();
            sb.append("set");
            sb.append(fieldName.substring(0, 1).toUpperCase());
            sb.append(fieldName.substring(1));
            Method method = objectClass.getMethod(sb.toString(), parameterTypes);
            return method;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 执行set方法
     *
     * @param o         执行对象
     * @param fieldName 属性
     * @param value     值
     */
    public static void invokeSet(Object o, String fieldName, Object value) {
        Method method = getSetMethod(o.getClass(), fieldName);
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
}