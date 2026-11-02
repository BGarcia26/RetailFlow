package com.example.retailflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [HistoryEntity::class, FlowEntryEntity::class, UserEntity::class],
    version = 5,
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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "retailflow_db"
                )
                    .addMigrations(MIGRATION_4_5)
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
