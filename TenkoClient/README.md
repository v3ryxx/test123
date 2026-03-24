# Tenko Client

**Tenko Client** is a Fabric 1.21.4 utility mod.

## Building

```bash
./gradlew build
```
Output JAR will be in `build/libs/`.

## Opening the GUI

Press **Right Shift** in-game to open the ClickGUI.

## Modules

### ⚔️ Combat
| Module | Keybind | Description |
|--------|---------|-------------|
| KillAura | `R` | Auto-attacks nearby entities |
| Criticals | — | Always land critical hits |
| Reach | — | Increases attack/block range |

### 🏃 Movement
| Module | Keybind | Description |
|--------|---------|-------------|
| Fly | `F` | Fly in survival |
| Speed | — | Move faster (Speed effect) |
| Sprint | — | Always sprint |
| NoFall | — | Prevents fall damage |

### 👁️ Visual
| Module | Description |
|--------|-------------|
| ESP | Show entity hitboxes through walls |
| Tracers | Draw lines to nearby entities |
| Fullbright | Night vision always active |
| Xray | See ores through blocks |

### 🍩 DonutSMP
| Module | Description |
|--------|-------------|
| ChunkFinder | Detects player bases below deepslate |
| ClusterFinder | Finds clusters of player-placed blocks |
| TunnelBaseFinder | Finds player-dug tunnels |
| LightFinder | Finds artificially lit underground areas |
| BaseNotifier | Chat alert when a base is nearby |
| ChestStealer | Auto-loots opened chests |
| AutoTotem | Auto-equips totem in offhand |
| AntiBot | Ignores bot players in modules |
| AntiKick | Prevents AFK kick |
| ScoreboardHider | Hides the server scoreboard |
| NameProtect | Replaces your name in chat |

### ⚙️ Misc
| Module | Description |
|--------|-------------|
| FreeCam | Smooth detached camera |
| HUD | Client watermark + module list |

## UI Colors
Tenko uses a **deep purple / magenta** theme:
- Background: `#0E0A16`
- Accent separator: `#B43CFF` (magenta)
- Combat: Hot pink `#FF50A0`
- Movement: Violet `#8C50FF`
- Visual: Cyan-blue `#50C8FF`
- DonutSMP: Lavender `#DC78FF`
- Misc: Soft purple-grey `#B4A0D2`
