package io.github.kydzombie.cairn.impl.storage;

import com.google.auto.service.AutoService;
import io.github.kydzombie.cairn.api.storage.SimpleInventory;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("io.github.kydzombie.cairn.api.storage.SimpleInventory")
@SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_17)
public class SimpleStorageProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(SimpleInventory.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@SimpleInventory can only be applied to fields.");
                throw new RuntimeException("@SimpleInventory is for fields.");
//                return true;
            }

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Hello. The processor is doing things!");
        }
        return true;
    }
}
