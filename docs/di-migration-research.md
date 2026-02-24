# Tarea de investigación: migración de Service Locator a DI framework (Hilt/Koin)

## Contexto actual
- La app usa `ServiceLocator` para construir y exponer repositorios de forma manual.
- Los `ViewModel` se crean con `ViewModelProvider.Factory` custom por pantalla.

## Objetivo
Evaluar migración a **Dagger Hilt** o **Koin** para inyectar dependencias directamente en `ViewModel` y reducir complejidad de fábricas manuales.

## Alcance de investigación
1. **Comparativa técnica Hilt vs Koin**
   - Tiempo de integración en proyecto existente.
   - Curva de aprendizaje para el equipo.
   - Rendimiento en arranque y tamaño de APK.
   - Testabilidad (reemplazo de dependencias en unit/instrumented tests).
2. **PoC mínima**
   - Inyección de `AuthRepository` en `RegisterTokenViewModel`.
   - Inyección de `ScannerRepository` en `ScannerViewModel`.
   - Eliminación de al menos una `Factory` manual.
3. **Impacto en arquitectura**
   - Reemplazo progresivo de `ServiceLocator`.
   - Estrategia de convivencia temporal para no romper flujos existentes.

## Entregables esperados
- Documento corto de decisión (ADR) con opción recomendada (Hilt o Koin).
- Lista de cambios de build/configuración requeridos.
- Plan incremental por fases para retirar `ServiceLocator` y `*ViewModelFactory`.
- Riesgos y mitigaciones.

## Criterios de aceptación
- Existe una recomendación explícita sustentada en la comparativa.
- Existe una PoC funcional en al menos un flujo real.
- Se define roadmap para migración completa sin Big Bang.
