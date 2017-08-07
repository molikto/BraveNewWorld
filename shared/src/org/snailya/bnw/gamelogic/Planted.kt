package org.snailya.bnw.gamelogic


/**
 * indicate something that touches the ground,
 */
interface Planted {
    val walkM: Float
    val sightM: Float // 1 means total sight, 0 means none, used for bullet calculations
    val coverage: Float
}

