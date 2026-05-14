package com.example.rentflowmax

import android.app.Application
import com.example.rentflowmax.data.db.AppDatabase
import com.example.rentflowmax.data.repository.*
import com.google.android.material.color.DynamicColors

class RentFlowMaxApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val propertyRepository by lazy { PropertyRepository(database.propertyDao()) }
    val tenantRepository by lazy { TenantRepository(database.tenantDao()) }
    val contractRepository by lazy { ContractRepository(database.contractDao()) }
    val paymentRepository by lazy { PaymentRepository(database.paymentDao()) }
    val maintenanceRepository by lazy { MaintenanceRepository(database.maintenanceDao()) }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
