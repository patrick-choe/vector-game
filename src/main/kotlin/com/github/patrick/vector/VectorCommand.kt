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

import com.github.patrick.vector.VectorUtils.configCommand
import com.github.patrick.vector.VectorUtils.getKeys
import com.github.patrick.vector.VectorUtils.unrecognizedMessage
import org.bukkit.Bukkit.broadcastMessage
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.Bukkit.getScheduler
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.event.HandlerList.unregisterAll
import org.bukkit.plugin.java.JavaPlugin
import kotlin.streams.toList

/**
 * This class manages vector commands for this plugin.
 * When the command starting with '/vector' is called,
 * this class handles the command.
 */
class VectorCommand(private val instance: VectorPlugin): CommandExecutor, TabCompleter {
    private var status = false

    /**
     * When the command starting with '/vector' is executed,
     * below codes are executed.
     */
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) when {
            args[0].contains("help", true) -> sender
                .sendMessage("\n \n \n/vector -> Toggles vector feature\n/vector config <key|reset> [value] -> Updates plugin.yml\n")
            args[0].resetRegexMatch() && sender.hasPermission("command.vector.config") -> configCommand(args, sender)
            else -> sender.unrecognizedMessage("args", args[0])
        } else if (sender.hasPermission("command.vector.toggle")) {
            if (!status) {
                getPluginManager().registerEvents(VectorEventListener(), instance)
                getScheduler().runTaskTimer(instance, VectorParticleTask(), 0, 1)
                broadcastMessage("Vector On")
            } else {
                unregisterAll(instance as JavaPlugin)
                getScheduler().cancelTasks(instance)
                broadcastMessage("Vector Off")
            }
            status = !status
        }
        return true
    }

    /**
     * When the players use 'tab' key to get suggestion,
     * below codes are executed.
     */
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>) =
        when (args.size) {
            1 -> listOf("config", "help").filter(args[0])
            2 -> if (args[0].resetRegexMatch()) getKeys().filter(args[1]) else emptyList()
            3 -> if (args[0].resetRegexMatch() && getKeys().contains(args[1])) when {
                args[1].contains("item", true) ->
                    Material.values().toList().stream().map(Material::name).toList().filter(args[2])
                !args[1].contains("double", true) -> listOf("true", "false").filter(args[2])
                else -> emptyList()
            } else emptyList()
            else -> emptyList()
        }


    private fun String.resetRegexMatch() = contains(Regex("(?i)conf|set"))
    private fun List<String>.filter(key: String) = filter { it.startsWith(key, true) }
}