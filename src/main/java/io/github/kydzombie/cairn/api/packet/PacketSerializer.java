package io.github.kydzombie.cairn.api.packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a method as being a
 * custom packet serializer. The method must be static and
 * have a single parameter of the type that is being serialized.
 * The method must return a byte array.
 * <pre>
 * {@code
 *  public record MyData(int value) {
 *      @PacketSerializer
 *      public static byte[] serialize(MyData data) {
 *          return ByteBuffer.allocate(4).putInt(data.value()).array();
 *      }
 *  }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketSerializer {
}
