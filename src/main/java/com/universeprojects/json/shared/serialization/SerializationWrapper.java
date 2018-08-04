package com.universeprojects.json.shared.serialization;

public class SerializationWrapper<T> {
    private T value;

    public SerializationWrapper() {
    }

    public SerializationWrapper(T value) {
        setValue(value);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
