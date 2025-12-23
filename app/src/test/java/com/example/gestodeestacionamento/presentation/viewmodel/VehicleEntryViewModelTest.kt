package com.example.gestodeestacionamento.presentation.viewmodel

import app.cash.turbine.test
import com.example.gestodeestacionamento.domain.model.PriceTable
import com.example.gestodeestacionamento.domain.model.Vehicle
import com.example.gestodeestacionamento.domain.repository.PriceTableRepository
import com.example.gestodeestacionamento.domain.repository.VehicleRepository
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
class VehicleEntryViewModelTest {

    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var priceTableRepository: PriceTableRepository
    private lateinit var viewModel: VehicleEntryViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val priceTablesFlow = MutableStateFlow<List<PriceTable>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        vehicleRepository = mockk()
        priceTableRepository = mockk()
        
        every { priceTableRepository.getAllPriceTables() } returns priceTablesFlow
        
        viewModel = VehicleEntryViewModel(vehicleRepository, priceTableRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deve inicializar com estado vazio`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initialState = awaitItem()
            
            assertEquals("", initialState.plate)
            assertEquals("", initialState.model)
            assertEquals("", initialState.color)
            assertNull(initialState.selectedPriceTableId)
            assertTrue(initialState.priceTables.isEmpty())
            assertFalse(initialState.isLoading)
            assertNull(initialState.errorMessage)
            assertFalse(initialState.isEntrySuccessful)
        }
    }

    @Test
    fun `deve atualizar placa e converter para maiúsculas`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            skipItems(1) // Pula o estado inicial
            viewModel.updatePlate("abc1234")
            advanceUntilIdle()
            val state = awaitItem()
            assertEquals("ABC1234", state.plate)
            assertNull(state.errorMessage)
        }
    }

    @Test
    fun `deve atualizar modelo`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            skipItems(1) // Pula o estado inicial
            viewModel.updateModel("Honda Civic")
            advanceUntilIdle()
            val state = awaitItem()
            assertEquals("Honda Civic", state.model)
        }
    }

    @Test
    fun `deve atualizar cor`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            skipItems(1) // Pula o estado inicial
            viewModel.updateColor("Branco")
            advanceUntilIdle()
            val state = awaitItem()
            assertEquals("Branco", state.color)
        }
    }

    @Test
    fun `deve selecionar tabela de preços`() = runTest(testDispatcher) {
        val priceTableId = 1L
        viewModel.uiState.test {
            skipItems(1) // Pula o estado inicial
            viewModel.selectPriceTable(priceTableId)
            advanceUntilIdle()
            val state = awaitItem()
            assertEquals(priceTableId, state.selectedPriceTableId)
        }
    }

    @Test
    fun `deve carregar tabelas de preços do repositório`() = runTest(testDispatcher) {
        val priceTables = listOf(
            PriceTable(id = 1L, name = "Tabela 1", initialTolerance = "00:15"),
            PriceTable(id = 2L, name = "Tabela 2", initialTolerance = "00:30")
        )
        
        viewModel.uiState.test {
            skipItems(1) // Pula o estado inicial
            priceTablesFlow.value = priceTables
            advanceUntilIdle()
            val state = awaitItem()
            assertEquals(2, state.priceTables.size)
            assertEquals("Tabela 1", state.priceTables[0].name)
            assertEquals("Tabela 2", state.priceTables[1].name)
        }
    }

    @Test
    fun `deve remover espaços em branco dos campos ao registrar`() = runTest(testDispatcher) {
        viewModel.updatePlate("  ABC1234  ")
        viewModel.updateModel("  Honda Civic  ")
        viewModel.updateColor("  Branco  ")
        viewModel.selectPriceTable(1L)
        
        coEvery { vehicleRepository.insertVehicle(any()) } returns 1L
        
        viewModel.registerVehicle { }
        advanceUntilIdle()
        
        coVerify { 
            vehicleRepository.insertVehicle(
                match {
                    it.plate == "ABC1234" &&
                    it.model == "Honda Civic" &&
                    it.color == "Branco"
                }
            )
        }
    }

}

