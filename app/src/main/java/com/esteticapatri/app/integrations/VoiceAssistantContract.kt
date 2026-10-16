package com.esteticapatri.app.integrations

/** Contrato de integración; no representa una implementación de App Actions. */
interface ExternalAssistantIntegration {
    fun openAgenda()
    fun openAppointmentDetails(appointmentId: Long)
}
