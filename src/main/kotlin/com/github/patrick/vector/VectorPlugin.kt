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
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import kotlin.random.Random.Default.nextDouble

class VectorPlugin : JavaPlugin(), Listener {
    private var status = false
    private val selectedEntities = HashMap<Player, Entity>()

    private var lastModified: Long? = null
    private var bothHands = false
    private var singleTime = false
    private var visibilityLength = 0.0
    private var velocityModifier = 0.0
    private var maxVelocity = 0.0

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

    private fun CommandSender.requireMessage(message: String) = sendMessage("Required: $message")

    private fun CommandSender.unrecognizedMessage(message: String, value: String) = sendMessage("Unrecognized $message: '$value'")

    private fun Player.getTarget(): Location {
        val loc = eyeLocation.clone()
        val view = loc.clone().add(loc.clone().direction.normalize().multiply(visibilityLength))
        val block =
            MATH.rayTraceBlock(loc.world, Vector(loc.x, loc.y, loc.z), Vector(view.x, view.y, view.z), 0)
                ?: return loc.clone().add(eyeLocation.direction.clone().normalize().multiply(visibilityLength))
        block.blockPoint.let {
            return loc.world.getBlockAt(it.x, it.y, it.z).getRelative(block.face).location.add(0.5, 0.5, 0.5)
        }
    }

    private fun String.resetRegexMatch(): Boolean = contains(Regex("(?i)conf|set"))

    private fun getKeys() = config.getKeys(false)

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
                visibilityLength = config.getDouble("visibility-length-double")
                velocityModifier = config.getDouble("velocity-modifier-double")
                maxVelocity = config.getDouble("max-velocity-double")
            }
        }, 0, 1)
    }

    @Throws(IOException::class, NumberFormatException::class)
    override fun onCommand(sender: CommandSender, command: Command?, label: String?, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            if (args[0].resetRegexMatch()) {
                when (args.size) {
                    1 -> sender.requireMessage("key, value").also { return false }
                    2 -> {
                        if (args[1].contains("reset", true)) {
                            File(dataFolder, "config.yml").delete()
                            saveDefaultConfig()
                            return true
                        }
                        if (config.getKeys(false).contains(args[1])) sender.requireMessage("value").also { return true }
                        sender.unrecognizedMessage("key", args[1])
                    }
                    3 -> {
                        if (config.getKeys(false).contains(args[1])) {
                            try {
                                val path = File(dataFolder, "config.yml").toPath()
                                val lines = Files.readAllLines(path, UTF_8)
                                for (i in 0 until lines.count()) {
                                    if (lines[i].contains(args[1])) when {
                                        args[1].contains("double") -> lines[i] = "${args[1]}: ${args[2].toDouble()}"
                                        args[2].contains("true", true) -> lines[i] = "${args[1]}: true"
                                        args[2].contains("false", true) -> lines[i] = "${args[1]}: false"
                                        else -> sender.unrecognizedMessage("value", args[2])
                                    }
                                }
                                Files.write(path, lines, UTF_8)
                                return true
                            } catch (e: Exception) {
                                when (e) {
                                    is IOException -> logger.info("Cannot read/write to config.yml")
                                    is NumberFormatException -> sender.unrecognizedMessage("value", args[2])
                                    else -> throw e
                                }
                                return false
                            }
                        }
                    }
                }
            }
            sender.unrecognizedMessage("args", args[0])
            return false
        }
        return if (!status) statusOn() else statusOff()
    }

    override fun onTabComplete(sender: CommandSender?, command: Command?, alias: String?, args: Array<out String>) =
        when (args.size) {
            1 -> setOf("config").filter { it.startsWith(args[0], true) }
            2 -> if (args[0].resetRegexMatch()) getKeys().filter { it.startsWith(args[1], true) } else emptyList()
            3 -> if (args[0].resetRegexMatch() && getKeys().contains(args[1])) {
                if (!args[1].contains("double", true))
                    setOf("true", "false").filter { it.startsWith(args[2], true) }
                else emptyList()
            } else emptyList()
            else -> emptyList()
        }

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