package org.snailya.bnw.gamelogic

import org.snailya.bnw.ps


class Agent() : Walker() {
    var faction: Int = -1
    // temp variables
    // constants
    val totalLockOnTime = 0.5F
    val maxLockOnDistance = 5F
    val maxHealth = 10F
    var health: Float = maxHealth
    var lockingOnTarget: Agent? = null
    var lockingOnTime = 0F

    @Strictfp
    fun tryFireOrLockOn() {
        if (walking) {
            lockingOnTarget = null
        } else {
            val target = lockingOnTarget
            if (target != null) {
                val distance = target.position.dis(position)
                if (distance <= maxLockOnDistance && game.map.hitWall(position, target.position) == null) {
                    lockingOnTime += 0.2F.ps
                    if (lockingOnTime >= totalLockOnTime) {
                        game.bullets.add(Bullet(faction, position, target.position))
                        lockingOnTarget = null
                        lockingOnTarget = null
                    }
                } else {
                    lockingOnTarget = null
                }
            } else {
                // TODO find target more smart...
                // TODO size of the target???
                for (a in game.agents) {
                    if (a != this) {
                        val distance = a.position.dis(position)
                        if (distance <= maxLockOnDistance) {
                            if (game.map.hitWall(position, a.position) == null) {
                                lockingOnTarget = a
                                lockingOnTime = 0F
                            }
                        }
                    }
                }
            }
        }
    }

    fun takeDamage(fl: Float) {
        health -= fl
    }
}
