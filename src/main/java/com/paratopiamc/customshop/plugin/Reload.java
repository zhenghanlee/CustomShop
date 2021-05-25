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

import org.bukkit.command.CommandSender;

public class Reload extends CSComd {

    public Reload(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean exec() {
        if (!sender.hasPermission("customshop.reload")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
        } else {
            CustomShop.getPlugin().reloadConfig();
            sender.sendMessage("§6[CustomShop] §aSuccessfully reloaded configurations!");
        }
        return true;
    }

}
