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

package com.paratopiamc.customshop.shop.vm;

import com.paratopiamc.customshop.gui.VMGUI;
import com.paratopiamc.customshop.player.PlayerState;
import com.paratopiamc.customshop.plugin.CustomShop;

import org.bukkit.Bukkit;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.InactivityConversationCanceller;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

/**
 * Listener for players interacting with custom shops' GUI, containing handlers
 * for which the player (owner or not) purchases items.
 */
public class VMInteractInventory implements Listener {
    private static ConversationFactory purchasingConversation;

    /**
     * Event handler for interactions with shop's GUI.
     *
     * @param evt event of inventory clicking
     */
    @EventHandler
    public void clickShop(InventoryClickEvent evt) {
        if (evt.getCurrentItem() == null) {
            return;
        }
        InventoryHolder holder = evt.getClickedInventory().getHolder();
        Player player = (Player) evt.getWhoClicked();
        String title = evt.getView().getTitle();
        if (title.equalsIgnoreCase("§5§lVending Machine")) {
            if (holder == null) {
                ItemMeta itemMeta = evt.getCurrentItem().getItemMeta();
                if (itemMeta.hasDisplayName() && itemMeta.getDisplayName().equals("§cClose")) {
                    Bukkit.getScheduler().runTask(CustomShop.getPlugin(), () -> player.closeInventory());
                } else if (evt.getSlot() < 27) {
                    PlayerState state = PlayerState.getPlayerState(player);
                    VMGUI ui = (VMGUI) state.getShopGUI();
                    ItemStack item = ui.getItem(evt.getSlot());
                    state.startPurchase(item, purchasingConversation);
                    Bukkit.getScheduler().runTask(CustomShop.getPlugin(), () -> player.closeInventory());
                }
            }
            evt.setCancelled(true);
        }
    }

    /**
     * Initialises conversation factory for shop purchases.
     *
     * @param plugin instance of plugin that owns the factory
     */
    public static void initConversationFactory(Plugin plugin) {
        purchasingConversation = new ConversationFactory(plugin).withFirstPrompt(new AmountPrompt()).withModality(false)
                .withLocalEcho(false)
                .withConversationCanceller(new InactivityConversationCanceller(CustomShop.getPlugin(), 10))
                .addConversationAbandonedListener(new ConversationAbandonedListener() {
                    @Override
                    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
                        ConversationCanceller canceller = abandonedEvent.getCanceller();
                        Player player = (Player) abandonedEvent.getContext().getForWhom();
                        if (canceller != null) {
                            player.sendMessage("§cShop purchase cancelled...");
                        }
                        PlayerState.getPlayerState(player).clearShopInteractions();
                    }
                });
        ;
    }

    /**
     * Prompt when player attempts to purchase from Vending Machine.
     */
    private static class AmountPrompt extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return "§aEnter the amount that you want to purchase...";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (context.getForWhom() instanceof Player) {
                Player player = (Player) context.getForWhom();
                PlayerState state = PlayerState.getPlayerState(player);
                ItemStack purchasingItem = state.removePurchase();
                VMGUI ui = (VMGUI) state.getShopGUI();
                try {
                    int inputInt = Integer.parseInt(input);
                    double inputDouble = Double.parseDouble(input);

                    if (inputInt != inputDouble || inputDouble <= 0) {
                        player.sendMessage("§cInvalid input!");
                    } else if (context.getForWhom() instanceof Player) {
                        ui.purchaseItem(purchasingItem, inputInt);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid input!");
                }
            } else {
                // Should not get here.
                context.getForWhom().sendRawMessage("This is a player-only command.");
            }
            return END_OF_CONVERSATION;
        }
    }
}
