package net.lr1ne;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin implements Listener {

    private int despawnTime;
    private String headName;
    private List<String> headLore;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        despawnTime = getConfig().getInt("head-drop.despawn-time", 30);
        headName = getConfig().getString("head-drop.head-name", "&cГолова %player%");
        headLore = getConfig().getStringList("head-drop.head-lore");

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("DropAHead включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DropAHead выключен!");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && isAxe(killer.getInventory().getItemInMainHand().getType())) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            skullMeta.setOwningPlayer(victim);

            String formattedName = headName.replace("%player%", victim.getName());
            Component nameComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(formattedName);
            skullMeta.displayName(nameComponent);

            List<Component> formattedLore = new ArrayList<>();
            for (String line : headLore) {
                String formattedLine = line.replace("%player%", victim.getName());
                formattedLore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(formattedLine));
            }
            skullMeta.lore(formattedLore);

            playerHead.setItemMeta(skullMeta);

            Item droppedItem = victim.getWorld().dropItemNaturally(victim.getLocation(), playerHead);
            droppedItem.setTicksLived(6000 - (despawnTime * 20));
        }
    }

    private boolean isAxe(Material material) {
        return material == Material.WOODEN_AXE ||
                material == Material.STONE_AXE ||
                material == Material.IRON_AXE ||
                material == Material.GOLDEN_AXE ||
                material == Material.DIAMOND_AXE ||
                material == Material.NETHERITE_AXE;
    }
}