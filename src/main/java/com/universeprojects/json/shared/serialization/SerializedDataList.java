package com.universeprojects.json.shared.serialization;

import com.universeprojects.json.shared.JSONArray;
import com.universeprojects.json.shared.JSONAware;
import com.universeprojects.json.shared.parser.JSONParserFactory;
import com.universeprojects.json.shared.parser.ParseException;


@SuppressWarnings("unused")
public class SerializedDataList implements JSONAware {
    protected String serializedData = null;
    protected Object[] structuredData = null;
    protected JSONArray serializedJsonArray;

    public static SerializedDataList EMPTY_LIST = new SerializedDataList("[]", new Object[0], new JSONArray());

    public SerializedDataList(String serializedData, Object[] structuredData, JSONArray jsonArray) {
        this.serializedData = serializedData;
        this.structuredData = structuredData;
        this.serializedJsonArray = jsonArray;
    }

    public Object[] getDeserializedData() {
        return structuredData;
    }

    public boolean isDeserialized() {
        return structuredData != null;
    }

    @Override
    public String toString() {
        return getSerializedData();
    }

    protected void serialize() {
        if (structuredData == null)
            return;
        serializedJsonArray = serializeArray(structuredData);
    }

    protected void deserialize(Class<?>[] decodingClassArray) {
        if (serializedData == null && serializedJsonArray == null) return;
        if (serializedJsonArray == null) {
            try {
                serializedJsonArray = (JSONArray) JSONParserFactory.getParser().parse(serializedData);
            } catch (ParseException e) {
                throw new SerializationException("ParseException while parsing: " + serializedData, e);
            }
        }
        structuredData = deserializeJSONArray(serializedJsonArray, decodingClassArray);
    }

    protected void deserializeConsistent(Class<?> decodingClass) {
        if (serializedData == null && serializedJsonArray == null) return;
        if (serializedJsonArray == null) {
            try {
                serializedJsonArray = (JSONArray) JSONParserFactory.getParser().parse(serializedData);
            } catch (ParseException e) {
                throw new SerializationException("ParseException while parsing: " + serializedData, e);
            }
        }
        structuredData = deserializeConsistentJSONArray(serializedJsonArray, decodingClass);
    }

    public static Object[] deserializeJSONArray(JSONArray jsonArray, Class<?>[] classes) {
        if (classes.length != jsonArray.size())
            throw new SerializationException("Number of classes != number of deserializable objects: " + classes.length + " vs " + jsonArray.size());
        Object[] arr = new Object[jsonArray.size()];
        int i = 0;
        for (Object o : jsonArray) {
            arr[i] = SerializerFactory.deserialize(o, classes[i]);
            i++;
        }
        return arr;
    }

    public static Object[] deserializeConsistentJSONArray(JSONArray jsonArray, Class<?> cl) {
        Object[] arr = new Object[jsonArray.size()];
        int i = 0;
        for (Object o : jsonArray) {
            arr[i] = SerializerFactory.deserialize(o, cl);
            i++;
        }
        return arr;
    }

    public static JSONArray serializeArray(Object... objects) {
        JSONArray arr = new JSONArray();
        for (Object element : objects) {
            arr.add(SerializerFactory.serialize(element));
        }
        return arr;
    }

    public Object[] deserializeData(Class<?>... decodingClassArray) {
        if (structuredData == null)
            deserialize(decodingClassArray);
        return structuredData;
    }

    public Object[] deserializeConsistentData(Class<?> decodingClass) {
        if (structuredData == null)
            deserializeConsistent(decodingClass);
        return structuredData;
    }

    @SuppressWarnings("ConstantConditions")
    public String getSerializedData() {
        if (serializedData != null) return serializedData;
        if (serializedJsonArray != null) {
            serializedData = serializedJsonArray.toJSONString();
            return serializedData;
        }
        if (structuredData == null) return null;
        if (serializedData == null) {
            if (serializedJsonArray == null)
                serialize();
            serializedData = serializedJsonArray.toJSONString();
        }
        return serializedData;
    }

    public boolean isEmpty() {
        if (structuredData != null) {
            return structuredData.length == 0;
        }
        if (serializedJsonArray != null) {
            return serializedJsonArray.isEmpty();
        }
        return serializedData == null || serializedData.equals("[]");
    }

    @Override
    public String toJSONString() {
        return getSerializedData();
    }


}
