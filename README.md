# App Móvil Educativa – Documentación Técnica

> **Nota:** El código se encuentra en la rama `master`

## Introducción

Esta aplicación móvil tiene como objetivo centralizar y facilitar la comunicación y planificación educativa entre estudiantes y profesores. Cada tipo de usuario tiene acceso a un conjunto de módulos específicos que responden a sus necesidades académicas.

Desarrollada en *Kotlin* para la plataforma *Android, con arquitectura **MVVM, la app utiliza servicios de **Firebase* para autenticación, base de datos y gestión de enlaces dinámicos.

> *Requisito obligatorio:*  
> Para clonar, compilar y ejecutar este proyecto, es necesario tener instalada la última versión de Android Studio.

## Guía de Instalación

### Descargar Android Studio
📥 [Descargar la última versión de Android Studio](https://developer.android.com/studio?hl=es-419)

### Tutorial de Instalación
📹 [Ver video tutorial de instalación en Android Studio](https://www.loom.com/share/15f943ff31d54af9bd01ba397073e96f?sid=102cda9a-3a41-4fbf-8644-323edfd2a12c)

## Video Tutorial de la Aplicación

📹 [Ver video tutorial de navegación por los módulos de la aplicación](https://www.loom.com/share/5d951433cef846c59616cb435473ca40?sid=e96314be-51f7-40ae-b092-0cf4be916dc6)

---

## Módulos Comunes

### Autenticación

*Disponible para:* Estudiante y Profesor

*Funciones:*
- Autenticación mediante correo electrónico y contraseña.
- Registro con rol asignado (estudiante o profesor).
- Recuperación de contraseña mediante un enlace enviado al correo registrado.
- Validación de sesión activa y persistencia de sesión.
- Cierre de sesión desde cualquier sección.

---

## Funcionalidades por Rol

---

### Rol: Estudiante

#### Módulo: Perfil del Estudiante

*Funciones:*
- Visualización de datos personales.
- Edición de información básica.
- Gestión de lista de profesores favoritos (agregar/eliminar).
- Consulta de calificaciones otorgadas a tutores.

#### Módulo: Comunicados

*Funciones:*
- Visualización de publicaciones en forma de feed.
- Comentar publicaciones.
- Dar "like" a publicaciones.

> *Nota técnica:*  
> La carga de los comunicados puede tardar algunos segundos debido a la consulta de datos en tiempo real desde Firestore.  
> Lo mismo aplica para el historial de comentarios dentro de cada publicación.

#### Módulo: Calificación de Tutores

*Funciones:*
- Calificar tutores luego de finalizar una tutoría.
- Registrar comentario y puntuación.
- Consultar historial de calificaciones realizadas.

#### Módulo: Planificación de Estudios

*Funciones:*
- Crear planes de estudio con metas personales.
- Actualizar el progreso en porcentaje.
- Editar o eliminar planes registrados.

#### Módulo: Calendario de Tutorías

*Funciones:*
- Visualizar tutorías agendadas por tutores.
- Consultar detalles de cada tutoría (tema, fecha, hora).
- Sincronización automática con tutorías asignadas.

---

### Rol: Profesor

#### Módulo: Perfil del Profesor

*Funciones:*
- Visualización y edición de datos personales.
- Registro y actualización de experiencia profesional.
- Visualización de calificaciones recibidas de estudiantes.
- Cierre de sesión.

#### Módulo: Comunicados

*Funciones:*
- Crear publicaciones dirigidas a estudiantes.
- Editar o eliminar publicaciones propias.
- Visualizar comentarios e interacciones.

> *Nota técnica:*  
> La carga inicial del feed de comunicados y los comentarios puede tardar unos segundos, especialmente si hay muchas publicaciones activas.

#### Módulo: Agendar Tutorías

*Funciones:*
- Agendar tutorías con estudiante, tema, fecha y hora.
- Editar o cancelar tutorías.
- Sincronización automática en el calendario del estudiante asignado.

---

## Tecnologías Utilizadas

- *Lenguaje:* Kotlin  
- *Plataforma:* Android SDK 24+  
- *IDE:* Android Studio 2024.3.2.14 (Windows)  
- *Arquitectura:* MVVM (Model-View-ViewModel)  
- *Servicios Backend:*  
  - Firebase Authentication  
  - Firebase Firestore  
  - Firebase Dynamic Links  

---
