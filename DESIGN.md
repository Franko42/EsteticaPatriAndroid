# DESIGN.md — Sistema de Diseño Estética Patri

## Modo de Uso: OPERATE
La usuaria completa tareas cotidianas de gestión (agendar turnos, revisar horarios, contactar clientas). Prioridades: legibilidad inmediata, velocidad de escaneo, ergonomía táctil y fidelidad nativa Android (Material 3).

---

## 1. Paleta de Colores y Esquema Material 3 (Identidad Oficial)

### Primario y Marca (Rosa / Isotipo Refinado)
- **Rosa principal (`#FF9CAC`):** Botones de acción, acentos y marca.
- **Rosa claro (`#FFD6DC`):** Superficies suaves, contenedores (`PrimaryContainer`).
- **Negro profundo (`#111111`):** Isotipo, títulos, textos de alto contraste (`OnSurface`, `OnPrimaryContainer`).
- **Rosa suave (`#E89CB0`):** Acentos secundarios, bordes sutiles y chips.

### Superficies y Fondo
- **Light:** `Surface: #FFFFFF`, `OnSurface: #111111`, `Background: #F7F4F2` (Marfil cálido), `SurfaceVariant: #FFF5F7`, `Outline: #EED5DC`
- **Dark:** `Surface: #1F151A`, `OnSurface: #F7F4F2`, `Background: #140E11`, `SurfaceVariant: #2F1E26`, `Outline: #523440`

### Estados Semánticos (Contraste WCAG AA >= 4.5:1)
- **Programado (`SCHEDULED`):** Azul `Light: #1565C0`, `Dark: #90CAF9`
- **Completado (`COMPLETED`):** Verde `Light: #2E7D32`, `Dark: #A5D6A7`
- **Cancelado (`CANCELLED`):** Rojo `Light: #C62828`, `Dark: #EF9A9A`
- **No asistió (`NO_SHOW`):** Naranja `Light: #EF6C00`, `Dark: #FFCC80`

### Dynamic Color (Material You)
En Android 12+ (`API 31+`), la app permite mantener fija la identidad de marca o derivar tonos secundarios del wallpaper si la usuaria lo prefiere.

---

## 2. Tipografía y Escala
- **Familia de marca recomendada:** **Montserrat** (Títulos: SemiBold 600; Textos y botones: Regular / Medium 400-500).
- **Fuente de sistema (Fallback):** Roboto / Sistema Android con escalado dinámico (`sp`).
- **Jerarquía:**
  - `TitleLarge`: Nombres de clientas, encabezados de tarjeta (22sp, SemiBold / Montserrat 600).
  - `TitleMedium`: Horarios, nombres de tratamientos (16sp, SemiBold / Montserrat 600).
  - `BodyMedium`: Teléfonos, notas, textos descriptivos (14sp, Regular / Montserrat 400).
  - `LabelMedium`: Insignias de estado (12sp, Bold / Montserrat 700).

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
