package io.github.kydzombie.cairn.api.gui;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SyncGetter {
    String value();
}
