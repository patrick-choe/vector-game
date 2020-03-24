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

import com.github.noonmaru.math.Vector
import com.github.noonmaru.tap.Tap.MATH
import org.bukkit.Bukkit.getScheduler
import org.bukkit.Location
import org.bukkit.Material.AIR
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 * This class is the main class of this plugin.  It keeps the global
 * variables and methods, and handles initial process of this plugin.
 */
class VectorPlugin : JavaPlugin() {
    /**
     * This companion object saves project-wide variables and methods.
     */
    companion object {
        /**
         * This [HashMap] saves the player, and the corresponding selected entity
         */
        val selectedEntities = HashMap<Player, Entity>()
        var lastModified: Long? = null
        var vectorItem = AIR
        var bothHands = false
        var singleTime = false
        var visibilityLength = 0.0
        var velocityModifier = 0.0
        var maxVelocity = 0.0

        /**
         * This method returns the player's eye target, multiplied by visibility length
         * set on 'plugin.yml'.  If the target matches a block, it returns a location
         * in front of the matched block.
         *
         * @return  player's eye target location
         */
        fun Player.getTarget(): Location {
            val loc = eyeLocation.clone()
            val view = loc.clone().add(loc.clone().direction.normalize().multiply(visibilityLength))
            val block =
                MATH.rayTraceBlock(loc.world, Vector(loc.x, loc.y, loc.z), Vector(view.x, view.y, view.z), 0)
                    ?: return loc.clone().add(eyeLocation.direction.clone().normalize().multiply(visibilityLength))
            block.blockPoint.let {
                return loc.world.getBlockAt(it.x, it.y, it.z).getRelative(block.face).location.add(0.5, 0.5, 0.5)
            }
        }
    }

    /**
     * This overridden method executes when the plugin is initializing.  It saves default
     * configuration if the 'config.yml' does not exist, sets command executor and tab completer
     * for '/vector' command, and registers 'VectorConfigTask'.
     */
    override fun onEnable() {
        saveDefaultConfig()
        getCommand("vector").executor = VectorCommand(this)
        getCommand("vector").tabCompleter = VectorCommand(this)
        getScheduler().runTaskTimer(this, VectorConfigTask(this), 0, 1)
    }
}