package com.example.rentflowmax.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rentflowmax.data.dao.*
import com.example.rentflowmax.data.model.*

@Database(
    entities = [Property::class, Tenant::class, Contract::class, Payment::class, Maintenance::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
    abstract fun tenantDao(): TenantDao
    abstract fun contractDao(): ContractDao
    abstract fun paymentDao(): PaymentDao
    abstract fun maintenanceDao(): MaintenanceDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rentflowmax.db"
                ).build().also { INSTANCE = it }
            }
    }
}
