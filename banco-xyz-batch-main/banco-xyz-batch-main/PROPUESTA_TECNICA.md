# Propuesta Técnica - Banco XYZ

## 1. Descripción de la solución

Se propone desarrollar una solución de procesamiento por lotes utilizando Spring Batch para automatizar el procesamiento de grandes volúmenes de información de Banco XYZ.

La solución permite leer información desde archivos CSV, validar y transformar los registros mediante procesos Batch y posteriormente almacenar los resultados procesados en una base de datos Oracle.

## 2. Arquitectura de la solución

La solución está desarrollada utilizando Java, Spring Boot y Spring Batch.

El procesamiento sigue principalmente la estructura:

Reader → Processor → Writer

- Reader: se encarga de leer los registros provenientes de los archivos CSV.
- Processor: aplica las reglas de negocio, validaciones y transformaciones.
- Writer: almacena los registros procesados en la base de datos.

Spring Batch organiza estos componentes mediante Jobs y Steps.

## 3. Jobs implementados

### transaccionesJob

Procesa el archivo `transacciones.csv`.

Este Job lee las transacciones, valida la información y prepara los registros válidos para su almacenamiento.

### interesesJob

Procesa el archivo `intereses.csv`.

Este Job procesa información relacionada con las cuentas bancarias y realiza el cálculo de intereses correspondiente.

### cuentasAnualesJob

Procesa el archivo `cuentas_anuales.csv`.

Este proceso permite manejar información histórica o anual de las cuentas y preparar los registros para almacenarlos en la base de datos.

## 4. Persistencia

Para almacenar los resultados se definieron las siguientes tablas:

- TRANSACCIONES_PROCESADAS
- INTERESES_PROCESADOS
- CUENTAS_ANUALES_PROCESADAS

Las estructuras necesarias para estas tablas se encuentran definidas en el archivo `schema.sql`.

La solución contempla una conexión con Oracle Database para la persistencia de los resultados.

## 5. Manejo de errores y tolerancia a fallos

Spring Batch permite incorporar mecanismos de tolerancia a fallos durante el procesamiento.

La solución contempla validaciones de los registros y mecanismos para controlar datos incorrectos, evitando que un único registro inválido provoque innecesariamente la interrupción de todo el procesamiento.

Además, Spring Batch registra información sobre la ejecución de Jobs y Steps, facilitando la identificación de errores.

## 6. Procesamiento por chunks

Los registros son procesados mediante chunks.

Este mecanismo permite trabajar con grupos de registros en lugar de procesar todo el archivo en memoria al mismo tiempo.

Esto mejora el uso de recursos y permite que la solución pueda manejar archivos de mayor tamaño.

## 7. Escalabilidad

La arquitectura basada en Spring Batch permite incorporar estrategias adicionales de escalamiento, como procesamiento paralelo y particionamiento.

El particionamiento permite dividir grandes cantidades de información en grupos más pequeños que pueden ser procesados independientemente.

Esta estrategia resulta adecuada para Banco XYZ debido al alto volumen de información que puede generarse en los procesos bancarios.

## 8. Flujo general

El flujo de procesamiento implementado es:

Archivo CSV
↓
ItemReader
↓
ItemProcessor
↓
Validación y transformación
↓
ItemWriter
↓
Oracle Database

## 9. Conclusión

La solución propuesta permite automatizar procesos repetitivos relacionados con grandes volúmenes de información bancaria.

Spring Batch proporciona una arquitectura adecuada para este escenario gracias al procesamiento mediante Jobs, Steps y chunks, además de sus mecanismos de control de errores y posibilidades de escalamiento.

La solución puede continuar evolucionando mediante la incorporación de nuevas reglas de negocio, mayor paralelización y mecanismos adicionales de monitoreo.