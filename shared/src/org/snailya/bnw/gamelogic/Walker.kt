package org.snailya.bnw.gamelogic

import org.snailya.base.*
import org.snailya.bnw.ps
import java.io.Serializable


// we do these is so that we have a "local static" temp variable
object TryWalkMethod {
    // temp
    val pos = svec2()

    @Strictfp fun Walker.tryWalk() {
        // TODO re-plan when game structure changes
        if (!route.isEmpty()) {
            val id = route.last()
            id.center(pos)
            pos - position
            val dis = pos.len()
            val time = dis / speed
            if (time < 1) { // we can finish this dis
                // move the player to id
                id.center(position)
                // remove last
                route.removeAt(route.size - 1)
                if (!route.isEmpty()) {
                    // I don't think... we don't never have a situation where a creature can move multiple tiles per tick
                    val nid = route.last()
                    nid.center(pos)
                    pos - position
                    position + pos.nor() * speed * (1 - time)
                }
            } else {
                position + pos.nor() * speed
            }
        }
    }
}

open class Walker : Serializable {
    var speed = 1F.ps

    lateinit var position: SVector2
    val size = 0.5F // TODO now all stuff is actually round!
    val route = mutableListOf<Tile>()
    inline val walking: Boolean
        get() = !route.isEmpty()

    fun findRoute(dest: IntVector2) {
        timed("finding route") { game.map.findRoute(position, dest, route) }
    }

    @Strictfp
    fun intersects(from: SVector2, to: SVector2): Float? {
        val dis = pointToLineDistance(from, to, position)
        if (dis < size) {
            val d2 = pointToLineSegmentDistance(from, to, position)
            if (d2 < size) {
                // the reason we return dis is because this is where the line will hit most deep
                return dis / size
            } else {
                return null
            }
        } else {
            return null
        }
    }
}
