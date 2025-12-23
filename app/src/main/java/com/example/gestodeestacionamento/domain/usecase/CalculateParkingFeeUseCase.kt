package com.example.gestodeestacionamento.domain.usecase

import com.example.gestodeestacionamento.domain.model.PriceTable

class CalculateParkingFeeUseCase {
    
    fun execute(
        priceTable: PriceTable,
        entryDateTime: Long,
        exitDateTime: Long
    ): Double {
        val entryTime = entryDateTime
        val exitTime = exitDateTime
        
        // Aplicar tolerância inicial
        val toleranceMinutes = parseTimeToMinutes(priceTable.initialTolerance)
        val entryTimeWithTolerance = entryTime + (toleranceMinutes * 60 * 1000)
        
        // Se saiu antes da tolerância, não cobra nada
        if (exitTime <= entryTimeWithTolerance) {
            return 0.0
        }
        
        // Calcular tempo de estadia em minutos
        val stayDurationMinutes = ((exitTime - entryTimeWithTolerance) / (60 * 1000)).toInt()
        
        var totalAmount = 0.0
        
        // Aplicar regra "Até"
        if (priceTable.untilTime != null && priceTable.untilValue != null) {
            val untilMinutes = parseTimeToMinutes(priceTable.untilTime)
            
            if (stayDurationMinutes <= untilMinutes) {
                totalAmount = priceTable.untilValue
            } else {
                // Aplicar regra "A partir de"
                if (priceTable.fromTime != null && priceTable.everyInterval != null && priceTable.addValue != null) {
                    val fromMinutes = parseTimeToMinutes(priceTable.fromTime)
                    val everyMinutes = parseTimeToMinutes(priceTable.everyInterval)
                    
                    // Valor até o período "até"
                    totalAmount = priceTable.untilValue
                    
                    // Calcular períodos adicionais após "até"
                    val additionalMinutes = stayDurationMinutes - untilMinutes
                    if (additionalMinutes > 0) {
                        val additionalPeriods = (additionalMinutes / everyMinutes) + if (additionalMinutes % everyMinutes > 0) 1 else 0
                        val additionalAmount = additionalPeriods * priceTable.addValue
                        totalAmount += additionalAmount
                    }
                } else {
                    // Se não tem regra "A partir de", cobra apenas o valor "até"
                    totalAmount = priceTable.untilValue
                }
            }
        } else {
            // Se não tem regra "Até", aplicar apenas "A partir de"
            if (priceTable.fromTime != null && priceTable.everyInterval != null && priceTable.addValue != null) {
                val fromMinutes = parseTimeToMinutes(priceTable.fromTime)
                val everyMinutes = parseTimeToMinutes(priceTable.everyInterval)
                
                val periods = (stayDurationMinutes / everyMinutes) + if (stayDurationMinutes % everyMinutes > 0) 1 else 0
                totalAmount = periods * priceTable.addValue
            }
        }
        
        // Aplicar valor máximo
        if (priceTable.maxChargePeriod != null && priceTable.maxChargeValue != null) {
            val maxPeriodMinutes = parseTimeToMinutes(priceTable.maxChargePeriod)
            if (stayDurationMinutes <= maxPeriodMinutes) {
                totalAmount = minOf(totalAmount, priceTable.maxChargeValue)
            }
        }
        
        return totalAmount
    }
    
    private fun parseTimeToMinutes(timeString: String): Int {
        if (timeString.isBlank()) return 0
        val parts = timeString.split(":")
        if (parts.size != 2) {
            return 0
        }
        val hours = parts[0].toIntOrNull() ?: 0
        val minutes = parts[1].toIntOrNull() ?: 0
        return (hours * 60) + minutes
    }
}

