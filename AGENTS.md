# Instrucciones para el agente

Leer `README.md` y `docs/` antes de programar.

## Reglas
1. Mantener el núcleo completamente offline; no añadir permiso INTERNET.
2. Usar Kotlin, Compose, MVVM, Room y coroutines.
3. Validar reglas de negocio en la capa domain.
4. No permitir turnos superpuestos en estado SCHEDULED.
5. Cancelar por cambio de estado, sin borrado irreversible.
6. No afirmar que WhatsApp envió un mensaje cuando solo se abrió el chat.
7. No asumir que Google Assistant/Gemini permite acciones arbitrarias: verificar documentación y probar en dispositivo real.
8. No guardar credenciales en el APK.
9. Antes de restaurar backup, validar integridad y crear una copia preventiva.
10. Toda fase debe incluir pruebas y actualizar documentación.

## Fases
1. Navegación Compose y ViewModels.
2. CRUD de clientas y servicios.
3. Agenda, horarios y reglas de disponibilidad.
4. Recordatorios locales y reprogramación tras reinicio.
5. Backup local, exportación y restauración segura.
6. Integración limitada con asistente externo, si está soportada.
7. Preparación de mensajes de WhatsApp con envío manual.
8. Accesibilidad, pruebas y revisión de privacidad.

Definition of Done: `./gradlew test` y `./gradlew assembleDebug` pasan; las limitaciones están documentadas; no se agregan permisos sin justificación.
