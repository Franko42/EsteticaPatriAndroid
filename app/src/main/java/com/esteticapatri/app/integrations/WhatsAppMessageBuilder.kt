package com.esteticapatri.app.integrations

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WhatsAppMessageBuilder {
    fun buildChatUrl(phoneDigitsWithCountryCode: String, message: String): String {
        val phone = phoneDigitsWithCountryCode.filter(Char::isDigit)
        require(phone.length >= 8) { "Ingresá un número con código de país y área." }
        val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        return "https://wa.me/$phone?text=$encoded"
    }

    fun buildReminderMessage(clientName: String, serviceName: String, formattedDate: String, formattedTime: String): String {
        return "¡Hola $clientName! Te escribo de Estética Patri para recordarte tu turno de $serviceName el día $formattedDate a las $formattedTime hs. ¿Me confirmás tu asistencia?"
    }

    fun buildConfirmationMessage(clientName: String, serviceName: String, formattedDate: String, formattedTime: String): String {
        return "¡Hola $clientName! Tu turno para $serviceName en Estética Patri quedó agendado para el $formattedDate a las $formattedTime hs. ¡Te esperamos!"
    }

    fun buildRescheduleMessage(clientName: String, serviceName: String, newDate: String, newTime: String): String {
        return "¡Hola $clientName! Te confirmamos la reprogramación de tu turno de $serviceName en Estética Patri para el $newDate a las $newTime hs."
    }

    fun buildCancellationMessage(clientName: String, serviceName: String, formattedDate: String, formattedTime: String): String {
        return "Hola $clientName, te informamos que tu turno para $serviceName del $formattedDate a las $formattedTime hs en Estética Patri ha sido cancelado. Escribinos para coordinar una nueva fecha."
    }
}
