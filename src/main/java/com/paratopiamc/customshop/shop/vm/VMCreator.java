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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.paratopiamc.customshop.plugin.CustomShop;
import com.paratopiamc.customshop.shop.ShopCreator;
import com.paratopiamc.customshop.utils.UIUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

/**
 * Vending machine's shop creator. Player's target block must have at least 2
 * blocks of air above for the shop to be spawned successfully.
 */
public class VMCreator implements ShopCreator {
    @Override
    public void createShop(Location location, Player owner, ItemStack item) {
        if (item.getItemMeta().getCustomModelData() == CustomShop.getPlugin().getConfig()
                .getInt("defaults.vending-machine")) {
            owner.sendMessage("§cYou have yet to unlock the selected Vending Machine!");
            return;
        }

        Location locationAddOne = location.clone();
        locationAddOne.setY(location.getY() + 1);
        if (location.getBlock().getType() != Material.AIR || locationAddOne.getBlock().getType() != Material.AIR) {
            owner.sendMessage("§cTarget location must have at least 2 blocks of air above...");
            return;
        }

        location.getBlock().setType(Material.BARRIER);
        locationAddOne.getBlock().setType(Material.BARRIER);

        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setCustomName("§5§lVending Machine");
        EntityEquipment armorStandBody = armorStand.getEquipment();
        armorStandBody.setHelmet(item);

        ItemStack container = new ItemStack(Material.SHULKER_BOX);
        BlockStateMeta blockMeta = (BlockStateMeta) container.getItemMeta();
        double[] prices = new double[27];
        List<String> lore = UIUtils.doubleToStringList(prices);
        blockMeta.setDisplayName(owner.getUniqueId().toString());
        blockMeta.setLore(lore);

        container.setItemMeta(blockMeta);
        armorStandBody.setChestplate(container);

        lockArmorStand(armorStand);

        CompletableFuture.runAsync(() -> {
            CustomShop.getPlugin().getDatabase().incrementTotalShopsOwned(owner.getUniqueId());
            owner.sendMessage("§aVending machine successfully created!");
        });
    }

    /**
     * Locks armor stand to prevent accessibility of items within its slots.
     *
     * @param armorStand ArmorStand to lock
     */
    private void lockArmorStand(ArmorStand armorStand) {
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        armorStand.setVisible(false);

        armorStand.addEquipmentLock(EquipmentSlot.HEAD, LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.HAND, LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING_OR_CHANGING);
    }
}
