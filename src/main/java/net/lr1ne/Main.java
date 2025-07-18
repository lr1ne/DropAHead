package net.lr1ne;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener {

    private String headName;
    private List<String> headLore;
    private NamespacedKey loreKey;
    // victim.getName()

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("dap")).setExecutor(new ReloadCommand());
        getLogger().info("DropAHead enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DropAHead disabled!");
    }

    private void loadConfig() {
        headName = getConfig().getString("head-drop.head-name", "&c&lГолова %player%");
        headLore = getConfig().getStringList("head-drop.head-lore");
        loreKey = new NamespacedKey(this, "head_lore");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && isAxe(killer.getInventory().getItemInMainHand().getType())) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            if (skullMeta == null) return;

            skullMeta.setOwningPlayer(victim);
            String formattedName = headName.replace("%player%", victim.getName()).replace("%killer%", killer.getName());
            Component nameComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(formattedName);
            skullMeta.displayName(nameComponent);

            List<Component> formattedLore = new ArrayList<>();
            List<String> loreStrings = new ArrayList<>();
            for (String line : headLore) {
                String formattedLine = line.replace("%player%", victim.getName()).replace("%killer%", killer.getName());
                loreStrings.add(formattedLine);
                formattedLore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(formattedLine));
            }
            skullMeta.lore(formattedLore);

            PersistentDataContainer pdc = skullMeta.getPersistentDataContainer();
            pdc.set(loreKey, PersistentDataType.LIST.strings(), loreStrings);

            playerHead.setItemMeta(skullMeta);

            getServer().getScheduler().runTask(this, () ->
                    victim.getWorld().dropItemNaturally(victim.getLocation(), playerHead)
            );
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
    private class ReloadCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
            if (!sender.hasPermission("dropahead.reload")) {
                sender.sendMessage(Component.text("You do not have permission for this command!"));
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                loadConfig();
                sender.sendMessage(Component.text("DropAHead config reloaded!"));
                return true;
            }
            sender.sendMessage(Component.text("Usage: /dap reload"));
            return true;
        }
    }
}