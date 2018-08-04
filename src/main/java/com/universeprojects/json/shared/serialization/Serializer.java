package com.universeprojects.json.shared.serialization;


public interface Serializer {


    /**
     * Serializes the given element
     *
     * @param element The element to serialize
     * @return The serialized version of the element
     */
    Object serialize(Object element);

    /**
     * De-serializes the given element
     *
     * @param element The element to de-serialize
     * @param type    The class representing the data type of the element
     * @return The de-serialized version of the input
     */
    <T> T deserialize(Object element, Class<T> type);

}
