# Meteor Enemies

Fairly simple Meteor Client addon to mark specific players as "enemies".

In order to change their name color in the player list (tab menu), the 'Better Tab' module has to be enabled.

## Commands

- `.enemy list`: Display the list of enemies in chat.
- `.enemy add (name)`: Add user to the list of enemies.
- `.enemy remove (name)`: Remove user from the list of enemies.

## Vanish Checker

The vanish checker works by exploiting a bug in some vanish plugins where a vanished player will not appear in the player list (tab menu), but will appear as a suggestion when writing a command.
If the vanish plugin does not work in that way, it is better to leave the feature disabled.
