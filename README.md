# Vivids — Mod de Minecraft Forge 1.20.1

Mod que agrega a los Vivids: criaturas con forma de puño, en tres razas
(Vivid Normal, Vivito, Vivids del Baño), cada una en dos tamaños.

**Importante sobre versiones:** este proyecto es para **Minecraft Java Edition
1.20.1 con Forge**. "1.20.10" no existe en Java Edition — esa numeración es
de Minecraft Bedrock, que usa un sistema de mods completamente distinto
(Add-Ons en JSON, no Forge/Java). Si en algún momento quieres también una
versión Bedrock, es un proyecto aparte, no un simple "port".

## Cómo compilar SIN instalar nada en tu computadora (recomendado si no quieres instalar Java/IntelliJ)

Este proyecto incluye un archivo `.github/workflows/build.yml` que le dice a GitHub
que compile el mod automáticamente en sus propios servidores. Solo necesitas una
cuenta de GitHub (gratis) y un navegador.

1. Crea una cuenta en https://github.com si no tienes una.
2. Crea un repositorio nuevo (botón verde "New").
   - Nómbralo, por ejemplo, `vividsmod`.
   - Puede ser público o privado, cualquiera funciona.
   - No marques "Add a README" (ya tenemos uno).
3. En la página del repositorio recién creado, busca el enlace
   **"uploading an existing file"** (o ve a Add file → Upload files).
4. Arrastra **todo el contenido** de la carpeta del proyecto (todo lo que está
   dentro de la carpeta que descomprimiste, no la carpeta en sí) a la zona de
   subida. Incluye la carpeta oculta `.github` — si tu navegador no la sube al
   arrastrar la carpeta completa, sube el archivo `.github/workflows/build.yml`
   por separado repitiendo el proceso, respetando esa misma ruta de carpetas.
5. Confirma el commit ("Commit changes").
6. Ve a la pestaña **"Actions"** en la parte superior del repositorio. Deberías
   ver que ya empezó a correr (o dale a "Run workflow" si no arrancó solo).
7. Espera a que termine (unos 5-10 minutos, verás un check verde ✅ cuando acabe).
8. Haz clic en la ejecución terminada, baja hasta **"Artifacts"**, y descarga
   `vividsmod-jar`. Es un .zip que contiene el `.jar` real del mod.
9. Descomprime ese .zip, y el archivo `.jar` de adentro es el que copias a la
   carpeta `mods` de tu instalación de Minecraft Forge 1.20.1.

Si la compilación falla (aparece una ❌ roja), abre esa ejecución en Actions,
copia el mensaje de error de los últimos pasos y pégamelo — lo más común es
algún typo en el código que hay que corregir.

## Cómo compilar localmente (alternativa, si prefieres tener Java instalado)

1. Necesitas **JDK 17** instalado.
2. Abre la carpeta en IntelliJ IDEA (recomendado) o Eclipse como proyecto Gradle.
3. Espera a que Gradle descargue Forge y las dependencias (la primera vez tarda varios minutos).
4. Corre la tarea `genIntellijRuns` (IntelliJ) o `genEclipseRuns` (Eclipse) si el IDE no la
   corre sola al importar.
5. Ejecuta la configuración de correr "Minecraft Client" para probar el mod en el juego.
6. Para generar el `.jar` final: `./gradlew build` (queda en `build/libs/`).

## Qué está implementado ya (funcional)

- Proyecto Forge 1.20.1 completo (`build.gradle`, `mods.toml`, etc.), listo para compilar.
- **Vivid Normal** completo:
  - Modelo 3D custom (`VividModel`): cuerpo-puño, dos dedos/orejas, pulgar-gorra, piernas.
  - Textura pixel art placeholder 64x64 (ver sección de texturas abajo).
  - Tamaño grande (2.5 bloques) / pequeño (1.8 bloques), elegido al azar al spawnear,
    con vida/velocidad/daño ajustados según el tamaño.
  - IA: camina, mira al jugador, se sienta/duerme/saluda/salta al azar cuando está
    tranquilo, se enoja y ataca a puñetazos si lo golpean (con `NeutralMob`, como
    las abejas: se calma después de un tiempo), sigue al jugador si tiene pan,
    defiende a otros Vivids Normales y a los Vivitos cercanos, evita lava,
    abre puertas.
  - Sonidos propios (placeholder, ver abajo) y loot table (suelta pan/flores).
  - Spawn natural en llanuras, bosques, bosque de abedules, etc. (biome modifier).
  - Spawnea un Vivito cerca (35% de probabilidad) cuando aparece de forma natural.
- **Vivito**: entidad completa, más rápido, nunca ataca, sigue al Vivid Normal más
  cercano, siempre tamaño pequeño.
- **Vivids del Baño**: entidad completa, defiende a otros de su raza, partículas de
  burbuja ambientales, loot table con jabón/burbuja/toalla.
- Items: 3 huevos de spawn (uno por raza) + Jabón, Burbuja, Toalla, con texturas e
  íconos generados.
- Pestaña de creativo "Vivids".
- Traducciones en español e inglés.
- Arquitectura documentada para agregar razas nuevas fácilmente (ver comentarios en
  `Vivids.java` y `AbstractVividEntity.java`).

## Qué falta / próximos pasos (no incluido aún, por alcance)

Esto es un proyecto grande; lo siguiente está pensado pero no implementado todavía
y te recomiendo pedírmelo en mensajes separados para poder darle la atención debida:

1. **Texturas finales de arte**: las texturas incluidas son *placeholder* generadas
   por código (colores planos + ojos/boca simples) para que el mod ya sea jugable y
   se vea la forma correcta. Reemplázalas en
   `src/main/resources/assets/vividsmod/textures/entity/*.png` por pixel art real
   (puedes editarlas en cualquier editor que soporte PNG con transparencia, respetando
   el mismo layout de UV documentado en `VividModel.java`).
2. **Sonidos finales**: los `.ogg` incluidos son tonos simples generados
   automáticamente, solo para que el juego no marque "sonido faltante". Reemplázalos
   en `assets/vividsmod/sounds/` por audio real (mismo nombre de archivo).
3. **Estructuras generadas automáticamente** (aldeas de Vivids, baños abandonados,
   alcantarillas, cuevas húmedas): esto requiere modelarlas en NBT con structure
   blocks dentro del juego y luego registrar `structure_set`/`template_pool` en
   datapack, o usar jigsaw. Es un sub-proyecto en sí mismo — dime cuándo quieras
   que lo armemos y empezamos por una estructura simple (el "baño abandonado").
4. **Vivids del Baño — spawn natural**: actualmente solo se pueden obtener con huevo
   de spawn o comandos, porque su spawn real depende de las estructuras del punto 3
   (alcantarillas, cuevas húmedas). Una vez existan esas estructuras, se agrega un
   `MobSpawnSettings`/spawner de estructura apuntando a ellas.
5. **Pulir la duración del "seguir por comida"**: ahora mismo `TemptGoal` hace que
   te siga mientras tengas pan en la mano (comportamiento vanilla estándor); si
   quieres el límite exacto de "unos minutos" hay que agregar un temporizador propio.
6. **Refinar animaciones**: las animaciones actuales usan matemáticas simples
   (senos/cosenos) tipo mobs vanilla clásicos. Se pueden reemplazar por el sistema
   de keyframes de Minecraft moderno (`AnimationDefinition`/`KeyframeAnimations`,
   como usan el Camello o el Sniffer) para curvas más suaves — te lo puedo migrar
   si quieres animaciones más elaboradas.
7. **Interacción "chocar los puños" entre Vivids amigables**: no implementada aún;
   es un goal nuevo que detecta a otro Vivid cercano en estado idle y sincroniza
   una animación conjunta.
8. **Datagen** (generar loot tables/recipes/tags vía código en vez de JSON a mano):
   opcional, mejora mantenibilidad a futuro.

## Estructura del código

```
src/main/java/com/vividsmod/
├── Vivids.java                 <- clase principal, registra todo
├── registry/                   <- DeferredRegisters (entidades, items, sonidos, tab)
├── entity/
│   ├── AbstractVividEntity.java   <- TODA la lógica común a cualquier raza
│   ├── VividNormalEntity.java
│   ├── VivitoEntity.java
│   ├── VividBathEntity.java
│   └── ai/                        <- Goals custom reutilizables
├── client/
│   ├── ClientModEvents.java    <- registra modelos y renderers (solo cliente)
│   ├── model/VividModel.java   <- geometría 3D + animaciones
│   └── renderer/VividRenderer.java
```

### Cómo agregar una raza nueva en el futuro

1. Crea `NuevaRazaEntity extends AbstractVividEntity` (copia `VividBathEntity` como plantilla).
2. Regístrala en `ModEntityTypes`.
3. Agrega su `createAttributes()` en `Vivids.registerEntityAttributes`.
4. Pon su textura 64x64 en `textures/entity/` (mismo layout UV que las otras).
5. Regístrala en `ClientModEvents` (nuevo `ModelLayerLocation` + renderer).
6. (Opcional) huevo de spawn en `ModItems` + entrada en la pestaña creativa.
7. (Opcional) loot table en `data/vividsmod/loot_tables/entities/`.

Todo esto reutiliza automáticamente el sistema de tamaños, ira, animaciones e
IA común de `AbstractVividEntity`, así que una raza nueva simple puede ser
muy poco código.
