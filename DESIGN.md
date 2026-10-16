# DESIGN.md — Sistema de Diseño Estética Patri

## Modo de Uso: OPERATE
La usuaria completa tareas cotidianas de gestión (agendar turnos, revisar horarios, contactar clientas). Prioridades: legibilidad inmediata, velocidad de escaneo, ergonomía táctil y fidelidad nativa Android (Material 3).

---

## 1. Paleta de Colores y Esquema Material 3

### Primario (Rosa / Flor de Loto)
- **Light:** `Primary: #9C415D`, `OnPrimary: #FFFFFF`, `PrimaryContainer: #FFD9E2`, `OnPrimaryContainer: #3E001D`
- **Dark:** `Primary: #FFB1C8`, `OnPrimary: #5E1133`, `PrimaryContainer: #7D2947`, `OnPrimaryContainer: #FFD9E2`

### Superficies y Fondo
- **Light:** `Surface: #FFF8F8`, `OnSurface: #22191C`, `SurfaceVariant: #F2DDE1`, `Outline: #837377`
- **Dark:** `Surface: #191114`, `OnSurface: #EFE0E2`, `SurfaceVariant: #514347`, `Outline: #9E8C90`

### Estados Semánticos (Contraste WCAG AA >= 4.5:1)
- **Programado (`SCHEDULED`):** Azul `Light: #1565C0`, `Dark: #90CAF9`
- **Completado (`COMPLETED`):** Verde `Light: #2E7D32`, `Dark: #A5D6A7`
- **Cancelado (`CANCELLED`):** Rojo `Light: #C62828`, `Dark: #EF9A9A`
- **No asistió (`NO_SHOW`):** Naranja `Light: #EF6C00`, `Dark: #FFCC80`

### Dynamic Color (Material You)
En Android 12+ (`API 31+`), la app deriva los tonos secundarios desde el wallpaper del dispositivo respetando la armonía tonal si la usuaria lo prefiere.

---

## 2. Tipografía y Escala
- **Fuente base:** Roboto / Sistema Android con escalado automático (`sp`).
- **Jerarquía:**
  - `TitleLarge`: Nombres de clientas, encabezados de tarjeta (22sp, SemiBold).
  - `TitleMedium`: Horarios, nombres de tratamientos (16sp, SemiBold).
  - `BodyMedium`: Teléfonos, notas, textos descriptivos (14sp).
  - `LabelMedium`: Insignias de estado (12sp, Bold).

---

## 3. Ergonomía Táctil y Accesibilidad
- **Touch Targets:** Mínimo de **48x48 dp** en todo elemento interactivo (`IconButton`, botones, chips, switches) con separación mínima de **8 dp**.
- **Acciones críticas y destructivas:** Cancelar turnos, marcar ausencias o dar de baja clientas/servicios siempre presentan un diálogo modal de confirmación (`AlertDialog`) antes de mutar el estado.
- **Iconografía:** Uso exclusivo de glifos vectoriales de **Material Symbols / Icons** (`Icons.Default.*`); se prohíbe el uso de emojis o glifos Unicode sueltos como iconos de interfaz.

---

## 4. Adaptabilidad (Adaptive Navigation)
- **Pantallas compactas (< 600 dp / Teléfonos verticales):** Barra de navegación inferior (`NavigationBar`) anclada a la base con 4 destinos clave (Agenda, Clientas, Servicios, Ajustes).
- **Pantallas expandidas (>= 600 dp / Tablets, plegables o modo horizontal):** `NavigationRail` lateral fijado a la izquierda para optimizar el campo visual y no deformar listas en horizontal.
- **Edge-to-Edge:** Activado en `MainActivity` mediante `enableEdgeToEdge()` para armonizar con la barra de gestos y status bar de Android 14+.
