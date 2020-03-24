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
import com.github.noonmaru.tap.entity.TapEntity.wrapEntity
import com.github.patrick.vector.VectorPlugin.Companion.bothHands
import com.github.patrick.vector.VectorPlugin.Companion.getTarget
import com.github.patrick.vector.VectorPlugin.Companion.maxVelocity
import com.github.patrick.vector.VectorPlugin.Companion.selectedEntities
import com.github.patrick.vector.VectorPlugin.Companion.singleTime
import com.github.patrick.vector.VectorPlugin.Companion.velocityModifier
import com.github.patrick.vector.VectorPlugin.Companion.visibilityLength
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.LEFT_CLICK_AIR
import org.bukkit.event.block.Action.LEFT_CLICK_BLOCK
import org.bukkit.event.block.Action.RIGHT_CLICK_AIR
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent

/**
 * This class listens to the 'PlayerInteractEvent', which represents the
 * player interacting with air/block.  When the listener listens to the
 * event, depending on the configuration, this will execute a specific
 * action.
 */
class VectorEventListener : Listener {
    /**
     * This annotated method will listen to the 'PlayerInteractEvent'.
     */
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val action = event.action
        if (event.item?.type == VectorPlugin.vectorItem && player.hasPermission("command.vector.use")) {
            if (setOf(RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK).contains(action))
                if (!bothHands) player.setTargetVelocity() ?: player.newEntityRayTrace() else player.newEntityRayTrace()
            if (setOf(LEFT_CLICK_AIR, LEFT_CLICK_BLOCK).contains(action) && bothHands) player.setTargetVelocity()
            event.isCancelled = true
        }
    }

    /**
     * This private method will set the velocity of the entity depending on
     * the distance between the entity and the target's location
     *
     * @return  if removed successfully, it returns true, else, it returns null
     */
    private fun Player.setTargetVelocity(): Boolean? {
        selectedEntities[this]?.let {
            val vector = player.getTarget().subtract(it.location).toVector()
            it.velocity = if (vector.length() < maxVelocity / velocityModifier) vector.multiply(velocityModifier) else
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
    private fun Player.newEntityRayTrace() {
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
}