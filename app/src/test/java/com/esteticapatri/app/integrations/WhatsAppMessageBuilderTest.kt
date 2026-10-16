package com.esteticapatri.app.integrations

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsAppMessageBuilderTest {

    @Test
    fun encodesMessageAndBuildsLink() {
        val url = WhatsAppMessageBuilder.buildChatUrl("5493764000000", "Hola, ¿confirmás tu turno?")
        assertTrue(url.startsWith("https://wa.me/5493764000000?text="))
        assertTrue(url.contains("%C2%BF"))
    }

    @Test
    fun cleansNonDigitsInPhone() {
        val url = WhatsAppMessageBuilder.buildChatUrl("+54 9 (3764) 12-3456", "Hola")
        assertTrue(url.startsWith("https://wa.me/5493764123456?text="))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsTooShortPhoneNumber() {
        WhatsAppMessageBuilder.buildChatUrl("12345", "Hola")
    }

    @Test
    fun reminderTemplateContainsDetails() {
        val msg = WhatsAppMessageBuilder.buildReminderMessage(
            clientName = "María",
            serviceName = "Manicura Rusa",
            formattedDate = "20/10/2026",
            formattedTime = "15:30"
        )
        assertTrue(msg.contains("María"))
        assertTrue(msg.contains("Manicura Rusa"))
        assertTrue(msg.contains("20/10/2026"))
        assertTrue(msg.contains("15:30"))
    }

    @Test
    fun confirmationTemplateContainsDetails() {
        val msg = WhatsAppMessageBuilder.buildConfirmationMessage(
            clientName = "Lucía",
            serviceName = "Lifting de Pestañas",
            formattedDate = "21/10/2026",
            formattedTime = "10:00"
        )
        assertTrue(msg.contains("Lucía"))
        assertTrue(msg.contains("Lifting de Pestañas"))
    }
}
