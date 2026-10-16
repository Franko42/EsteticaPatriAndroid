# PRODUCT.md — Estética Patri (Android)

## Propósito
Estética Patri es una aplicación móvil nativa para Android diseñada específicamente para la gestión offline-first de turnos, clientas, tratamientos y respaldos en un salón de estética personal.

## Principios Centrales
1. **100% Offline-First y Privacidad:** Sin dependencia de backend, servidores en la nube ni cuenta de usuario. Los datos nunca salen del dispositivo sin intervención explícita de la usuaria.
2. **Cero pérdida de información:**
   - Turnos nunca se eliminan físicamente de la base de datos; la cancelación es un cambio de estado a `CANCELLED`.
   - Modificaciones de estados a `COMPLETED` o `NO_SHOW` preservan la trazabilidad.
   - Todo intento de restaurar un archivo de respaldo genera previamente una copia preventiva automática en el almacenamiento local.
3. **No solapamiento de turnos:** Transacciones Room con comprobación de concurrencia estricta que rechazan superposiciones parciales, totales o exactas entre citas `SCHEDULED`.
4. **WhatsApp con supervisión humana:** Preparación inteligente de plantillas pre-cargadas (recordatorios, confirmaciones, reprogramaciones) abriendo el chat oficial de WhatsApp sin asumir que el mensaje fue enviado.

## Usuarias y Escenario de Uso
- **Operadora del salón:** La profesional organiza su jornada de trabajo, agenda citas rápidamente, consulta el día en curso, contacta a las clientas para confirmar asistencia y gestiona su horario comercial semanal.
- **Entorno:** Dispositivo móvil en mano en el salón, a menudo con conectividad intermitente o en modo avión.
