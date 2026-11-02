# 🛒 RetailFlow - Sistema de Monitoreo y Análisis de Flujo en Retail

**RetailFlow** es una aplicación móvil nativa para Android diseñada para la toma de muestras, análisis demográfico y control de tránsito de personas en accesos de tiendas y centros comerciales. 

Permite a los supervisores de tienda registrar en tiempo real ingresos por rango etario, sentido de circulación, acompañamiento y conversión de compra, consolidando la información en una base de datos local SQLite mediante **Room**.

---

## 🛠️ Tecnologías y Arquitectura

La aplicación está desarrollada siguiendo las buenas prácticas recomendadas por Google para el desarrollo nativo en Android:

* **Lenguaje:** Kotlin
* **UI Framework:** Jetpack Compose (Declarativo)
* **Arquitectura:** MVVM (Model-View-ViewModel) con capa de repositorios
* **Persistencia Local:** Room Database (SQLite), versión 5 con migración `MIGRATION_4_5`
* **Asincronía:** Corrutinas de Kotlin + StateFlow
* **Navegación:** Navigation Component con `NavHost` y rutas tipadas (`Route`)
* **Diseño:** Material Design 3

---

## 📱 Pantallas y Funcionalidades

1. **🔐 Autenticación (`LoginScreen`):**
   * Validación en vivo del formato del correo mediante `AuthViewModel` e inmutabilidad con `LoginUiState`.
   * Verifica las credenciales contra Room y muestra un error si no coinciden. Las cuentas se crean en la app: no hay un usuario de ejemplo precargado.
2. **📝 Crear cuenta (`RegisterScreen`):**
   * Nombre, correo corporativo `@rinconsur.com`, contraseña (mínimo 8 caracteres, una mayúscula y un número) y confirmación, con errores en vivo e indicador animado de seguridad.
   * La contraseña se guarda como hash SHA-256 con salt, nunca en texto plano.
3. **🔑 Recuperar contraseña (`RecoverPasswordScreen`):**
   * Valida el dominio corporativo y simula el envío del código con una corrutina (no se envía ningún correo real).
4. **📊 Dashboard (`DashboardScreen`):**
   * Vista consolidada por punto de acceso (*Conexión Mall, Estacionamiento, Calle Externa, Pasarela Metro*).
   * Cálculo en vivo de porcentaje de compradores vs. tránsito.
   * Datos reales: suma de **todos los turnos cerrados** (tabla `shift_access_stats`, personas y compradores por acceso) más el turno en curso. Ya no usa cifras base inventadas.
   * La app parte sin datos de ejemplo. Los turnos cerrados antes de la versión 6 de la BD reparten los compradores por acceso de forma estimada (proporcional); los turnos nuevos guardan el valor exacto.
5. **⏱️ Consola de Muestreo (`CaptureScreen`):**
   * Temporizador asíncrono controlado con corrutinas (`delay`).
   * Captura en 1-Tap por grupo etario (*Niños, Jóvenes, Adultos, Seniors*) y tipo de visita (*Solo, Pareja, Grupo*), con vibración al guardar.
6. **📈 Bitácora y Demografía (`DemographicsScreen`):**
   * Métricas detalladas del turno en curso con opción de borrado o edición.
   * Editar un registro (`EditEntryDialog`) recalcula edad, tipo de visita y tamaño del grupo. Borrarlo ofrece "Deshacer".
7. **📜 Historial y Detalles (`HistoryScreen` / `ShiftDetailScreen`):**
   * Cierre de turno con confirmación y almacenamiento persistente de históricos.
   * Compartir el reporte de un turno con `Intent.ACTION_SEND`.
8. **👤 Perfil y foto (`ProfileScreen`):**
   * Foto desde la cámara (`TakePicture` + `FileProvider`) o la galería (`PickVisualMedia`), sin permisos peligrosos.
   * La imagen se reduce, se corrige su rotación EXIF y se guarda en `filesDir/avatars`; la ruta queda en Room y la foto anterior se borra.

---

## 📁 Estructura del Proyecto

```text
com.example.retailflow/
├── data/
│   ├── AccessZones.kt       # Lista única de accesos
│   ├── local/               # Entidades Room, DAOs y AppDatabase (SQLite)
│   │   ├── AppDatabase.kt
│   │   ├── FlowEntryEntity.kt
│   │   ├── HistoryDao.kt
│   │   ├── HistoryEntity.kt
│   │   ├── UserDao.kt
│   │   └── UserEntity.kt
│   └── repository/          # Única puerta a los datos para los ViewModels
│       ├── FlowRepository.kt
│       └── UserRepository.kt
├── domain/
│   └── ShiftSummarizer.kt   # Convierte los registros en vivo en un turno del historial
├── navigation/
│   ├── AppNavGraph.kt       # NavHost completo y transiciones animadas
│   └── Route.kt             # Rutas centralizadas con Sealed Classes
├── ui/
│   ├── components/          # Barras, AgeCounterRow, AppearAnimated, ConversionBar, EditEntryDialog, UserAvatar
│   ├── screens/             # Pantallas Composable (Login, Register, Dashboard, Capture, etc.)
│   ├── theme/               # Paleta de colores, tipografía y tema Material 3
│   └── viewmodel/           # AuthViewModel, HistoryViewModel y AppViewModelFactory
├── util/                    # Validators, PasswordHasher, FlowCalculations, ShiftReport, ImageUtils
└── MainActivity.kt          # Punto de entrada: Scaffold y barras
```

---

## ▶️ Compilar y probar

Gradle debe ejecutarse con **JDK 17 o 21**. Kotlin 2.0.21 no reconoce JDK 25, que es el que trae la versión actual de Android Studio.

```bash
./gradlew :app:assembleDebug
```

```bash
./gradlew :app:testDebugUnitTest
```
