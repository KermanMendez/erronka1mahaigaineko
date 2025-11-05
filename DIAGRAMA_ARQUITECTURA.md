# 🏛️ ARQUITECTURA COMPLETA DEL PROYECTO LRLL GYM

> **Sistema de Gestión de Gimnasio** con arquitectura MVC + Service Layer  
> **Tecnología**: Java 21 + Swing + Firebase/Firestore  
> **Modo de Operación**: Dual (Online/Offline)  
> **Idioma**: Euskera (UI) + Español (Técnico)

---

## 📋 Índice

1. [Introducción al Proyecto](#introducción)
2. [Arquitectura General](#arquitectura-general)
3. [Capa de Vista (View Layer)](#capa-de-vista)
4. [Capa de Servicio (Service Layer)](#capa-de-servicio)
5. [Capa de Controlador (Controller Layer)](#capa-de-controlador)
6. [Capa de Modelo (Model Layer)](#capa-de-modelo)
7. [Capa de Utilidades (Util Layer)](#capa-de-utilidades)
8. [Capa de Persistencia](#capa-de-persistencia)
9. [Flujos de Datos Detallados](#flujos-de-datos)
10. [Patrones de Diseño Utilizados](#patrones-de-diseño)
11. [Seguridad y Encriptación](#seguridad)
12. [Casos de Uso Completos](#casos-de-uso)

---

## 🎯 Introducción al Proyecto {#introducción}

### **¿Qué es LRLL GYM?**

**LRLL (Long Ring Long Land) GYM** es una aplicación de escritorio desarrollada en Java que permite a usuarios y entrenadores gestionar rutinas de entrenamiento, ejecutar workouts cronometrados y mantener un histórico de entrenamientos completados.

### **Características Principales**

- ✅ **Autenticación segura** con Firebase Authentication
- ✅ **Gestión de perfiles** de usuario con validación completa
- ✅ **Sistema de niveles progresivos** (1-5) basado en completitud
- ✅ **Ejecución de workouts en tiempo real** con 3 threads paralelos
- ✅ **Modo offline completo** con backups encriptados
- ✅ **Histórico de entrenamientos** con estadísticas
- ✅ **Sincronización automática** cuando vuelve la conexión
- ✅ **Interfaz en Euskera** con diseño moderno

### **Tecnologías del Stack**

| Categoría | Tecnología | Versión | Propósito |
|-----------|-----------|---------|-----------|
| **Lenguaje** | Java | 21 | Lenguaje principal |
| **UI Framework** | Swing | JDK 21 | Interfaz gráfica |
| **Build Tool** | Maven | 3.x | Gestión de dependencias |
| **Backend** | Firebase Admin SDK | 9.2.0 | Autenticación y base de datos |
| **Database** | Cloud Firestore | 9.2.0 | NoSQL database en la nube |
| **HTTP Client** | OkHttp | 3.14.9 | Peticiones REST a Firebase |
| **JSON Parser** | Gson | 2.10.1 | Parsing JSON |
| **Logging** | SLF4J Simple | 2.0.7 | Logs de aplicación |
| **Encryption** | PBKDF2WithHmacSHA256 | JDK | Hashing de contraseñas |

---

## 📐 Arquitectura General {#arquitectura-general}

### **Vista de Alto Nivel**

```
┌─────────────────────────────────────────────────────────────────┐
│                         USUARIO FINAL                            │
└─────────────────────────────────┬───────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                          VIEW LAYER                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  FirstView → LoginFrame → RegisterDialog                  │  │
│  │       ↓                                                    │  │
│  │     Inter (Hub)                                           │  │
│  │    ┌───────┴────────┐                                     │  │
│  │  Profile         Workouts → ThreadFrame → ViewHistoric   │  │
│  │                                                            │  │
│  │  Componentes:                                             │  │
│  │  • UIStyle, Theme, LoadLogo, CardListRenderer            │  │
│  └──────────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────────┘
                            │ Usa servicios
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                        SERVICE LAYER                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  AuthenticationService                                    │  │
│  │    ├─ Login/Logout                                        │  │
│  │    └─ Registro de usuarios                               │  │
│  │                                                            │  │
│  │  ProfileService                                           │  │
│  │    ├─ Cargar perfil                                       │  │
│  │    └─ Actualizar perfil                                   │  │
│  │                                                            │  │
│  │  RoutineService                                           │  │
│  │    ├─ Obtener rutinas por nivel                          │  │
│  │    └─ Listar ejercicios                                   │  │
│  │                                                            │  │
│  │  WorkoutExecutionService                                  │  │
│  │    ├─ Ejecutar entrenamiento                             │  │
│  │    ├─ Control de threads (pausar/reanudar/saltar)       │  │
│  │    └─ Registro en histórico                              │  │
│  │                                                            │  │
│  │  BackupService / BackupReaderService                     │  │
│  │    ├─ Crear backup de Firestore                          │  │
│  │    └─ Leer backup para modo offline                      │  │
│  │                                                            │  │
│  │  HistoricReaderService / OfflineHistoricService          │  │
│  │    ├─ Leer histórico online                              │  │
│  │    └─ Sincronizar histórico offline                      │  │
│  │                                                            │  │
│  │  UserBackupService                                        │  │
│  │    └─ Gestión de sesión local                            │  │
│  └──────────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────────┘
                            │ Usa controllers y models
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                       CONTROLLER LAYER                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Controller (Singleton)                                   │  │
│  │    ├─ Estado global de la app                            │  │
│  │    ├─ Online/Offline mode                                │  │
│  │    └─ Instancia de Firestore                             │  │
│  │                                                            │  │
│  │  DBConnection                                             │  │
│  │    ├─ Inicializa Firebase                                │  │
│  │    └─ Verifica conectividad                              │  │
│  │                                                            │  │
│  │  MainApp                                                  │  │
│  │    └─ Entry point, configuración inicial                 │  │
│  └──────────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────────┘
                            │ Manipula
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                         MODEL LAYER                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  User                                                     │  │
│  │    • id, name, surname, email                            │  │
│  │    • password (hash), birthDate                          │  │
│  │    • trainer, level                                       │  │
│  │                                                            │  │
│  │  Exercise                                                 │  │
│  │    • name, description                                    │  │
│  │    • sets, reps, serieTime, restTime                     │  │
│  │    • img, videoURL                                        │  │
│  │                                                            │  │
│  │  Workout                                                  │  │
│  │    • name, level, description                            │  │
│  │    • exercises: List<Exercise>                           │  │
│  │                                                            │  │
│  │  RoutineData (DTO)                                       │  │
│  │    • exercises, description, totalSets                   │  │
│  └──────────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────────┘
                            │ Usa
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                         UTIL LAYER                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  ValidationUtils     → Validación de datos               │  │
│  │  PasswordUtils       → PBKDF2, hashing seguro            │  │
│  │  CryptoUtils         → Encriptación XOR                  │  │
│  │  FirestoreUtils      → Helpers para Firestore            │  │
│  │  ParseUtils          → Parsing y conversión              │  │
│  │  XMLUtils            → Gestión de XML                    │  │
│  │  DateUtils           → Utilidades de fecha               │  │
│  │  DateFormater        → Formato de fechas                 │  │
│  │  ExceptionHandler    → Gestión centralizada de errores   │  │
│  └──────────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CAPA DE PERSISTENCIA                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  ☁️ Firebase/Firestore (Online)                          │  │
│  │    ├─ Collections: users, workouts                       │  │
│  │    ├─ Subcollections: exercises, historic               │  │
│  │    └─ Authentication                                      │  │
│  │                                                            │  │
│  │  💾 Local Storage (Offline)                              │  │
│  │    ├─ backup.dat (encriptado)                            │  │
│  │    ├─ historic.xml                                        │  │
│  │    ├─ offlineHistoric.xml                                │  │
│  │    └─ user.dat (sesión)                                  │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Flujo de Datos

### 📥 **Flujo de Lectura (Query)**

```
Usuario → View → Service → Controller → Firebase/Backup → Model → Service → View → Usuario
```

**Ejemplo: Cargar rutinas**
1. Usuario selecciona nivel en `Workouts` view
2. View llama a `RoutineService.getRoutines(level)`
3. Service verifica online/offline en `Controller`
4. Online: Consulta Firestore directamente
5. Offline: Lee de `BackupReaderService.loadBackupSafe()`
6. Service convierte datos a `List<Exercise>`
7. View renderiza con `CardListRenderer`

---

### 📤 **Flujo de Escritura (Command)**

```
Usuario → View → Service → Validación (Utils) → Controller → Firebase → Backup
```

**Ejemplo: Actualizar perfil**
1. Usuario edita datos en `Profile` view
2. View llama a `ProfileService.updateProfileInDb()`
3. Service valida con `ValidationUtils`
4. Service hashea password con `PasswordUtils` (si cambió)
5. Service actualiza Firestore vía `Controller.getDb()`
6. Service actualiza Firebase Auth (si cambió email/password)
7. `BackupService` sincroniza cambios a local
8. View muestra confirmación

---

## 🧵 Flujo de Ejecución de Entrenamientos

```
ThreadFrame (View)
    ↓
WorkoutExecutionService.executeWorkout()
    ↓
    ├─ RoutineService.loadRoutine() → Obtiene ejercicios
    ↓
    ├─ Thread 1: Temporizador total
    ├─ Thread 2: Sets actuales
    └─ Thread 3: Descansos
    ↓
    ├─ Pausar: pauseLock.wait()
    ├─ Reanudar: pauseLock.notifyAll()
    └─ Saltar descanso: skipRestRequested = true
    ↓
WorkoutExecutionService.historyLog()
    ↓
    ├─ Online: Firestore users/{uid}/historic.add()
    └─ Offline: OfflineHistoricService.gehituSarrera()
```

---

## 🔐 Flujo de Autenticación

```
LoginFrame (View)
    ↓
AuthenticationService.handleLogin(email, password)
    ↓
    ├─ Online:
    │   ├─ Firebase REST API: signInWithPassword
    │   ├─ Obtiene UID
    │   ├─ Firestore: users/{uid}.get()
    │   └─ OfflineHistoricService.sinkronizatuLineazKanpoDBra()
    │
    └─ Offline:
        ├─ BackupReaderService.loadBackupSafe()
        ├─ Busca usuario por email
        └─ PasswordUtils.egiaztaturPasahitza() → PBKDF2
    ↓
UserBackupService.saveEmail(email) → Guarda sesión local
    ↓
Inter (View) → Hub principal
```

---

## 📊 Diagrama de Dependencias

```
MainApp
  └─ Controller (Singleton)
      ├─ DBConnection
      │   └─ Firebase SDK
      ├─ FirstView
      │   └─ LoginFrame
      │       └─ AuthenticationService
      │           ├─ ValidationUtils
      │           ├─ PasswordUtils
      │           ├─ BackupReaderService
      │           ├─ UserBackupService
      │           └─ OfflineHistoricService
      └─ BackupService (Thread daemon)
          └─ CryptoUtils
```

---

## 🚦 Estados del Sistema

```
┌─────────────┐
│   STARTING  │ (MainApp)
└──────┬──────┘
       │
       ├─ Firebase Available? ──YES→ ┌─────────────┐
       │                              │   ONLINE    │
       │                              └──────┬──────┘
       │                                     │
       │                                     ├─ Full Firestore access
       │                                     ├─ Sync offline data
       │                                     └─ Create backup
       │
       └─ Firebase Available? ──NO──→ ┌─────────────┐
                                       │   OFFLINE   │
                                       └──────┬──────┘
                                              │
                                              ├─ Load from backup.dat
                                              ├─ Limited functionality
                                              └─ Queue changes for sync
```

---

**Última actualización**: 5 de noviembre de 2025  
**Versión del diagrama**: 1.0
