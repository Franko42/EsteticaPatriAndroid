# Arquitectura y modelo

## Capas implementadas:
- `ui/`:
  - `theme/`: Paleta visual y estilos Compose para Estética Patri (`Color.kt`, `Theme.kt`).
  - `navigation/`: Enrutamiento con Jetpack Navigation Compose y barra de navegación inferior (`MainScreen.kt`, `NavRoutes.kt`).
  - `agenda/`: Vista diaria con navegación entre días, estados de turnos con insignias de color, y acciones rápidas (`AgendaScreen.kt`, `AgendaViewModel.kt`).
  - `clients/`: Directorio y búsqueda de clientas, diálogos de alta/edición, contacto directo por WhatsApp y baja lógica (`ClientsScreen.kt`, `ClientsViewModel.kt`).
  - `services/`: Catálogo de tratamientos y servicios con duración y precios (`ServicesScreen.kt`, `ServicesViewModel.kt`).
  - `newappointment/`: Asistente de reserva con validación de horarios, cálculo automático de fin según servicio, detección de solapamientos y confirmación por WhatsApp (`NewAppointmentScreen.kt`, `NewAppointmentViewModel.kt`).
  - `settings/`: Configuración de horarios de atención comercial, creación/exportación/restauración de backups y panel de privacidad (`SettingsScreen.kt`, `SettingsViewModel.kt`).
- `domain/`:
  - Reglas y casos de uso para turnos: `CreateAppointment`, `CancelAppointment`, `RescheduleAppointment`, `UpdateAppointmentStatus`, `GetAgendaAppointments`.
  - Reglas para clientas: `CreateClient`, `UpdateClient`, `DeactivateClient`, `GetClients`.
  - Reglas para servicios: `CreateService`, `UpdateService`, `DeactivateService`, `GetServices`.
  - Horarios de atención: `WorkingHoursUseCases` (detección de horario comercial, configuración por día).
- `data/local/`:
  - Room SQLite: `EsteticaDatabase`, `DatabaseProvider`.
  - Entidades: `ClientEntity`, `ServiceEntity`, `AppointmentEntity`, `WorkingHoursEntity`.
  - Relaciones: `AppointmentWithDetails` (unión de turno con clienta y servicio).
  - DAOs: `ClientDao`, `ServiceDao`, `AppointmentDao` (con transacciones de validación de superposición), `WorkingHoursDao`.
- `backup/`:
  - `BackupManager`: exportación e importación con Storage Access Framework (SAF), cálculo de checksum SHA-256, soporte de cifrado AES-GCM con PBKDF2, retención local de hasta 14 copias y generación obligatoria de backup preventivo antes de restaurar.
- `reminders/`:
  - `NotificationHelper`: creación de canal de notificación y aviso local de turno.
  - `AppointmentReminderWorker`: ejecución diferida con WorkManager.
  - `WorkManagerReminderScheduler`: encolamiento único y cancelación de avisos.
  - `BootReceiver` y `RescheduleRemindersWorker`: reprogramación automática de turnos pendientes tras el reinicio del teléfono.
- `integrations/`:
  - `WhatsAppMessageBuilder`: generador de URLs `https://wa.me/` y plantillas de recordatorio, confirmación, reprogramación y cancelación.
  - `WhatsAppHelper`: apertura del chat externo para revisión y envío manual.
  - `VoiceAssistantContract`: interfaz para apertura de agenda y detalles.

## Entidades:
- Client: nombre, teléfono opcional, teléfono normalizado numérico, notas, createdAtEpochMillis, activo.
- Service: nombre, duración en minutos, precio en unidades menores de moneda (centavos), notas, activo.
- Appointment: clientId, serviceId, inicio/fin epoch millis, estado (`SCHEDULED`, `COMPLETED`, `CANCELLED`, `NO_SHOW`), notas, fechas de creación y actualización.
- WorkingHours: día de semana (1 a 7) y minutos de inicio y fin en el día.
