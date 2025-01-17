package io.github.kydzombie.cairn.api.block.entity;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SaveToNbt {
    String value() default "";
}
