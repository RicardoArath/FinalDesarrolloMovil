package com.example.rentflowmax.data.repository

import com.example.rentflowmax.data.dao.PaymentDao
import com.example.rentflowmax.data.model.Payment

class PaymentRepository(private val dao: PaymentDao) {
    fun getAllPayments() = dao.getAllPayments()
    fun getPaymentsForContract(contractId: Long) = dao.getPaymentsForContract(contractId)
    fun getPaymentsByPeriod(month: Int, year: Int) = dao.getPaymentsByPeriod(month, year)
    fun getTotalCollectedByPeriod(month: Int, year: Int) = dao.getTotalCollectedByPeriod(month, year)
    fun getPaymentById(id: Long) = dao.getPaymentById(id)

    suspend fun insert(payment: Payment) = dao.insert(payment)
    suspend fun update(payment: Payment) = dao.update(payment)
    suspend fun delete(payment: Payment) = dao.delete(payment)

    suspend fun countForContractPeriod(contractId: Long, month: Int, year: Int, excludeId: Long = -1L): Int =
        dao.countForContractPeriod(contractId, month, year, excludeId)
}
