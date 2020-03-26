package com.github.patrick.vector

import com.github.patrick.vector.VectorPlugin.Companion.instance
import com.github.patrick.vector.VectorPlugin.Companion.maxVelocity
import com.github.patrick.vector.VectorPlugin.Companion.selectedEntities
import com.github.patrick.vector.VectorPlugin.Companion.singleTime
import com.github.patrick.vector.VectorPlugin.Companion.velocityModifier
import com.github.patrick.vector.VectorPlugin.Companion.version
import com.github.patrick.vector.VectorPlugin.Companion.visibilityLength
import com.github.patrick.vector.v1_12_R1.TapSupport.newTapRayTraceBlock
import com.github.patrick.vector.v1_12_R1.TapSupport.newTapRayTraceEntity
import com.github.patrick.vector.v1_15_R1.BukkitSupport.newBukkitRayTraceBlock
import com.github.patrick.vector.v1_15_R1.BukkitSupport.newBukkitRayTraceEntity
import org.bukkit.Location
import org.bukkit.Material.AIR
import org.bukkit.Material.getMaterial
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * This object contains multiple methods used throughout
 * this plugin.
 */
object VectorUtils {
    private val dataFolder = instance.dataFolder
    private val config = instance.config
    private val logger = instance.logger
    private val file = File(dataFolder, "config.yml")

    /**
     * This private method will set the velocity of the entity depending on
     * the distance between the entity and the target's location
     *
     * @return  if removed successfully, it returns true, else, it returns null
     */
    fun Player.setTargetVelocity(): Boolean? {
        selectedEntities[this]?.let {
            val vector = getTarget().subtract(it.location).toVector()
            it.velocity = if (vector.length() < maxVelocity / velocityModifier) vector.multiply(
                velocityModifier
            ) else
                vector.normalize().multiply(maxVelocity)
            if (singleTime) selectedEntities.remove(this)
            return true
        }
        return null
    }

    /**
     * This private method raytraces the entities and find the
     * closest entity to the player.
     */
    fun Player.newEntityRayTrace() {
        val loc = getTargetMapping().key
        val view = getTargetMapping().value
        var found: Entity? = null
        var distance = 0.0

        world.entities.forEach {
            val result = when {
                isLegacyVersion() -> it.newTapRayTraceEntity(loc, view)
                isModernVersion() -> it.newBukkitRayTraceEntity(loc, view)
                else -> false
            }
            val currentDistance = loc.distance(it.location)
            if ((currentDistance < distance || distance == 0.0) && it != this && result) {
                distance = currentDistance
                found = it
            }
        }
        found?.let { selectedEntities[this] = it }
    }

    /**
     * This method returns the player's eye target, multiplied by visibility length
     * set on 'plugin.yml'.  If the target matches a block, it returns a location
     * in front of the matched block.
     *
     * @return  player's eye target location
     */
    fun Player.getTarget(): Location {
        val loc = getTargetMapping().key
        return when {
            isLegacyVersion() -> newTapRayTraceBlock(loc, getTargetMapping().value)
            isModernVersion() -> newBukkitRayTraceBlock(loc, eyeLocation.direction)
            else -> null
        }?.location?.add(0.5, 0.5, 0.5)
            ?: loc.clone().add(eyeLocation.direction.clone().normalize().multiply(visibilityLength))
    }

    /**
     * This private method handles config-related commands
     *
     * @param   args    arguments from the command
     * @param   sender  command sender from the command
     */
    fun configCommand(args: Array<out String>, sender: CommandSender) = when (args.size) {
        1 -> sender.sendMessage("Required: key, value")
        2 -> when {
            args[1].contains("reset", true) -> {
                file.delete()
                instance.saveDefaultConfig()
            }
            getKeys().contains(args[1]) -> getCurrentConfig(args, sender)
            else -> sender.unrecognizedMessage("key", args[1])
        }
        3 -> when {
            getKeys().contains(args[1]) -> setConfig(args, sender)
            else -> sender.unrecognizedMessage("Unrecognized key", args[1])
        } else -> sender.unrecognizedMessage("args", args.drop(3).toString())
    }

    /**
     * This method sends sender an alert saying that the message
     * cannot be recognized by the server.
     *
     * @param   type the type that can't be recognized
     * @param   value   the message value that can't be recognized
     */
    fun CommandSender.unrecognizedMessage(type: String, value: String) =
        sendMessage("Unrecognized $type: '$value'")

    /**
     * This method gets keys from 'config.yml'
     *
     * @return  [List] of [String] containing keys
     */
    fun getKeys() = config.getKeys(false).toList()

    /**
     * This method checks if the version is a legacy support.
     *
     * @return  true if the version contains 1.10 ~ 1.12
     */
    fun isLegacyVersion() = version.contains(Regex("1.10|1.11|1.12"))

    /**
     * This method checks if the version is a modern support.
     *
     * @return  true if the version contains 1.13 ~ 1.15
     */
    fun isModernVersion() = version.contains(Regex("1.13|1.14|1.15"))

    private fun getCurrentConfig(args: Array<out String>, sender: CommandSender) = try {
        val path = file.toPath()
        val lines = Files.readAllLines(path, StandardCharsets.UTF_8)

        for (i in 0 until lines.count())
            if (lines[i].contains(args[1]))
                sender.sendMessage("\n \n \n" +
                        "${lines[i - 3].substring(2)}\n" +
                        "${lines[i - 2].substring(2)}\n" +
                        "${lines[i - 1].substring(2)}\n \n" +
                        "Current ${args[1]}: ${config.get(args[1])}\n")
    } catch (e: IOException) { logger.info("Cannot read/write to config.yml") }

    private fun setConfig(args: Array<out String>, sender: CommandSender) {
        try {
            val path = file.toPath()
            val lines = Files.readAllLines(path, StandardCharsets.UTF_8)
            for (i in 0 until lines.count()) {
                if (lines[i].contains(args[1])) when {
                    args[1].contains("double") -> {
                        if (args[2].toDouble() > 0) lines[i] = "${args[1]}: ${args[2].toDouble()}"
                        else sender.sendMessage("value should be positive.").also { return }
                    }
                    args[1].contains("item") -> {
                        getMaterial(args[2].toUpperCase()) ?: sender.unrecognizedMessage("key", args[2]).also { return }
                        if (getMaterial(args[2]) == AIR) sender.sendMessage("value should not be AIR.").also { return }
                        lines[i] = "${args[1]}: ${args[2].toUpperCase()}"
                    }
                    args[2].matches(Regex("true|false")) -> lines[i] = "${args[1]}: ${args[2]}"
                    else -> sender.unrecognizedMessage("value", args[2]).also { return }
                }.also { sender.sendMessage(lines[i]) }
            }
            Files.write(path, lines, StandardCharsets.UTF_8)
        } catch (e: NumberFormatException) { sender.unrecognizedMessage("value", args[2]) }
        catch (e: IOException) { logger.info("Cannot read/write to config.yml") }
    }

    private fun Player.getTargetMapping() =
        mapOf(eyeLocation.clone() to eyeLocation.clone().add(eyeLocation.clone().direction.normalize().multiply(visibilityLength))).entries.first()
}