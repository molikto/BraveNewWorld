package org.snailya.bnw.gamelogic

import org.serenaz.Edge
import org.serenaz.Point
import org.serenaz.Voronoi
import org.snailya.base.StrictVector2
import org.snailya.base.svec2
import org.snailya.base.time
import java.util.*
import kotlin.collections.ArrayList


/**
 * this class use a Voronoi generation algorithm found from GitHub
 * this algorithm is NOT intended to be used in other places
 */
class MapGen(random: Random, size: Int) {

    val debug_edges: List<Edge>
    val res: List<StrictVector2>
    init {
        var randomDots = ArrayList<Point>()
        for (i in 0 .. size * 2) {
            randomDots.add(Point(random.nextFloat(), random.nextFloat()))
        }
        var voronoi: Voronoi? = null
        for (i in 0 until 5) {
            voronoi = time("generating Voronoi diagram") { Voronoi(randomDots) }
            for (e in voronoi.edges) {
                e.site_left.x += e.start.x
            }
            randomDots = ArrayList()
            for (p in voronoi.sites) {
                p.x = 0F
                p.y = 0F
            }
        }
        res = randomDots.map { svec2(it.x * size, it.y * size) }
        debug_edges = voronoi!!.edges
    }
}


