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


    @Strictfp
    fun gen() {
        var randomDots = ArrayList<InputPoint>()
        val nPoints = size * size / 10
        for (i in 0 .. nPoints) {
            randomDots.add(InputPoint(random.nextDouble(), random.nextDouble()))
        }
        var voronoi: Voronoi? = null
        for (i in 0 until 3) {
            voronoi = time("generating Voronoi diagram") { Voronoi(randomDots) }
            for (p in voronoi.sites) {
                p.x = 0.0
                p.y = 0.0
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
        val v = voronoi!!
        for (p in v.sites) {
            p.attachment = InputPoint.Attachment()
        }
        // set all edge points to sea
        for (e in v.edges) {
            e.site_left.attachment.edges.add(e)
            e.site_right.attachment.edges.add(e)
            if (e.isSea()) {
                e.site_left.attachment.isSea = true
                e.site_right.attachment.isSea = true
            }
        }
        // randomly set some beach points to sea
        v.sites.filter { !it.attachment.isSea && it.attachment.nearSea() }
                .forEach { if (random.nextInt(3) == 1) it.attachment.isSea = true }
        v.sites.filter { !it.attachment.isSea && it.attachment.nearSea() }
                .forEach { it.attachment.isBeach = true }
        // generate biome
        res = randomDots.map { svec2((it.x * size).toFloat(), (it.y * size).toFloat()) }
        debug_edges = v.edges
    }

    init {
        gen()
    }
}

@Strictfp inline fun clamp(p: Double) = if (p < 0) 0.0 else if (p > 1.0) 1.0 else p
@Strictfp inline fun outside(p: Point) = p.x <= 0 || p.y <= 0 || p.x >= 1 || p.y >= 1

fun Edge.isSea() =  outside(this.start) || outside(this.end)
