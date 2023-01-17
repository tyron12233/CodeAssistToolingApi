package com.tyron.tooling.packet.serializer;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Base64;

public class GsonHelper {

    public static final Gson customGson = new GsonBuilder().registerTypeHierarchyAdapter(Serializable.class,
            new TypeAdapter()).create();

    public static Gson getGson() {
        return customGson;
    }

    public static class TypeAdapter implements JsonSerializer<Serializable>, JsonDeserializer<Serializable> {

        @Override
        public JsonElement serialize(Serializable src, Type typeOfSrc, JsonSerializationContext context) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos;
            try {
                oos = new ObjectOutputStream( baos );
                oos.writeObject(src);
            } catch (IOException e) {
                throw new JsonIOException(e);
            }
            String encoded = Base64.getEncoder().encodeToString(baos.toByteArray());
            return new JsonPrimitive(encoded);
        }

        @Override
        public Serializable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String asString = json.getAsString();
            byte[] decoded = Base64.getDecoder().decode(asString);
            try {
                ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(decoded));
                return (Serializable) is.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new JsonIOException(e);
            }
        }
    }
}
