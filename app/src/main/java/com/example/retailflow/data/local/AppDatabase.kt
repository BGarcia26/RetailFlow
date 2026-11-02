package com.example.retailflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [HistoryEntity::class, FlowEntryEntity::class, UserEntity::class, ShiftAccessEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migración 4 -> 5: agrega la tabla `users` SIN borrar el historial existente.
         * El SQL debe coincidir exactamente con lo que Room espera de UserEntity.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `users` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`fullName` TEXT NOT NULL, " +
                        "`email` TEXT NOT NULL, " +
                        "`passwordHash` TEXT NOT NULL, " +
                        "`salt` TEXT NOT NULL, " +
                        "`photoPath` TEXT)"
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)")
            }
        }

        /**
         * Migración 5 -> 6: agrega la tabla `shift_access_stats` (personas y compradores por acceso
         * de cada turno cerrado) SIN borrar nada. Los turnos que ya existían reciben sus
         * estadísticas al abrir la app (FlowRepository.backfillAccessStats).
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `shift_access_stats` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`shiftId` INTEGER NOT NULL, " +
                        "`accessName` TEXT NOT NULL, " +
                        "`totalPeople` INTEGER NOT NULL, " +
                        "`buyersCount` INTEGER NOT NULL)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "retailflow_db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    // Solo para versiones antiguas (1..3) que no tienen migración escrita.
                    // Se nombran una a una: el fallback general también borraría los datos
                    // en cualquier versión futura a la que se le olvide escribir su migración.
                    .fallbackToDestructiveMigrationFrom(1, 2, 3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
