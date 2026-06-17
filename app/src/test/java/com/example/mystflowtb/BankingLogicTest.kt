package com.example.mystflowtb

import com.example.mystflowtb.ui.screens.formatBalance
import com.example.mystflowtb.ui.screens.formatCardNumber
import com.example.mystflowtb.ui.screens.maskCardNumber
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Teste Automate Unitare pentru funcționalitățile logice (5 puncte).
 */
class BankingLogicTest {

    @Test
    fun testFormatBalance() {
        // Arrange
        val amount1 = 1500.5
        val amount2 = 0.0
        val amount3 = 1234567.89

        // Act
        val result1 = formatBalance(amount1)
        val result2 = formatBalance(amount2)
        val result3 = formatBalance(amount3)

        // Verificăm că formatul include zecimalele și moneda RON (în limba Română).
        // Depinzând de setup-ul mașinii locale, delimitatorul de mii este de obicei spațiu sau punct, 
        // iar cel zecimal este virgulă.
        // Formatul strict arată în general aşa: "1.500,50 RON"
        
        // Assert
        assertEquals(true, result1.contains("RON"))
        assertEquals(true, result1.contains("50")) 
        assertEquals(true, result2.contains("0,00") || result2.contains("0.00"))
    }

    @Test
    fun testFormatCardNumber() {
        // Arrange
        val rawCard = "1234567812345678"

        // Act
        val formatted = formatCardNumber(rawCard)

        // Assert
        assertEquals("1234 5678 1234 5678", formatted)
    }

    @Test
    fun testMaskCardNumber() {
        // Arrange
        val rawCard = "1234567812345678"
        val shortCard = "123"

        // Act
        val masked = maskCardNumber(rawCard)
        val maskedShort = maskCardNumber(shortCard)

        // Assert
        assertEquals("**** **** **** 5678", masked)
        // Funcția noastră returnează stringul original dacă e prea scurt
        assertEquals("123", maskedShort) 
    }
}
