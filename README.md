# Java TOTP Custom PoC

## 1. Descripción

Este proyecto es una prueba de concepto (PoC) desarrollada con Spring Boot 3 y Java 21 para implementar un mecanismo de autenticación basado en TOTP (Time-based One-Time Password) siguiendo los lineamientos del RFC 6238.

La implementación utiliza un algoritmo propio basado en:

* HMAC-SHA256
* Ventanas de tiempo de 30 segundos
* Secretos codificados en Base32
* OTP de 6 dígitos
* Tolerancia configurable de ventanas para validación

El objetivo es demostrar cómo generar y validar códigos TOTP sin depender de librerías especializadas de OTP.

### Funcionalidades

* Generación de secretos aleatorios Base32.
* Enrolamiento de usuarios para MFA.
* Generación de códigos TOTP.
* Validación de códigos TOTP.
* Implementación basada en RFC 6238.
* Uso de HMAC-SHA256 como algoritmo criptográfico.

---

# 2. Arquitectura

## Diagrama de Arquitectura

```text
+------------------+
| Cliente REST     |
| Postman / App    |
+--------+---------+
         |
         | HTTP
         |
         v
+----------------------------+
| Spring Boot Application    |
| java-TOTP-custom           |
+-------------+--------------+
              |
              |
              v
+----------------------------+
| TotpController             |
+-------------+--------------+
              |
              v
+----------------------------+
| TotpService                |
| RFC6238 Implementation     |
+-------------+--------------+
              |
              v
+----------------------------+
| HMAC-SHA256                |
| Base32 Secret              |
| Time Counter (30 sec)      |
+----------------------------+
```

## Flujo de Enrolamiento

```text
Usuario
   |
   | POST /totp/enroll
   |
   v
Generación de Secret Base32
   |
   v
Entrega del Secret
```

## Flujo de Generación OTP

```text
Secret Base32
      |
      v
Epoch Time
      |
      v
Counter = Epoch / 30
      |
      v
HMAC-SHA256
      |
      v
Dynamic Truncation
      |
      v
OTP de 6 dígitos
```

## Flujo de Validación

```text
OTP ingresado
      |
      v
Generar OTP esperado
      |
      v
Comparar ventana actual
      |
      +---- Ventana -1
      |
      +---- Ventana  0
      |
      +---- Ventana +1
      |
      v
Resultado TRUE/FALSE
```

---

# 3. Estructura del Proyecto

```text
java-TOTP-custom
│
├── pom.xml
│
├── src
│   ├── main
│   │
│   ├── java
│   │   └── com.poc.totp
│   │
│   │       ├── TotpApplication.java
│   │
│   │       ├── handler
│   │       │   └── TotpController.java
│   │
│   │       ├── domain
│   │       │   └── TotpRequest.java
│   │
│   │       └── util
│   │           ├── SecretGenerator.java
│   │           └── TotpService.java
│   │
│   └── resources
│       ├── application.properties
│       └── TOTP.postman_collection.json
│
└── README.md
```

---

# 4. Componentes Principales

## TotpController

Archivo:

```text
handler/TotpController.java
```

Expone los endpoints REST de enrolamiento, generación y validación.

### Endpoints

| Endpoint       | Método | Descripción             |
| -------------- | ------ | ----------------------- |
| /totp/enroll   | POST   | Genera un nuevo secreto |
| /totp/generate | POST   | Genera un OTP           |
| /totp/validate | POST   | Valida un OTP           |

---

## SecretGenerator

Archivo:

```text
util/SecretGenerator.java
```

Responsabilidades:

* Generar secretos criptográficamente seguros.
* Utilizar SecureRandom.
* Codificar el secreto en Base32.

Implementación:

```java
byte[] buffer = new byte[sizeBytes];
new SecureRandom().nextBytes(buffer);
```

Características:

| Propiedad    | Valor    |
| ------------ | -------- |
| Longitud     | 32 bytes |
| Entropía     | 256 bits |
| Codificación | Base32   |

---

### Configuración

```java
algorithm = "HmacSHA256";
digits = 6;
timeStepSeconds = 30;
```

### Responsabilidades

* Generar OTP.
* Validar OTP.
* Calcular contador temporal.
* Aplicar HMAC-SHA256.
* Aplicar Dynamic Truncation.
* Formatear OTP.

---

### Generación de OTP

#### Paso 1

Obtiene el contador temporal.

```java
counter = epochSeconds / 30;
```

#### Paso 2

Convierte el contador a bytes.

```java
ByteBuffer.allocate(8)
```

#### Paso 3

Calcula el hash.

```java
Mac.getInstance("HmacSHA256")
```

#### Paso 4

Aplica Dynamic Truncation.

```java
offset = hash[last] & 0x0F;
```

#### Paso 5

Genera OTP.

```java
otp = binary % 1000000;
```

Resultado:

```text
123456
```

---

### Validación de OTP

La validación admite tolerancia temporal.

```java
validateOtp(secret, otp, instant, 1)
```

Ventanas evaluadas:

```text
-1 ventana
 0 ventana
+1 ventana
```

Con ventanas de 30 segundos:

```text
90 segundos de tolerancia total
```

---

## TotpRequest

Modelo utilizado para las operaciones REST.

Campos:

| Campo        | Tipo   |
| ------------ | ------ |
| secret       | String |
| otp          | String |
| base32Secret | String |

Ejemplo:

```json
{
  "otp": "123456",
  "base32Secret": "JBSWY3DPEHPK3PXP"
}
```
## Enroll User

### Request

```http
POST /totp/enroll
```

### Response

```json
{
  "base32Secret": "MFRGGZDFMZTWQ2LKNNWG23TPOI======"
}
```

---

## Generate OTP

### Request

```http
POST /totp/generate
```

```json
{
  "base32Secret": "MFRGGZDFMZTWQ2LKNNWG23TPOI======"
}
```

### Response

```text
582194
```

---

## Validate OTP

### Request

```http
POST /totp/validate
```

```json
{
  "base32Secret": "MFRGGZDFMZTWQ2LKNNWG23TPOI======",
  "otp": "582194"
}
