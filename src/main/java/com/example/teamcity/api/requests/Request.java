package com.example.teamcity.api.requests;

import com.example.teamcity.api.enums.Endpoint;
import io.restassured.specification.RequestSpecification;

public class Request {
    /**
     * Request — класс, описывающий меняющиеся параметры запроса:
     *  spec — КТО делает запрос (авторизация)
     *  endpoint — КУДА делаем запрос (URL + модель)
     */
    protected final RequestSpecification spec;
    protected final Endpoint endpoint;

    public Request(RequestSpecification spec, Endpoint endpoint) {
        this.spec = spec;
        this.endpoint = endpoint;
    }
}
