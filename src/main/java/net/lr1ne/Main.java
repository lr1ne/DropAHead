package net.lr1ne;

//
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Tag;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener {
    private static final MiniMessage SERIALIZER = MiniMessage.miniMessage();

    private String headName;
    private List<String> headLore;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();
        getServer().getPluginManager().registerEvents(this, this);

        var cmd = getCommand("dap");
        if (cmd != null) {
            cmd.setExecutor(new ReloadCommand());
        }

        getLogger().info("DropAHead enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DropAHead disabled!");
    }

    private void loadConfigValues() {
        reloadConfig();
        headName = getConfig().getString("head-drop.head-name", "&c&lГолова %player%");
        headLore = getConfig().getStringList("head-drop.head-lore");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && Tag.ITEMS_AXES.isTagged(killer.getInventory().getItemInMainHand().getType())) {

            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            if (!(playerHead.getItemMeta() instanceof SkullMeta skullMeta)) {
                return;
            }
            skullMeta.setOwningPlayer(victim);
            String formattedName = headName
                    .replace("%player%", victim.getName())
                    .replace("%killer%", killer.getName());
            skullMeta.displayName(SERIALIZER.deserialize(formattedName));

            if (!headLore.isEmpty()) {
                List<Component> formattedLore = new ArrayList<>();
                for (String line : headLore) {
                    String formattedLine = line
                            .replace("%player%", victim.getName())
                            .replace("%killer%", killer.getName());
                    formattedLore.add(SERIALIZER.deserialize(formattedLine));
                }
                skullMeta.lore(formattedLore);
            }
            playerHead.setItemMeta(skullMeta);
            event.getDrops().add(playerHead);
        }
    }

    private class ReloadCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
            if (!sender.hasPermission("dropahead.reload")) {
                sender.sendMessage(Component.text("You do not have permission for this command!"));
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                loadConfigValues();
                sender.sendMessage(Component.text("DropAHead config reloaded!"));
            } else {
                sender.sendMessage(Component.text("Usage: /dap reload"));
            }
            return true;
        }
    }
}