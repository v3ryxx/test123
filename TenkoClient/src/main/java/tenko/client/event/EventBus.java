package tenko.client.event;

import java.util.*;
import java.util.function.Consumer;

public class EventBus {
    private final Map<Class<?>, List<Consumer<Object>>> map = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> cls, Consumer<T> c) {
        map.computeIfAbsent(cls, k -> new ArrayList<>()).add((Consumer<Object>) c);
    }

    public <T> void post(T event) {
        List<Consumer<Object>> list = map.get(event.getClass());
        if (list != null) new ArrayList<>(list).forEach(c -> c.accept(event));
    }
}
