package com.github.patrick.vector.v1_15_R1

import com.github.patrick.vector.VectorPlugin.Companion.hitBoxExpansion
import com.github.patrick.vector.VectorPlugin.Companion.visibilityLength
import org.bukkit.FluidCollisionMode.NEVER
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.util.Vector

/**
 * This object manages Bukkit-supported ray tracing methods for this plugin
 */
object BukkitSupport {
    /**
     * This method checks whether an entity's bounding box passes the
     * line between player's location and player's target location.
     *
     * @param   loc the location of a player
     * @param   view the location of a player's eye target
     * @return  true if the entity passes the line
     */
    fun Entity.newBukkitRayTraceEntity(loc: Location, view: Location) = boundingBox.expand(
        (hitBoxExpansion - 1) / 2 * boundingBox.widthX,
        (hitBoxExpansion - 1) / 2 * boundingBox.height,
        (hitBoxExpansion - 1) / 2 * boundingBox.widthZ
    ).rayTrace(loc.toVector(), view.toVector(), visibilityLength) != null

    /**
     * This method gets the block in front of the eye target
     * of a player.
     *
     * @param   loc the location of a player
     * @param   view the location of a player's eye target
     * @return  the block in front of the eye target
     */
    fun newBukkitRayTraceBlock(loc: Location, view: Vector): Block? {
        val block = loc.world?.rayTraceBlocks(loc, view, visibilityLength, NEVER, true)?: return null
        block.hitBlock?.let {
            return block.hitBlockFace?.let { face -> it.getRelative(face) }
        }?: return null
    }
}