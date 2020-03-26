/*
 * Copyright (C) 2020 PatrickKR
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Contact me on <mailpatrickkr@gmail.com>
 */

package com.github.patrick.vector

import com.github.noonmaru.tap.LibraryLoader.getBukkitVersion
import com.github.patrick.vector.VectorUtils.isLegacyVersion
import com.github.patrick.vector.VectorUtils.isModernVersion
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.Bukkit.getScheduler
import org.bukkit.Material.BLAZE_ROD
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 * This class is the main class of this plugin.  It keeps the global
 * variables and methods, and handles initial process of this plugin.
 */
class VectorPlugin : JavaPlugin() {
    /**
     * This companion object saves project-wide variables.
     */
    companion object {
        lateinit var version: String
        lateinit var instance: VectorPlugin
        val selectedEntities = HashMap<Player, Entity>()
        var lastModified: Long? = null
        var vectorItem = BLAZE_ROD
        var bothHands = false
        var singleTime = false
        var hitBoxExpansion = 0.0
        var visibilityLength = 0.0
        var velocityModifier = 0.0
        var maxVelocity = 0.0
    }

    /**
     * This overridden method executes when the plugin is initializing.  It saves default
     * configuration if the 'config.yml' does not exist, sets command executor and tab completer
     * for '/vector' command, and registers 'VectorConfigTask'.
     */
    override fun onEnable() {
        version = getBukkitVersion().split(" ")[0]
        instance = this
        saveDefaultConfig()
        getCommand("vector")?.setExecutor(VectorCommand(this))
        getCommand("vector")?.tabCompleter = VectorCommand(this)
        getScheduler().runTaskTimer(this, VectorConfigTask(this), 0, 1)
        if (isLegacyVersion()) {
            if (!getPluginManager().isPluginEnabled("Tap")) getPluginManager().disablePlugin(this)
        }
        else if (!isModernVersion()) getPluginManager().disablePlugin(this)
    }
}