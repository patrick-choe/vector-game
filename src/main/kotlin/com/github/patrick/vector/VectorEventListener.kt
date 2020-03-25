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
import com.github.patrick.vector.VectorUtils.newEntityRayTrace
import com.github.patrick.vector.VectorUtils.setTargetVelocity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.*
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
}