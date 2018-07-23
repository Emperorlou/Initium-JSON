package com.universeprojects.json.shared.serialization;

import com.universeprojects.json.shared.JSONAware;
import com.universeprojects.json.shared.JSONObject;
import com.universeprojects.json.shared.parser.JSONParserFactory;
import com.universeprojects.json.shared.parser.ParseException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SerializedDataMap<K, V> implements JSONAware {
    protected String serializedData = null;
    protected Map<K, V> structuredData = null;

    public SerializedDataMap(String serializedData, Map<K, V> structuredData, JSONObject jsonObject) {
        this.serializedData = serializedData;
        this.structuredData = structuredData;
        this.serializedJsonObject = jsonObject;
    }

    public Map<K, V> getDeserializedData() {
        return structuredData;
    }

    public boolean isDeserialized() {
        return structuredData != null;
    }

    public boolean isEmpty() {
        return structuredData == null && serializedData == null;
    }

    @Override
    public String toString() {
        return getSerializedData();
    }

    protected JSONObject serializedJsonObject;


    protected void serialize() {
        if (structuredData == null)
            return;
        if (serializedJsonObject == null)
            serializedJsonObject = serializeMap(structuredData);
        serializedData = serializedJsonObject.toJSONString();
    }

    @SuppressWarnings("unchecked")
    protected void deserialize(Class<?> decodingKeyClass, Class<?> decodingValueClass) {
        if (serializedData == null && serializedJsonObject == null) return;
        if (serializedJsonObject == null)
            try {
                serializedJsonObject = (JSONObject) JSONParserFactory.getParser().parse(serializedData);
            } catch (ParseException e) {
                throw new SerializationException("ParseException while parsing: " + serializedData, e);
            }
        structuredData = (Map<K, V>) deserializeJSONObject(serializedJsonObject, decodingKeyClass, decodingValueClass);
    }

    @SuppressWarnings({"rawtypes"})
    public static JSONObject serializeMap(Map<?, ?> map) {
        JSONObject obj = new JSONObject();
        for (Map.Entry entry : map.entrySet()) {
            obj.put(SerializerFactory.serialize(entry.getKey()).toString(), SerializerFactory.serialize(entry.getValue()));
        }
        return obj;
    }

    @SuppressWarnings({"rawtypes"})
    public static <K, V> Map<K, V> deserializeJSONObject(JSONObject jsonObject, Class<K> keyClass, Class<V> valueClass) {
        Map<K, V> map = new LinkedHashMap<>();
        for (Object entryOb : jsonObject.entrySet()) {
            Map.Entry entry = (Entry) entryOb;
            K key = SerializerFactory.deserialize(entry.getKey(), keyClass);
            V value = SerializerFactory.deserialize(entry.getValue(), valueClass);
            map.put(key, value);
        }
        return map;
    }

    public Map<K, V> deserializeData(Class<?> keyClass, Class<?> valueClass) {
        if (structuredData == null)
            deserialize(keyClass, valueClass);
        return structuredData;
    }

    public Map<K, V> deserializeDataChecked(Class<K> keyClass, Class<V> valueClass) {
        if (structuredData == null)
            deserialize(keyClass, valueClass);
        return structuredData;
    }

    public String getSerializedData() {
        if (serializedData != null) return serializedData;
        if (serializedJsonObject != null) {
            serializedData = serializedJsonObject.toJSONString();
            return serializedData;
        }
        if (structuredData == null) return null;
        serialize();
        serializedData = serializedJsonObject.toJSONString();
        return serializedData;
    }

    @Override
    public String toJSONString() {
        return getSerializedData();
    }
}
