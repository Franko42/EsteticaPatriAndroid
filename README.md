# Estética Patri — Android offline-first

Aplicación integral para la gestión de turnos, clientas y servicios de estética en un teléfono Android, 100% offline, sin cuenta ni servidor externo.

## Stack
- Kotlin & Jetpack Compose (Material 3)
- Arquitectura MVVM + Capa de Dominio (Clean Architecture)
- Room (SQLite) con transacciones para prevención de solapamientos
- WorkManager para recordatorios diferidos locales
- Notificaciones locales nativas (`POST_NOTIFICATIONS`)
- Storage Access Framework (SAF) para backups portables
- JDK 17

## Módulos y Funcionalidades
1. **Agenda y Turnos:**
   - Visualización por fecha con navegación rápida.
   - Estados de turno: `SCHEDULED`, `COMPLETED`, `CANCELLED`, `NO_SHOW`.
   - Cancelación lógica sin borrado físico de registros.
   - Detección estricta de turnos superpuestos en transacciones Room.
2. **Clientas y Servicios:**
   - Alta, edición, búsqueda y baja lógica de clientas y tratamientos.
   - Normalización de números telefónicos.
3. **Recordatorios y Reinicio:**
   - Recordatorios locales programados mediante `WorkManager`.
   - Receptor `BootReceiver` para re-programación automática tras reinicio del sistema.
4. **Respaldos y Seguridad:**
   - Respaldos instantáneos con checksum SHA-256.
   - Soporte para cifrado opcional AES-GCM con derivación PBKDF2.
   - Respaldo preventivo automático antes de cualquier restauración.
   - Retención de las 14 copias más recientes.
5. **WhatsApp:**
   - Generación de mensajes estructurados (recordatorios, confirmaciones, reprogramaciones).
   - Apertura del chat externo con texto precargado para confirmación y envío manual de la usuaria.

## Principios
- Núcleo sin Internet ni analítica.
- Cancelar turnos mediante estado, no borrarlos.
- Validar solapamientos dentro de una transacción Room.
- Backup versionado, validado y cifrado antes de usar con datos reales.
- WhatsApp queda en modo preparar mensaje y abrir chat para envío manual.
