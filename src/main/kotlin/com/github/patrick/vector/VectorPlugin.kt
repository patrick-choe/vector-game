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
import com.github.noonmaru.tap.entity.TapEntity.wrapEntity
import com.github.noonmaru.tap.event.ASMEventExecutor.registerEvents
import org.bukkit.Bukkit.broadcastMessage
import org.bukkit.Bukkit.getScheduler
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle.REDSTONE
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList.unregisterAll
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.LEFT_CLICK_AIR
import org.bukkit.event.block.Action.LEFT_CLICK_BLOCK
import org.bukkit.event.block.Action.RIGHT_CLICK_AIR
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import kotlin.random.Random.Default.nextDouble

class VectorPlugin : JavaPlugin(), Listener {
    private var status = false
    private val selectedEntities = HashMap<Player, Entity>()

    private var lastModified: Long? = null
    private var bothHands: Boolean = false
    private var singleTime: Boolean = false
    private var velocityModifier: Double = 0.0
    private var maxVelocity: Double = 0.0

    private fun statusOn(): Boolean {
        status = true
        registerEvents(this, this)
        getScheduler().runTaskTimer(this, { selectedEntities.forEach { newParticle(it) } }, 0, 1)
        broadcastMessage("Vector On")
        return true
    }

    private fun statusOff(): Boolean {
        status = false
        unregisterAll(this as JavaPlugin)
        getScheduler().cancelTasks(this)
        broadcastMessage("Vector Off")
        return true
    }

    private fun Player.getTarget(): Location {
        val loc = eyeLocation.clone()
        val view = loc.clone().add(loc.clone().direction.normalize().multiply(5))
        val block =
            MATH.rayTraceBlock(loc.world, Vector(loc.x, loc.y, loc.z), Vector(view.x, view.y, view.z), 0)
                ?: return loc.clone().add(eyeLocation.direction.clone().normalize().multiply(5))
        block.blockPoint.let {
            return loc.world.getBlockAt(it.x, it.y, it.z).getRelative(block.face).location.add(0.5, 0.5, 0.5)
        }
    }

    private fun setTargetVelocity(player: Player, remove: Boolean): Boolean? {
        selectedEntities[player]?.let {
            val vector = player.getTarget().subtract(it.location).toVector()
            it.velocity = if (vector.length() < maxVelocity / velocityModifier) vector.multiply(velocityModifier) else
                vector.normalize().multiply(maxVelocity)
            if (remove) selectedEntities.remove(player)
            return true
        }
        return null
    }

    private fun newRayTrace(player: Player) {
        val loc = player.eyeLocation
        val view = loc.clone().add(loc.clone().direction.normalize().multiply(20.0))
        var found: Entity? = null
        var distance = 0.0

        player.world.entities?.forEach { entity ->
            if (entity != player)
                wrapEntity(entity)?.boundingBox?.expand(5.0)
                    ?.calculateRayTrace(Vector(loc.x, loc.y, loc.z), Vector(view.x, view.y, view.z))?.let {
                        val currentDistance = loc.distance(entity.location)
                        if (currentDistance < distance || distance == 0.0) {
                            distance = currentDistance
                            found = entity
                        }
                    }
        }
        found?.let { selectedEntities[player] = it }
    }

    private fun newParticle(it: Map.Entry<Player, Entity>) {
        val pos = it.value.location.clone()
        if (!it.key.isValid || !it.value.isValid) selectedEntities.remove(it.key)
        else it.key.let { player ->
            player.getTarget().let { target ->
                for (i in 0 until pos.distance(target).times(5).toInt()) {
                    val loc = pos.add(target.toVector().clone().subtract(pos.toVector()).normalize().multiply(0.2))
                    pos.world.spawnParticle(REDSTONE, loc, 0, newRandom(), newRandom(), newRandom())
                }
            }
        }
    }

    private fun newRandom(): Double = nextDouble(255.0)

    override fun onEnable() {
        saveDefaultConfig()
        getScheduler().runTaskTimer(this, {
            val file = File(dataFolder, "config.yml")
            val last = file.lastModified()
            if (last != lastModified) {
                lastModified = last
                reloadConfig()
                bothHands = config.getBoolean("use-both-hands")
                singleTime = config.getBoolean("set-single-time")
                velocityModifier = config.getDouble("velocity-modifier")
                maxVelocity = config.getDouble("max-velocity")
            }
        }, 0, 1)
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?) =
        if (!status) statusOn() else statusOff()

    override fun onTabComplete(sender: CommandSender?, command: Command?, alias: String?, args: Array<out String>?) =
        emptyList<String>()

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val action = event.action
        if (event.item?.type == Material.BLAZE_ROD && player.hasPermission("command.vector.use")) {
            if (setOf(RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK).contains(action)) {
                if (!bothHands) setTargetVelocity(player, singleTime) ?: newRayTrace(player)
                else newRayTrace(player)
            }
            if (setOf(LEFT_CLICK_AIR, LEFT_CLICK_BLOCK).contains(action) && bothHands) setTargetVelocity(player,singleTime)
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) = selectedEntities.remove(event.player)
}