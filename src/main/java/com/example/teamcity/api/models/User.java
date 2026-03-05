package com.example.teamcity.api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder      // генерирует паттерн Builder: User.builder().username("x").build()
@Data         // генерирует getters, setters, toString, equals, hashCode
@AllArgsConstructor   // конструктор со ВСЕМИ полями
@NoArgsConstructor    // конструктор БЕЗ аргументов
public class User extends BaseModel {
    private String username;
    private String password;
}
