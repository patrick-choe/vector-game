package com.github.patrick.vector

import com.github.noonmaru.math.Vector
import com.github.noonmaru.tap.Tap.MATH
import com.github.noonmaru.tap.entity.TapEntity.wrapEntity
import com.github.patrick.vector.VectorPlugin.Companion.instance
import com.github.patrick.vector.VectorPlugin.Companion.maxVelocity
import com.github.patrick.vector.VectorPlugin.Companion.selectedEntities
import com.github.patrick.vector.VectorPlugin.Companion.singleTime
import com.github.patrick.vector.VectorPlugin.Companion.velocityModifier
import com.github.patrick.vector.VectorPlugin.Companion.visibilityLength
import org.bukkit.Location
import org.bukkit.Material
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

    /**
     * This private method will set the velocity of the entity depending on
     * the distance between the entity and the target's location
     *
     * @return  if removed successfully, it returns true, else, it returns null
     */
    fun Player.setTargetVelocity(): Boolean? {
        selectedEntities[this]?.let {
            val vector = player.getTarget().subtract(it.location).toVector()
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
        val loc = this.eyeLocation
        val view = loc.clone().add(loc.clone().direction.normalize().multiply(visibilityLength))
        var found: Entity? = null
        var distance = 0.0

        this.world.entities?.forEach { entity ->
            if (entity != this)
                wrapEntity(entity)?.boundingBox?.expand(5.0)
                    ?.calculateRayTrace(Vector(loc.x, loc.y, loc.z), Vector(view.x, view.y, view.z))?.let {
                        val currentDistance = loc.distance(entity.location)
                        if (currentDistance < distance || distance == 0.0) {
                            distance = currentDistance
                            found = entity
                        }
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
        val loc = eyeLocation.clone()
        val view = loc.clone().add(loc.clone().direction.normalize().multiply(visibilityLength))
        val block =
            MATH.rayTraceBlock(loc.world, Vector(loc.x, loc.y, loc.z), Vector(view.x, view.y, view.z), 0)
                ?: return loc.clone().add(eyeLocation.direction.clone().normalize().multiply(visibilityLength))
        block.blockPoint.let {
            return loc.world.getBlockAt(it.x, it.y, it.z).getRelative(block.face).location.add(0.5, 0.5, 0.5)
        }
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
                File(dataFolder, "config.yml").delete()
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
     * Gets a new random integer value between 0 and 255,
     * and converts it to double
     *
     * @return  [Double] random value
     */

    private fun getCurrentConfig(args: Array<out String>, sender: CommandSender) = try {
        val path = File(dataFolder, "config.yml").toPath()
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
            val path = File(dataFolder, "config.yml").toPath()
            val lines = Files.readAllLines(path, StandardCharsets.UTF_8)
            for (i in 0 until lines.count()) {
                if (lines[i].contains(args[1])) { when {
                    args[1].contains("double") -> lines[i] = "${args[1]}: ${args[2].toDouble()}"
                    args[1].contains("item") -> {
                        Material.getMaterial(args[2].toUpperCase()) ?: sender.unrecognizedMessage("key", args[2])
                        lines[i] = "${args[1]}: ${args[2].toUpperCase()}"
                    }
                    args[2].matches(Regex("true|false")) -> lines[i] = "${args[1]}: ${args[2]}"
                    else -> sender.unrecognizedMessage("value", args[2]).also { return }
                }
                    sender.sendMessage(lines[i])
                }
            }
            Files.write(path, lines, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            when (e) {
                is IOException -> logger.info("Cannot read/write to config.yml")
                is NumberFormatException -> sender.unrecognizedMessage("value", args[2])
            }
        }
    }
}