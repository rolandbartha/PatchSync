package net.rolandbrt.patchsync.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;

public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(Object object) throws Exception {
        return MAPPER.writeValueAsString(object);
    }
    public static <T> T fromJson(String message, Class<T> clazz) throws Exception {
        return MAPPER.readValue(message, clazz);
    }

    public static <T> T fromJson(InputStream is, Class<T> clazz) throws Exception {
        return MAPPER.readValue(is, clazz);
    }

    public static <T> T fromJson(File file, Class<T> clazz) throws Exception {
        return MAPPER.readValue(file, clazz);
    }
}