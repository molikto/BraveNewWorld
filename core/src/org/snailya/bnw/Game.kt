package org.snailya.bnw

import com.badlogic.gdx.math.Vector2
import ktx.math.*
import ktx.scene2d.splitPane

/**
 * Created by molikto on 08/06/2017.
 */

class Game {
    // constants
    val speed = 1F

    // states
    val position = vec2(0F, 0F)

    // updates
    fun move(direction: Vector2, time: Float) {
        position + direction * (speed * time)
    }
}
