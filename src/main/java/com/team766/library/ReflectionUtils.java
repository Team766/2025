package com.team766.library;

public class ReflectionUtils {
    /**
     * Works the same as {@code object.getClass()}, but the return type has the full, unerased type.
     *
     * For example, {@code getClass(new List<String>())} will return a
     * {@code Class<? extends List<String>>}, but {@code new List<String>().getClass()} will return
     * a {@code Class<? extends List>}.
     *
     * See https://stackoverflow.com/a/65020013 for discussion.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> getClass(T object) {
        return (Class<? extends T>) object.getClass();
    }
}
