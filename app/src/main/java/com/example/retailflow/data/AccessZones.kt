package com.example.retailflow.data

/**
 * Los 4 accesos del mall. Antes esta lista traía cifras "históricas" inventadas;
 * ahora los totales salen de la base de datos (turnos cerrados + turno en curso).
 */
data class AccessZone(val name: String) {
    /** "Acceso 1 - Conexión Mall" -> "Acceso 1" */
    val shortName: String get() = name.substringBefore(" -")
}

object AccessZones {
    val all: List<AccessZone> = listOf(
        AccessZone("Acceso 1 - Conexión Mall"),
        AccessZone("Acceso 2 - Estacionamiento"),
        AccessZone("Acceso 3 - Calle Externa"),
        AccessZone("Acceso 4 - Pasarela Metro")
    )

    val names: List<String> get() = all.map { it.name }
}
