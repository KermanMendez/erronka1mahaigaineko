# 📋 RESUMEN ULTRA COMPLETO DEL PROYECTO LRLL GYM

## 🎯 **VISIÓN GENERAL DEL PROYECTO**

**Long Ring Long Land Gym** es una aplicación de escritorio Java Swing para gestión de gimnasios con las siguientes características principales:

- ✅ Sistema de autenticación (registro/login)
- ✅ Gestión de rutinas de entrenamiento por niveles
- ✅ Ejecución de workouts con cronómetros en tiempo real
- ✅ Histórico completo de entrenamientos
- ✅ **Modo ONLINE/OFFLINE con sincronización automática**
- ✅ Sistema de progresión por niveles
- ✅ Gestión de perfiles de usuario
- ✅ Backups automáticos cifrados

---

## 📁 **ESTRUCTURA DE CARPETAS Y CONTENIDO**

### **📂 src/controller/** - Controladores y Conexión
Contiene 3 archivos que gestionan el flujo de control y la conexión a Firebase.

### **📂 src/model/** - Modelos de Datos
Contiene 4 archivos que representan las entidades del dominio.

### **📂 src/view/** - Interfaces Gráficas
Contiene 11 archivos que implementan todas las pantallas de la aplicación.

### **📂 src/service/** - Lógica de Negocio
Contiene 9 archivos que implementan toda la lógica de negocio y operaciones.

### **📂 src/util/** - Utilidades
Contiene 9 archivos con funciones auxiliares reutilizables.

### **📂 lib/** - Librerías Externas
Contiene 40+ JARs de Firebase, Google Cloud, gRPC, Gson, jBcrypt, etc.

### **📂 bin/** - Archivos Compilados
Contiene los .class generados por el compilador Java.

### **📄 Archivos Raíz**
- `pom.xml` - Configuración Maven
- `serviceAccountKey.json` - Credenciales Firebase (NO incluir en git)
- `backup.dat` - Backup cifrado de la base de datos
- `historic.xml` - Histórico sincronizado
- `offlineHistoric.xml` - Histórico pendiente de sincronizar
- `user.dat` - Email del usuario actual (cifrado)

---

## 🔍 **ANÁLISIS DETALLADO POR ARCHIVO**

---

## 📂 **CONTROLLER (3 archivos)**

### **1. MainApp.java** - Punto de Entrada
**Propósito**: Inicializa la aplicación y gestiona el arranque.

**Métodos**:

#### `main(String[] args)`
- **Entrada**: Argumentos de línea de comandos (no usados)
- **Proceso**:
  1. Aplica tema visual con `Theme.apply()`
  2. Inicializa Controller con `Controller.initialize(false)`
  3. Crea instancia de `DBConnection`
  4. Intenta conectar a Firebase con `dbConnection.initialize(true)`
  5. Si conecta: `controller.setOnline(true)`, `controller.setDbConnection()`
  6. Si falla: Modo offline, imprime advertencia
  7. Lanza `FirstView` en el Event Dispatch Thread con `SwingUtilities.invokeLater()`
  8. Crea thread daemon para backup automático en background
  9. Si online: ejecuta `BackupService.saveBackup(true)`
- **Salida**: Aplicación iniciada, ventana visible, backup en proceso

---

### **2. Controller.java** - Singleton Global
**Propósito**: Gestiona el estado global de la aplicación (conexión, DB, vistas).

**Atributos**:
- `instance` (static Controller) - Instancia singleton
- `firestoreInstantzia` (static Firestore) - Instancia Firestore compartida
- `dbConnection` (DBConnection) - Gestor de conexión Firebase
- `db` (Firestore) - Instancia base de datos
- `firstView` (FirstView) - Ventana inicial
- `online` (boolean) - Estado de conexión

**Métodos**:

#### `getInstance() : Controller`
- **Entrada**: Ninguna
- **Proceso**: Lazy initialization - crea instancia si no existe
- **Salida**: Instancia única del Controller

#### `initialize(boolean online)`
- **Entrada**: `online` - Estado inicial de conexión
- **Proceso**: Crea instancia o actualiza estado online
- **Salida**: Controller inicializado

#### Constructor `Controller(Boolean connect)`
- **Entrada**: `connect` - Indicador de conexión (no usado realmente)
- **Proceso**: Inicializa `online = false` por defecto
- **Salida**: Instancia Controller

#### `onOnline()`
- **Entrada**: Ninguna
- **Proceso**: 
  - Verifica si `dbConnection` está inicializado
  - Obtiene instancia Firestore con `getFirestore()`
  - Asigna a `db`
- **Salida**: Controller configurado en modo online

#### `getDbConnection() : DBConnection`
- **Entrada**: Ninguna
- **Salida**: Instancia DBConnection

#### `getDb() : Firestore`
- **Entrada**: Ninguna
- **Proceso**: Si `db` es null pero estamos online, intenta obtener Firestore
- **Salida**: Instancia Firestore o null

#### `getFirstView(Boolean connect) : FirstView`
- **Entrada**: `connect` - Estado de conexión
- **Proceso**: Lazy initialization de FirstView
- **Salida**: Instancia FirstView

#### `setDbConnection(DBConnection dbConnection)`
- **Entrada**: `dbConnection` - Conexión a establecer
- **Proceso**: Asigna la conexión
- **Salida**: Void

#### `setOnline(boolean online)`
- **Entrada**: `online` - Nuevo estado
- **Proceso**: Actualiza estado, llama `onOnline()` si true
- **Salida**: Void

#### `isOnline() : boolean`
- **Entrada**: Ninguna
- **Proceso**: Verifica `online && dbConnection != null && dbConnection.isInitialized()`
- **Salida**: true si está online

#### `getFirestore() : Firestore` (private static)
- **Entrada**: Ninguna
- **Proceso**:
  - Double-checked locking para thread-safety
  - Verifica si FirebaseApp está inicializado
  - Obtiene instancia con `FirestoreClient.getFirestore()`
  - Cachea en `firestoreInstantzia`
- **Salida**: Instancia Firestore o null

---

### **3. DBConnection.java** - Gestor de Conexión Firebase
**Propósito**: Maneja la inicialización y verificación de conexión a Firebase.

**Atributos**:
- `initialized` (boolean) - Estado de inicialización
- `CONNECTION_TIMEOUT_MS` (final int = 1000) - Timeout de conexión
- `FIRESTORE_HOST` (final String = "firestore.googleapis.com")
- `FIRESTORE_PORT` (final int = 443)

**Métodos**:

#### `isInitialized() : boolean`
- **Entrada**: Ninguna
- **Salida**: Estado de inicialización

#### `initialize(Boolean connect) : boolean`
- **Entrada**: `connect` - Intentar conectar o no
- **Proceso**:
  1. Si `connect` es false, retorna false (modo offline)
  2. Verifica existencia de `serviceAccountKey.json`
  3. Carga credenciales con `GoogleCredentials.fromStream()`
  4. Crea `FirebaseOptions` con las credenciales
  5. **Prueba de conexión**: Intenta socket a FIRESTORE_HOST:443 con timeout
  6. Si socket falla, retorna false
  7. Inicializa `FirebaseApp` con `FirebaseApp.initializeApp(options)`
  8. Verifica si ya estaba inicializado con `FirebaseApp.getApps().isEmpty()`
  9. Marca `initialized = true`
- **Salida**: true si conexión exitosa, false si falla
- **Excepciones**: Captura IOException, Exception

---

## 📂 **MODEL (4 archivos)**

### **1. User.java** - Entidad Usuario
**Propósito**: Representa un usuario del sistema.

**Atributos**:
- `serialVersionUID` (long) - Versionado Serializable
- `id` (String) - UID Firebase
- `name` (String) - Nombre
- `surname` (String) - Primer apellido
- `surname2` (String) - Segundo apellido
- `email` (String) - Email
- `password` (String) - Password hasheada
- `birthDate` (Date) - Fecha de nacimiento
- `trainer` (boolean) - Si es entrenador

**Constructores**:

#### `User(String id, String name, String surname, String surname2, String email, String password, Date birthDate, boolean trainer)`
- Constructor completo con todos los campos

#### `User(String username, String email, String password)`
- Constructor simplificado para login

#### `User(String name, String surname, String surname2, String password, String birthDateStr)`
- Constructor con parsing de fecha desde String "dd/MM/yyyy"

**Métodos** (todos son getters/setters estándar):
- `getBirthDate()`, `getEmail()`, `getId()`, `trainer()`, `getName()`, `getPassword()`, `getSurname()`, `getSurname2()`
- `getFullSurname()` - Concatena surname y surname2
- `getDobString()` - Retorna fecha formateada "dd/MM/yyyy"
- `setBirthDate()`, `setEmail()`, `setId()`, `setName()`, `setPassword()`, `setSurname()`, `setSurname2()`, `setTrainer()`

---

### **2. Exercise.java** - Entidad Ejercicio
**Propósito**: Representa un ejercicio individual dentro de una rutina.

**Atributos**:
- `name` (String) - Nombre del ejercicio
- `description` (String) - Descripción
- `img` (String) - URL de imagen
- `videoURL` (String) - URL de video
- `sets` (int) - Número de series
- `reps` (int) - Repeticiones por serie
- `serieTime` (int) - Duración de cada serie en segundos
- `restTime` (int) - Tiempo de descanso en segundos

**Constructores**:

#### `Exercise()`
- Constructor vacío

#### `Exercise(String name, String description, String img, String videoURL, int sets, int reps, int serieTime, int restTime)`
- Constructor completo

#### `Exercise(String name, int sets, int reps)`
- Constructor simplificado

**Métodos**:

#### Getters estándar:
- `getName()`, `getDescription()`, `getImg()`, `getVideoURL()`, `getSets()`, `getReps()`, `getSerieTime()`, `getRestTimeSec()`

#### Setters con parsing:
- `setName(String)`, `setDescription(String)`, `setImg(String)`, `setVideoURL(String)`
- `setSets(Object)` - Usa `ParseUtils.parseInt()` para conversión segura
- `setReps(Object)` - Usa `ParseUtils.parseInt()`
- `setSerieTime(Object)` - Usa `ParseUtils.parseInt()`
- `setRestTimeSec(Object)` - Usa `ParseUtils.parseInt()`

#### `toString() : String`
- **Salida**: Formato "nombre — X sets × Y reps"

#### `equals(Object obj) : boolean`
- **Entrada**: Objeto a comparar
- **Proceso**: Compara todos los campos (name, description, img, videoURL, sets, reps, serieTime, restTime)
- **Salida**: true si son iguales

---

### **3. Workout.java** - Entidad Rutina
**Propósito**: Representa una rutina de entrenamiento completa.

**Atributos**:
- `name` (String) - Nombre de la rutina
- `duration` (Double) - Duración estimada en minutos
- `level` (int) - Nivel de dificultad (1-5+)
- `videoURL` (String) - URL de video explicativo
- `exercises` (Exercise[]) - Array de ejercicios

**Constructor**:

#### `Workout(String name, Double duration, int level, String videoURL, Exercise[] exercises)`
- Constructor completo con todos los campos

**Métodos** (getters/setters estándar):
- `getDuration()`, `getLevel()`, `getName()`, `getExercises()`, `getVideoURL()`
- `setDuration()`, `setExercises()`, `setLevel()`, `setName()`, `setVideoURL()`

---

### **4. RoutineData.java** - DTO Rutina Completa
**Propósito**: Data Transfer Object que encapsula una rutina con metadatos.

**Atributos** (todos final):
- `exercises` (List<Exercise>) - Lista de ejercicios
- `description` (String) - Descripción de la rutina
- `totalSets` (int) - Total de series calculadas

**Constructor**:

#### `RoutineData(List<Exercise> exercises, String description, int totalSets)`
- Constructor completo inmutable

**Métodos**:
- `getExercises() : List<Exercise>`
- `getDescription() : String`
- `getTotalSets() : int`

---

## 📂 **SERVICE (9 archivos)**

### **1. AuthenticationService.java** - Autenticación
**Propósito**: Gestiona registro, login y autenticación de usuarios.

**Atributos**:
- `DEFAULT_API_KEY` (String) - API Key Firebase por defecto
- `API_KEY` (String) - Lee de variable entorno o usa default
- `HTTP_BEZEROA` (OkHttpClient) - Cliente HTTP para API Firebase
- `JSON_MEDIA` (MediaType) - Tipo de contenido JSON
- `dateUtils` (DateUtils) - Utilidad de fechas

**Métodos**:

#### `eskaeraRegistratu(String izena, String abizena1, String abizena2, String email, String password, Date birthdate, Boolean trainer, Boolean connect) : Boolean`
- **Entrada**: Datos completos del usuario a registrar
- **Proceso**:
  1. Valida datos con `ValidationUtils.balidatuErregistroa()`
  2. Si falla validación: muestra error y retorna false
  3. Formatea fecha con `dateUtils.formatDate()`
  4. Llama a `createUser()` para crear en Firebase
  5. Si éxito: muestra mensaje "Ondo erregistratu zara"
  6. Si falla EMAIL_EXISTS: muestra "Email jadanik erregistratuta"
- **Salida**: true si registro exitoso, false si falla

#### `hashPassword(String password) : String`
- **Entrada**: Password en texto plano
- **Proceso**: Llama `PasswordUtils.hashPasahitza()`
- **Salida**: Password hasheada con PBKDF2
- **Excepciones**: NoSuchAlgorithmException, InvalidKeySpecException

#### `createUser(String name, String surname1, String surname2, String email, String password, String birthdate, Boolean trainer, Boolean connect)`
- **Entrada**: Datos completos del usuario
- **Proceso**:
  1. Obtiene Firestore con `Controller.getInstance().getDb()`
  2. Determina nivel: trainer = 5, normal = 1
  3. Hashea password con `hashPassword()`
  4. Crea usuario en Firebase Auth con `FirebaseAuth.getInstance().createUser()`
  5. Obtiene UID del UserRecord
  6. Crea documento en `users/{uid}` con todos los datos
  7. Guarda Map con: name, surname1, surname2, email, birthdate, trainer, password, level
- **Salida**: Void (lanza excepción si falla)
- **Excepciones**: Exception

#### `handleLogin(JTextField textFieldUser, JPasswordField passwordField, Boolean connect) : String`
- **Entrada**: Campos de usuario/password y estado conexión
- **Proceso**:
  1. Obtiene email y password de los campos
  2. Valida que no estén vacíos con `ValidationUtils`
  3. **Si ONLINE**:
     - Llama `checkLogin(email, password)` → obtiene UID
     - Si UID es null: error "Erabiltzailea edo pasahitza ez dira zuzenak"
     - Busca documento en `users/{uid}` de Firestore
     - Lee campo `trainer`
     - Abre ventana `Inter` con estado de conexión real
     - Guarda email con `UserBackupService.saveEmail()`
  4. **Si OFFLINE**:
     - Carga backup con `BackupReaderService.loadBackupSafe()`
     - Busca usuario por email en el backup
     - Verifica password hasheada con `PasswordUtils.egiaztaturPasahitza()`
     - Si coincide: abre `Inter` y guarda email
     - Si no existe: error "Ez dago erabiltzailerik email horrekin"
  5. Cierra ventana actual
- **Salida**: Email del usuario si éxito, null si falla
- **Excepciones**: Captura Exception

#### `checkLogin(String email, String password) : String` (private)
- **Entrada**: Email y password
- **Proceso**:
  1. Construye URL de Firebase Auth API: `signInWithPassword?key=API_KEY`
  2. Crea JSON: `{"email": "...", "password": "...", "returnSecureToken": true}`
  3. Hace POST con OkHttpClient
  4. Si respuesta no exitosa: retorna null
  5. Parsea JSON respuesta, extrae "localId" (UID)
  6. **Sincroniza histórico offline** con `OfflineHistoricService.sinkronizatuLineazKanpoDBra(true)`
- **Salida**: UID del usuario o null si falla
- **Excepciones**: Exception

---

### **2. BackupService.java** - Creación de Backups
**Propósito**: Genera backups cifrados de toda la base de datos Firebase.

**Atributos**:
- `FICHERO` (String = "backup.dat") - Nombre archivo backup
- `db` (Firestore) - Instancia base de datos
- `cryptoUtils` (CryptoUtils) - Utilidad de cifrado

**Métodos**:

#### `saveBackup(Boolean connect)`
- **Entrada**: `connect` - Si está conectado
- **Proceso**:
  1. Verifica que FirebaseApp esté inicializado
  2. Obtiene Firestore con `Controller.getInstance().getDb()`
  3. Crea lista `lines` para almacenar datos
  4. **Lee usuarios de Firebase Auth**:
     - `FirebaseAuth.getInstance().listUsers(null)`
     - Por cada usuario: añade "USER_UID:[cifrado]" y "USER_EMAIL:[cifrado]"
  5. **Lee todas las colecciones de Firestore**:
     - `db.listCollections()` → itera colecciones
     - Por cada colección: añade "COLLECTION:nombre"
     - Llama `addDocumentsToDat()` recursivamente
  6. **Cifra y guarda**:
     - Serializa `lines` con `ObjectOutputStream`
     - Cifra bytes con `cryptoUtils.xorBytes()`
     - Escribe a `backup.dat`
  7. **Genera historic.xml separado**:
     - Si hay registros históricos, crea XML
     - Por cada HistoricRecord: añade nodo `<user uid="...">` con campos
     - Guarda XML formateado con indentación
  8. Imprime "[INFO] Backup-a ondo gordeta"
- **Salida**: Void
- **Excepciones**: Captura Exception

#### `addDocumentsToDat(CollectionReference collection, List<String> lines, String indent, List<HistoricRecord> historicList)` (private)
- **Entrada**: Colección, lista de líneas, indentación, lista históricos
- **Proceso**:
  1. Obtiene todos los documentos de la colección con `.get().get()`
  2. Por cada documento:
     - Añade "DOCUMENT_ID:id" (con indentación)
     - Por cada campo: añade "FIELD:clave=valor_cifrado"
     - Cifra valores con `cryptoUtils.xorEncrypt()`
  3. **Procesa subcolecciones**:
     - `document.getReference().listCollections()`
     - **Caso especial**: Si es `users/{uid}/historic`:
       - Extrae datos y añade a `historicList` (para historic.xml)
       - No procesa como subcolección normal
     - Otras subcolecciones: añade "SUBCOLLECTION:nombre" y llama recursivamente
- **Salida**: Void (modifica listas por referencia)
- **Excepciones**: Exception

**Clase interna**:
#### `HistoricRecord`
- Atributos: `userId` (String), `fields` (Map<String, String>)
- Propósito: Almacenar temporalmente registros históricos para XML

---

### **3. BackupReaderService.java** - Lectura de Backups
**Propósito**: Lee y descifra backups para modo offline.

**Atributos**:
- `FICHERO` (String = "backup.dat")
- `cryptoUtils` (CryptoUtils)

**Clases internas**:

#### `UserData`
- Atributos: `uid`, `email`
- Constructor: `UserData(String uid, String email)`
- `toString()` - Para debugging

#### `DocumentData`
- Atributos: 
  - `id` (String) - ID del documento
  - `fields` (Map<String, String>) - Campos del documento
  - `subcollections` (Map<String, List<DocumentData>>) - Subcolecciones anidadas
- `toString()` - Para debugging

#### `BackupData`
- Atributos:
  - `users` (List<UserData>) - Lista de usuarios
  - `collections` (Map<String, List<DocumentData>>) - Colecciones principales
- `toString()` - Para debugging

**Métodos estáticos**:

#### `loadBackupSafe() : BackupData` (static)
- **Entrada**: Ninguna
- **Proceso**:
  - Crea instancia de BackupReaderService
  - Llama `loadBackupData()`
  - Captura excepciones y retorna null si falla
- **Salida**: BackupData o null
- **Uso**: Método helper para uso común sin try-catch

#### `getTagValue(String tag, Element element) : String` (static private)
- **Entrada**: Nombre de tag XML, elemento padre
- **Proceso**: Busca primer hijo con ese tag, extrae texto
- **Salida**: Contenido del tag o "" si no existe

**Métodos de instancia**:

#### `loadBackupData() : BackupData`
- **Entrada**: Ninguna
- **Proceso**:
  1. Verifica existencia de `backup.dat`
  2. **Si no existe**: Intenta leer XML legacy
     - Parsea `backup.dat` como XML
     - Extrae usuarios: `<user><uid>...</uid><email>...</email></user>`
     - Descifra con `cryptoUtils.xorDecrypt()`
     - Parsea colecciones y documentos
  3. **Si existe backup.dat binario**:
     - Lee bytes del archivo
     - Descifra con `cryptoUtils.xorBytes()`
     - Deserializa con `ObjectInputStream` → List<String>
     - Parsea líneas:
       - "USER_UID:" → descifra y añade a `users`
       - "USER_EMAIL:" → asocia con UID anterior
       - "COLLECTION:" → inicia nueva colección
       - "DOCUMENT_ID:" → crea nuevo DocumentData
       - "FIELD:clave=valor" → descifra valor y añade a fields
       - "SUBCOLLECTION:" → detecta indentación, extrae líneas, parsea recursivamente con `parseDocumentsFromLines()`
  4. Construye estructura completa BackupData
- **Salida**: BackupData con toda la estructura o null si error
- **Excepciones**: Captura Exception

#### `parseDocuments(Element parentElement) : List<DocumentData>` (private)
- **Entrada**: Elemento XML padre
- **Proceso**:
  - Busca todos los `<document>` hijos directos
  - Por cada documento: lee atributo `id`, parsea campos
  - Si encuentra `<subcollection>`: llama recursivamente
  - Descifra valores de campos
- **Salida**: Lista de DocumentData

#### `parseDocumentsFromLines(List<String> lines) : List<DocumentData>` (private)
- **Entrada**: Líneas de texto con indentación
- **Proceso**:
  - Parsea líneas eliminando espacios
  - "DOCUMENT_ID:" → crea DocumentData
  - "FIELD:" → extrae clave=valor, descifra, añade a fields
- **Salida**: Lista de DocumentData

---

### **4. RoutineService.java** - Gestión de Rutinas
**Propósito**: Maneja la obtención y filtrado de rutinas de ejercicios.

**Atributos**:
- `db` (Firestore) - Instancia base de datos
- `connect` (Boolean) - Estado de conexión
- `listModel` (DefaultListModel<String>) - Modelo para JList
- `firestoreUtils` (FirestoreUtils)

**Constructor**:
#### `RoutineService(Boolean connect)`
- Inicializa db con `Controller.getInstance().getDb()`, asigna connect

**Métodos**:

#### `levels() : String[]`
- **Entrada**: Ninguna
- **Proceso**:
  1. Obtiene email con `UserBackupService.getCurrentUserEmail()`
  2. **Si OFFLINE**:
     - Carga backup con `BackupReaderService.loadBackupSafe()`
     - Obtiene nivel con `firestoreUtils.getUserLevelFromBackup(backup, email)`
  3. **Si ONLINE**:
     - Busca usuario por email con `firestoreUtils.getUserDocumentByEmail()`
     - Lee campo `level` del documento
  4. Crea array de Strings: ["1. Maila", "2. Maila", ..., "N. Maila"]
- **Salida**: Array de niveles disponibles o ["Ez dago mailarik eskuragarri"] si error
- **Excepciones**: InterruptedException, ExecutionException

#### `ariketak(int aukera)`
- **Entrada**: `aukera` - Índice del nivel (0-based)
- **Proceso**:
  1. Calcula nivel real: `maila = aukera + 1`
  2. Crea thread nuevo para operación asíncrona
  3. Llama `getAriketak(maila)` → obtiene lista de ejercicios
  4. En EDT con `SwingUtilities.invokeLater()`:
     - Limpia `listModel`
     - Por cada ejercicio: añade `exercise.toString()` al modelo
     - Si vacío: añade "Ez dago ariketarik maila honetarako"
- **Salida**: Void (actualiza UI asíncronamente)

#### `getAriketak(int level) : List<Exercise>` (private)
- **Entrada**: `level` - Nivel de dificultad
- **Proceso**:
  1. Query Firestore: `db.collection("workouts").whereEqualTo("level", level).get()`
  2. Toma primer documento (primera rutina del nivel)
  3. Obtiene subcolección `exercise`: `routineDoc.getReference().collection("exercise")`
  4. Convierte cada documento a objeto Exercise con `doc.toObject(Exercise.class)`
- **Salida**: Lista de ejercicios o lista vacía
- **Excepciones**: InterruptedException, ExecutionException

#### `getRoutines(int selectedLevel, Boolean connect) : String[]`
- **Entrada**: Nivel seleccionado, estado conexión
- **Proceso**:
  1. **Si ONLINE**:
     - Query: `db.collection("workouts").whereEqualTo("level", selectedLevel)`
     - Por cada documento: extrae campo `name`
     - Retorna array de nombres
  2. **Si OFFLINE**:
     - Carga backup
     - Busca en `backup.collections.get("workouts")`
     - Filtra por `level == selectedLevel`
     - Extrae campo `name` de cada documento
- **Salida**: Array de nombres de rutinas o ["Ez dago rutinik maila honetarako"]
- **Excepciones**: InterruptedException, ExecutionException

#### `getLevels(int nivelSeleccionado, String nivelText, Boolean connect) : String[]`
- **Entrada**: Nivel, nombre de rutina, estado conexión
- **Proceso**:
  1. **Si ONLINE**:
     - Query: `workouts` WHERE `level == nivel` AND `name == nivelText`
     - Obtiene subcolección `exercises`
     - Por cada ejercicio: formatea "nombre – descripción (Total Sets: X)"
  2. **Si OFFLINE**:
     - Busca en backup: filtra por level y name
     - Extrae subcolección `exercises`
     - Formatea igual que online
- **Salida**: Array de strings con ejercicios formateados o ["Ez daude ariketarik"]
- **Excepciones**: InterruptedException, ExecutionException

#### `getListModel() : DefaultListModel<String>`
- **Salida**: Modelo de lista interno

**Métodos estáticos de UI**:

#### `updateRoutinesComboBox(JComboBox comboMaila, JComboBox comboMailaRutinakLevel, RoutineService routines, Boolean connect, JList listaWorkout, boolean isHistoric)` (static)
- **Entrada**: ComboBoxes de nivel y rutinas, servicio, conexión, lista, modo (historic/workout)
- **Proceso**:
  1. Lee nivel seleccionado del comboBox
  2. Crea thread nuevo
  3. Llama `routines.getRoutines(nivel, connect)`
  4. En EDT: actualiza modelo del comboBox de rutinas
  5. Espera 50ms
  6. Llama `updateList()` para actualizar lista de ejercicios
- **Salida**: Void (actualiza UI asíncronamente)

#### `updateWorkoutList(JComboBox comboMaila, JComboBox comboMailaRutinakLevel, Boolean connect, JList listaWorkout, boolean isHistoric)` (static)
- **Entrada**: Similar a anterior
- **Proceso**:
  1. Lee nivel y rutina seleccionados
  2. Crea thread
  3. Llama `updateList()` directamente
- **Salida**: Void

#### `updateList(int nivel, String rutinaNombre, Boolean connect, JList listaWorkout, boolean isHistoric)` (static private)
- **Entrada**: Nivel, nombre rutina, conexión, lista, modo
- **Proceso**:
  1. **Si isHistoric == true**:
     - Crea `HistoricReaderService`
     - Llama `getHistoric(nivel, rutinaNombre, connect)` → obtiene registros históricos
  2. **Si isHistoric == false**:
     - Crea `RoutineService`
     - Llama `getLevels(nivel, rutinaNombre, connect)` → obtiene ejercicios
  3. En EDT: actualiza modelo de la lista con `AbstractListModel` anónimo
- **Salida**: Void
- **Excepciones**: InterruptedException, ExecutionException

---

### **5. WorkoutExecutionService.java** - Ejecución de Entrenamientos
**Propósito**: Gestiona la ejecución de rutinas con cronómetros y lógica de progresión.

**Atributos**:
- `amaituta` (boolean) - Si se completó el entrenamiento
- `totalSeconds` (long) - Tiempo total transcurrido
- `completedSets` (int) - Series completadas
- `expectedTotalSets` (int) - Total de series esperadas
- `totalTime` (int) - Tiempo restante
- `elapsedSeconds` (int) - Segundos transcurridos
- `expectedTotalSeconds` (int) - Duración total esperada
- `db` (Firestore) - Base de datos
- `skipNow` (volatile boolean) - Flag para saltar descanso
- `level` (int) - Nivel actual
- `firestoreUtils` (FirestoreUtils)

**Métodos principales**:

#### `start(int level, String routineName, Boolean connect) : List<Exercise>` [@Deprecated]
- **Entrada**: Nivel, nombre rutina, conexión
- **Proceso**: Llama `getExercises()`
- **Salida**: Lista de ejercicios
- **Nota**: Deprecado, usar `loadRoutine()` en su lugar

#### `getExercises(int level, String routineName, Boolean connect) : List<Exercise>`
- **Entrada**: Nivel, nombre rutina, conexión
- **Proceso**: Llama `loadRoutine()`, extrae solo la lista de ejercicios
- **Salida**: Lista de ejercicios
- **Excepciones**: InterruptedException, ExecutionException

#### `loadRoutine(int level, String routineName, Boolean connect) : RoutineData`
- **Entrada**: Nivel, nombre rutina, conexión
- **Proceso**:
  1. Guarda nivel en `this.level`
  2. **Si OFFLINE**:
     - Carga backup
     - Llama `loadExercisesFromBackup()`
  3. **Si ONLINE**:
     - Llama `loadExercisesFromFirestore()`
  4. Obtiene descripción con `getDefaultRoutineDescription()`
  5. Crea y retorna `RoutineData` con ejercicios, descripción y total sets
- **Salida**: RoutineData completo
- **Excepciones**: InterruptedException, ExecutionException

#### `loadExercisesFromBackup(int level, String routineName, BackupData backup) : ExercisesResult` (private)
- **Entrada**: Nivel, nombre rutina, backup
- **Proceso**:
  1. Busca en `backup.collections.get("workouts")`
  2. Filtra por `level` y `name`
  3. Extrae campo `description`
  4. Busca subcolección `exercises` o `exercise`
  5. Por cada ejercicio: crea objeto con `exerciseFromBackupDoc()`
  6. Suma sets totales
- **Salida**: ExercisesResult(ejercicios, totalSets, descripción)

#### `exerciseFromBackupDoc(DocumentData exDoc) : Exercise` (private)
- **Entrada**: DocumentData del backup
- **Proceso**:
  - Crea Exercise vacío
  - Lee campos: name, description, reps, sets, timeSets, timePauseSec
  - Usa setters con parsing automático
- **Salida**: Objeto Exercise

#### `loadExercisesFromFirestore(int level, String routineName) : ExercisesResult` (private)
- **Entrada**: Nivel, nombre rutina
- **Proceso**:
  1. Query: `workouts` WHERE `level == level` AND `name == routineName`
  2. Toma primer documento
  3. Lee campo `description`
  4. Obtiene subcolección `exercises`
  5. Por cada documento: crea Exercise con `exerciseFromFirestoreDoc()`
  6. Suma sets totales
- **Salida**: ExercisesResult
- **Excepciones**: InterruptedException, ExecutionException

#### `exerciseFromFirestoreDoc(QueryDocumentSnapshot exerciseDoc) : Exercise` (private)
- **Entrada**: DocumentSnapshot de Firestore
- **Proceso**: Similar a `exerciseFromBackupDoc()` pero lee de Firestore
- **Salida**: Exercise

#### `getDefaultRoutineDescription(String routineDescription, List<Exercise> exercises) : String` (private)
- **Entrada**: Descripción de rutina, lista de ejercicios
- **Proceso**:
  - Si `routineDescription` no es null/vacía: retorna esa
  - Si no: intenta obtener descripción del primer ejercicio
  - Si no: retorna ""
- **Salida**: String con descripción

**Métodos de ejecución**:

#### `executeWorkout(int level, String routineName, Boolean connect, JLabel labelTotal, JLabel labelSeries, JLabel labelDescansos, JLabel labelHasiera, JLabel lblRutinaDeskribapena, JLabel lblRutinaSets, Supplier<Boolean> stopSupplier, Supplier<Boolean> skipSupplier, Supplier<Boolean> pauseSupplier, Object lock, Runnable onWorkoutStarted, Runnable onWorkoutFinished)`
- **Entrada**: Nivel, rutina, conexión, 6 labels UI, 3 suppliers de estado, lock, 2 callbacks
- **Proceso**:
  1. Crea thread nuevo
  2. Carga rutina con `loadRoutine()`
  3. Actualiza labels de descripción y sets en EDT
  4. Llama `startExerciseThreads()` con todos los parámetros
- **Salida**: Void (ejecución asíncrona)

#### `startExerciseThreads(List<Exercise> exercises, [muchos parámetros], boolean thread1, boolean thread2, boolean thread3, Runnable onWorkoutStarted, Runnable onWorkoutFinished)`
- **Entrada**: Ejercicios, labels, suppliers, flags de threads, callbacks
- **Proceso**:
  1. **Countdown**: Loop 5→1 segundos, actualiza label cada segundo
  2. Muestra "Goazen! Entrenamendua hasi da!"
  3. Verifica que haya ejercicios
  4. Hace visibles labels de tiempo, oculta label de inicio
  5. **Ejecuta callback `onWorkoutStarted`** (habilita botones)
  6. Calcula `expectedTotalSets` y `expectedTotalSeconds` con `computeExpectedTotalSeconds()`
  7. Inicializa contadores a 0
  8. **Crea 3 threads paralelos**:
     - `tTotal` - Tiempo total (modo 0)
     - `tSeries` - Series actuales (modo 1)
     - `tRest` - Descansos (modo 2)
  9. Lanza los 3 threads con `start()`
  10. Espera con `join()` a que terminen los 3
  11. Si no se detuvo: marca `amaituta = true`
  12. **Muestra popup con estadísticas**:
      - Tiempo total, series completadas/esperadas, porcentaje
      - "Zorionak zure ahaleginagatik!"
  13. **Ejecuta callback `onWorkoutFinished`** (cierra ventana)
  14. Llama `historyLog()` para guardar en histórico
- **Salida**: Void
- **Excepciones**: InterruptedException

#### `runExerciseThread(List<Exercise> exercises, JLabel label, Supplier<Boolean> stopSupplier, Supplier<Boolean> skipRest, Supplier<Boolean> pauseSupplier, Object pauseLock, int mode, boolean canPause)` (private)
- **Entrada**: Ejercicios, label, suppliers, lock, modo (0/1/2), si permite pausa
- **Proceso**:
  1. Por cada ejercicio en la lista:
     - Llama `executeExerciseSets()` para hacer todas las series
     - Si se detuvo: sale
     - Si no es el último: llama `handleRestPeriod()` para descanso entre ejercicios
  2. Actualiza `totalSeconds` al finalizar
- **Salida**: Void

#### `executeExerciseSets(Exercise exercise, [parámetros], int mode, boolean canPause) : boolean` (private)
- **Entrada**: Ejercicio, label, suppliers, mode, canPause
- **Proceso**:
  1. Por cada serie (1 a `exercise.getSets()`):
     - Llama `executeSet()` para ejecutar la serie
     - Si se detuvo: retorna true
     - Incrementa `completedSets` si mode == 0
     - Si no es la última serie: llama `handleRestPeriod()` para descanso entre series
- **Salida**: true si se detuvo, false si completó

#### `executeSet(int currentSet, int serieTime, [parámetros], int mode, boolean canPause) : boolean` (private)
- **Entrada**: Número de serie, duración, label, suppliers, mode, canPause
- **Proceso**:
  1. Loop de 1 a `serieTime` (segundos):
     - Verifica `stopSupplier()` → si true, sale
     - Llama `waitIfPaused()` si `canPause`
     - Si mode == 0: incrementa `elapsedSeconds`, calcula tiempo restante
     - Actualiza label en EDT:
       - Mode 0: "Denbora totala: X seg"
       - Mode 1: "Sets N - X/Y seg"
     - Duerme 1 segundo con `sleep(1000)`
- **Salida**: true si se detuvo, false si completó

#### `handleRestPeriod(int restDuration, int mode, [parámetros], boolean isInterExercise) : boolean` (private)
- **Entrada**: Duración descanso, modo, label, suppliers, si es entre ejercicios
- **Proceso**:
  1. Loop mientras `elapsed < restDuration` y `!skipNow`:
     - Verifica `stopSupplier()` → sale
     - Verifica `skipRest()` → marca `skipNow = true` y sale
     - Llama `waitIfPaused()` si está pausado
     - Si mode == 0: incrementa `elapsedSeconds`
     - Incrementa `elapsed`
     - Actualiza label:
       - Mode 0: Tiempo total
       - Mode 2: "Atsedena X/Y seg"
     - Duerme en chunks de 200ms (5 veces) para respuesta rápida a skip
- **Salida**: true si se detuvo, false si completó

#### `waitIfPaused(Supplier<Boolean> pauseSupplier, Object pauseLock)` (private)
- **Entrada**: Supplier de pausa, lock
- **Proceso**:
  - Mientras `pauseSupplier.get()` sea true:
    - Bloquea en `pauseLock.wait()` hasta que se notifique
- **Salida**: Void

#### `sleep(int ms)` (private)
- **Entrada**: Milisegundos
- **Proceso**: `Thread.sleep()`, captura InterruptedException
- **Salida**: Void

#### `computeExpectedTotalSeconds(List<Exercise> exercises) : int` (private)
- **Entrada**: Lista de ejercicios
- **Proceso**:
  1. Por cada ejercicio:
     - Suma `computeExerciseTime(exercise)`
     - Si no es el último: suma `exercise.getRestTimeSec()` (descanso entre ejercicios)
- **Salida**: Total de segundos esperados

#### `computeExerciseTime(Exercise exercise) : int` (private)
- **Entrada**: Ejercicio
- **Proceso**:
  - `totalSerieTime = sets * serieTime`
  - `totalRestTime = (sets - 1) * restTime` (descansos entre series)
  - `total = totalSerieTime + totalRestTime`
- **Salida**: Tiempo total del ejercicio

**Métodos de histórico y nivel**:

#### `getUserLevel() : int`
- **Entrada**: Ninguna
- **Proceso**:
  1. Obtiene email con `UserBackupService.getCurrentUserEmail()`
  2. Busca usuario en Firestore con `firestoreUtils.getUserDocumentByEmail()`
  3. Lee campo `level`, convierte con `ParseUtils.parseInt()`
  4. Si < 1: retorna 1
- **Salida**: Nivel del usuario

#### `sumLevel()`
- **Entrada**: Ninguna
- **Proceso**:
  1. Verifica `amaituta` (si completó entrenamiento)
  2. Si level actual < nivel del usuario: no hace nada (ya superado)
  3. Si level actual >= nivel usuario:
     - Busca documento del usuario
     - Incrementa level: `level++`
     - Actualiza en Firestore: `db.collection("users").document(uid).update("level", level)`
- **Salida**: Void (actualiza nivel en DB)

#### `historyLog(String routineName)`
- **Entrada**: Nombre de la rutina
- **Proceso**:
  1. Obtiene email con `UserBackupService.getCurrentUserEmail()`
  2. **Intenta guardar ONLINE**:
     - Query: busca rutina por nombre para obtener ID
     - Obtiene UID del usuario con `firestoreUtils.getUserIdByEmail()`
     - Obtiene fecha actual con `DateUtils.getCurrentFormattedDate()`
     - Crea Map con:
       - `completed`: amaituta
       - `date`: fecha actual
       - `totalSets`: completedSets
       - `totalTime`: totalSeconds
       - `workoutId`: ID de la rutina
       - `level`: nivel
     - Guarda en `users/{uid}/historic`
     - Llama `sumLevel()` para incrementar nivel si aplica
  3. **Si falla ONLINE, guarda OFFLINE**:
     - Crea `OfflineHistoricService`
     - Carga backup para obtener UID
     - Busca workoutId en el backup por nombre y nivel
     - Crea Map similar pero con Strings
     - Llama `offline.gehituSarrera(uid, email, fields)`
     - Guarda en `offlineHistoric.xml`
- **Salida**: Void
- **Excepciones**: Captura Exception

**Clase interna**:
#### `ExercisesResult` (private static)
- Atributos: `exercises`, `totalSets`, `routineDescription`
- Constructor: `ExercisesResult(List<Exercise>, int, String)`
- Propósito: Encapsular resultado de carga de ejercicios

---

### **6. ProfileService.java** - Gestión de Perfiles
**Propósito**: Maneja la actualización de datos de perfil de usuario.

**Métodos**:

#### `updateUserDocument(String email, String name, String surname1, String surname2, String birthdate) : boolean`
- **Entrada**: Email del usuario y datos a actualizar
- **Proceso**:
  1. Obtiene Firestore con `Controller.getInstance().getDb()`
  2. Busca usuario con `FirestoreUtils.getUserDocumentByEmail()`
  3. Obtiene referencia del documento
  4. Crea Map con: name, surname, surname2, birthdate
  5. Ejecuta `docRef.update(updates).get()` (bloqueante)
  6. Imprime "[INFO] Erabiltzailearen datuak eguneratuta"
- **Salida**: true si éxito, false si error
- **Excepciones**: Captura Exception

#### `updatePasswordAuthAndSaveHash(String email, String newPassword) : boolean`
- **Entrada**: Email y nueva contraseña
- **Proceso**:
  1. Si password es null/vacía: retorna true (no cambia)
  2. Obtiene Firestore
  3. Obtiene UID con `FirestoreUtils.getUserIdByEmail()`
  4. **Actualiza en Firebase Auth**:
     - Crea `UserRecord.UpdateRequest(uid).setPassword(newPassword)`
     - Ejecuta `FirebaseAuth.getInstance().updateUser(req)`
  5. **Hashea y guarda en Firestore**:
     - Hashea con `PasswordUtils.hashPasahitza(newPassword)`
     - Busca documento del usuario
     - Actualiza campo `password` con el hash
  6. Imprime "[INFO] Pasahitza eguneratuta"
- **Salida**: true si éxito, false si error
- **Excepciones**: Captura Exception

#### `loadProfileFromDb(JTextField tfName, JTextField tfSurname1, JTextField tfSurname2, JTextField tfDob)`
- **Entrada**: 4 campos de texto a rellenar
- **Proceso**:
  1. Crea thread nuevo para operación asíncrona
  2. Obtiene email con `UserBackupService.getCurrentUserEmail()`
  3. Obtiene Firestore
  4. Busca usuario con `FirestoreUtils.getUserDocumentByEmail()`
  5. Lee campos: name, surname, surname2, birthdate
  6. **Procesa apellidos**:
     - Si tiene surname2: usa surname y surname2 separados
     - Si no: divide surname por espacios (asume "Apellido1 Apellido2")
  7. En EDT con `SwingUtilities.invokeLater()`:
     - Rellena los 4 campos de texto
- **Salida**: Void (actualiza UI asíncronamente)
- **Excepciones**: Captura Exception

#### `showMessage(Boolean dbOk, Boolean pwdOk, String name, String surname, String dob, Runnable onSuccess)`
- **Entrada**: Flags de éxito, datos, callback
- **Proceso**:
  1. Si ambos flags son true:
     - Muestra popup "Profila gordeta" con datos actualizados
     - Ejecuta callback `onSuccess`
  2. Si alguno falló:
     - Muestra popup de error "Errorea profila eguneratzean"
- **Salida**: Void

#### `validateChanges(JTextField tfName, JTextField tfSurname1, JTextField tfSurname2, JPasswordField pfPassword, JPasswordField pfPassword2, JTextField finalTfDob) : User`
- **Entrada**: Campos del formulario
- **Proceso**:
  1. Extrae valores de los campos
  2. **Valida campos obligatorios**:
     - Nombre: `ValidationUtils.balidatuBeteBeharrekoa()`
     - Apellido 1: ídem
     - Apellido 2: ídem
  3. **Valida password si se cambió**:
     - Si algún campo de password no está vacío:
       - `ValidationUtils.balidatuPasahitzakBerdinak(pwd, pwd2)`
       - Si fallan: muestra error y retorna null
  4. **Valida fecha**:
     - Si fecha no es válida: muestra error y retorna null
  5. Crea objeto User con los datos validados
- **Salida**: Objeto User o null si validación falla

#### `setLocalEmail(String email)`
- **Entrada**: Email a establecer
- **Proceso**: Guarda email localmente (implementación no mostrada en fragmento)
- **Salida**: Void

#### `updateProfileInDb(User userProfile, String targetEmail, Runnable onSuccess)`
- **Entrada**: Usuario, email objetivo, callback
- **Proceso**:
  1. Crea thread nuevo
  2. Llama `updateUserDocument()` con datos del usuario
  3. Si hay nueva password: llama `updatePasswordAuthAndSaveHash()`
  4. Llama `showMessage()` con resultados
- **Salida**: Void (actualiza asíncronamente)

---

### **7. OfflineHistoricService.java** - Sincronización Offline
**Propósito**: Gestiona el histórico offline y sincronización con Firestore.

**Atributos**:
- `FITXATEGIA` (String = "offlineHistoric.xml")

**Métodos**:

#### `gehituHistorialeraXml(String erabiltzaileId, Map<String, Object> datuak)` (private)
- **Entrada**: UID del usuario, datos a guardar
- **Proceso**:
  1. Parsea o crea `historic.xml` con `XMLUtils.parseOrCreateXmlDocument()`
  2. Obtiene elemento raíz `<historicBackup>`
  3. Crea elemento `<user uid="...">` con atributo
  4. Por cada entrada en datuak:
     - Crea elemento hijo con clave como tag
     - Añade valor como texto
  5. Añade nodo user al documento raíz
  6. Guarda XML con `XMLUtils.saveXmlDocument()`
- **Salida**: Void
- **Excepciones**: Exception

#### `gehituSarrera(String erabiltzaileId, String email, Map<String, String> eremuak)`
- **Entrada**: UID, email, campos del registro
- **Proceso**:
  1. Parsea o crea `offlineHistoric.xml`
  2. Obtiene raíz
  3. Crea elemento `<user uid="..." email="...">`
  4. Por cada campo en eremuak:
     - Crea elemento hijo con clave/valor
  5. Añade al documento
  6. Guarda XML
- **Salida**: Void (guarda en offlineHistoric.xml)
- **Excepciones**: Captura Exception

#### `sinkronizatuLineazKanpoDBra(Boolean konektatuta) : boolean`
- **Entrada**: Estado de conexión
- **Proceso**:
  1. Si no está conectado: retorna false
  2. Obtiene Firestore con `Controller.getInstance().getDb()`
  3. Verifica existencia de `offlineHistoric.xml`
  4. Si no existe o está vacío: retorna true (nada que sincronizar)
  5. Parsea XML con `XMLUtils.parseXmlDocument()`
  6. Busca todos los elementos `<user>`
  7. **Por cada usuario**:
     - Lee atributos uid y email
     - Valida que coincida con usuario actual
     - Lee todos los campos hijo
     - **Conversión de tipos**:
       - Si valor es numérico: convierte a Integer
       - Si es "true"/"false"/"bai"/"ez": convierte a Boolean
       - Resto: deja como String
     - Busca UID del usuario en Firestore por email
     - **Guarda en Firestore**:
       - Referencia: `users/{uid}/historic/{autoId}`
       - Ejecuta `add(data)` bloqueante con `.get()`
     - **Mueve a historic.xml**:
       - Llama `gehituHistorialeraXml()` para registro permanente
       - Añade índice a lista de sincronizados
  8. **Limpia offlineHistoric.xml**:
     - Crea nuevo documento XML
     - Copia solo nodos NO sincronizados
     - Guarda documento limpio
  9. **Si quedó vacío**:
     - Verifica con parsing
     - Si no tiene hijos: elimina archivo `offlineHistoric.xml`
  10. Imprime "[INFO] Lineaz kanpoko historiko guztia ondo sinkronizatuta"
- **Salida**: true si todo sincronizó, false si hubo errores
- **Excepciones**: Captura InterruptedException, ExecutionException

---

### **8. HistoricReaderService.java** - Lectura de Histórico
**Propósito**: Lee el histórico de entrenamientos (online y offline).

**Atributos**:
- `db` (Firestore)
- `parse` (ParseUtils)

**Constructor**:
#### `HistoricReaderService(Boolean connect)`
- Inicializa db con `Controller.getInstance().getDb()`

**Métodos**:

#### `getHistoric(int aukeratutakoMaila, String rutinarenIzena, Boolean connect) : String[]`
- **Entrada**: Nivel, nombre rutina (puede ser vacío), conexión
- **Proceso**:
  1. Crea lista de resultados
  2. **Si ONLINE**:
     - Obtiene email con `UserBackupService.getCurrentUserEmail()`
     - Busca usuario con `FirestoreUtils.getUserDocumentByEmail()`
     - Obtiene subcolección `historic`
     - Filtra: `whereEqualTo("level", aukeratutakoMaila)`
     - Por cada documento: llama `addEntryIfMatch()` (compara rutina, formatea)
  3. **Lee backups offline**:
     - Carga backup con `BackupReaderService.loadBackupSafe()`
     - Busca UID del usuario en backup por email
     - Llama `readOfflineXml("historic.xml", ...)` → añade entradas
     - Llama `readOfflineXml("offlineHistoric.xml", ...)` → añade entradas
  4. Si lista vacía: retorna ["Ez dago historikorik rutina honetarako"]
  5. Elimina duplicados con `LinkedHashSet`
  6. Imprime cantidad de registros encontrados
- **Salida**: Array de strings formateados
- **Excepciones**: InterruptedException, ExecutionException

#### `addEntryIfMatch(List<String> list, Firestore db, DocumentSnapshot exerciseDoc, String rutinarenIzena)` (private)
- **Entrada**: Lista destino, db, documento, nombre rutina filtro
- **Proceso**:
  1. Lee campos del documento:
     - `completed` (boolean) → convierte a euskera "Bai"/"Ez"
     - `date` (String)
     - `totalSets` (int)
     - `totalTime` (int)
     - `workoutId` (String)
  2. Busca nombre de rutina:
     - Query: `db.collection("workouts").document(workoutId)`
     - Lee campo `name`
  3. **Filtra por rutina** (si se especificó):
     - Normaliza y compara nombres (contiene/igual)
     - Si no coincide: sale sin añadir
  4. **Calcula porcentaje de completado**:
     - Busca ejercicios de la rutina: `workouts/{id}/exercises`
     - Suma sets totales de la rutina
     - `percent = (totalSets * 100.0) / totalSetsInWorkout`
     - Limita a 100%
  5. **Formatea entrada**:
     - `"Data: DD/MM/YYYY | Bukatuta: Bai/Ez (XX,X%) | Serieak: X/Y | ⏱Denbora: ZZZ seg"`
  6. Añade a la lista
- **Salida**: Void (modifica lista por referencia)
- **Excepciones**: InterruptedException, ExecutionException

#### `readOfflineXml(String fileName, String userId, String email, BackupData backup, int level, String rutinarenIzena) : List<String>` (private)
- **Entrada**: Archivo XML, UID, email, backup, nivel, rutina filtro
- **Proceso**:
  1. Parsea XML con `XMLUtils.parseXmlDocument()`
  2. Si falla: retorna lista vacía
  3. Busca elementos `<user>`
  4. **Por cada usuario**:
     - Lee atributos uid y email
     - Valida que coincida con usuario actual (por UID o email)
     - Lee todos los campos hijo a Map
     - Filtra por nivel: si no coincide, continúa
  5. **Extrae campos**:
     - `completed` → convierte a euskera
     - `date`, `totalSets`, `totalTime`, `workoutId`
  6. **Busca nombre de rutina en backup**:
     - Itera `backup.collections.get("workouts")`
     - Busca por workoutId
     - Obtiene campo `name`
     - Suma sets de subcolección `exercises`
  7. **Filtra por rutina** (si se especificó):
     - Compara nombres normalizados
  8. **Calcula porcentaje y formatea**:
     - Igual que `addEntryIfMatch()`
     - Añade a lista de resultados
- **Salida**: Lista de strings formateados
- **Excepciones**: No lanza, captura internamente

---

### **9. UserBackupService.java** - Backup de Usuario Actual
**Propósito**: Guarda y lee el email del usuario actual (cifrado).

**Atributos**:
- `FICHERO` (String = "user.dat")
- `cryptoUtils` (CryptoUtils)

**Métodos estáticos**:

#### `getCurrentUserEmail() : String` (static)
- **Entrada**: Ninguna
- **Proceso**:
  - Crea instancia de UserBackupService
  - Llama `loadEmail()`
- **Salida**: Email del usuario actual o null
- **Uso**: Método helper para evitar crear instancias repetidas

**Métodos de instancia**:

#### `saveEmail(String email)`
- **Entrada**: Email a guardar
- **Proceso**:
  1. Crea `ByteArrayOutputStream` y `ObjectOutputStream`
  2. Serializa email con `oos.writeObject(email)`
  3. Obtiene bytes del stream
  4. Cifra bytes con `cryptoUtils.xorBytes()`
  5. Escribe bytes cifrados a `user.dat` con `FileOutputStream`
- **Salida**: Void (guarda archivo cifrado)
- **Excepciones**: IOException

#### `loadEmail() : String`
- **Entrada**: Ninguna
- **Proceso**:
  1. Verifica existencia de `user.dat`
  2. Si no existe o está vacío: retorna null
  3. Lee bytes del archivo con `Files.readAllBytes()`
  4. Descifra con `cryptoUtils.xorBytes()`
  5. Deserializa con `ObjectInputStream`
  6. Verifica que sea String
  7. Si formato inválido: imprime error y retorna null
- **Salida**: Email del usuario o null si falla
- **Excepciones**: Captura IOException, ClassNotFoundException

---

## 📂 **UTIL (9 archivos)**

### **1. CryptoUtils.java** - Utilidades de Cifrado
**Propósito**: Cifrado/descifrado simétrico con XOR para datos sensibles.

**Atributos**:
- `DEFAULT_KEY` (byte = 0x5A) - Clave XOR por defecto

**Métodos**:

#### `xorBytes(byte[] data) : byte[]`
- **Entrada**: Bytes a cifrar/descifrar
- **Proceso**: Llama `xorBytes(data, DEFAULT_KEY)`
- **Salida**: Bytes cifrados/descifrados

#### `xorBytes(byte[] data, byte key) : byte[]` (static)
- **Entrada**: Bytes y clave XOR
- **Proceso**: 
  - Crea array resultado del mismo tamaño
  - Por cada byte: aplica XOR con la clave `result[i] = data[i] ^ key`
- **Salida**: Array cifrado/descifrado
- **Nota**: XOR es simétrico (cifrar = descifrar)

#### `xorEncrypt(String text) : String`
- **Entrada**: Texto plano
- **Proceso**:
  1. Llama `xorEncrypt(text, DEFAULT_KEY)`
  2. Si texto null/vacío: retorna ""
  3. Convierte a bytes con `text.getBytes()`
  4. Aplica XOR con `xorBytes()`
  5. Codifica en Base64 con `Base64.getEncoder()`
- **Salida**: String cifrado en Base64

#### `xorDecrypt(String base64Text) : String`
- **Entrada**: Texto cifrado en Base64
- **Proceso**:
  1. Llama `xorDecrypt(base64Text, DEFAULT_KEY)`
  2. Si null/vacío: retorna ""
  3. Decodifica Base64 con `Base64.getDecoder()`
  4. Aplica XOR para descifrar
  5. Convierte bytes a String
  6. Si error de Base64: retorna ""
- **Salida**: Texto descifrado
- **Excepciones**: Captura IllegalArgumentException

---

### **2. DateUtils.java** - Utilidades de Fechas
**Propósito**: Formateo y parsing de fechas con formato consistente.

**Atributos**:
- `DEFAULT_DATE_FORMAT` (String = "dd/MM/yyyy")
- `sdf` (SimpleDateFormat) - Formateador compartido

**Métodos estáticos**:

#### `getCurrentFormattedDate() : String` (static)
- **Entrada**: Ninguna
- **Proceso**:
  - Sincroniza en `sdf` (thread-safe)
  - Formatea `new Date()` con SimpleDateFormat
- **Salida**: Fecha actual en formato "dd/MM/yyyy"

#### `formatDateStatic(Date date) : String` (static)
- **Entrada**: Fecha a formatear
- **Proceso**: Si null retorna "", si no formatea sincronizado
- **Salida**: String formateado o ""

**Métodos de instancia**:

#### `formatDate(Date date) : String`
- **Entrada**: Fecha
- **Proceso**: Igual que estático pero no-static
- **Salida**: String formateado

#### `parseDate(String dateStr) : Date`
- **Entrada**: String de fecha "dd/MM/yyyy"
- **Proceso**: 
  - Valida null/vacío
  - Parsea sincronizado con `sdf.parse()`
- **Salida**: Objeto Date
- **Excepciones**: ParseException

#### `parseDateSafe(String dateStr) : Date`
- **Entrada**: String de fecha
- **Proceso**: Llama `parseDate()` y captura excepciones
- **Salida**: Date o null si falla

#### `getCurrentDateFormatted() : String`
- **Entrada**: Ninguna
- **Proceso**: Llama `getCurrentFormattedDate()`
- **Salida**: Fecha actual formateada

---

### **3. PasswordUtils.java** - Seguridad de Contraseñas
**Propósito**: Hashing seguro de contraseñas con PBKDF2 y salt aleatorio.

**Atributos**:
- `ITERAZIOAK` (int = 65536) - Iteraciones PBKDF2
- `GAKO_LUZERA` (int = 256) - Longitud de clave en bits
- `GATZ_LUZERA` (int = 16) - Longitud del salt
- `ALGORITMOA` (String = "PBKDF2WithHmacSHA256") - Algoritmo

**Métodos públicos**:

#### `hashPasahitza(String pasahitza) : String` (static)
- **Entrada**: Password en texto plano
- **Proceso**:
  1. Valida no null/vacío
  2. Genera salt aleatorio con `sortuGatza()`
  3. Genera hash con `sortuHash(pasahitza, gatza)`
  4. Codifica salt y hash en Base64
  5. Retorna formato "salt$hash"
- **Salida**: String con salt y hash separados por $
- **Excepciones**: NoSuchAlgorithmException, InvalidKeySpecException, IllegalArgumentException

#### `egiaztaturPasahitza(String pasahitza, String gordetakoPasahitza) : boolean` (static)
- **Entrada**: Password plano, hash almacenado "salt$hash"
- **Proceso**:
  1. Valida inputs y formato (debe contener $)
  2. Divide por $ en 2 partes
  3. Decodifica salt y hash desde Base64
  4. Genera hash con password dado y salt almacenado
  5. Compara hashes con `denboraKonstanteanBerdinak()` (previene timing attacks)
- **Salida**: true si coinciden, false si no
- **Excepciones**: Captura Exception

**Métodos privados**:

#### `sortuGatza() : byte[]` (private static)
- **Entrada**: Ninguna
- **Proceso**: 
  - Crea `SecureRandom`
  - Genera 16 bytes aleatorios
- **Salida**: Salt aleatorio

#### `sortuHash(String pasahitza, byte[] gatza) : byte[]` (private static)
- **Entrada**: Password, salt
- **Proceso**:
  1. Crea `PBEKeySpec` con password.toCharArray(), salt, iteraciones, longitud
  2. Obtiene `SecretKeyFactory` con algoritmo PBKDF2WithHmacSHA256
  3. Genera hash con `fabrika.generateSecret(spec).getEncoded()`
- **Salida**: Hash generado
- **Excepciones**: NoSuchAlgorithmException, InvalidKeySpecException

#### `denboraKonstanteanBerdinak(byte[] a, byte[] b) : boolean` (private static)
- **Entrada**: Dos arrays de bytes
- **Proceso**:
  - Verifica misma longitud
  - Compara byte por byte con XOR acumulativo
  - Evita short-circuit para tiempo constante (seguridad contra timing attacks)
- **Salida**: true si idénticos

**Métodos deprecated**:
- `hashPassword()` → usar `hashPasahitza()`
- `verifyPassword()` → usar `egiaztaturPasahitza()`

---

### **4. ValidationUtils.java** - Validación de Datos
**Propósito**: Validaciones centralizadas con mensajes en euskera.

**Atributos**:
- `EMAIL_PATTERN` (Pattern) - Regex para emails
- `DATE_PATTERN` (Pattern) - Regex para "dd/MM/yyyy"

**Métodos estáticos**:

#### `emailBaliozkoa(String email) : boolean`
- **Entrada**: Email
- **Proceso**: Valida con regex `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$`
- **Salida**: true si válido

#### `pasahitzaBaliozkoa(String pasahitza) : boolean`
- **Entrada**: Password
- **Proceso**: Verifica no null y longitud >= 6
- **Salida**: true si válida

#### `pasahitzakBerdinak(String pasahitza1, String pasahitza2) : boolean`
- **Entrada**: Dos passwords
- **Proceso**: Verifica no null y equals
- **Salida**: true si idénticas

#### `testuaHutsik(String testua) : boolean`
- **Entrada**: Texto
- **Proceso**: Verifica null o trim().isEmpty()
- **Salida**: true si vacío

#### `dataFormatuaZuzena(String data) : boolean`
- **Entrada**: Fecha como String
- **Proceso**: Valida con regex `^\\d{2}/\\d{2}/\\d{4}$`
- **Salida**: true si formato correcto

#### `balidatuBeteBeharrekoa(String balioa, String eremuIzena) : String`
- **Entrada**: Valor, nombre del campo
- **Proceso**: Verifica no vacío
- **Salida**: Mensaje de error o null si OK

#### `balidatuEmail(String email) : String`
- **Entrada**: Email
- **Proceso**: Verifica no vacío y formato válido
- **Salida**: "Email-a beteta egon behar du." o "Email formatua ez da zuzena." o null

#### `balidatuPasahitza(String pasahitza) : String`
- **Entrada**: Password
- **Proceso**: Verifica no vacío y longitud >= 6
- **Salida**: Mensaje de error o null

#### `balidatuPasahitzakBerdinak(String pasahitza1, String pasahitza2) : String`
- **Entrada**: Dos passwords
- **Proceso**: 
  1. Valida primera password
  2. Verifica que sean iguales
- **Salida**: "Pasahitzak ez dira berdinak." o null

#### `balidatuErregistroa(String izena, String abizena1, String abizena2, String email, String pasahitza1, String pasahitza2, Object jaiotzeData) : String`
- **Entrada**: Todos los campos del registro
- **Proceso**:
  1. Valida nombre (obligatorio)
  2. Valida apellido1 (obligatorio)
  3. Valida apellido2 (obligatorio)
  4. Valida email (formato)
  5. Valida passwords (iguales y >= 6)
  6. Valida fecha de nacimiento (no null)
- **Salida**: Primer error encontrado o null si todo OK

---

### **5. ParseUtils.java** - Conversión de Tipos
**Propósito**: Parsing seguro de tipos primitivos sin excepciones.

**Métodos estáticos**:

#### `parseInt(Object value) : int` (static)
- **Entrada**: Objeto a convertir
- **Proceso**:
  1. Si null: retorna 0
  2. Si es Number: extrae intValue()
  3. Si es String: trim(), verifica vacío, parsea con Integer.parseInt()
  4. Si falla: retorna 0
- **Salida**: Entero o 0 si no se puede convertir

#### `getIntValue(Long val) : int` (static)
- **Entrada**: Long
- **Proceso**: Si null retorna 0, si no convierte a int
- **Salida**: int

**Métodos de instancia**:

#### `parseIntFirstValid(Object... values) : int`
- **Entrada**: Varios valores
- **Proceso**: Itera, retorna primer valor válido != 0
- **Salida**: Primer entero válido o 0

#### `parseBoolean(String val) : boolean`
- **Entrada**: String
- **Proceso**: Convierte a lowercase, compara con "true", "bai", "yes", "1"
- **Salida**: true/false

#### `booleanToEuskera(boolean value) : String`
- **Entrada**: Boolean
- **Proceso**: Traduce a euskera
- **Salida**: "Bai" o "Ez"

---

### **6. FirestoreUtils.java** - Utilidades Firestore
**Propósito**: Operaciones comunes de Firestore y backup.

**Métodos**:

#### `getUserDocumentByEmail(Firestore db, String email) : DocumentSnapshot`
- **Entrada**: Instancia Firestore, email
- **Proceso**:
  1. Valida parámetros no null
  2. Query: `db.collection("users").whereEqualTo("email", email).get().get()`
  3. Si vacío: retorna null
  4. Retorna primer documento
- **Salida**: DocumentSnapshot del usuario o null
- **Excepciones**: InterruptedException, ExecutionException

#### `getUserIdByEmail(Firestore db, String email) : String`
- **Entrada**: DB, email
- **Proceso**: Llama `getUserDocumentByEmail()`, extrae ID
- **Salida**: UID del usuario o null
- **Excepciones**: InterruptedException, ExecutionException

#### `getUserIdFromBackup(BackupData backup, String email) : String`
- **Entrada**: Backup, email
- **Proceso**: 
  - Itera `backup.users`
  - Busca por email
  - Retorna uid
- **Salida**: UID o null

#### `getUserLevelFromBackup(BackupData backup, String email) : int`
- **Entrada**: Backup, email
- **Proceso**:
  1. Valida parámetros
  2. Obtiene colección "users" del backup
  3. Busca documento por email
  4. Extrae campo "level"
  5. Parsea con `ParseUtils.parseInt()`
  6. Si no encuentra: retorna 1 (nivel default)
- **Salida**: Nivel del usuario (1-5+)

---

### **7. XMLUtils.java** - Operaciones XML
**Propósito**: Parsing, creación y guardado de documentos XML.

**Métodos estáticos**:

#### `parseXmlDocument(String fileName) : Document` (static)
- **Entrada**: Nombre de archivo
- **Proceso**:
  1. Verifica existencia y tamaño > 0
  2. Crea `DocumentBuilderFactory` y `DocumentBuilder`
  3. Parsea con `builder.parse(file)`
  4. Normaliza con `doc.getDocumentElement().normalize()`
- **Salida**: Document parseado o null si error
- **Excepciones**: Captura ParserConfigurationException, SAXException, IOException

#### `parseOrCreateXmlDocument(String fileName, String rootElement) : Document` (static)
- **Entrada**: Archivo, nombre del elemento raíz
- **Proceso**:
  1. Si archivo existe y no vacío: parsea y retorna
  2. Si no: crea nuevo Document con elemento raíz
- **Salida**: Document existente o nuevo
- **Excepciones**: ParserConfigurationException, SAXException, IOException

#### `createNewDocument(String rootElement) : Document` (static)
- **Entrada**: Nombre elemento raíz
- **Proceso**: Crea Document vacío, añade elemento raíz
- **Salida**: Nuevo Document
- **Excepciones**: ParserConfigurationException

#### `saveXmlDocument(Document doc, String fileName) : void` (static)
- **Entrada**: Document, nombre archivo
- **Proceso**:
  1. Crea `Transformer` con `TransformerFactory`
  2. Configura propiedades: INDENT=yes, ENCODING=UTF-8, indent-amount=2
  3. Transforma Document a archivo con `FileOutputStream`
- **Salida**: Void (guarda archivo XML formateado)
- **Excepciones**: TransformerException, IOException

#### `getDocumentBuilder() : DocumentBuilder` (static)
- **Entrada**: Ninguna
- **Proceso**: Crea y retorna DocumentBuilder
- **Salida**: DocumentBuilder configurado
- **Excepciones**: ParserConfigurationException

#### `getTagValue(String tag, Element element) : String` (static)
- **Entrada**: Nombre de tag, elemento padre
- **Proceso**:
  1. Busca elementos con ese tag name
  2. Si encuentra: extrae textContent del primero
  3. Si no: retorna ""
- **Salida**: Contenido del tag o ""

---

### **8. ExceptionHandler.java** - Manejo de Errores
**Propósito**: Gestión centralizada de errores con mensajes en euskera.

**Enum ErrorMota**:
- KONEXIO_ERROREA, AUTENTIFIKAZIO_ERROREA, DATU_ERROREA
- VALIDAZIO_ERROREA, SISTEMA_ERROREA, SINKRONIZAZIO_ERROREA, EZEZAGUNA

**Métodos estáticos**:

#### `erakutsiErrorea(String mezua)` (static)
- **Entrada**: Mensaje de error
- **Proceso**: Llama sobrecarga con título "Errorea"
- **Salida**: Void

#### `erakutsiErrorea(String titulua, String mezua)` (static)
- **Entrada**: Título, mensaje
- **Proceso**: Llama sobrecarga con exception=null
- **Salida**: Void

#### `erakutsiErrorea(String titulua, String mezua, Exception exception)` (static)
- **Entrada**: Título, mensaje, excepción opcional
- **Proceso**:
  1. Si exception != null: imprime en consola con printStackTrace()
  2. En EDT con `SwingUtilities.invokeLater()`:
     - Muestra `JOptionPane.showMessageDialog()` con tipo ERROR_MESSAGE
- **Salida**: Void

#### `erakutsiInfo(String mezua)` (static)
- **Entrada**: Mensaje
- **Proceso**: Muestra JOptionPane con INFORMATION_MESSAGE
- **Salida**: Void

#### `erakutsiInfo(String titulua, String mezua)` (static)
- Similar con título personalizado

#### `erakutsiAbisua(String mezua)` (static)
- **Entrada**: Mensaje
- **Proceso**: Muestra JOptionPane con WARNING_MESSAGE
- **Salida**: Void

#### `erakutsiAbisua(String titulua, String mezua)` (static)
- Similar con título personalizado

#### `eskaBaieztapena(String mezua) : boolean` (static)
- **Entrada**: Mensaje
- **Proceso**: Muestra diálogo de confirmación YES/NO
- **Salida**: true si usuario elige SÍ

---

### **9. DateFormater.java** - Formateador JDatePicker
**Propósito**: Formateador personalizado para componente JDatePicker.

**Atributos**:
- `serialVersionUID` (long)
- `sdf` (SimpleDateFormat) - Formato "dd-MM-yyyy"

**Métodos**:

#### `valueToString(Object value) : String` (@Override)
- **Entrada**: Valor del picker (Date o Calendar)
- **Proceso**:
  1. Si es Date: formatea con sdf
  2. Si es Calendar: extrae Date y formatea
  3. Si no: llama super.valueToString()
- **Salida**: String formateado "dd-MM-yyyy"
- **Excepciones**: ParseException

---

## 📂 **VIEW (12 archivos)**

### **1. Theme.java** - Gestor de Tema Visual
**Propósito**: Aplica tema Nimbus con colores personalizados (thread-safe).

**Atributos**:
- `applied` (volatile boolean) - Flag de aplicación

**Métodos**:

#### `apply()` (static)
- **Entrada**: Ninguna
- **Proceso**:
  1. **Double-checked locking** para thread-safety
  2. Si ya aplicado: retorna
  3. Sincroniza en Theme.class
  4. Busca "Nimbus" en Look and Feels instalados
  5. Aplica con `UIManager.setLookAndFeel()`
  6. **Personaliza UIManager**:
     - `control`, `info` → UIStyle.BACKGROUND
     - `nimbusBase` → UIStyle.PRIMARY
     - `Button.background` → UIStyle.BUTTON_BG
     - Fuentes: BUTTON_FONT, LABEL_FONT, FIELD_FONT
  7. Marca `applied = true`
- **Salida**: Void (aplica tema globalmente)
- **Excepciones**: Captura ClassNotFoundException, InstantiationException, etc.

#### `isApplied() : boolean` (static)
- **Salida**: Estado de aplicación

---

### **2. UIStyle.java** - Estilos Visuales
**Propósito**: Colores, fuentes y estilos consistentes para toda la UI.

**Clase interna RoundedBorder**:
- **Atributos**: `color`, `radius`
- **Métodos**: `paintBorder()` - Dibuja borde redondeado con antialiasing

**Constantes de Color**:
- `PRIMARY` = Color(33, 150, 243) - Azul principal
- `SECONDARY` = Color(30, 30, 30) - Gris oscuro
- `ACCENT` = Color(255, 193, 7) - Amarillo/dorado
- `BACKGROUND` = Color(245, 245, 245) - Gris claro
- `BUTTON_BG` = Color(25, 118, 210) - Azul botones
- `BUTTON_FG` = WHITE
- `FIELD_BG` = WHITE
- `FIELD_FG` = BLACK
- `BORDER_COLOR` = Color(200, 200, 200)

**Constantes de Fuente**:
- `TITLE_FONT` = "Segoe UI" Bold 22pt
- `LABEL_FONT` = "Segoe UI" Plain 16pt
- `BUTTON_FONT` = "Segoe UI" Bold 16pt
- `FIELD_FONT` = "Segoe UI" Plain 15pt

**Métodos estáticos**:

#### `styleButton(JButton button)` (static)
- **Entrada**: Botón a estilizar
- **Proceso**:
  1. Establece colores, fuente, borde redondeado
  2. Desactiva focus painting, content area filled
  3. Tamaño mínimo 120×40
  4. **Custom ButtonUI**:
     - Pinta fondo redondeado con Graphics2D
     - Antialiasing activado
     - Si presionado: color más oscuro
  5. PropertyChangeListeners para repintar
- **Salida**: Void (modifica botón)

#### `styleLabel(JLabel label, boolean isTitle)` (static)
- **Entrada**: Label, si es título
- **Proceso**: Aplica TITLE_FONT o LABEL_FONT, color SECONDARY
- **Salida**: Void

#### `stylePanel(JPanel panel)` (static)
- **Entrada**: Panel
- **Proceso**: Aplica BACKGROUND, borde con BORDER_COLOR
- **Salida**: Void

#### `styleField(JComponent comp)` (static)
- **Entrada**: Componente (TextField, List, etc.)
- **Proceso**:
  1. Aplica colores de fondo/texto
  2. Aplica FIELD_FONT
  3. Si es JList: configura colores de selección (ACCENT)
- **Salida**: Void

#### `styleScrollPane(JScrollPane sp)` (static)
- **Entrada**: ScrollPane
- **Proceso**: Aplica borde, color de fondo al viewport
- **Salida**: Void

#### `addHoverEffect(JButton button)` (static)
- **Entrada**: Botón
- **Proceso**:
  1. Cambia cursor a HAND_CURSOR
  2. **MouseListener**:
     - `mouseEntered`: cambia a ACCENT, texto negro
     - `mouseExited`: restaura BUTTON_BG, texto blanco
- **Salida**: Void (añade efecto hover)

#### `styleIconButton(JButton button)` (static)
- **Entrada**: Botón de icono
- **Proceso**:
  1. Tamaño fijo 48×48
  2. Fondo BACKGROUND, foreground PRIMARY
  3. Borde redondeado con radio 8
  4. Custom UI con fondo redondeado pintado
  5. Hover effect: cambia a ACCENT.brighter()
- **Salida**: Void

---

### **3. FirstView.java** - Pantalla Inicial
**Propósito**: Primera ventana que se muestra al iniciar la aplicación.

**Atributos**:
- `contentPane` (JPanel) - Panel principal
- `loadLogo` (LoadLogo) - Cargador de logo
- `controller` (Controller) - Singleton

**Constructor FirstView(Boolean connect)**:
- **Proceso**:
  1. Configura ventana: título "LONG RING LONG LAND GYM", icono, EXIT_ON_CLOSE
  2. Crea panel con BorderLayout
  3. **Centro**: Label con logo escalado, centrado
  4. **Sur**: Botón "Sartu" con hover effect
     - Listener: abre LoginFrame, cierra FirstView
  5. Tamaño 560×380, centrado
  6. Aplica estilos con UIStyle
- **UI**: Logo grande + botón "Sartu"

---

### **4. LoginFrame.java** - Pantalla de Login
**Propósito**: Autenticación de usuarios (login/registro).

**Atributos**:
- `contentPane` (JPanel)
- `textFieldUser` (JTextField) - Campo email
- `passwordField` (JPasswordField) - Campo password
- `authService` (AuthenticationService)

**Constructor LoginFrame(Boolean connect)**:
- **Proceso**:
  1. Configura ventana: título "LOGIN", icono, EXIT_ON_CLOSE
  2. Panel central con BoxLayout vertical
  3. **Logo**: Escalado a 240×180
  4. **Campo Email**: Tooltip "Zure emaila sartu", tamaño 360×40
  5. **Campo Password**: Tooltip "Zure pasahitza sartu"
  6. **Botón Login**: 
     - Llama `authService.handleLogin()`
     - Si éxito: cierra ventana (abre Inter desde service)
  7. **Botón Register**:
     - Abre RegisterDialog
  8. Espaciado vertical con `Box.createVerticalStrut()`
  9. Panel blanco con borde redondeado
  10. Tamaño 480×600, centrado
- **UI**: Logo + 2 campos + 2 botones en columna

---

### **5. Inter.java** - Menú Principal
**Propósito**: Hub central con acceso a Perfil y Workouts.

**Atributos**:
- `contentPane` (JPanel)

**Constructor Inter(Boolean connect)**:
- **Proceso**:
  1. Título "Ongi Etorri LRLL", 600×450
  2. **Norte**: Label "LONG RING LONG LAND GYM" en azul, fuente grande
  3. **Centro**: GridBagLayout con 2 botones:
     - **btnProfile**: Icono profile_icon.png (80×54)
       - Tooltip "Zure profila ikusi eta editatu"
       - Listener: abre Profile, cierra Inter
     - **btnWorkouts**: Icono workout_icon.png
       - Tooltip "Zure entrenamenduen errutina ikusi"
       - Listener: abre Workouts, cierra Inter
  4. Botones con icono arriba, texto abajo
  5. Efectos hover en ambos
- **UI**: Título + 2 botones grandes con iconos

---

### **6. RegisterDialog.java** - Diálogo de Registro
**Propósito**: Formulario modal para registro de nuevos usuarios.

**Atributos**:
- `textFieldEmail`, `passwordField`, `textFieldIzena` (JTextField/JPasswordField)
- `abizena1Field`, `abizena2Field` (JTextField) - Apellidos
- `datePicker` (JDatePickerImpl) - Selector de fecha
- `checkboxtrainer` (JCheckBox) - Si es entrenador
- `authService` (AuthenticationService)

**Constructor RegisterDialog(Boolean connect)**:
- **Proceso**:
  1. Configura ventana: título "Erabiltzailearen Registroa", 520×420
  2. **Formulario GridBagLayout** con 8 campos:
     - Email (tooltip: "Zure email-a sartu")
     - Pasahitza
     - Izena
     - Abizena (primer apellido)
     - Bigarren Abizena (segundo apellido)
     - Jaiotze Data (JDatePicker con locale euskera)
     - Entrenatzailea da? (checkbox)
  3. **Botón Registratu**:
     - Llama `authService.eskaeraRegistratu()` con todos los campos
     - Si éxito: cierra diálogo
  4. **Botón Utzi**: Cierra sin guardar
  5. Espaciado 8px entre campos
- **UI**: Formulario vertical + 2 botones abajo

---

### **7. Workouts.java** - Pantalla de Entrenamientos
**Propósito**: Navegación y selección de rutinas por nivel.

**Atributos**:
- `edukiontzia` (JPanel) - Panel principal
- `comboMaila` (JComboBox<String>) - Selector de nivel
- `listaWorkout` (JList<String>) - Lista de ejercicios
- `btnIkusiHistoria`, `btnHasiWorkout` (JButton)
- `lblMailaAktuala` (JLabel) - Muestra nivel actual

**Constructor Workouts(Boolean connect)**:
- **Proceso**:
  1. Ventana 700×500, BorderLayout
  2. **Header Norte**:
     - **Botón Atzera**: Icono flecha, vuelve a Inter
     - **Label "Workouts"**: Centrado, fuente grande, azul
     - **Botón Logout**: Vuelve a LoginFrame
  3. **Filtros Centro-Norte**:
     - Label "Maila: X" (muestra nivel actual)
     - Label "Zure maila aukeratu:"
     - **ComboBox Nivel**: Carga con `routines.levels()`
     - **ComboBox Rutinas**: Carga con `routines.getRoutines(nivel, connect)`
  4. **Centro**: ScrollPane con JList
     - Renderer personalizado: `CardListRenderer`
     - Cell height flexible (-1)
     - Muestra ejercicios de la rutina seleccionada
  5. **Botones Sur**:
     - **Ikusi historia**: Abre ViewHistoric
     - **Hasi Workout-a**: Abre ThreadFrame con nivel y rutina
  6. **Listeners**:
     - `comboMaila.addActionListener()`: Actualiza label y recarga rutinas
     - `comboMailaRutinakLevel.addActionListener()`: Recarga ejercicios
  7. Actualización inicial con `RoutineService.updateWorkoutList()`
- **UI**: Header + filtros + lista + 2 botones

---

### **8. ThreadFrame.java** - Ejecución de Workout
**Propósito**: Pantalla de ejecución en tiempo real con cronómetros.

**Atributos**:
- `workoutService` (WorkoutExecutionService) - Ejecutor de rutinas
- `paused`, `skipRestRequested`, `stopRequested` (boolean) - Flags de control
- `pauseLock` (Object) - Lock para sincronización de pausa
- `labelTotala`, `labelSerieak`, `labelAtsedenak` (JLabel) - Cronómetros
- `labelHasiera` (JLabel) - Countdown inicial
- `lblRutinaIzena`, `lblRutinaDeskribapena`, `lblRutinaSets` (JLabel) - Info rutina

**Constructor ThreadFrame(int level, String routineName, Boolean connect)**:
- **Proceso**:
  1. Ventana 693×490, título "Workout - {nombre}"
  2. **Info Superior**:
     - Label con nombre de rutina (título grande)
     - Label con descripción (cargado dinámicamente)
     - Label con total de series
  3. **Panel Info Centro** (GridLayout 1×3):
     - **Label Total**: Borde "⏱️ Total", invisible al inicio
     - **Label Serieak**: Borde "🏋️ Serieak", invisible al inicio
     - **Label Atsedenak**: Borde "💤 Atsedenak", invisible al inicio
  4. **Label Hasiera**: Para countdown 5→1, visible al inicio
  5. **Panel Botones Sur** (3 botones):
     - **Pausatu / Jarraitu**: Toggle pausa, notifica pauseLock
     - **Atsedena saltatu**: Activa flag skipRestRequested
     - **Amaitu rutina**: Activa flag stopRequested
     - Los 3 deshabilitados al inicio, se habilitan en callback
  6. **Ejecución**: Llama `workoutService.executeWorkout()` con:
     - Suppliers de flags para control thread-safe
     - Objeto pauseLock para sincronización
     - Callback onWorkoutStarted: Habilita botones, muestra labels
     - Callback onWorkoutFinished: Vuelve a Workouts
- **Salida**: Void (ejecución asíncrona)

---

### **9. ViewHistoric.java** - Histórico de Entrenamientos
**Propósito**: Visualización de entrenamientos completados.

**Atributos**:
- `edukiontzia` (JPanel)
- `comboMaila` (JComboBox<String>) - Filtro por nivel
- `listaWorkout` (JList<String>) - Lista de registros históricos

**Constructor ViewHistoric(Boolean connect)**:
- **Proceso**:
  1. Ventana 700×480, título "Historic Workouts"
  2. **Header**: Similar a Workouts (Atzera, Título, Logout)
  3. **Filtros**: Nivel + Rutinas (sin opción "todos")
  4. **Lista**: Muestra registros formateados con CardListRenderer
     - Formato: "Data: DD/MM/YYYY | Bukatuta: Bai/Ez (XX%) | Serieak: X/Y | ⏱Denbora: ZZZ seg"
  5. **Listeners**: Recargan con `isHistoric = true`
  6. Carga inicial con `RoutineService.updateWorkoutList(..., true)`
- **UI**: Similar a Workouts pero solo visualización, sin botón "Hasi"
- **Diferencia clave**: Usa `HistoricReaderService` en lugar de ejercicios

---

### **10. LoadLogo.java** - Cargador de Logo
**Propósito**: Carga y cacheo del logo (optimización).

**Atributos**:
- `cachedLogo` (static ImageIcon) - Logo cacheado (Singleton)

**Métodos**:

#### `getLogo(ImageIcon logo) : ImageIcon`
- **Entrada**: Logo original (ignorado)
- **Proceso**:
  1. Double-checked locking para thread-safety
  2. Si cachedLogo es null: carga de recursos, escala a 360×260
  3. Retorna logo cacheado
- **Salida**: ImageIcon escalado
- **Optimización**: Solo escala una vez

#### `getLogo() : ImageIcon`
- Sobrecarga sin parámetros, llama a `getLogo(null)`

---

### **11. CardListRenderer.java** - Renderizador de Lista
**Propósito**: Renderer personalizado para JList con estilo tarjetas.

**Métodos**:

#### `getListCellRendererComponent(...) : Component`
- **Entrada**: Lista, valor, índice, seleccionado, foco
- **Proceso**:
  1. Crea JPanel con BorderLayout
  2. Borde compuesto: LineBorder + EmptyBorder (8px padding)
  3. Color fondo: ACCENT si seleccionado, FIELD_BG si no
  4. Label con HTML (ancho 520px, permite saltos de línea)
  5. Color texto: BUTTON_FG si seleccionado, FIELD_FG si no
- **Salida**: Panel como celda de lista
- **Efecto**: Lista con apariencia de tarjetas estilizadas

---

## 🗂️ **ARQUITECTURA Y FLUJO DE LA APLICACIÓN**

### **📊 Flujo de Inicio**
1. **MainApp.main()**: Punto de entrada
2. Aplica tema Nimbus con `Theme.apply()`
3. Inicializa `Controller` (singleton)
4. Intenta conectar Firebase → `DBConnection.initialize()`
5. Si éxito: modo ONLINE, si falla: modo OFFLINE
6. Muestra `FirstView`
7. Ejecuta backup automático en thread background

### **🔐 Flujo de Autenticación**
1. `FirstView` → "Sartu" → `LoginFrame`
2. Usuario introduce email/password
3. `AuthenticationService.handleLogin()`:
   - **ONLINE**: Valida Firebase Auth, sincroniza histórico offline
   - **OFFLINE**: Valida contra backup.dat descifrado
4. Si éxito: guarda email en user.dat cifrado, abre `Inter`

### **🏋️ Flujo de Entrenamiento**
1. `Inter` → "Workouts" → `Workouts`
2. Usuario selecciona nivel y rutina
3. `RoutineService` carga ejercicios (Firestore u offline)
4. "Hasi Workout-a" → `ThreadFrame`
5. `WorkoutExecutionService.executeWorkout()`:
   - Countdown 5→1
   - 3 threads paralelos (total, series, descansos)
   - Control: Pausar/Saltar/Detener
6. Al finalizar: popup estadísticas, guarda histórico
7. Si completa nivel actual: incrementa nivel con `sumLevel()`

### **📖 Flujo de Histórico**
1. `Workouts` → "Ikusi historia" → `ViewHistoric`
2. `HistoricReaderService.getHistoric()`:
   - **ONLINE**: Firestore users/{uid}/historic
   - **OFFLINE**: historic.xml + offlineHistoric.xml
3. Formatea registros con %, tiempo, fecha

### **👤 Flujo de Perfil**
1. `Inter` → "Profila" → `Profile`
2. Carga datos con `ProfileService.loadProfileFromDb()`
3. Usuario edita → "Gorde" → valida y actualiza
4. Actualiza Firestore + hashea nueva password si existe

### **🔄 Sincronización Offline**
1. Login ONLINE → `OfflineHistoricService.sinkronizatuLineazKanpoDBra()`
2. Lee offlineHistoric.xml
3. Sube registros a Firestore
4. Mueve a historic.xml (permanente)
5. Limpia offlineHistoric.xml

### **💾 Sistema de Backups**
1. Thread daemon ejecuta `BackupService.saveBackup()` al inicio
2. Lee Firebase Auth + Firestore recursivamente
3. Cifra con XOR, serializa a backup.dat
4. Históricos separados en historic.xml
5. Se usa en modo offline

---

## 🔒 **SEGURIDAD**

### **Contraseñas**
- PBKDF2WithHmacSHA256 con 65536 iteraciones
- Salt aleatorio 16 bytes (SecureRandom)
- Formato "salt$hash" en Base64
- Verificación tiempo constante (anti timing attacks)

### **Cifrado de Datos**
- XOR simétrico con clave 0x5A
- Archivos: backup.dat, user.dat
- **Limitación**: XOR simple, no criptográficamente seguro

### **Validaciones**
- Email: Regex completo
- Password: Mínimo 6 caracteres
- Campos obligatorios validados
- Fecha: dd/MM/yyyy con regex

---

## 📡 **MODO ONLINE vs OFFLINE**

### **Online (Firebase)**
✅ Auth con Firebase Auth API  
✅ Datos tiempo real Firestore  
✅ Sincronización automática  
✅ Backup automático  
✅ Incremento nivel  

### **Offline (Backups locales)**
✅ Auth contra backup.dat  
✅ Lectura rutinas desde backup  
✅ Histórico desde XML  
✅ Registro en offlineHistoric.xml  
❌ No sincroniza hasta próxima conexión  
❌ No crea usuarios nuevos  

---

## 🎨 **DISEÑO UI**

- **Look and Feel**: Nimbus
- **Colores**: PRIMARY (#2196F3), ACCENT (#FFC107)
- **RoundedBorder**: Bordes redondeados con antialiasing
- **Hover effects**: Cambio a amarillo
- **Fuentes**: Segoe UI (22pt títulos, 16pt labels, 16pt botones)

---

## 📚 **LIBRERÍAS EXTERNAS**

- Firebase Admin SDK 9.2.0
- Google Cloud Firestore 3.13.1
- Gson 2.10.1 (JSON)
- jDatePicker 1.3.4 (selector fechas)
- OkHttp 3.14.9 (HTTP client)
- gRPC (comunicación Firebase)
- SLF4J 2.0.9 (logs)

---

## 🚀 **CARACTERÍSTICAS DESTACADAS**

✨ Sistema dual online/offline con sincronización automática  
✨ Backups cifrados completos  
✨ Ejecución workouts tiempo real con 3 threads paralelos  
✨ Control avanzado: Pausar/Saltar/Detener  
✨ Histórico persistente XML offline  
✨ Progresión automática niveles  
✨ UI moderna Nimbus + efectos hover  
✨ Seguridad PBKDF2 + salt aleatorio  
✨ Thread-safe double-checked locking  
✨ Validaciones euskera  

---

## 📝 **NOTAS FINALES**

### **Buenas Prácticas**
- Singleton thread-safe
- Separación capas (MVC)
- Services para lógica negocio
- Utils reutilizables
- Manejo centralizado errores
- Caching recursos

### **Mejoras Sugeridas**
- Migrar XOR → AES
- Pool de threads
- Tests unitarios
- DI framework
- Migrar a JavaFX
- Patrón Repository
- Logs estructurados

### **Compatibilidad**
- Java 8+ requerido
- Windows (Segoe UI)
- Internet opcional

---

**FIN DEL RESUMEN COMPLETO**
