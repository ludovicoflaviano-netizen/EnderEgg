package net.ludovicoflaviano.enderegg;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class EnderEggItem {
    private EnderEggItem() {}

    public static ItemStack create(NamespacedKey key) {
        ItemStack item = new ItemStack(Material.DRAGON_EGG);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName("§5§lEnder Egg");
        meta.setLore(List.of(
                "§8A mysterious egg from the End.",
                "",
                "§dWhile in your inventory:",
                "§7• §fSpeed I",
                "§7• §fStrength I",
                "§7• §fResistance I",
                "",
                "§8Effects are passive and use no potion effects."
        ));
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }
}
