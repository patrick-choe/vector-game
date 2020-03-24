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

import com.github.patrick.vector.VectorPlugin.Companion.bothHands
import com.github.patrick.vector.VectorPlugin.Companion.lastModified
import com.github.patrick.vector.VectorPlugin.Companion.maxVelocity
import com.github.patrick.vector.VectorPlugin.Companion.singleTime
import com.github.patrick.vector.VectorPlugin.Companion.vectorItem
import com.github.patrick.vector.VectorPlugin.Companion.velocityModifier
import com.github.patrick.vector.VectorPlugin.Companion.visibilityLength
import org.bukkit.Material.getMaterial
import java.io.File

class VectorConfigTask(private val instance: VectorPlugin) : Runnable {
    override fun run() {
        val last = File(instance.dataFolder, "config.yml").lastModified()
        val config = instance.config
        if (last != VectorPlugin.lastModified) {
            lastModified = last
            instance.reloadConfig()
            vectorItem = getMaterial(config.getString("vector-item"))
            bothHands = config.getBoolean("use-both-hands")
            singleTime = config.getBoolean("set-single-time")
            visibilityLength = config.getDouble("visibility-length-double")
            velocityModifier = config.getDouble("velocity-modifier-double")
            maxVelocity = config.getDouble("max-velocity-double")
        }
    }
}