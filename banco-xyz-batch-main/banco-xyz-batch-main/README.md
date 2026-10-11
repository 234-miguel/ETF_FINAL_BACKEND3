# Banco XYZ - Procesamiento Batch

## Descripción

Este proyecto implementa una solución de procesamiento por lotes para Banco XYZ utilizando Java, Spring Boot y Spring Batch.

El objetivo es automatizar el procesamiento de grandes volúmenes de información bancaria proveniente de archivos CSV, aplicando validaciones y transformaciones antes de almacenar los resultados procesados.

## Tecnologías utilizadas

- Java
- Spring Boot
- Spring Batch
- Maven
- Oracle Database
- IntelliJ IDEA

## Procesos Batch

El proyecto contempla tres procesos principales:

### 1. Procesamiento de transacciones

El Job `transaccionesJob` procesa el archivo `transacciones.csv`.

Su función es leer las transacciones bancarias, validar los datos y almacenar las transacciones procesadas.

### 2. Cálculo de intereses

El Job `interesesJob` procesa el archivo `intereses.csv`.

Este proceso obtiene la información de las cuentas, realiza las validaciones correspondientes y calcula los intereses asociados antes de almacenar los resultados.

### 3. Procesamiento de cuentas anuales

El Job `cuentasAnualesJob` procesa el archivo `cuentas_anuales.csv`.

Su objetivo es procesar la información histórica de las cuentas y almacenar los registros procesados para su posterior consulta.

## Estructura de archivos

Los archivos de entrada se encuentran en:

`src/main/resources/data/`

- `transacciones.csv`
- `intereses.csv`
- `cuentas_anuales.csv`

El archivo `schema.sql` contiene la definición de las tablas utilizadas para almacenar los resultados procesados.

## Ejecución

1. Configurar la conexión a la base de datos.
2. Verificar que los archivos CSV se encuentren en `src/main/resources/data/`.
3. Ejecutar la aplicación Spring Boot.
4. Spring Batch ejecutará los procesos configurados.
5. Revisar la consola para verificar el estado de ejecución de los Jobs.

## Manejo de errores

Los procesos Batch incorporan mecanismos para controlar registros inválidos y evitar que un registro con problemas interrumpa innecesariamente el procesamiento completo.

También se utilizan las capacidades de Spring Batch para administrar la ejecución de Jobs y Steps.

## Resultado esperado

Al finalizar correctamente los procesos, la información procesada queda almacenada en las tablas correspondientes de la base de datos, permitiendo manejar de forma automatizada los datos bancarios proporcionados en los archivos CSV.

## Autor

Proyecto desarrollado para la actividad de procesamiento Batch de Banco XYZ.