package org.snailya.bnw.gamelogic

import org.snailya.base.*
import org.snailya.bnw.*
import java.util.*


var _game: BnwGame? = null

val game by lazy { _game!! }

class BnwGame(val myIndex: Int, val playerSize: Int, seed: Long) {

    // singleton so you don't need to pass them around, though, this means that you only call it when it is valid
    init {
        assert(_game == null)
        _game = this
    }

    fun dispose() {
        _game = null
    }
    /**
     *
     * basic
     */
    val random = Random(seed)
    val map = Map()
    val bullets = mutableListOf<Bullet>()

    // TODO this is native
    val agents = (0 until playerSize).map { index ->
        configured(Agent()) { faction = index; position = svec2(0.5F, 0.5F) }
    }


    init {
        for (a in agents) {
            while (true) {
                val t = map.randomTile()
                if (!t.nonWalkable) {
                    t.center(a.position)
                    break
                }
            }
        }
    }

    /**
     * tick! tick!
     */
    fun tick(commands: List<List<PlayerCommand>>?) {
        if (commands != null) {
            assert(commands.size == playerSize)
            for (i in 0 until playerSize) {
                val agent = agents[i]
                val cs = commands[i]
                for ((dest) in cs) {
                    agent.findRoute(dest)
                }
            }
        }
        for (b in bullets) {
            b.fly()
        }
        bullets.removeAll { it.lifetime <= 0 }
        for (a in agents) {
            a.tryFireOrLockOn()
            a.tryWalk()
        }
    }


    /**
     *
     *
     * debug
     *
     *
     */
    fun  debug_hash(): Int {
        return agents.map { it.position.hashCode() }.sum()
    }
}





