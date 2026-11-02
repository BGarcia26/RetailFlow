package com.example.retailflow.data

/**
 * Los 4 accesos del mall con sus cifras históricas de base (simuladas).
 * Antes esta lista estaba duplicada en CaptureScreen y DashboardScreen.
 */
data class AccessZone(
    val name: String,
    val baseTotal: Int,
    val baseBuyers: Int
) {
    /** "Acceso 1 - Conexión Mall" -> "Acceso 1" */
    val shortName: String get() = name.substringBefore(" -")
}

object AccessZones {
    val all: List<AccessZone> = listOf(
        AccessZone("Acceso 1 - Conexión Mall", 2840, 994),
        AccessZone("Acceso 2 - Estacionamiento", 1960, 1000),
        AccessZone("Acceso 3 - Calle Externa", 1240, 533),
        AccessZone("Acceso 4 - Pasarela Metro", 850, 374)
    )

    val names: List<String> get() = all.map { it.name }
}
