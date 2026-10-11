# Banco XYZ - Backend for Frontend (BFF)

## Objetivo

Este proyecto implementa el patrón Backend for Frontend (BFF) para el sistema del Banco XYZ.

Se desarrollaron diferentes BFF para adaptar las respuestas del backend según las necesidades de tres tipos de clientes: Web, Móvil y Cajero Automático.

## Fuente de datos

El proyecto utiliza datos basados en el archivo `intereses.csv` del repositorio `bank_legacy_data`.

Los datos contienen información de cuentas bancarias como:

- cuenta_id
- nombre
- saldo
- edad
- tipo

## Estructura del proyecto

- model: contiene el modelo Cuenta.
- service: contiene la lógica para obtener las cuentas.
- bff.web: contiene el BFF destinado al cliente Web.
- bff.mobile: contiene el BFF destinado al cliente Móvil.
- bff.cajero: contiene el BFF destinado al Cajero Automático.

## BFF implementados

### BFF Web

Entrega información completa de las cuentas.

GET /api/web/cuentas

### BFF Móvil

Entrega una respuesta más ligera con los datos esenciales.

GET /api/mobile/cuentas

### BFF Cajero

Permite realizar operaciones relacionadas con el cajero automático.

Consulta de saldo:

GET /api/cajero/saldo/{id}

Retiro:

POST /api/cajero/retiro/{id}/{monto}

## Ejemplos

Consulta de cuenta:

GET http://localhost:8080/api/cajero/saldo/137

Retiro:

POST http://localhost:8080/api/cajero/retiro/137/1000

## Ejecución

1. Abrir el proyecto en IntelliJ IDEA.
2. Ejecutar la clase BffApplication.
3. Esperar a que Spring Boot inicie en el puerto 8080.
4. Probar las APIs utilizando Postman o el navegador.

## Tecnologías utilizadas

- Java
- Spring Boot
- Spring Web
- Maven
- Postman

## Patrón BFF

El patrón Backend for Frontend permite adaptar el backend según las necesidades de cada cliente.

En este proyecto:

- Web recibe información completa.
- Móvil recibe una respuesta más ligera.
- Cajero dispone de operaciones específicas como consulta de saldo y retiro.recibe información completa, el cliente Móvil recibe una respuesta más ligera y el Cajero dispone de operaciones específicas como consulta de saldo y retiro.