package com.example.gestodeestacionamento.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestodeestacionamento.domain.model.PaymentMethod
import com.example.gestodeestacionamento.domain.model.PriceTable
import com.example.gestodeestacionamento.domain.model.Vehicle
import com.example.gestodeestacionamento.domain.repository.*
import com.example.gestodeestacionamento.domain.usecase.CalculateParkingFeeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class VehicleDetailUiState(
    val vehicle: Vehicle? = null,
    val priceTable: PriceTable? = null,
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethodId: Long? = null,
    val calculatedAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isExitSuccessful: Boolean = false
)

class VehicleDetailViewModel(
    private val vehicleRepository: VehicleRepository,
    private val priceTableRepository: PriceTableRepository,
    private val paymentRepository: PaymentRepository,
    private val calculateParkingFeeUseCase: CalculateParkingFeeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleDetailUiState())
    val uiState: StateFlow<VehicleDetailUiState> = _uiState.asStateFlow()

    fun loadVehicle(vehicleId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val vehicle = vehicleRepository.getVehicleById(vehicleId)
            if (vehicle == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Veículo não encontrado"
                )
                return@launch
            }

            // Buscar tabela de preços
            val priceTable = priceTableRepository.getPriceTableById(vehicle.priceTableId)

            // Combinar métodos de pagamento com recálculo periódico do valor
            paymentRepository.getAllPaymentMethods().collect { methods ->
                // Recalcular o valor sempre que os métodos de pagamento forem atualizados
                val currentExitTime = System.currentTimeMillis()
                val currentCalculatedAmount = if (priceTable != null) {
                    calculateParkingFeeUseCase.execute(
                        priceTable,
                        vehicle.entryDateTime,
                        currentExitTime
                    )
                } else {
                    0.0
                }
                
                _uiState.value = _uiState.value.copy(
                    vehicle = vehicle,
                    priceTable = priceTable,
                    paymentMethods = methods,
                    calculatedAmount = currentCalculatedAmount,
                    isLoading = false
                )
            }
        }
    }
    
    fun recalculateAmount() {
        val vehicle = _uiState.value.vehicle ?: return
        val priceTable = _uiState.value.priceTable
        
        if (priceTable != null) {
            viewModelScope.launch {
                val exitTime = System.currentTimeMillis()
                val calculatedAmount = calculateParkingFeeUseCase.execute(
                    priceTable,
                    vehicle.entryDateTime,
                    exitTime
                )
                _uiState.value = _uiState.value.copy(calculatedAmount = calculatedAmount)
            }
        }
    }

    fun selectPaymentMethod(paymentMethodId: Long) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethodId = paymentMethodId, errorMessage = null)
    }

    fun exitVehicle(onSuccess: () -> Unit) {
        val vehicle = _uiState.value.vehicle ?: return
        val paymentMethodId = _uiState.value.selectedPaymentMethodId
        val amount = _uiState.value.calculatedAmount

        if (paymentMethodId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Selecione uma forma de pagamento")
            return
        }

        // Permitir saída mesmo com valor 0 (pode ser tolerância ou entrada gratuita)
        // A validação será feita apenas se houver erro na tabela de preços
        if (amount < 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Valor inválido")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val exitTime = System.currentTimeMillis()
            val updatedVehicle = vehicle.copy(
                exitDateTime = exitTime,
                totalAmount = amount,
                paymentMethodId = paymentMethodId,
                isInParkingLot = false
            )

            vehicleRepository.updateVehicle(updatedVehicle)
            paymentRepository.insertPayment(vehicle.id, paymentMethodId, amount)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isExitSuccessful = true
            )
            onSuccess()
        }
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetSuccessState() {
        _uiState.value = _uiState.value.copy(isExitSuccessful = false)
    }
}

