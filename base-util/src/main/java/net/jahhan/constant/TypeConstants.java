package net.jahhan.constant;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

public class TypeConstants {

    public final static Type mapList = new TypeToken<List<Map<String, String>>>() {
    }.getType();

    public final static Type objectMap = new TypeToken<Map<String, Object>>() {
    }.getType();

    public final static Type stringMap = new TypeToken<Map<String, String>>() {
    }.getType();

}
