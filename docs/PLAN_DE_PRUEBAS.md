# Plan de pruebas

## Unitarias y Room
- Fin igual/anterior al inicio rechazado.
- Solapamientos parciales, contenidos y exactos rechazados.
- Turnos consecutivos permitidos.
- Turnos cancelados no bloquean horarios.
- Cancelación lógica y doble cancelación.
- Persistencia tras reinicio de app y orden de agenda.
- Claves foráneas y migraciones.

## Recordatorios
- Permiso concedido/denegado, reinicio, cambio de hora/zona, turno cancelado y ausencia de duplicados.

## Backup
- Archivo válido, checksum incorrecto, truncamiento, versión incompatible, contraseña incorrecta, falta de espacio e interrupción.
- Restauración fallida no deja datos parciales.
- Backup preventivo previo a restaurar.
- La retención nunca borra la última copia válida.

## Manual
- Probar en Android mínimo y actual.
- Confirmar WhatsApp precarga texto pero requiere envío manual.
- Probar el asistente en dispositivo real sin asumir capacidades no verificadas.
