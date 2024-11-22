package io.github.kydzombie.cairn.api.packet;

import com.google.common.primitives.Primitives;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class UpdatePacketHelper {
    private record Serializer<T>(BiConsumer<ByteBuffer, T> serializeFunc, Function<ByteBuffer, T> deserializeFunc, Function<T, Integer> sizeFunc) {
        public void serialize(ByteBuffer buffer, T value) {
            serializeFunc.accept(buffer, value);
        }

        public T deserialize(ByteBuffer buffer) {
            return deserializeFunc.apply(buffer);
        }

        public int getSize(T value) {
            return sizeFunc.apply(value);
        }
    }

    private static final Map<Class<?>, Serializer<?>> SERIALIZERS = new HashMap<>();

    static {
        registerSerializer(Byte.class, ByteBuffer::put, ByteBuffer::get, (value) -> 1);
        registerSerializer(Short.class, ByteBuffer::putShort, ByteBuffer::getShort, (value) -> 2);
        registerSerializer(Integer.class, ByteBuffer::putInt, ByteBuffer::getInt, (value) -> 4);
        registerSerializer(Long.class, ByteBuffer::putLong, ByteBuffer::getLong, (value) -> 8);
        registerSerializer(Float.class, ByteBuffer::putFloat, ByteBuffer::getFloat, (value) -> 4);
        registerSerializer(Double.class, ByteBuffer::putDouble, ByteBuffer::getDouble, (value) -> 8);
        registerSerializer(String.class,
                (buffer, value) -> {
                    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                    buffer.putInt(bytes.length);
                    buffer.put(bytes);
                },
                (buffer) -> {
                    int length = buffer.getInt();
                    byte[] bytes = new byte[length];
                    buffer.get(bytes);
                    return new String(bytes, StandardCharsets.UTF_8);
                },
                (value) -> {
                    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                    return 4 + bytes.length;
                }
        );
    }

    public static <T> void registerSerializer(Class<T> type, BiConsumer<ByteBuffer, T> serializer, Function<ByteBuffer, T> deserializer, Function<T, Integer> sizeFunc) {
        SERIALIZERS.put(type, new Serializer<T>(serializer, deserializer, sizeFunc));
    }

    public static void autoSerialize(ByteBuffer buffer, Object value) throws RuntimeException {
        Class<?> type = value.getClass();
        if (type.isPrimitive()) {
            type = Primitives.unwrap(type);
        }

        if (SERIALIZERS.containsKey(type)) {
            @SuppressWarnings("rawtypes")
            Serializer serializer = SERIALIZERS.get(value.getClass());
            //noinspection unchecked
            serializer.serialize(buffer, value);
        } else {
            throw new RuntimeException("Unsupported type for autoSerialize: " + type);
        }
    }

    public static <T extends Record> byte[] autoSerialize(T data) {
        RecordComponent[] components = data.getClass().getRecordComponents();
        int size = 0;
        try {
            for (RecordComponent component : components) {
                Object value = component.getAccessor().invoke(data);
                Class<?> type = value.getClass();
                if (SERIALIZERS.containsKey(type)) {
                    @SuppressWarnings("rawtypes")
                    Serializer serializer = SERIALIZERS.get(type);
                    //noinspection unchecked
                    size += serializer.getSize(value);
                } else {
                    throw new RuntimeException("Unsupported type for autoSerialize: " + type);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);
        try {
            for (RecordComponent component : components) {
                autoSerialize(buffer, component.getAccessor().invoke(data));
            }
        } catch (Exception e) {
            throw new RuntimeException("Couldn't serialize record.", e);
        }
        return buffer.compact().array();
    }

    public static <T> T autoDeserialize(Class<?> type, ByteBuffer buffer) throws RuntimeException {
        if (type.isPrimitive()) {
            type = Primitives.wrap(type);
        }

        if (SERIALIZERS.containsKey(type)) {
            //noinspection unchecked
            return (T) SERIALIZERS.get(type).deserialize(buffer);
        } else {
            throw new RuntimeException("Unsupported type for autoDeserialize: " + type);
        }
    }

    public static <T extends Record> T autoDeserialize(Class<T> recordClass, byte[] data) {
        RecordComponent[] components = recordClass.getRecordComponents();
        Object[] values = new Object[components.length];

        ByteBuffer buffer = ByteBuffer.wrap(data);
        try {
            for (int i = 0; i < components.length; i++) {
                values[i] = autoDeserialize(components[i].getType(), buffer);
            }
            return recordClass.getDeclaredConstructor(Arrays.stream(components).map(RecordComponent::getType).toArray(Class[]::new)).newInstance(values);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't deserialize record.", e);
        }

    }
}
