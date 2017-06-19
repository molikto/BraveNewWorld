package org.snailya.bnw.gamelogic

import org.serenaz.Edge
import org.serenaz.InputPoint
import org.serenaz.Point
import org.serenaz.Voronoi
import org.snailya.base.StrictVector2
import org.snailya.base.svec2
import org.snailya.base.time
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.experimental.EmptyCoroutineContext.plus


/**
 * this class use a Voronoi generation algorithm found from GitHub
 * this algorithm is NOT intended to be used in other places
 */
class MapGen(val random: Random, val size: Int) {

    lateinit var debug_edges: List<Edge>
    lateinit var res: List<StrictVector2>

    @Strictfp inline fun clamp(p: Float) = if (p < 0) 0F else if (p > 1) 1F else p

    @Strictfp
    fun gen() {
        var randomDots = ArrayList<InputPoint>()
        for (i in 0 .. size * 3) {
            randomDots.add(InputPoint(random.nextFloat(), random.nextFloat()))
        }
        var voronoi: Voronoi? = null
        for (i in 0 until 4) {
            voronoi = time("generating Voronoi diagram") { Voronoi(randomDots) }
            for (p in voronoi.sites) {
                p.x = 0F
                p.y = 0F
            }
            for (e in voronoi.edges) {
                val x = clamp(e.start.x) + clamp(e.end.x)
                val y = clamp(e.start.y) + clamp(e.end.y)
                e.site_left.x += x
                e.site_right.x += x
                e.site_left.y += y
                e.site_right.y += y
                e.site_left.edgeCount += 1
                e.site_right.edgeCount += 1
            }
            randomDots = ArrayList()
            for (p in voronoi.sites) {
                randomDots.add(InputPoint(p.x / p.edgeCount / 2, p.y / p.edgeCount / 2))
            }
        }
        res = randomDots.map { svec2(it.x * size, it.y * size) }
        debug_edges = voronoi!!.edges
    }

    init {
        gen()
    }
}


