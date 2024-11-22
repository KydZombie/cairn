package io.github.kydzombie.cairn.api.packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a method as being a
 * custom packet deserializer. The method must be static and
 * have a single parameter of type byte array.
 * <pre>
 * {@code
 *  public record MyData(int value) {
 *      @PacketSerializer
 *      public static MyData deserialize(byte[] data) {
 *          return new MyData(ByteBuffer.wrap(data).getInt());
 *      }
 *  }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketDeserializer {
}
