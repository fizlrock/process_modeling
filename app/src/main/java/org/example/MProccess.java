package org.example;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Создаем аннотацию
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) // Для методов
public @interface MProccess {
  Class<?> activateOn(); // Описание класса-параметра
}
