# Meteor Enemies

Fairly simple Meteor Client addon to mark specific players as "enemies".

If the 'Highlight' setting is active:
- Enemies will appear as a different colour in the player list (tab menu) if the 'Better Tab' module is enabled.
- Enemies will appear as a different colour in nametags if the 'Nametags' module is enabled.

[![Download](https://cdn12.picryl.com/photo/2016/12/31/download-button-download-now-button-computer-communication-e5ae87-1024.png)](https://github.com/RacoonDog/MeteorEnemies/releases/tag/snapshot)

## Commands

- `.enemy list`: Display the list of enemies in chat.
- `.enemy add (name)`: Add user to the list of enemies.
- `.enemy remove (name)`: Remove user from the list of enemies.

## Vanish Checker

The vanish checker works by exploiting a bug in some vanish plugins where a vanished player will not appear in the player list (tab menu), but will appear as a suggestion when writing a command.
If the vanish plugin does not work in that way, it is better to leave the feature disabled.
