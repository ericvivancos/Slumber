# SLUMBER – Documentación General del Proyecto

## 1. Visión General

Slumber es un ecosistema de aplicaciones diseñado para detectar inactividad, somnolencia o falta de atención del usuario durante el consumo de contenido multimedia, con el objetivo de pausar automáticamente la reproducción o intervenir de forma inteligente.

El sistema está compuesto por múltiples módulos interconectados que trabajan de forma coordinada:

- PC Agent (Windows)
- Mobile Hub (Android)
- Watch Agent (Wear OS)
- Shared (lógica común futura)

La arquitectura sigue un modelo distribuido con un **hub central (mobile)** que coordina dispositivos periféricos.

---

## 2. Arquitectura del Sistema

### Flujo principal

Smartwatch → Mobile Hub → PC Agent

### Roles

- **Watch Agent:** Captura señales biométricas y de movimiento
- **Mobile Hub:** Centro de control y decisión
- **PC Agent:** Ejecuta acciones en el sistema (pausa, overlay, etc.)

---

## 3. PC Agent (Windows)

### Descripción
Aplicación de escritorio desarrollada en .NET (WPF) que monitoriza la actividad del usuario y controla la reproducción multimedia del sistema.

### Funcionalidades implementadas

#### 3.1 Detección de inactividad
- Monitoriza ratón y teclado
- Detecta periodos sin interacción

#### 3.2 Detección de reproducción de audio
- Uso de NAudio
- Identificación de sesiones activas de audio
- Evita falsos positivos cuando no hay contenido en reproducción

#### 3.3 Overlay de confirmación
- Ventana emergente: "¿Sigues despierto?"
- Cuenta atrás configurable
- Cancelable con:
  - botón
  - movimiento global de ratón/teclado

#### 3.4 Pausa automática
- Simulación de tecla multimedia (VK_MEDIA_PAUSE)
- Compatible con múltiples reproductores

#### 3.5 Hooks globales
- Captura input fuera de la aplicación
- Permite cancelar overlay desde cualquier contexto

#### 3.6 Configuración persistente
- Archivo JSON en AppData
- Parámetros configurables:
  - Tiempo de inactividad
  - Tiempo de cuenta atrás
  - Cooldown

#### 3.7 Interfaz de usuario
- UI en WPF
- Configuración editable
- Validación de entradas

#### 3.8 Bandeja del sistema
- Minimización a tray
- Menú contextual

#### 3.9 Inicio automático con Windows
- Registro en HKCU\Software\Microsoft\Windows\CurrentVersion\Run
- Activable desde UI

---

## 4. Mobile Hub (Android)

### Descripción
Aplicación Android desarrollada en Kotlin + Jetpack Compose que actúa como núcleo del sistema.

### Objetivos

- Gestionar dispositivos
- Recibir señales del smartwatch
- Enviar comandos al PC
- Centralizar configuración

### Estado actual

#### 4.1 Base del proyecto
- Proyecto creado con Android Studio
- Arquitectura inicial configurada
- Uso de Jetpack Compose

#### 4.2 Próximas funcionalidades

- UI principal
- Registro de dispositivos
- Comunicación con PC Agent
- Panel de control

---

## 5. Watch Agent (Wear OS)

### Descripción
Aplicación para smartwatch que capturará señales físicas del usuario.

### Objetivos

- Detectar inactividad física
- Medir frecuencia cardíaca
- Inferir estados de somnolencia

### Funcionalidades planificadas

#### 5.1 Señales de entrada
- Acelerómetro
- Sensor de movimiento
- Ritmo cardíaco

#### 5.2 Interacción inicial
- Botón manual de prueba
- Envío de eventos al móvil

#### 5.3 Evolución futura
- Algoritmos de detección de sueño
- Modelos de predicción

---

## 6. Shared (futuro)

### Descripción
Módulo compartido entre aplicaciones.

### Posibles contenidos

- Modelos de datos
- Protocolos de comunicación
- Lógica de decisión
- Tipos de eventos

---

## 7. Características Diferenciales

- Sistema distribuido multi-dispositivo
- Integración biométrica (watch)
- Detección contextual inteligente
- Automatización de consumo multimedia
- Arquitectura escalable

---

## 8. Roadmap

### Completado
- PC Agent funcional
- Configuración persistente
- Autoarranque

### En desarrollo
- Mobile Hub

### Próximos pasos
- Comunicación Mobile ↔ PC
- Integración Watch
- Detección avanzada de sueño

---

## 9. Conclusión

Slumber no es solo una aplicación, sino una plataforma que combina software de escritorio, móvil y wearable para crear una experiencia inteligente y automatizada centrada en el usuario.

El objetivo final es reducir el consumo pasivo inconsciente y mejorar la interacción con el contenido multimedia mediante decisiones automáticas basadas en el estado real del usuario.

