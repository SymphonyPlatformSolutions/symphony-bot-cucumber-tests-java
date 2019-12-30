package com.symphony.ps.sdk.bdd;

import clients.SymBotClient;
import java.util.Set;
import org.reflections.Reflections;

public class TestUtils {
    private static Reflections reflections = new Reflections("");

    public static <T> Set<Class<? extends T>> locateImplementations(Class<T> clazz) {
        return reflections.getSubTypesOf(clazz);
    }

    public static <T> T locateImplementation(Class<T> clazz, SymBotClient botClient) {
        Set<Class<? extends T>> impls = locateImplementations(clazz);
        if (impls.isEmpty()) {
            return null;
        }
        Class<? extends T> impl = impls.iterator().next();
        try {
            return impl.getConstructor(SymBotClient.class).newInstance(botClient);
        } catch (Exception e) {
            return null;
        }
    }
}
