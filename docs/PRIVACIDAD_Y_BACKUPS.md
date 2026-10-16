# Privacidad y backups

- El manifiesto no declara INTERNET y usa `allowBackup=false`.
- Pedir notificaciones solo cuando la usuaria active recordatorios.
- No pedir ubicación, micrófono, contactos ni almacenamiento amplio para el MVP.
- Para exportar, usar Storage Access Framework.

Implementación requerida antes de usar datos reales:
1. Snapshot consistente y formato versionado.
2. Manifiesto y checksum SHA-256.
3. Cifrado AES-GCM; proteger la clave con Android Keystore o usar contraseña elegida por la usuaria.
4. Escribir archivo temporal y renombrar atómicamente.
5. Verificar nueva copia antes de marcarla válida.
6. Retener unas 14 copias locales, sin borrar la última válida ante fallos.
7. Validar integridad/versión antes de restaurar y generar backup preventivo.
8. No guardar contraseñas junto al archivo.

Una copia solo en el mismo teléfono no protege frente a pérdida, robo o daño. La exportación externa manual sigue siendo necesaria para ese escenario.
