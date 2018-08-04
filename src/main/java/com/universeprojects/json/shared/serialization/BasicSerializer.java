package com.universeprojects.json.shared.serialization;

import com.universeprojects.json.shared.JSONArray;
import com.universeprojects.json.shared.JSONAware;
import com.universeprojects.json.shared.JSONObject;
import com.universeprojects.json.shared.parser.JSONParserFactory;
import com.universeprojects.json.shared.parser.ParseException;

/**
 * This serializer handles the basic data types, provided by the language / libraries
 */
public class BasicSerializer implements Serializer {

    static final Class<?>[] supportedTypes = new Class<?>[]{
        String.class,
        Boolean.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        Enum.class,
        Class.class,
        SerializedDataList.class,
        SerializedDataMap.class,
        SerializationWrapper.class,
    };
    public static final int LONG_RADIX = 32;

    @Override
    public Object serialize(Object element) {
        if (element == null || element instanceof JSONAware) {
            return element;
        } else if (element instanceof String) {
            return element;
        } else if (element instanceof Enum) {
            return ((Enum<?>) element).name();
        } else if (element instanceof Double) {
            return serializeDoubleLongBits((Number) element);
        } else if (element instanceof Float) {
            return serializeFloatTeIntBits((Number) element);
        } else if (element instanceof Integer) {
            return element;
        } else if (element instanceof Long) {
            return element;
        } else if (element instanceof Boolean) {
            return element;
        } else if (element instanceof Class) {
            return SerializerFactory.getSimpleName((Class) element);
        } else if (element instanceof SerializationWrapper) {
            SerializationWrapper wrapper = (SerializationWrapper) element;

            final Object value = wrapper.getValue();
            Class clazz = null;
            if (value != null) {
                clazz = value.getClass();
            }
            JSONArray arr2 = new JSONArray();
            arr2.add(serialize(value));
            arr2.add(SerializerFactory.getSimpleName(clazz));
            return arr2;
        } else {
            throw new SerializationException("Unsupported data type: " + element.getClass().getCanonicalName());
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T deserialize(Object element, Class<T> clazz) {
        if (clazz == null) {
            throw new SerializationException("Type cannot be null");
        }

        if (element == null) {
            return null;
        } else if (clazz == String.class) {
            return (T) element;
        } else if (clazz.isEnum()) {
            String name = element.toString();
            try {
                return (T) Enum.valueOf((Class<? extends Enum>) clazz, name);
            } catch (IllegalArgumentException ex) {
                throw new SerializationException(element, clazz, ex);
            }
        } else if (clazz == Boolean.class) {
            if (element instanceof Boolean) {
                return (T) element;
            } else if (element instanceof String) {
                String str = (String) element;
                if ("true".equals(str)) {
                    return (T) Boolean.TRUE;
                }
                if ("false".equals(str)) {
                    return (T) Boolean.FALSE;
                }
                // Boolean.valueOf() is not used here for a reason:
                // it is bug-prone because it does not explicitly recognize "false"
                throw new SerializationException("Can't convert value " + str + " to a Boolean");
            }
        } else if (clazz == Double.class) {
            if (element instanceof Number) {
                return (T) Double.valueOf(((Number) element).doubleValue());
            } else if (element instanceof String) {
                String str = (String) element;
                if (str.startsWith("D")) {
                    return (T) deserializeDoubleLongBits(str);
                } else {
                    return (T) Double.valueOf(str);
                }
            }
        } else if (clazz == Float.class) {
            if (element instanceof Number) {
                return (T) Float.valueOf(((Number) element).floatValue());
            } else if (element instanceof String) {
                String str = (String) element;
                if (str.startsWith("F")) {
                    Float fl = deserializeFloatTeIntBits(str);
                    return (T) fl;
                } else if(str.startsWith("D")) {
                    Double dbl = deserializeDoubleLongBits(str);
                    return (T) Float.valueOf(dbl.floatValue());
                } else {
                    return (T) Float.valueOf(str);
                }
            }
        } else if (clazz == Long.class) {
            if (element instanceof Number) {
                return (T) Long.valueOf(((Number) element).longValue());
            } else if (element instanceof String) {
                return (T) Long.valueOf((String) element);
            }
        } else if (clazz == Integer.class) {
            if (element instanceof Number) {
                return (T) Integer.valueOf(((Number) element).intValue());
            } else if (element instanceof String) {
                return (T) Integer.valueOf((String) element);
            }
        } else if (clazz == Class.class) {
            return (T) SerializerFactory.getCompatibleClassForName((String) element);
        } else if (clazz == SerializedDataList.class) {
            if (element instanceof String)
                return (T) new SerializedDataList((String) element, null, null);
            JSONArray arr = (JSONArray) element;
            return (T) new SerializedDataList(null, null, arr);
        } else if (clazz == SerializedDataMap.class) {
            if (element instanceof String)
                return (T) new SerializedDataMap((String) element, null, null);
            JSONObject obj = (JSONObject) element;
            return (T) new SerializedDataMap(null, null, obj);
        } else if (clazz == SerializationWrapper.class) {
            JSONArray json = getJSONArray(element);
            Object serializedValue = json.get(0);
            Class wrapperClass = deserialize(json.get(1), Class.class);
            Object value = null;
            if (wrapperClass != null) {
                value = deserialize(serializedValue, wrapperClass);
            }
            return (T) new SerializationWrapper<>(value);
        } else {
            throw new SerializationException("Unsupported data type: " + clazz.getCanonicalName());
        }

        throw new SerializationException(element, clazz);
    }

    public static Double deserializeDoubleLongBits(String s) {
        return Double.longBitsToDouble(Long.valueOf(s.substring(1), LONG_RADIX));
    }

    public static String serializeDoubleLongBits(Number n) {
        return "D" + Long.toString(Double.doubleToLongBits(n.doubleValue()), LONG_RADIX);
    }

    public static Float deserializeFloatTeIntBits(String s) {
        return Float.intBitsToFloat(Integer.valueOf(s.substring(1), LONG_RADIX));
    }

    public static String serializeFloatTeIntBits(Number n) {
        return "F" + Integer.toString(Float.floatToIntBits(n.floatValue()), LONG_RADIX);
    }

    private JSONArray getJSONArray(Object element) {
        if (element instanceof JSONArray)
            return (JSONArray) element;
        else if (element instanceof String) {
            try {
                return (JSONArray) JSONParserFactory.getParser().parse((String) element);
            } catch (ParseException e) {
                throw new SerializationException(e);
            }
        } else
            return null;
    }

}
