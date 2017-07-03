package org.snailya.bnw.gamelogic

import org.snailya.base.*
import org.snailya.bnw.ps
import java.io.Serializable


class Bullet(val shooterFaction: Int, val initial: SVector2, to: SVector2) : Serializable {
    val temp_pos: SVector2 = svec2()
    val maxLifetime = 1F
    var lifetime = maxLifetime
    val speed = 10F.ps
    val position: SVector2 = initial.copy()
    val positionTick = (to.copy() - initial).nor() * speed

    @Strictfp
    fun fly() {
        assert(lifetime > 0)
        temp_pos.set(position)
        position + positionTick
        // TODO hit wall
        if (game.map.hitWall(temp_pos, position) == null) {
            for (a in game.agents) {
                if (a.faction == shooterFaction && initial.dis(position) <1.3F) {
                    // ignore this
                } else {
                    val inter = a.intersects(temp_pos, position)
                    if (inter != null) {
                        a.takeDamage((1 - inter) * 2F)
                        lifetime = 0F
                    }
                }
            }
        } else {
            lifetime = 0F
        }
        lifetime -= 1F.ps
    }
}
