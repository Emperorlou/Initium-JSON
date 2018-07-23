package com.universeprojects.json.shared.serialization;


@SuppressWarnings("unused")
public class SerializationException extends RuntimeException {
    @SuppressWarnings("unused")
    public SerializationException() {
    }

    public SerializationException(Object deserializationObject, Class deserializationClass) {
        this(deserializationObject, deserializationClass, null);
    }

    public SerializationException(Object deserializationObject, Class deserializationClass, Throwable cause) {
        this("Exception when deserializing object " + deserializationObject + " with class " +
            (deserializationObject != null ? deserializationObject.getClass().getCanonicalName() : "null") + " to class " + deserializationClass.getCanonicalName(), cause);
    }

    public SerializationException(Object serializationObject) {
        this(serializationObject, (Throwable) null);
    }

    public SerializationException(Object serializationObject, Throwable cause) {
        this("Exception when serializing object " + serializationObject + " with class " +
            (serializationObject != null ? serializationObject.getClass().getCanonicalName() : "null"), cause);
    }

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }
}
