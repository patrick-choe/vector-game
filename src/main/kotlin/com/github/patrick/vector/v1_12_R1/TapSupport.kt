package com.github.patrick.vector.v1_12_R1

import com.github.noonmaru.math.Vector
import com.github.noonmaru.tap.Tap.MATH
import com.github.noonmaru.tap.entity.TapEntity.wrapEntity
import com.github.patrick.vector.VectorPlugin.Companion.hitBoxExpansion
import com.github.patrick.vector.VectorPlugin.Companion.visibilityLength
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity

/**
 * This object manages Tap-supported ray tracing methods for this plugin
 */
object TapSupport {
    /**
     * This method checks whether an entity's bounding box passes the
     * line between player's location and player's target location.
     *
     * @param   loc the location of a player
     * @param   view the location of a player's eye target
     * @return  true if the entity passes the line
     */
    fun Entity.newTapRayTraceEntity(loc: Location, view: Location): Boolean {
        wrapEntity(this)?.boundingBox?.let {
            it.expand(
                (hitBoxExpansion - 1) / 2 * (it.maxX - it.minX),
                (hitBoxExpansion - 1) / 2 * (it.maxY - it.minY),
                (hitBoxExpansion - 1) / 2 * (it.maxZ - it.minZ)
            )?.calculateRayTrace(Vector(loc.x, loc.y, loc.z), Vector(view.x, view.y, view.z))?.let {
                if (location.distance(view) > visibilityLength) return false
                return true
            }
        }
        return false
    }

    /**
     * This method gets the block in front of the eye target
     * of a player.
     *
     * @param   loc the location of a player
     * @param   view the location of a player's eye target
     * @return  the block in front of the eye target
     */
    fun newTapRayTraceBlock(loc: Location, view: Location): Block? {
        val block =
            MATH.rayTraceBlock(loc.world, Vector(loc.x, loc.y, loc.z), Vector(view.x, view.y, view.z), 0)?: return null
        block.blockPoint?.let {
            if (Location(loc.world, it.x.toDouble(), it.y.toDouble(), it.z.toDouble()).distance(loc) > visibilityLength)
                return null
            return loc.world?.getBlockAt(it.x, it.y, it.z)?.getRelative(block.face)
        }?: return null
    }
}