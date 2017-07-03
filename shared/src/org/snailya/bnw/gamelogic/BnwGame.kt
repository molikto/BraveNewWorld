package org.snailya.bnw.gamelogic

import org.snailya.base.*
import org.snailya.bnw.*
import java.util.*
import org.snailya.bnw.gamelogic.TryWalkMethod.tryWalk
import java.io.Serializable


private var _game: BnwGame? = null

val game by lazy { _game!! }


fun registerGameSingleton(game: BnwGame) {
    assert(_game == null)
    _game = game
}

fun unregisterGameSingleton() {
    assert(_game != null)
    _game = null
}

/**
 * the state of the entire game can be serialized.
 *
 * we are currently using Java standard Serializable.
 *
 * also we are using a global variable as the game state singleton,
 * and this singleton is accessed in sub-states's initialization phrase
 *
 * but this is not the case when the object graph is deserialized
 *
 * so you need to call [registerGameSingleton] when you load a game from save file
 */
class BnwGame(val myIndex: Int, playerSize: Int, val seed: Long) : Serializable {

    // singleton so you don't need to pass them around, though, this means that you only call it when it is valid
    init {
        registerGameSingleton(this)
    }
    /**
     *
     * basic
     */
    val random = Random(seed)
    val map = Map()
    val bullets = mutableListOf<Bullet>()

    val masterMinds = (0 until playerSize).map { MasterMind() }

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
            assert(commands.size == masterMinds.size)
            for (i in 0 until masterMinds.size) {
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





