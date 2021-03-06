/*
 *  This file is part of CustomShop. Copyright (c) 2021 Paratopia.
 *
 *  CustomShop is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CustomShop is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CustomShop. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.paratopiamc.customshop.plugin;

import net.milkbowl.vault.economy.Economy;
import com.paratopiamc.customshop.crate.UnlockShop;
import com.paratopiamc.customshop.database.Database;
import com.paratopiamc.customshop.database.SQLite;
import com.paratopiamc.customshop.gui.CreationGUI;
import com.paratopiamc.customshop.player.PlayerJoin;
import com.paratopiamc.customshop.player.PlayerLeave;
import com.paratopiamc.customshop.player.PlayerMove;
import com.paratopiamc.customshop.player.PlayerState;
import com.paratopiamc.customshop.player.PlayerTeleport;
import com.paratopiamc.customshop.plugin.CustomShopLogger.Level;
import com.paratopiamc.customshop.shop.ShopCreation;
import com.paratopiamc.customshop.shop.ShopExit;
import com.paratopiamc.customshop.shop.ShopOpening;
import com.paratopiamc.customshop.shop.ShopRemoval;
import com.paratopiamc.customshop.shop.briefcase.BriefcaseInteractInventory;
import com.paratopiamc.customshop.shop.briefcase.BriefcaseListItem;
import com.paratopiamc.customshop.shop.vm.VMInteractInventory;
import com.paratopiamc.customshop.shop.vm.VMListItem;
import com.paratopiamc.customshop.utils.LanguageUtils;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A custom chestshop plugin that implements custom shop designs.
 *
 * @author SolarRabbit
 */
public final class CustomShop extends JavaPlugin {
    private static CustomShop pluginInstance;
    private Economy economy;
    private Database database;
    private ExternalPluginsSupport support;

    @Override
    public void onEnable() {
        pluginInstance = this;

        if (!setUpEconomy()) {
            CustomShopLogger.sendMessage("No Vault dependencies found! Disabling plugin...", Level.FAIL);
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            CustomShopLogger.sendMessage("Successfully hooked onto Vault", Level.SUCCESS);
        }

        this.support = new ExternalPluginsSupport(this);
        this.support.init();

        if (!this.getDataFolder().exists()) {
            try {
                this.getDataFolder().mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ShopOpening(), this);
        pluginManager.registerEvents(new ShopExit(), this);
        pluginManager.registerEvents(new VMInteractInventory(), this);
        pluginManager.registerEvents(new VMListItem(), this);
        pluginManager.registerEvents(new BriefcaseInteractInventory(), this);
        pluginManager.registerEvents(new BriefcaseListItem(), this);
        pluginManager.registerEvents(new ShopCreation(), this);
        pluginManager.registerEvents(new ShopRemoval(), this);
        pluginManager.registerEvents(new UnlockShop(), this);
        pluginManager.registerEvents(new PlayerTeleport(), this);
        pluginManager.registerEvents(new PlayerMove(), this);
        pluginManager.registerEvents(new PlayerLeave(), this);
        pluginManager.registerEvents(new PlayerJoin(), this);
        PluginCommand mainCommand = getCommand("customshop");
        mainCommand.setExecutor(new CSComdExec());
        mainCommand.setTabCompleter(new AutoComplete());

        this.database = new SQLite(this);
        this.database.load();

        saveDefaultConfig();
        LanguageUtils.loadLanguageConfig();
        CreationGUI.initialize();
    }

    @Override
    public void onDisable() {
        PlayerState.clearAllShopInteractions();
        super.onDisable();
    }

    /**
     * Get economy provider of the server.
     *
     * @return {@code true} if the detected provider is valid
     */
    private boolean setUpEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        } else {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            } else {
                economy = rsp.getProvider();
                return economy != null;
            }
        }
    }

    /**
     * Return instance of plugin that is running.
     *
     * @return plugin instance
     */
    public static CustomShop getPlugin() {
        return pluginInstance;
    }

    /**
     * Get the current economy that the plugin is using.
     *
     * @return economy instance
     */
    public Economy getEconomy() {
        return this.economy;
    }

    /**
     * Return database used by the plugin.
     *
     * @return database
     */
    public Database getDatabase() {
        return this.database;
    }

    public ExternalPluginsSupport support() {
        return this.support;
    }
}
