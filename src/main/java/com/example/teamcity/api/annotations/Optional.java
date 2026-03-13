package com.example.teamcity.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Поля с этой аннотацией не будут генерироваться рандомным или параметризированным значением.
 * Необходимо указывать значение вручную.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Optional {
}
