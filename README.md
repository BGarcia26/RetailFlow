# 🛒 RetailFlow - Sistema de Monitoreo y Análisis de Flujo en Retail

**RetailFlow** es una aplicación móvil nativa para Android diseñada para la toma de muestras, análisis demográfico y control de tránsito de personas en accesos de tiendas y centros comerciales. 

Permite a los supervisores de tienda registrar en tiempo real ingresos por rango etario, sentido de circulación, acompañamiento y conversión de compra, consolidando la información en una base de datos local SQLite mediante **Room**.

---

## 🛠️ Tecnologías y Arquitectura

La aplicación está desarrollada siguiendo las buenas prácticas recomendadas por Google para el desarrollo nativo en Android:

* **Lenguaje:** Kotlin
* **UI Framework:** Jetpack Compose (Declarativo)
* **Arquitectura:** MVVM (Model-View-ViewModel)
* **Persistencia Local:** Room Database (SQLite)
* **Asincronía:** Corrutinas de Kotlin + StateFlow
* **Navegación:** Navigation Component con `NavHost` y rutas tipadas (`Route`)
* **Diseño:** Material Design 3

---

## 📱 Pantallas y Funcionalidades

1. **🔐 Autenticación (`LoginScreen`):**
   * Validación en vivo de correo corporativo y contraseña mediante `AuthViewModel` e inmutabilidad con `LoginUiState`.
2. **📊 Dashboard (`DashboardScreen`):**
   * Vista consolidada por punto de acceso (*Conexión Mall, Estacionamiento, Calle Externa, Pasarela Metro*).
   * Cálculo en vivo de porcentaje de compradores vs. tránsito.
3. **⏱️ Consola de Muestreo (`CaptureScreen`):**
   * Temporizador asíncrono controlado con corrutinas (`delay`).
   * Captura en 1-Tap por grupo etario (*Niños, Jóvenes, Adultos, Seniors*) y tipo de visita (*Solo, Pareja, Grupo*).
4. **📈 Bitácora y Demografía (`DemographicsScreen`):**
   * Métricas detalladas del turno en curso con opción de borrado o edición.
5. **📜 Historial y Detalles (`HistoryScreen` / `ShiftDetailScreen`):**
   * Cierre de turno y almacenamiento persistente de históricos.

---

## 📁 Estructura del Proyecto

```text
com.example.retailflow/
├── data/local/              # Entidades Room, DAOs y AppDatabase (SQLite)
│   ├── AppDatabase.kt
│   ├── FlowEntryEntity.kt
│   ├── HistoryDao.kt
│   └── HistoryEntity.kt
├── navigation/              # Definición de rutas centralizadas con Sealed Classes
│   └── Route.kt
├── ui/
│   ├── components/          # TopBar y BottomBar reutilizables
│   ├── screens/             # Pantallas Composable (Login, Dashboard, Capture, etc.)
│   ├── theme/               # Paleta de colores, tipografía y tema Material 3
│   └── viewmodel/           # Lógica de negocio (AuthViewModel, HistoryViewModel)
└── MainActivity.kt          # Punto de entrada y NavHost central
