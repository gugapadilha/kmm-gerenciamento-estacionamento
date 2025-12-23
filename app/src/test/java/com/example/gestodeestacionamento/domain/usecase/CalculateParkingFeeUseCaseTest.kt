package com.example.gestodeestacionamento.domain.usecase

import com.example.gestodeestacionamento.domain.model.PriceTable
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateParkingFeeUseCaseTest {

    private lateinit var useCase: CalculateParkingFeeUseCase

    @Before
    fun setup() {
        useCase = CalculateParkingFeeUseCase()
    }

    @Test
    fun `deve retornar zero quando saída ocorre dentro da tolerância inicial`() {
        val priceTable = createPriceTable(
            initialTolerance = "00:15", // 15 minutos
            untilTime = "01:00",
            untilValue = 5.0
        )
        
        val entryTime = 1000000L
        val exitTime = entryTime + (10 * 60 * 1000) // 10 minutos depois
        
        val result = useCase.execute(priceTable, entryTime, exitTime)
        
        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun `deve cobrar valor até quando estadia está dentro do período até`() {
        val priceTable = createPriceTable(
            initialTolerance = "00:15",
            untilTime = "01:00", // 1 hora
            untilValue = 5.0
        )
        
        val entryTime = 1000000L
        val exitTime = entryTime + (30 * 60 * 1000) + (15 * 60 * 1000) // 45 minutos (15 de tolerância + 30)
        
        val result = useCase.execute(priceTable, entryTime, exitTime)
        
        assertEquals(5.0, result, 0.01)
    }

    @Test
    fun `deve cobrar valor até mais períodos adicionais quando excede período até`() {
        val priceTable = createPriceTable(
            initialTolerance = "00:15",
            untilTime = "01:00", // 1 hora
            untilValue = 5.0,
            fromTime = "01:00",
            everyInterval = "00:30", // 30 minutos
            addValue = 2.0
        )
        
        val entryTime = 1000000L
        // 15 min tolerância + 1h até + 1h adicional = 2h15min total
        val exitTime = entryTime + (15 * 60 * 1000) + (60 * 60 * 1000) + (60 * 60 * 1000)
        
        val result = useCase.execute(priceTable, entryTime, exitTime)
        
        // 5.0 (até) + 2 períodos de 30min (2.0 cada) = 9.0
        assertEquals(9.0, result, 0.01)
    }

    @Test
    fun `deve cobrar apenas períodos quando não há regra até`() {
        val priceTable = createPriceTable(
            initialTolerance = "00:15",
            fromTime = "00:00",
            everyInterval = "00:30",
            addValue = 3.0
        )
        
        val entryTime = 1000000L
        // 15 min tolerância + 1h30min = 1h45min total
        val exitTime = entryTime + (15 * 60 * 1000) + (90 * 60 * 1000)
        
        val result = useCase.execute(priceTable, entryTime, exitTime)
        
        // 90 minutos / 30 minutos = 3 períodos * 3.0 = 9.0
        assertEquals(9.0, result, 0.01)
    }

    @Test
    fun `deve aplicar valor máximo quando configurado`() {
        val priceTable = createPriceTable(
            initialTolerance = "00:15",
            untilTime = "01:00",
            untilValue = 10.0,
            maxChargePeriod = "02:00",
            maxChargeValue = 8.0
        )
        
        val entryTime = 1000000L
        // Dentro do período máximo
        val exitTime = entryTime + (15 * 60 * 1000) + (30 * 60 * 1000)
        
        val result = useCase.execute(priceTable, entryTime, exitTime)
        
        // Deve aplicar o máximo (8.0) ao invés do valor até (10.0)
        assertEquals(8.0, result, 0.01)
    }

    @Test
    fun `deve arredondar para cima períodos parciais`() {
        val priceTable = createPriceTable(
            initialTolerance = "00:15",
            fromTime = "00:00",
            everyInterval = "00:30",
            addValue = 2.0
        )
        
        val entryTime = 1000000L
        // 15 min tolerância + 35 minutos = 50 minutos totais
        val exitTime = entryTime + (15 * 60 * 1000) + (35 * 60 * 1000)
        
        val result = useCase.execute(priceTable, entryTime, exitTime)
        
        // 35 minutos / 30 minutos = 1.17 períodos, arredondado para 2 períodos * 2.0 = 4.0
        assertEquals(4.0, result, 0.01)
    }

    @Test
    fun `deve retornar zero quando não há regras de preço configuradas`() {
        val priceTable = createPriceTable(
            initialTolerance = "00:15"
        )
        
        val entryTime = 1000000L
        val exitTime = entryTime + (60 * 60 * 1000)
        
        val result = useCase.execute(priceTable, entryTime, exitTime)
        
        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun `deve processar corretamente formato de tempo hora minuto`() {
        val priceTable = createPriceTable(
            initialTolerance = "00:30",
            untilTime = "02:30", // 2 horas e 30 minutos
            untilValue = 10.0
        )
        
        val entryTime = 1000000L
        // 30 min tolerância + 1h = 1h30min total
        val exitTime = entryTime + (30 * 60 * 1000) + (60 * 60 * 1000)
        
        val result = useCase.execute(priceTable, entryTime, exitTime)
        
        assertEquals(10.0, result, 0.01)
    }

    private fun createPriceTable(
        initialTolerance: String = "00:00",
        untilTime: String? = null,
        untilValue: Double? = null,
        fromTime: String? = null,
        everyInterval: String? = null,
        addValue: Double? = null,
        maxChargePeriod: String? = null,
        maxChargeValue: Double? = null
    ): PriceTable {
        return PriceTable(
            id = 1L,
            name = "Test Table",
            initialTolerance = initialTolerance,
            untilTime = untilTime,
            untilValue = untilValue,
            fromTime = fromTime,
            everyInterval = everyInterval,
            addValue = addValue,
            maxChargePeriod = maxChargePeriod,
            maxChargeValue = maxChargeValue
        )
    }
}

