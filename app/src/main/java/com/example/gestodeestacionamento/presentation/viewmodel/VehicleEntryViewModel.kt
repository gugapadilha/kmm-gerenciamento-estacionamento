package com.example.gestodeestacionamento.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestodeestacionamento.domain.model.PriceTable
import com.example.gestodeestacionamento.domain.model.Vehicle
import com.example.gestodeestacionamento.domain.repository.PriceTableRepository
import com.example.gestodeestacionamento.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VehicleEntryUiState(
    val plate: String = "",
    val model: String = "",
    val color: String = "",
    val selectedPriceTableId: Long? = null,
    val priceTables: List<PriceTable> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEntrySuccessful: Boolean = false
)

class VehicleEntryViewModel(
    private val vehicleRepository: VehicleRepository,
    private val priceTableRepository: PriceTableRepository
) : ViewModel() {

    private val _plate = MutableStateFlow("")
    private val _model = MutableStateFlow("")
    private val _color = MutableStateFlow("")
    private val _selectedPriceTableId = MutableStateFlow<Long?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isEntrySuccessful = MutableStateFlow(false)

    private val priceTablesFlow = priceTableRepository.getAllPriceTables()

    private val formDataFlow = combine(_plate, _model, _color, _selectedPriceTableId) { plate, model, color, selectedPriceTableId ->
        FormData(plate, model, color, selectedPriceTableId)
    }

    private data class FormData(
        val plate: String,
        val model: String,
        val color: String,
        val selectedPriceTableId: Long?
    )

    val uiState: StateFlow<VehicleEntryUiState> = combine(
        formDataFlow,
        priceTablesFlow,
        _isLoading,
        _errorMessage,
        _isEntrySuccessful
    ) { formData, tables, isLoading, errorMessage, isEntrySuccessful ->
        VehicleEntryUiState(
            plate = formData.plate,
            model = formData.model,
            color = formData.color,
            selectedPriceTableId = formData.selectedPriceTableId,
            priceTables = tables,
            isLoading = isLoading,
            errorMessage = errorMessage,
            isEntrySuccessful = isEntrySuccessful
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = VehicleEntryUiState()
    )

    fun updatePlate(plate: String) {
        _plate.value = plate.uppercase()
        _errorMessage.value = null
    }

    fun updateModel(model: String) {
        _model.value = model
        _errorMessage.value = null
    }

    fun updateColor(color: String) {
        _color.value = color
        _errorMessage.value = null
    }

    fun selectPriceTable(priceTableId: Long) {
        _selectedPriceTableId.value = priceTableId
        _errorMessage.value = null
    }

    fun registerVehicle(onSuccess: () -> Unit) {
        val plate = _plate.value.trim()
        val model = _model.value.trim()
        val color = _color.value.trim()
        val priceTableId = _selectedPriceTableId.value

        if (plate.isEmpty()) {
            _errorMessage.value = "Placa é obrigatória"
            return
        }

        if (model.isEmpty()) {
            _errorMessage.value = "Modelo é obrigatório"
            return
        }

        if (color.isEmpty()) {
            _errorMessage.value = "Cor é obrigatória"
            return
        }

        if (priceTableId == null) {
            _errorMessage.value = "Selecione uma tabela de preços"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val vehicle = Vehicle(
                    plate = plate,
                    model = model,
                    color = color,
                    priceTableId = priceTableId,
                    entryDateTime = System.currentTimeMillis(),
                    isInParkingLot = true
                )

                vehicleRepository.insertVehicle(vehicle)
                
                _isLoading.value = false
                _isEntrySuccessful.value = true
                _plate.value = ""
                _model.value = ""
                _color.value = ""
                _selectedPriceTableId.value = null
                
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erro ao cadastrar veículo: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetSuccessState() {
        _isEntrySuccessful.value = false
    }
}

