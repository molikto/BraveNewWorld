package org.snailya.bnw.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.gl20
import com.badlogic.gdx.Gdx.graphics
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import ktx.math.*
import ktx.scene2d.*
import org.lwjgl.opengl.GL11
import org.snailya.base.*
import org.snailya.bnw.PlayerCommand
import org.snailya.bnw.gamelogic.BnwGame
import org.snailya.bnw.gamelogic.Terrain
import org.snailya.bnw.networking.ServerConnection
import org.snailya.bnw.timePerGameTick
import org.snailya.bnw.timePerTick

/**
 *
 * a normal game loop will be like:
 *
 * get and apply input
 * update game state
 * render
 *
 * a networked game loop will be:
 *
 */



class GamePage(val c: ServerConnection) : Page() {

    /**
     * textures
     *
     * game textures should be later defined in data files, not here
     */
    object textures {
        /**
         * currently a texture contains 16 tiles...
         */
        val black = textureOf("black")
        val white = textureOf("white")
    }


    object gl {
        object terrain {
            val shader = shaderOf("terrain")

            val textureArray = textureArrayOf(Terrain.values().map { "Terrain/${it.name}" })

            val cache = FloatArray(3000)
            val mesh = Mesh(false, 1000, 0,
                    VertexAttribute(VertexAttributes.Usage.Position, 2, "position"),
                    VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 1, "v_terrain"))

        }
    }

    override fun dispose() {
        // TODO textures
    }




    /**
     * ui
     */

    object widgets {
        lateinit var paused: Label
        lateinit var debug_info: Label
    }
    init {
        ui = stack {
            table {
                widgets.paused = label("paused")
            }
            table {
                align(Align.topLeft)
                widgets.debug_info = label("").cell(align = Align.topLeft)
            }
        }
    }

    /**
     * game simulation
     */
    val g = BnwGame(c.myIndex, c.playerSize, c.serverGameStartTime)

    /**
     * commands buffer
     */
    val commands = mutableListOf<PlayerCommand>()


    /**
     * ticks
     */
    var gameTickedTime = c.gameStartTime
    var networkTickedTime: Long = c.gameStartTime


    /**
     * game ui
     */
    var focus: Vector2 = g.agents[g.myIndex].position.vec2()
    var focusSpeed = 10F
    val maxZoom = 33.dp
    val minZoom = 9.dp
    var zoom = 20.dp // how many back buffer width is 1 game meter?

    init {
        inputProcessor = object : BaseInputProcessor() {
            override fun scrolled(amount: Int): Boolean {
                zoom *= (1 + amount.dp / 50)
                zoom = maxOf(minZoom, minOf(maxZoom, zoom))
                // TODO focus speed, snapping
                focusSpeed = (40F * Math.sqrt((minZoom / zoom).toDouble())).toFloat()
                return true
            }
        }

    }


    /**
     * rendering
     */
    val projection: Matrix4 = Matrix4()
    val inverseProjection : Matrix4 = Matrix4()
    var top = 0
    var bottom = 0
    var left = 0
    var right = 0

    fun inputGameCoor(x: Int, y: Int) =
            vec2(x.tf * 2 / game.backBufferWidth() - 1, 1 - y.tf * 2 / game.backBufferHeight()).extends().mul(inverseProjection).lose()


    override fun render() {
        // all these functions SHOULD know when they are called
        debug_renderDebugUi()
        if (!c.gamePaused) render_gatherCommands()
        render_tickGameAndNetwork()
        render_updateGameUi()
        if (c.gamePaused) return
        render_processLocalInput()
        render_setupProjection()
        render_terrain()
        //debug_renderPathFindingResult()
        render_sprites()
        //debug_renderVoronoiDiagram()
    }



    private fun render_gatherCommands() {
        if (Gdx.input.justTouched()) {
            val dest = inputGameCoor(Gdx.input.x, Gdx.input.y).ivec2()
            if (g.map.inBound(dest)) {
                if (!g.map(dest).notWalk) {
                    commands.add(PlayerCommand(dest))
                }
            }
        }
    }

    /**
     * probably will pause the game
     */
    private fun render_tickGameAndNetwork() {
        val time = System.currentTimeMillis()
        if (c.gamePaused && c.received != null) {
            gameTickedTime = c.receivedTime - timePerGameTick
            networkTickedTime = c.receivedTime - timePerTick
        }
        var gameTicks = 0
        var netTicks = 0
        while (true) {
            val toTime = gameTickedTime + timePerGameTick
            if (toTime <= time) {
                gameTicks += 1
                var confirmedCommands: List<List<PlayerCommand>>? = null
                if (networkTickedTime + timePerTick == toTime) {
                    netTicks += 1
                    confirmedCommands = c.tick(commands, g.debug_hash())
                    networkTickedTime += timePerTick
                    if (c.gamePaused) {
                        // we schedule a resend at next tick timed
                        gameTickedTime += timePerTick
                        break
                    } else {
                        commands.clear()
                    }
                }
                g.tick(confirmedCommands)
                gameTickedTime += timePerGameTick
            } else {
                break
            }
        }
        // info { "game tick $gameTicks, net tick $netTicks" }
    }


    private fun render_updateGameUi() {
        widgets.paused.isVisible = c.gamePaused
    }


    private fun render_processLocalInput() {
        run {
            val delta = Gdx.graphics.deltaTime
            val direction = vec2(0F, 0F)
            run {
                fun keyed(i: Int) = Gdx.input.isKeyPressed(i)
                if (keyed(Input.Keys.W)) direction.add(0F, 1F)
                if (keyed(Input.Keys.S)) direction.add(0F, -1F)
                if (keyed(Input.Keys.A)) direction.add(-1F, 0F)
                if (keyed(Input.Keys.D)) direction.add(1F, 0F)
                direction.nor()
            }
            focus + (direction * (focusSpeed * delta))
        }
    }

    private fun render_setupProjection() {

        projection.setToOrtho2DCentered(focus.x, focus.y, game.backBufferWidth() / zoom, game.backBufferHeight() / zoom)

        inverseProjection.set(projection)
        inverseProjection.inv()

        val gtl = vec3(-1F, -1F, 0F) * inverseProjection
        val gbr = vec3(1F, 1F, 0F) * inverseProjection


        val margin = 0
        top = Math.max(gtl.y.toInt() - margin, 0)
        left = Math.max(gtl.x.toInt() - margin, 0)
        bottom = Math.min(gbr.y.toInt() + 1 + margin, g.map.size)
        right = Math.min(gbr.x.toInt() + 1 + margin, g.map.size)

    }

    private fun render_sprites() {
        batch.projectionMatrix = projection
        batch.begin()

        for (agent in g.agents) {
            batch.draw(textures.black, agent.position.x - 0.5F, agent.position.y - 0.5F, 1F, 1F)
            if (agent.lockingOnTarget != null) {
                val lockOnSize = agent.lockingOnTime / agent.totalLockOnTime
                batch.draw(textures.black, agent.position.x - lockOnSize/2, agent.position.y - lockOnSize/2, lockOnSize, lockOnSize)
            }
            if (agent.health != agent.maxHealth) {
                batch.color = Color.RED
                batch.draw(textures.white, agent.position.x - 0.5F, agent.position.y - 0.5F, 0.1F, agent.health / agent.maxHealth)
                batch.color = Color.WHITE
            }
        }
        for (bullet in g.bullets) {
            batch.draw(textures.black, bullet.position.x - 0.1F, bullet.position.y - 0.1F, 0.2F, 0.2F)
        }

        batch.end()
    }

    private fun render_terrain() {
        // constants
        val paddingSize = 0.2F // in game coordinate
        val pointSize = 1 + paddingSize * 2

        val shader = gl.terrain.shader
        val textureArray = gl.terrain.textureArray
        val mesh = gl.terrain.mesh
        val cache = gl.terrain.cache

        GL11.glPointSize(zoom * pointSize)
        gl20.glEnable(GL20.GL_BLEND)
        gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
//            gl20.glClearColor(0F, 0F, 0F, 0F)
//            gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_BLEND_SRC_ALPHA)
        shader.begin()
        textureArray.bind()
        shader.setUniformMatrix("projection", projection)
        shader.setUniformi("texture", 0)

        var index = 0
        for (y in top until bottom) {
            for (x in left until right) {
                val tile = g.map(x, y)
                cache[index++] = tile.position.x + 0.5F
                cache[index++] = tile.position.y + 0.5F
                cache[index++] = tile.terrain.ordinal.toFloat()
                if (index == cache.size)  {
                    mesh.setVertices(cache, 0, index)
                    mesh.render(shader, GL20.GL_POINTS)
                    index = 0
                }
            }
        }
        mesh.setVertices(cache, 0, index)
        mesh.render(shader, GL20.GL_POINTS)
        index = 0

        shader.end()
    }


    /**
     *
     *
     *
     *
     * debug methods
     *
     *
     *
     *
     *
     */

    private fun debug_renderDebugUi() {
        widgets.debug_info.setText("FPS: ${graphics.framesPerSecond}\nrender time: ${game.renderTime}")
    }

    val shapeRenderer = ShapeRenderer()
    private fun debug_renderVoronoiDiagram() {
        val renderer = shapeRenderer
        renderer.projectionMatrix = projection
        renderer.begin(ShapeRenderer.ShapeType.Line)
        val size = g.map.size
        for (e in g.map.debug_mapGen.debug_edges) {
            renderer.line((e.start.x * size).toFloat(), (e.start.y * size).toFloat(), (e.end.x * size).toFloat(), (e.end.y * size).toFloat())
        }
        renderer.end()
        renderer.begin(ShapeRenderer.ShapeType.Filled)
        val dest = inputGameCoor(Gdx.input.x, Gdx.input.y).ivec2()
        for (p in g.map.debug_mapGen.debug_points) {
            if (p.attachment.isSea) {
                renderer.color = Color.BLUE
            } else if (p.attachment.isBeach) {
                renderer.color = Color.BROWN
            } else if (p.attachment.height < 2) {
                renderer.color = Color.GREEN
            } else if (p.attachment.height < 3) {
                renderer.color = Color.DARK_GRAY
            } else if (p.attachment.height < 4) {
                renderer.color = Color.GRAY
            } else {
                renderer.color = Color.WHITE
            }
            if (g.map(dest).debug_inputPoint == p) {
                renderer.color = Color.RED
            }
            renderer.circle(p.x.toFloat(), p.y.toFloat(), 1F)
        }
        renderer.end()
    }


    fun debug_renderPathFindingResult() {

        for (y in top until bottom) {
            for (x in left until right) {
                val tile = g.map(x, y)
                if (g.findRoute.counter == tile.temp_visited) {
                    batch.color = Color(1F, 1F, 1F, tile.temp_cost / 30)
                    batch.draw(textures.black, x.tf, y.tf, 1F, 1F)
                    batch.color = Color.WHITE
                }
            }
        }
    }

}


