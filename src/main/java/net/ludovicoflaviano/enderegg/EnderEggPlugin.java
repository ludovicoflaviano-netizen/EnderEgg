package net.ludovicoflaviano.enderegg;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public final class EnderEggPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    private NamespacedKey eggKey;
    private NamespacedKey speedModifierKey;

    @Override
    public void onEnable() {
        eggKey = new NamespacedKey(this, "ender_egg");
        speedModifierKey = new NamespacedKey(this, "ender_egg_speed");

        Bukkit.getPluginManager().registerEvents(this, this);
        if (getCommand("enderegg") != null) {
            getCommand("enderegg").setExecutor(this);
            getCommand("enderegg").setTabCompleter(this);
        }

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateSpeed(player);
            }
        }, 1L, 10L);
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeSpeedModifier(player);
        }
    }

    private void updateSpeed(Player player) {
        AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed == null) return;

        boolean hasEgg = hasEnderEgg(player);
        AttributeModifier existing = findSpeedModifier(speed);

        if (hasEgg && existing == null) {
            speed.addModifier(new AttributeModifier(
                    speedModifierKey,
                    0.20,
                    AttributeModifier.Operation.ADD_SCALAR,
                    org.bukkit.inventory.EquipmentSlotGroup.ANY
            ));
        } else if (!hasEgg && existing != null) {
            speed.removeModifier(existing);
        }
    }

    private AttributeModifier findSpeedModifier(AttributeInstance speed) {
        for (AttributeModifier modifier : speed.getModifiers()) {
            if (modifier.getKey().equals(speedModifierKey)) {
                return modifier;
            }
        }
        return null;
    }

    private void removeSpeedModifier(Player player) {
        AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed == null) return;
        AttributeModifier existing = findSpeedModifier(speed);
        if (existing != null) speed.removeModifier(existing);
    }

    public boolean hasEnderEgg(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isEnderEgg(item)) return true;
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (isEnderEgg(item)) return true;
        }
        return isEnderEgg(player.getInventory().getItemInOffHand());
    }

    public boolean isEnderEgg(ItemStack item) {
        if (item == null || item.getType() != Material.DRAGON_EGG || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(eggKey);
    }

    public ItemStack createEnderEgg() {
        return EnderEggItem.create(eggKey);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!hasEnderEgg(player)) return;

        // Resistance I reduces incoming damage by 20%, without applying a potion effect.
        event.setDamage(event.getDamage() * 0.80);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!hasEnderEgg(player)) return;

        // Strength I adds 3 attack damage in Java Edition, without a potion effect.
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            event.setDamage(event.getDamage() + 3.0);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("enderegg")) return false;
        if (!sender.hasPermission("enderegg.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length < 1 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage("§7Usage: §f/enderegg give [player] [amount]");
            return true;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage("§cConsole must specify a player.");
            return true;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Math.min(64, Integer.parseInt(args[2])));
            } catch (NumberFormatException ignored) {
                sender.sendMessage("§cAmount must be a number from 1 to 64.");
                return true;
            }
        }

        ItemStack egg = createEnderEgg();
        egg.setAmount(amount);
        target.getInventory().addItem(egg);
        sender.sendMessage("§aGave §f" + amount + " §aEnder Egg(s) to §f" + target.getName() + "§a.");
        if (target != sender) {
            target.sendMessage("§5§lEnder Egg §7» §fYou received an Ender Egg.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("enderegg")) return List.of();
        if (args.length == 1) return List.of("give");
        if (args.length == 2) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) names.add(player.getName());
            return names;
        }
        return List.of("1", "16", "64");
    }
}
