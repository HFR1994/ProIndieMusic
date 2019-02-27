package com.proindiemusic.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


@SuppressWarnings("ALL")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Prueba extends Entity {

    private String valor;
    private Boolean bool;
    private Float num;
    private Double num1;

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Boolean getBool() {
        return bool;
    }

    public void setBool(Boolean bool) {
        this.bool = bool;
    }

    public Float getNum() {
        return num;
    }

    public void setNum(Float num) {
        this.num = num;
    }

    public Double getNum1() {
        return num1;
    }

    public void setNum1(Double num1) {
        this.num1 = num1;
    }

    @Override
    public Class findDomainClass() {
        return Prueba.class;
    }

    @Override
    public String toString() {
        return "{" +
                "valor='" + valor + '\'' +
                ", bool=" + bool +
                ", num=" + num +
                ", num1=" + num1 +
                '}';
    }
}
