package com.example.teamcity.api.generators;

import com.example.teamcity.api.annotations.Optional;
import com.example.teamcity.api.annotations.Parameterizable;
import com.example.teamcity.api.annotations.Random;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.models.TestData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class TestDataGenerator {

    private TestDataGenerator() {
    }

    /**
     * Основной метод генерации тестовых данных.
     *
     * Логика для каждого поля:
     * 1) @Optional — пропускаем (значение задаётся вручную)
     * 2) @Parameterizable + есть параметры — подставляем параметр по порядку
     * 3) @Random + тип String — заполняем рандомной строкой
     * 4) Тип наследник BaseModel — рекурсивно генерируем вложенный объект
     * 5) Тип List<наследник BaseModel> — генерируем список из одного элемента
     */
    public static <T extends BaseModel> T generate(List<BaseModel> generatedModels, Class<T> generatorClass,
                                                   Object... parameters) {
        try {
            var instance = generatorClass.getDeclaredConstructor().newInstance();
            for (var field : generatorClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (!field.isAnnotationPresent(Optional.class)) {
                    var generatedClass = generatedModels.stream().filter(m
                            -> m.getClass().equals(field.getType())).findFirst();
                    if (field.isAnnotationPresent(Parameterizable.class) && parameters.length > 0) {
                        field.set(instance, parameters[0]);
                        parameters = Arrays.copyOfRange(parameters, 1, parameters.length);
                    } else if (field.isAnnotationPresent(Random.class)) {
                        if (String.class.equals(field.getType())) {
                            field.set(instance, RandomData.getString());
                        }
                    } else if (BaseModel.class.isAssignableFrom(field.getType())) {
                        var finalParameters = parameters;
                        field.set(instance, generatedClass.orElseGet(() -> generate(
                                generatedModels, field.getType().asSubclass(BaseModel.class), finalParameters)));
                    } else if (List.class.isAssignableFrom(field.getType())) {
                        if (field.getGenericType() instanceof ParameterizedType pt) {
                            var typeClass = (Class<?>) pt.getActualTypeArguments()[0];
                            if (BaseModel.class.isAssignableFrom(typeClass)) {
                                var finalParameters = parameters;
                                field.set(instance, generatedClass.map(List::of).orElseGet(() -> List.of(generate(
                                        generatedModels, typeClass.asSubclass(BaseModel.class), finalParameters))));
                            }
                        }
                    }
                }
                field.setAccessible(false);
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                 | NoSuchMethodException e) {
            throw new IllegalStateException("Cannot generate test data", e);
        }
    }

    // Упрощённый вызов — без ранее сгенерированных моделей
    public static <T extends BaseModel> T generate(Class<T> generatorClass, Object... parameters) {
        return generate(Collections.emptyList(), generatorClass, parameters);
    }

    // Генерация полного набора тестовых данных (User + Project + BuildType)
    public static TestData generate() {
        try {
            var instance = TestData.class.getDeclaredConstructor().newInstance();
            var generatedModels = new ArrayList<BaseModel>();
            for (var field : TestData.class.getDeclaredFields()) {
                field.setAccessible(true);
                if (BaseModel.class.isAssignableFrom(field.getType())) {
                    var generatedModel = generate(generatedModels, field.getType().asSubclass(BaseModel.class));
                    field.set(instance, generatedModel);
                    generatedModels.add(generatedModel);
                }
                field.setAccessible(false);
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("Cannot generate test data", e);
        }
    }
}
