# EnderEgg

A lightweight Spigot 1.21.11 plugin that adds a custom **Ender Egg**.

## Ability

When a player has the Ender Egg anywhere in their inventory, they receive:

- Speed I: +20% movement speed
- Strength I: +3 melee attack damage
- Resistance I: 20% incoming damage reduction

No Minecraft potion effects are applied. The plugin uses the Bukkit attribute API for speed and normal damage events for Strength and Resistance.

The Ender Egg is a tagged Dragon Egg, so normal Dragon Eggs do not activate the ability.

## Command

`/enderegg give [player] [amount]`

Permission: `enderegg.admin`

## Build

Requires Java 21 and Maven.

```bash
mvn clean package
```

The resulting jar is `target/EnderEgg.jar`.
