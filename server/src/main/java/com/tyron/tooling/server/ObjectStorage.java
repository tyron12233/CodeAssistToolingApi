package com.tyron.tooling.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class ObjectStorage {

    private static final class InstanceHolder {
        static final ObjectStorage INSTANCE = new ObjectStorage();
    }

    public static ObjectStorage getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final Map<String, Object> objectMap = Collections.synchronizedMap(new HashMap<>());

    private ObjectStorage() {

    }

    public Object getObject(String id) {
        return objectMap.get(id);
    }

    public void putObject(String id, Object object) {
        objectMap.put(id, object);
    }
}
