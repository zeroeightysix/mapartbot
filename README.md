# mapartbot
A discord bot for converting blocks in a minecraft schematica

All commands are prefixed by an exclamation mark `!`.

## Building
Building can be done easily with `mvn install`. Because I don't know why maven won't package JNBT with the jar, you'll have to drop JNBT binaries in yourself.

## Using
The `!help` command:
```
This bot converts schematics for easy mapart-ready schematics without the need of mcedit, worldedit..
What blocks it replaces with which, is completely up to you. Use the commands below to update the replace map.
To convert a file, just drop it in this chat.

reset ~ Return the replace map to defaults
map ~ View the entries (groups) in the current replace map
open <groupid> ~ View the mappings of a group
remove <groupid> ~ Removes a group from the map
help ~ displays this message

add <group> ~ Add a new replacegroup. Requires specific formatting, f.e: (Must be one message!)
    add
    1:1 -> 1:3
    5 -> 6
    0 -> 3:1
```

extra note
```
The "replace map" of the bot can be edited. To view what the contents (groups) are, use
    !map
format: (id) shortdescription

To view the contents of a group, enter its id (which is the word in bold, in the brackets)
    f.e. !open woolcarpet

To add a group, use the command !add. Then, on every new line, specify the "from" blockid and the "to" blockid, seperated with an arrow like so:
    !add
    1 -> 6
where "1" is stone, "6" are oak saplings. Check out https://minecraft-ids.grahamedgecombe.com/ for a list of ids
!add will create a group with id yourname0 (where 0 will increase if a group by the name already exists)
```
