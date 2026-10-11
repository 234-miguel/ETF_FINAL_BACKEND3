package com.bancoxyz.bff.model;

public class Cuenta {

    private Long cuentaId;
    private String nombre;
    private Double saldo;
    private Integer edad;
    private String tipo;

    public Cuenta(Long cuentaId, String nombre, Double saldo, Integer edad, String tipo) {
        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.saldo = saldo;
        this.edad = edad;
        this.tipo = tipo;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public String getNombre() {
        return nombre;
    }

    public Double getSaldo() {
        return saldo;
    }

    public Integer getEdad() {
        return edad;
    }

    public String getTipo() {
        return tipo;
    }
}