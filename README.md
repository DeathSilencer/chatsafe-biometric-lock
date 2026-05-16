# ChatSafe | Guardián Biométrico para Apps de Mensajería 🛡️

**ChatSafe** es una solución avanzada de seguridad y privacidad para Android diseñada para proteger conversaciones específicas dentro de aplicaciones de mensajería de terceros (WhatsApp, Facebook Messenger e Instagram) mediante autenticación biométrica forzada en tiempo real.

A diferencia de los bloqueadores de aplicaciones convencionales que restringen el acceso a toda la app, ChatSafe intercepta de manera quirúrgica la apertura de hilos de chat individuales o menús de ajustes confidenciales, desplegando un escudo de verificación instantáneo sin romper la experiencia de usuario.

## 🚀 Características Clave

* **Vigilancia Multi-Plataforma Automática:** Monitoreo activo sobre los paquetes oficiales de WhatsApp (`com.whatsapp`), Messenger (`com.facebook.orca`) e Instagram (`com.instagram.android`).
* **Detección Proactiva Híbrida:** Interceptación inmediata mediante captura de clics en hilos de conversación (`TYPE_VIEW_CLICKED`) y escaneo profundo asíncrono del árbol de nodos activos (`AccessibilityNodeInfo`).
* **Autenticación Biométrica Nativa Avanzada:** Implementación de `BiometricPrompt` anclada a una actividad translúcida de alta prioridad (`FragmentActivity`), garantizando el cumplimiento de los estándares de seguridad biométrica del sistema.
* **Estrategia Anti-Race Condition:** Comunicación síncrona optimizada mediante el uso de memoria compartida (`Companion Objects`), erradicando bucles infinitos de renderizado y garantizando un bloqueo/desbloqueo inmediato.
* **Ventana de Gracia Táctica:** Sistema inteligente de sesión temporal que concede un margen personalizable de 10 segundos al salir de un chat protegido, permitiendo multitarea rápida sin fricciones redundantes.
* **Aislamiento y Eficiencia Energética:** Restricción de lectura del sistema (`android:packageNames`) para mitigar bloqueos por políticas bancarias externas y reducir drásticamente el consumo de batería en segundo plano.

## 🛠️ Tech Stack & Arquitectura

* **Lenguaje:** Kotlin 100%
* **Framework de UI:** Jetpack Compose (Material Design 3) con Gradle con soporte estable para Target SDK 35.
* **Componentes de Android:** Android Accessibility Services, Biometric API, BroadcastReceivers de estado de pantalla, Live Lifecycle Observers.
* **Persistencia:** Repositorio unificado encapsulado mediante `SharedPreferences`.

## 🔒 Parche de Seguridad Bancaria y de Privacidad
El servicio está explícitamente limitado en su manifiesto de configuración XML para ignorar cualquier actividad fuera de los objetivos de mensajería designados, ofreciendo una capa transparente que no vulnera datos sensibles del dispositivo ni credenciales bancarias.
