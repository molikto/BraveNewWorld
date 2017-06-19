package org.snailya.bnw.gamelogic

import org.ajwerner.voronoi.Point
import org.ajwerner.voronoi.Voronoi
import org.ajwerner.voronoi.VoronoiEdge
import org.snailya.base.StrictVector2
import org.snailya.base.svec2
import java.util.*
import kotlin.collections.ArrayList


/**
 * this class use a Voronoi generation algorithm found from GitHub
 * this algorithm is NOT intended to be used in other places
 */
class MapGen(random: Random, size: Int) {

    val debug_edges: List<VoronoiEdge>
    val res: List<StrictVector2>
    init {
        Voronoi.MAX_DIM = size.toFloat()
        Voronoi.MIN_DIM = 0F
        var randomDots = ArrayList<Point>()
        for (i in 0 .. size * 2) {
            randomDots.add(Point(1 + random.nextFloat() * (size - 2), 1 + random.nextFloat() * (size - 2)))
        }
        var voronoi: Voronoi? = null
        voronoi = Voronoi(randomDots)
//        for (i in 0 until 5) {
//            randomDots = ArrayList()
//        }
        res = randomDots.map { svec2(it.x, it.y) }
        debug_edges = voronoi.edgeList
    }
}


object MapGenTest {
    @JvmStatic fun main(args: Array<String>) {
        Voronoi.MAX_DIM = 10F
        Voronoi.MIN_DIM = 0F
        var randomDots = ArrayList<Point>()
        // a vertical line
//        randomDots.add(Point(1F, 5F))
//        randomDots.add(Point(9F, 5F))

        // a h line
        randomDots.add(Point(5F, 1F))
        randomDots.add(Point(5F, 9F))
        val voronoi = Voronoi(randomDots)
        val e = voronoi.edgeList;
    }
}
