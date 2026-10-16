package com.esteticapatri.app.integrations

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppHelper {
    /**
     * Abre el chat de WhatsApp con el mensaje pre-cargado.
     * IMPORTANTE: No registra el mensaje como enviado, ya que la usuaria debe confirmar y enviarlo manualmente en WhatsApp.
     */
    fun launchChat(context: Context, phoneDigitsWithCountryCode: String, message: String): Boolean {
        return try {
            val url = WhatsAppMessageBuilder.buildChatUrl(phoneDigitsWithCountryCode, message)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
