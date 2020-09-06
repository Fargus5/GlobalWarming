package me.poma123.globalwarming.items;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.ToolUseHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNet;
import io.github.thebusybiscuit.slimefun4.utils.holograms.SimpleHologram;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.Objects.handlers.ItemHandler;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.poma123.globalwarming.GlobalWarming;
import me.poma123.globalwarming.TemperatureType;
import me.poma123.globalwarming.utils.TemperatureUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class Thermometer extends SlimefunItem {

    private static TemperatureType temperatureType = TemperatureType.CELSIUS;

    @ParametersAreNonnullByDefault
    public Thermometer(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);

        SlimefunItem.registerBlockHandler(getID(), (p, b, stack, reason) -> {
            SimpleHologram.remove(b);
            return true;
        });
    }

    @Nonnull
    private BlockPlaceHandler onPlace() {
        return new BlockPlaceHandler(false) {

            @Override
            public void onPlayerPlace(BlockPlaceEvent e) {
                Block b = e.getBlockPlaced();
                BlockStorage.addBlockInfo(b,"type", temperatureType.name());
                BlockStorage.addBlockInfo(b, "owner", e.getPlayer().getUniqueId().toString());
                SimpleHologram.update(e.getBlock(), "&7Calculating...");
            }

        };
    }

    @Nonnull
    private BlockUseHandler onRightClick() {
        return (e) -> {
            Player p = e.getPlayer();
            Block b = e.getClickedBlock().get();

            if (BlockStorage.getLocationInfo(b.getLocation(), "owner").equals(p.getUniqueId().toString())) {
                TemperatureType saved = TemperatureType.valueOf(BlockStorage.getLocationInfo(b.getLocation(), "type"));

                if (saved == TemperatureType.CELSIUS) {
                    saved = TemperatureType.FAHRENHEIT;
                } else {
                    saved = TemperatureType.CELSIUS;
                }

                BlockStorage.addBlockInfo(b,"type", saved.name());
                e.getPlayer().sendMessage("§7Temperature type: §e" + saved.toString());
            }

        };
    }

    @Override
    public void preRegister() {
        addItemHandler(onPlace());
        addItemHandler(onRightClick());
        addItemHandler(new BlockTicker() {

            @Override
            public boolean isSynchronized() {
                return false;
            }

            @Override
            public void tick(Block b, SlimefunItem item, Config data) {
                Thermometer.this.tick(b);
            }
        });
    }

    private void tick(@Nonnull Block b) {
        SimpleHologram.update(b, TemperatureUtils.getTemperatureString(b, TemperatureType.valueOf(BlockStorage.getLocationInfo(b.getLocation(), "type"))));
    }
}