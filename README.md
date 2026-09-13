# Lunamatic

A configurable moon plugin for Minecraft Paper and Folia.

Lunamatic is a fork of the original Lunamatic project, updated for Minecraft 26.2 and extended with configurable Blood Moon visual effects.

## Features

- Full Moon effects
- New Moon effects
- Harvest Moon events
- Blood Moon events
- Configurable Blood Moon visual effects
- Configurable Blood Moon sky, fog and lighting colors
- Optional Allay spawning during Harvest Moon
- Optional Vex spawning during Blood Moon
- Configurable sleeping during Blood Moon
- Configurable Blood and Harvest Moon chances
- Per-world moon-effect disabling
- Paper and Folia support
- Automatic update checking through GitHub Releases
- Custom Blood Moon environment using Minecraft's data-driven timeline system

## Minecraft / Server Compatibility

| Component | Version |
|---|---|
| Minecraft | 26.2 |
| Paper API | 26.2 |
| Folia | Supported in original (not tested in this fork)|
| Java | 25 |
| Lunamatic | 3.0.1-26.2 |

## Original Lunamatic Description

The original Lunamatic project makes nights worth staying awake for by adding special effects to full and new moons, including rare Harvest and Blood Moons.

The original project adds special effects for new and full moons. These effects range from simple good or bad luck to special mob spawns, increased crop production and other moon-related events.

### Full Moon 🌕

- All players are blessed with luck.

### Harvest Moon 🌾🌕

- By default, 50% of all Full Moons become a Harvest Moon.
- With enough light, crops grow 10x faster.
- A small percentage of bees will not enter their hives at night.
- Bees that remain outside immediately begin pollinating.
- Hostile mobs do not spawn at night.
- Allays spawn in place of Creepers at night, with a chance to hold rare loot.
- Weather remains clear once the Harvest Moon has risen.

### New Moon 🌑

- All players are cursed with bad luck.

### Blood Moon 🩸🌑

- By default, 50% of all New Moons become a Blood Moon.
- Monster speed, jump strength and detection range are increased based on world difficulty.
- Humanoid monsters can spawn with random chainmail armor based on difficulty.
  - Easy = 2 pieces
  - Normal = 3 pieces
  - Hard = full chainmail
- Vexes spawn in place of Creepers at night.
- Sleeping is disabled during the Blood Moon.

The original project also provides commands for reloading the configuration, checking moon status, advancing the moon phase and forcing Harvest or Blood Moons.

Supported languages in the original project include English, Czech, German, Chinese, Russian, Spanish and Slovak.

Original description and project information:

https://modrinth.com/plugin/lunamatic

## Blood Moon Visuals

Lunamatic 3.0.1-26.2 extends the original Blood Moon mechanics with configurable environmental visuals.

During a Blood Moon, Lunamatic can modify the Minecraft environment using a custom timeline.

The following visual properties can be configured independently:

- Ambient light
- Block light tint
- Sky light
- Sky color
- Fog color

Example configuration:

    bloodMoonVisuals:
      enabled: true

      ambientLight:
        enabled: true
        color: "#241010"

      blockLight:
        enabled: true
        color: "#fff2f2"

      skyLight:
        enabled: true
        color: "#9f1b22"

      sky:
        enabled: true
        color: "#720008"

      fog:
        enabled: true
        color: "#3a080d"

Each visual effect can be disabled independently.

Colors must be specified as six-digit hexadecimal RGB values in the form:

    #RRGGBB

For example:

    #720008

## Blood Moon Default Colors

The default Blood Moon environment uses a dark red atmosphere:

| Effect | Default |
|---|---|
| Ambient light | `#241010` |
| Block light | `#fff2f2` |
| Sky light | `#9f1b22` |
| Sky | `#720008` |
| Fog | `#3a080d` |

The goal is to create a strong Blood Moon atmosphere while keeping normal blocks and terrain relatively natural.

## Configuration

The main configuration file is located at:

    plugins/Lunamatic/config.yml

Example:

    ### Lunamatic configuration ###

    # Choose plugin language.
    # Valid options: en_US, cs_CZ, de_DE
    lang: "en_US"

    # Choose which moon effects to enable
    fullMoonEnabled: true
    newMoonEnabled: true

    ## Ignored unless fullMoonEnabled == true
    harvestMoonEnabled: true

    ## Enable Allay spawning while harvest moon is active
    harvestMoonSpawnAllay: true

    ## Ignored unless newMoonEnabled == true
    bloodMoonEnabled: true

    ## Enable Vex spawning while blood moon is active
    bloodMoonSpawnVex: true

    ## Enable sleeping during a blood moon
    bloodMoonAllowSleep: false

    # Chance for Blood and Harvest Moons
    # This sets the number of sides in the dice roll.
    # Example:
    # 2 = 50% chance
    # 6 = approximately 17% chance
    bloodMoonDieSides: 2
    harvestMoonDieSides: 2

    # Set worlds to disable moon effects on
    # By design both the Nether and the End cannot have moon effects.
    disabledWorlds:
      - world_nether
      - world_the_end

    # Do you want to check for updates at server start?
    checkForUpdates: true

    bloodMoonVisuals:
      enabled: true

      ambientLight:
        enabled: true
        color: "#241010"

      blockLight:
        enabled: true
        color: "#fff2f2"

      skyLight:
        enabled: true
        color: "#9f1b22"

      sky:
        enabled: true
        color: "#720008"

      fog:
        enabled: true
        color: "#3a080d"

Missing configuration options are automatically added when Lunamatic starts. Existing configuration values are preserved.

## Per-World Configuration

Moon effects can be disabled for specific worlds using:

    disabledWorlds:
      - world_nether
      - world_the_end

Additional worlds can be added:

    disabledWorlds:
      - world_nether
      - world_the_end
      - minigames

## Update Checker

Lunamatic checks for new releases on GitHub when update checking is enabled.

The update source is:

https://github.com/daretmavi/Lunamatic/releases

The plugin uses the latest GitHub Release to determine whether a newer version is available.

Release versions use the following format:

    3.0.1-26.2

Future releases may use versions such as:

    3.0.2-26.2
    3.1.0-26.2
    4.0.0-26.2

## Installation

1. Download the latest Lunamatic release.
2. Put the `.jar` file into your server's `plugins` directory.
3. Start the server.
4. Edit `plugins/Lunamatic/config.yml`.
5. Restart the server after changing configuration.

For Paper or Folia, make sure the server is running a compatible Minecraft 26.2 build and Java 25.

## Datapack

Lunamatic uses Minecraft's data-driven systems for the Blood Moon environment.

The plugin includes its own datapack resources and registers them automatically when the server starts.

The Blood Moon environment uses a custom clock:

    lunamatic:blood_moon

and a custom timeline:

    lunamatic:blood_moon

The timeline controls the Blood Moon visual environment.

## Commands

Lunamatic primarily operates automatically based on the current Minecraft moon phase and configured settings.

The original Lunamatic project provides the following commands:

- `/luma reload` - Reloads the configuration.
- `/luma status` - Shows the current plugin and moon status.
- `/luma nextmoon` - Moves the moon phase ahead by one.
- `/luma makebloodmoon` - Skips ahead to the next Blood Moon.
- `/luma makeharvestmoon` - Skips ahead to the next Harvest Moon.
- `/luma debug` - Enables debug messages in the console.

The original project uses the `luma.command.*` permission for these commands.

## Permissions

Lunamatic uses the permissions provided by the original plugin command system.

For the original command permission structure, see the original project documentation.

## Paper and Folia

Lunamatic supports Paper and Folia.

The plugin is marked as Folia-compatible in `plugin.yml`.

## Building From Source

Requirements:

- Java 25
- Git
- Gradle Wrapper

Clone the repository:

    git clone https://github.com/daretmavi/Lunamatic.git
    cd Lunamatic

Build the plugin:

    ./gradlew clean build

On Windows:

    .\gradlew.bat clean build

The resulting plugin JAR will be available in:

    build/libs/

## Project Structure

Important project files:

    Lunamatic/
    ├── build.gradle.kts
    ├── gradle.properties
    ├── README.md
    ├── src/
    │   └── main/
    │       ├── java/
    │       │   └── org/
    │       │       └── evlis/
    │       │           └── lunamatic/
    │       │               ├── Lunamatic.java
    │       │               ├── LunamaticBootstrap.java
    │       │               ├── BloodMoonVisuals.java
    │       │               ├── GlobalVars.java
    │       │               └── ...
    │       └── resources/
    │           ├── config.yml
    │           ├── plugin.yml
    │           └── pack/
    │               └── ...
    └── ...

## Development

Lunamatic is developed for modern Paper/Minecraft versions using Java 25.

The project uses:

- Paper API
- Gradle
- Kotlin Gradle DSL
- Minecraft data packs
- Minecraft timeline/world-clock systems
- Gson for GitHub release information

## Repository

Current fork:

https://github.com/daretmavi/Lunamatic

Codeberg mirror:

https://codeberg.org/daretmavi/Lunamatic

## Original Project

This project is a fork of the original Lunamatic project.

Original repository:

https://codeberg.org/Ifiht/Lunamatic

Original Modrinth project:

https://modrinth.com/plugin/lunamatic

The original Lunamatic project is licensed under GPL-3.0-only.

This fork contains Minecraft 26.2-specific updates, configuration improvements and additional Blood Moon visual effects.

## Source Version

This fork is based on the original Lunamatic codebase from the 2.x generation.

The fork's current release is:

    Lunamatic 3.0.1-26.2

Source project:

    Lunamatic

Original project:

    https://codeberg.org/Ifiht/Lunamatic

Original Modrinth project:

    https://modrinth.com/plugin/lunamatic

The original Modrinth project currently lists the 2.x series, including Lunamatic 2.0.5 and Lunamatic 2.0.0, with compatibility for Minecraft 1.21.x. This fork moves the project to the Minecraft 26.2 / Java 25 environment and adds the new configurable Blood Moon visual system.

## License

The original Lunamatic project is licensed under GPL-3.0-only.

See the repository and source files for the applicable license information.

## Support and Contributions

Bug reports, improvements and pull requests are welcome.

When reporting a problem, please include:

- Minecraft version
- Paper/Folia version
- Java version
- Lunamatic version
- Relevant server log output
- Relevant configuration
- Steps to reproduce the problem

---

**Lunamatic 3.0.1-26.2**

Updated to Minecraft 26.2
Configurable moon events and Blood Moon atmosphere for Minecraft Paper and Folia.

**Lunamatic 2.0.8**

Original version for Minecraft 1.21-1.21.10