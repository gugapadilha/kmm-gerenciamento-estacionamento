package com.example.gestodeestacionamento.presentation.viewmodel

import app.cash.turbine.test
import com.example.gestodeestacionamento.domain.model.PaymentMethod
import com.example.gestodeestacionamento.domain.model.PriceTable
import com.example.gestodeestacionamento.domain.model.Vehicle
import com.example.gestodeestacionamento.domain.repository.PaymentRepository
import com.example.gestodeestacionamento.domain.repository.PriceTableRepository
import com.example.gestodeestacionamento.domain.repository.VehicleRepository
import com.example.gestodeestacionamento.domain.usecase.CalculateParkingFeeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleDetailViewModelTest {

    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var priceTableRepository: PriceTableRepository
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var calculateParkingFeeUseCase: CalculateParkingFeeUseCase
    private lateinit var viewModel: VehicleDetailViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val paymentMethodsFlow = MutableStateFlow<List<PaymentMethod>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        vehicleRepository = mockk()
        priceTableRepository = mockk()
        paymentRepository = mockk()
        calculateParkingFeeUseCase = CalculateParkingFeeUseCase()
        
        every { paymentRepository.getAllPaymentMethods() } returns paymentMethodsFlow
        
        viewModel = VehicleDetailViewModel(
            vehicleRepository,
            priceTableRepository,
            paymentRepository,
            calculateParkingFeeUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deve inicializar com estado vazio`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initialState = awaitItem()
            
            assertNull(initialState.vehicle)
            assertNull(initialState.priceTable)
            assertTrue(initialState.paymentMethods.isEmpty())
            assertNull(initialState.selectedPaymentMethodId)
            assertEquals(0.0, initialState.calculatedAmount, 0.01)
            assertFalse(initialState.isLoading)
            assertNull(initialState.errorMessage)
            assertFalse(initialState.isExitSuccessful)
        }
    }

    @Test
    fun `deve carregar veículo e calcular valor quando loadVehicle é chamado`() = runTest(testDispatcher) {
        val vehicleId = 1L
        val priceTableId = 1L
        val entryTime = System.currentTimeMillis() - (2 * 60 * 60 * 1000) // 2 horas atrás
        
        val vehicle = Vehicle(
            id = vehicleId,
            plate = "ABC1234",
            model = "Honda Civic",
            color = "Branco",
            priceTableId = priceTableId,
            entryDateTime = entryTime,
            isInParkingLot = true
        )
        
        val priceTable = PriceTable(
            id = priceTableId,
            name = "Tabela 1",
            initialTolerance = "00:15",
            untilTime = "01:00",
            untilValue = 5.0
        )
        
        val paymentMethods = listOf(
            PaymentMethod(id = 1L, name = "Dinheiro"),
            PaymentMethod(id = 2L, name = "Cartão")
        )
        
        coEvery { vehicleRepository.getVehicleById(vehicleId) } returns vehicle
        coEvery { priceTableRepository.getPriceTableById(priceTableId) } returns priceTable
        coEvery { priceTableRepository.getAllPriceTables() } returns flowOf(listOf(priceTable))
        paymentMethodsFlow.value = paymentMethods
        
        viewModel.loadVehicle(vehicleId)
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.vehicle)
            assertEquals(vehicleId, state.vehicle?.id)
            assertNotNull(state.priceTable)
            assertEquals(priceTableId, state.priceTable?.id)
            assertEquals(2, state.paymentMethods.size)
            assertTrue(state.calculatedAmount > 0)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `deve mostrar erro quando veículo não é encontrado`() = runTest(testDispatcher) {
        val vehicleId = 999L
        
        coEvery { vehicleRepository.getVehicleById(vehicleId) } returns null
        
        viewModel.loadVehicle(vehicleId)
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Veículo não encontrado", state.errorMessage)
            assertNull(state.vehicle)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `deve recalcular valor quando recalculateAmount é chamado`() = runTest(testDispatcher) {
        val vehicleId = 1L
        val priceTableId = 1L
        val entryTime = System.currentTimeMillis() - (60 * 60 * 1000) // 1 hora atrás
        
        val vehicle = Vehicle(
            id = vehicleId,
            plate = "ABC1234",
            model = "Honda Civic",
            color = "Branco",
            priceTableId = priceTableId,
            entryDateTime = entryTime,
            isInParkingLot = true
        )
        
        val priceTable = PriceTable(
            id = priceTableId,
            name = "Tabela 1",
            initialTolerance = "00:15",
            untilTime = "01:00",
            untilValue = 5.0
        )
        
        coEvery { vehicleRepository.getVehicleById(vehicleId) } returns vehicle
        coEvery { priceTableRepository.getPriceTableById(priceTableId) } returns priceTable
        coEvery { priceTableRepository.getAllPriceTables() } returns flowOf(listOf(priceTable))
        paymentMethodsFlow.value = emptyList()
        
        viewModel.loadVehicle(vehicleId)
        advanceUntilIdle()
        
        val initialAmount = viewModel.uiState.value.calculatedAmount
        
        // Aguardar um pouco para que o tempo passe
        kotlinx.coroutines.delay(100)
        
        viewModel.recalculateAmount()
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.calculatedAmount >= initialAmount)
        }
    }

    @Test
    fun `deve selecionar método de pagamento`() = runTest(testDispatcher) {
        val paymentMethodId = 1L
        
        viewModel.selectPaymentMethod(paymentMethodId)
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(paymentMethodId, state.selectedPaymentMethodId)
            assertNull(state.errorMessage)
        }
    }

    @Test
    fun `deve mostrar erro quando método de pagamento não está selecionado na saída`() = runTest(testDispatcher) {
        val vehicleId = 1L
        val priceTableId = 1L
        val entryTime = System.currentTimeMillis() - (60 * 60 * 1000)
        
        val vehicle = Vehicle(
            id = vehicleId,
            plate = "ABC1234",
            model = "Honda Civic",
            color = "Branco",
            priceTableId = priceTableId,
            entryDateTime = entryTime,
            isInParkingLot = true
        )
        
        val priceTable = PriceTable(
            id = priceTableId,
            name = "Tabela 1",
            initialTolerance = "00:15",
            untilTime = "01:00",
            untilValue = 5.0
        )
        
        coEvery { vehicleRepository.getVehicleById(vehicleId) } returns vehicle
        coEvery { priceTableRepository.getPriceTableById(priceTableId) } returns priceTable
        coEvery { priceTableRepository.getAllPriceTables() } returns flowOf(listOf(priceTable))
        paymentMethodsFlow.value = emptyList()
        
        viewModel.loadVehicle(vehicleId)
        advanceUntilIdle()
        
        var onSuccessCalled = false
        viewModel.exitVehicle { onSuccessCalled = true }
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Selecione uma forma de pagamento", state.errorMessage)
            assertFalse(onSuccessCalled)
        }
        
        coVerify(exactly = 0) { vehicleRepository.updateVehicle(any()) }
        coVerify(exactly = 0) { paymentRepository.insertPayment(any(), any(), any()) }
    }

    @Test
    fun `deve processar saída de veículo com sucesso`() = runTest(testDispatcher) {
        val vehicleId = 1L
        val priceTableId = 1L
        val paymentMethodId = 1L
        val entryTime = System.currentTimeMillis() - (60 * 60 * 1000)
        
        val vehicle = Vehicle(
            id = vehicleId,
            plate = "ABC1234",
            model = "Honda Civic",
            color = "Branco",
            priceTableId = priceTableId,
            entryDateTime = entryTime,
            isInParkingLot = true
        )
        
        val priceTable = PriceTable(
            id = priceTableId,
            name = "Tabela 1",
            initialTolerance = "00:15",
            untilTime = "01:00",
            untilValue = 5.0
        )
        
        coEvery { vehicleRepository.getVehicleById(vehicleId) } returns vehicle
        coEvery { priceTableRepository.getPriceTableById(priceTableId) } returns priceTable
        coEvery { priceTableRepository.getAllPriceTables() } returns flowOf(listOf(priceTable))
        coEvery { vehicleRepository.updateVehicle(any()) } returns Unit
        coEvery { paymentRepository.insertPayment(any(), any(), any()) } returns Unit
        paymentMethodsFlow.value = listOf(
            PaymentMethod(id = paymentMethodId, name = "Dinheiro")
        )
        
        viewModel.loadVehicle(vehicleId)
        advanceUntilIdle()
        
        viewModel.selectPaymentMethod(paymentMethodId)
        
        var onSuccessCalled = false
        viewModel.exitVehicle { onSuccessCalled = true }
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isExitSuccessful)
            assertNull(state.errorMessage)
            assertFalse(state.isLoading)
        }
        
        assertTrue(onSuccessCalled)
        
        coVerify(exactly = 1) { 
            vehicleRepository.updateVehicle(
                match {
                    it.id == vehicleId &&
                    it.isInParkingLot == false &&
                    it.exitDateTime != null &&
                    it.totalAmount != null &&
                    it.paymentMethodId == paymentMethodId
                }
            )
        }
        
        coVerify(exactly = 1) { 
            paymentRepository.insertPayment(vehicleId, paymentMethodId, any())
        }
    }

    @Test
    fun `deve formatar data e hora corretamente`() = runTest(testDispatcher) {
        val timestamp = 1704067200000L // 2024-01-01 00:00:00 UTC
        
        val formatted = viewModel.formatDateTime(timestamp)
        
        assertTrue(formatted.contains("/"))
        assertTrue(formatted.contains(":"))
    }

    @Test
    fun `deve limpar erro quando clearError é chamado`() = runTest(testDispatcher) {
        val vehicleId = 1L
        
        coEvery { vehicleRepository.getVehicleById(vehicleId) } returns null
        
        viewModel.loadVehicle(vehicleId)
        advanceUntilIdle()
        
        assertNotNull(viewModel.uiState.value.errorMessage)
        
        viewModel.clearError()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.errorMessage)
        }
    }

    @Test
    fun `deve resetar estado de sucesso quando resetSuccessState é chamado`() = runTest(testDispatcher) {
        val vehicleId = 1L
        val priceTableId = 1L
        val paymentMethodId = 1L
        val entryTime = System.currentTimeMillis() - (60 * 60 * 1000)
        
        val vehicle = Vehicle(
            id = vehicleId,
            plate = "ABC1234",
            model = "Honda Civic",
            color = "Branco",
            priceTableId = priceTableId,
            entryDateTime = entryTime,
            isInParkingLot = true
        )
        
        val priceTable = PriceTable(
            id = priceTableId,
            name = "Tabela 1",
            initialTolerance = "00:15",
            untilTime = "01:00",
            untilValue = 5.0
        )
        
        coEvery { vehicleRepository.getVehicleById(vehicleId) } returns vehicle
        coEvery { priceTableRepository.getPriceTableById(priceTableId) } returns priceTable
        coEvery { priceTableRepository.getAllPriceTables() } returns flowOf(listOf(priceTable))
        coEvery { vehicleRepository.updateVehicle(any()) } returns Unit
        coEvery { paymentRepository.insertPayment(any(), any(), any()) } returns Unit
        paymentMethodsFlow.value = listOf(
            PaymentMethod(id = paymentMethodId, name = "Dinheiro")
        )
        
        viewModel.loadVehicle(vehicleId)
        advanceUntilIdle()
        
        viewModel.selectPaymentMethod(paymentMethodId)
        viewModel.exitVehicle { }
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.isExitSuccessful)
        
        viewModel.resetSuccessState()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isExitSuccessful)
        }
    }

    @Test
    fun `deve permitir saída com valor zero quando dentro da tolerância`() = runTest(testDispatcher) {
        val vehicleId = 1L
        val priceTableId = 1L
        val paymentMethodId = 1L
        val entryTime = System.currentTimeMillis() - (5 * 60 * 1000) // 5 minutos atrás
        
        val vehicle = Vehicle(
            id = vehicleId,
            plate = "ABC1234",
            model = "Honda Civic",
            color = "Branco",
            priceTableId = priceTableId,
            entryDateTime = entryTime,
            isInParkingLot = true
        )
        
        val priceTable = PriceTable(
            id = priceTableId,
            name = "Tabela 1",
            initialTolerance = "00:15", // 15 minutos de tolerância
            untilTime = "01:00",
            untilValue = 5.0
        )
        
        coEvery { vehicleRepository.getVehicleById(vehicleId) } returns vehicle
        coEvery { priceTableRepository.getPriceTableById(priceTableId) } returns priceTable
        coEvery { priceTableRepository.getAllPriceTables() } returns flowOf(listOf(priceTable))
        coEvery { vehicleRepository.updateVehicle(any()) } returns Unit
        coEvery { paymentRepository.insertPayment(any(), any(), any()) } returns Unit
        paymentMethodsFlow.value = listOf(
            PaymentMethod(id = paymentMethodId, name = "Dinheiro")
        )
        
        viewModel.loadVehicle(vehicleId)
        advanceUntilIdle()
        
        // Valor calculado deve ser 0.0 (dentro da tolerância)
        assertEquals(0.0, viewModel.uiState.value.calculatedAmount, 0.01)
        
        viewModel.selectPaymentMethod(paymentMethodId)
        
        var onSuccessCalled = false
        viewModel.exitVehicle { onSuccessCalled = true }
        advanceUntilIdle()
        
        assertTrue(onSuccessCalled)
        assertTrue(viewModel.uiState.value.isExitSuccessful)
    }
}

