# Minecraft [LWJGL Edition]
[Guarda il video su Youtube](https://youtu.be/bnIEqK2hLDQ?si=AHKdck-XqZ79HnD7)

![screenshot](app/res/img/screenshot.png)

## Implementazioni:
- Ombre sulle facce dei blocchi + ambient occlusion
- Ray casting della camera per l'aggiunta/rimozione dei blocchi
- Chunk mesh per un rendering più ottimizzato
- Perlin noise per la generazione di un chunk in una qualsiasi posizione
- Mondo senza limiti con generazione dinamica dei chunk
- Supporto ad un qualsiasi controller wireless/via cavo
- Unico bioma
- Nebbia in lontananza
- Blocchi principali (grass, dirt, stone, sand, tree_wood, tree_leaves, diamond, raw_diamond, gold)
- Texture atlas con mipmapping per il LOD (level of detail)
- Time stamp fissato a 120Hz (modificabile nel main)
- Frustum culling
- Alto ancora

## Possibili miglioramenti/aggiunte:
- Cicli giorno/notte
- Nuovi biomi
- Spawn casuale nel mondo
- Collisioni
- Supporto al mouse
- Modifica del chunk mesh più ottimizzato
- Multiplayer
- Codice classe main più pulito

## Input:
#### Tastiera:
- `WASD` per il movimento
- `← ↑ ↓ →` per lo spostamento visuale
- `space` per aumentare quota
- `alt` / `option` per diminuire quota
- `E` per spaccare i blocchi
- `R` per aggiungere i blocchi

#### Controller Playstation:
- `analogico L` per il movimento
- `analogico R` per lo spostamento visuale
- `R2` per aumentare quota
- `L2` per diminuire quota
- `L3` per scattare
- `□` per spaccare i blocchi
- `○` per aggiungere i blocchi

**Nota**: è necessario semplicemente collegare il controller via bluetooth/cavo e la connessione verrà automaticamente stabilita.
Il controller può essere di una qualsiasi marca.

## Building

#### MacOS con processore arm64 (M1, M2, ...) su Visual Studio Code:
Sulla barra di ricerca, scrivere `>Gradle: Run a Gradle Build`, premere invio, poi `run` e di nuovo invio.

#### Altre piattaforme:
Nei giorni successivi cercherò di eseguire il codice pure su Windows, MacOS Intel e Linux. Terrò la repository aggiornata.

