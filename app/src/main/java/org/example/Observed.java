package org.example;

import java.lang.annotation.*;

/**
 * Аннотация для пометки полей как наблюдаемых (Observed).
 */
@Retention(RetentionPolicy.RUNTIME) // Сохраняется во время выполнения
@Target(ElementType.FIELD) // Применяется только к полям
public @interface Observed {
  // Можно добавить параметры для аннотации, если это потребуется
}
