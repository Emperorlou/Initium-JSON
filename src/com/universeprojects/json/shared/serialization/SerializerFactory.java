package com.universeprojects.json.shared.serialization;

import com.universeprojects.json.shared.JSONArray;
import com.universeprojects.json.shared.JSONObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The purpose of this class is to locate a serializer for a given data type.
 */
public class SerializerFactory {

    private static Map<Class<?>, Serializer> serializerByType = new LinkedHashMap<>();
    private static Map<String, Class<?>> classesByName = new LinkedHashMap<>();
    private static Map<String, Class<?>> classesBySimpleName = new LinkedHashMap<>();

    private static BasicSerializer basicSerializer = new BasicSerializer();

    @SuppressWarnings("unused")
    public static final SerializedDataList EMPTY_DATA_LIST = createSerializedDataList();
    public static final SerializedDataMap EMPTY_DATA_MAP = createSerializedDataMap(Collections.EMPTY_MAP);

    @SuppressWarnings({"unchecked", "unused"})
    public static <K, V> SerializedDataMap<K, V> emptyDataMap() {
        return EMPTY_DATA_MAP;
    }

    static {
        for (Class<?> cl : BasicSerializer.supportedTypes) {
            registerSerializer(cl, basicSerializer);
        }
    }

    public static void registerSerializer(Class<?> cl, Serializer ser) {
        serializerByType.put(cl, ser);
        classesByName.put(cl.getName(), cl);
        classesBySimpleName.put(cl.getSimpleName(), cl);
    }

    @SuppressWarnings("unused")
    public static Class<?> getCompatibleClassForName(String name) {
        if (name.contains(".")) {
            return classesByName.get(name);
        } else {
            return classesBySimpleName.get(name);
        }
    }

    @SuppressWarnings("unused")
    public static boolean isCompatible(Class<?> testClass) {
        return testClass.isEnum() || serializerByType.containsKey(testClass);
    }

    public static Class<?> getCompatibleClassForSimpleName(String name) {
        return classesBySimpleName.get(name);
    }

    /**
     * Class.getSimpleName() is not supported in GWT 2.5
     */
    public static String getSimpleName(Class<?> clazz) {
        assert clazz!=null;
        String name = clazz.getName();
        int dotIndex = name.lastIndexOf(".");
        if (dotIndex == -1) return name;
        return name.substring(dotIndex + 1);
    }

    /**
     * Class.getSimpleName() is not supported in GWT 2.5
     */
    public static String getSimpleClassName(Object obj) {
        assert obj!=null;
        return getSimpleName(obj.getClass());
    }

    @SuppressWarnings("unused")
    public static String serializeList(Object... structuredData) {
        return createSerializedDataList(structuredData).getSerializedData();
    }

    @SuppressWarnings("unused")
    public static String serializeMap(Map<?, ?> structuredData) {
        return createSerializedDataMap(structuredData).getSerializedData();
    }

    public static SerializedDataList createSerializedDataList(Object... structuredData) {
        return createSerializedDataList(null, structuredData);
    }

    public static SerializedDataList createSerializedDataList(String serializedData) {
        return createSerializedDataList(serializedData, null);
    }

    public static Object[] deserializeList(String serializedData, Class<?>[] decodingClassArray) {
        SerializedDataList list = createSerializedDataList(serializedData);
        return list.deserializeData(decodingClassArray);
    }

    @SuppressWarnings("unused")
    public static Object[] deserializeConsistentList(String serializedData, Class<?> decodingClass) {
        SerializedDataList list = createSerializedDataList(serializedData);
        return list.deserializeConsistentData(decodingClass);
    }

    public static <K, V> SerializedDataMap<K, V> createSerializedDataMap(Map<K, V> structuredData) {
        return createSerializedDataMap(null, structuredData);
    }

    public static <K, V> SerializedDataMap<K, V> createSerializedDataMap(String serializedData) {
        return createSerializedDataMap(serializedData, null);
    }

    @SuppressWarnings("unused")
    public static <K, V> Map<K, V> deserializeMap(String serializedData, Class<K> decodingKeyClass,
                                                  Class<V> decodingValueClass) {
        SerializedDataMap<K, V> map = createSerializedDataMap(serializedData);
        return map.deserializeData(decodingKeyClass, decodingValueClass);
    }

    /*----  Here are the actual implementation-definitions  ----*/

    private static <K, V> SerializedDataMap<K, V> createSerializedDataMap(String serializedData,
                                                                          Map<K, V> structuredData) {
        return new SerializedDataMap<>(serializedData, structuredData, null);
    }

    public static SerializedDataList createSerializedDataList(String serializedData, Object[] structuredData) {
        return new SerializedDataList(serializedData, structuredData, null);
    }

    public static Object serialize(Object o) {
        if (o == null)
            return null;
        else if (o.getClass().isEnum())
            return basicSerializer.serialize(o);
        else if (o instanceof JSONArray)
            return o;
        else if (o instanceof JSONObject)
            return o;
        else {
            Serializer ser = serializerByType.get(o.getClass());
            if (ser == null) {
                throw new SerializationException("Couldn't find Serializer to serialize " + o + " with class " + o.getClass());
            }
            try {
                return ser.serialize(o);
            } catch (SerializationException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new SerializationException(o, ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(Object o, Class<T> cl) {
        if (o == null)
            return null;
        else if (cl == Object.class || cl == o.getClass())
            return (T) o;
        else if (cl.isEnum())
            return basicSerializer.deserialize(o, cl);
        else {
            Serializer ser = serializerByType.get(cl);
            if (ser == null) {
                throw new SerializationException("Couldn't find Serializer to deserialize " + o + " with class " + cl);
            }
            try {
                return ser.deserialize(o, cl);
            } catch (SerializationException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new SerializationException(o, cl, ex);
            }
        }
    }

}
