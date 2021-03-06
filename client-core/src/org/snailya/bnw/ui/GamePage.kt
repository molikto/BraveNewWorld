package org.snailya.bnw.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.*
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
import org.nustaq.serialization.FSTObjectInput
import org.nustaq.serialization.FSTObjectOutput
import org.snailya.base.*
import org.snailya.base.app
import org.snailya.base.logging.debug
import org.snailya.base.logging.timed
import org.snailya.bnw.data.PlayerCommand
import org.snailya.bnw.gamelogic.*
import org.snailya.bnw.gamelogic.def.*
import org.snailya.bnw.networking.ServerConnection
import org.snailya.bnw.timePerGameTick
import org.snailya.bnw.timePerTick
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

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

    override fun dispose() {
        unregisterGameSingleton()
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
    val myIndex = c.myIndex
    init {
        // when you want to debug save file across different JVM instance, comment these two lines manually
        // TODO remove this when we have save files
        // debug_loadSaveFileAsGame()
        BnwGame(200, c.playerSize, seed = c.serverGameStartTime)
    }

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
    var focus: Vector2 = game.agents[myIndex].position.vec2()
    var focusSpeed = 10F
    val maxZoom = 33.dp
    val minZoom = 9.dp
    var zoom = 20.dp // how many back buffer width is 1 game meter?

    init {
        inputProcessor = object : BaseInputProcessor() {
            override fun scrolled(amount: Int): Boolean {
                zoom *= (1 + amount.dp / 50)
                zoom = maxOf(minZoom, minOf(maxZoom, zoom))
                // TODO focus speed, slow out when input is gone
                focusSpeed = (40F * Math.sqrt((minZoom / zoom).toDouble())).toFloat()
                return true
            }
        }

    }


    /**
     * rendering
     */
    val projection: Matrix4 = Matrix4()
    val inverseProjection: Matrix4 = Matrix4()
    var top = 0
    var bottom = 0
    var left = 0
    var right = 0

    fun inputGameCoor(x: Int, y: Int) =
            vec2(x.toFloat() * 2 / app.backBufferWidth() - 1, 1 - y.toFloat() * 2 / app.backBufferHeight()).extends().mul(inverseProjection).lose()


    override fun render() {
        // all these functions SHOULD know when they are called
        debug_processSaveAndReloadInput()
        debug_renderDebugUi()
        if (!c.gamePaused) gatherCommands()
        tickGameAndTickMaybePauseNetwork()
        updateGameUi()
        if (c.gamePaused) return
        processLocalInput()
        setupProjection()
        terrain.render()
        waterSurface.render()
        //debug_renderPathFindingResult()
        renderSprites()
        //debug_renderVoronoiDiagram()
    }



    private fun gatherCommands() {
        if (Gdx.input.justTouched()) {
            val dest = inputGameCoor(Gdx.input.x, Gdx.input.y).ivec2()
            if (game.map.inBound(dest)) {
                if (!game.map(dest).noWalk) {
                    commands.add(PlayerCommand(dest))
                }
            }
        }
    }

    /**
     * probably will pause the game
     */
    private fun tickGameAndTickMaybePauseNetwork() {
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
                    confirmedCommands = c.tick(commands, game.debug_hash())
                    networkTickedTime += timePerTick
                    if (c.gamePaused) {
                        // we schedule a resend at next tick timed
                        gameTickedTime += timePerTick
                        break
                    } else {
                        commands.clear()
                    }
                }
                game.tick(confirmedCommands)
                gameTickedTime += timePerGameTick
            } else {
                break
            }
        }
        debug { "game tick $gameTicks, net tick $netTicks" }
    }


    private fun updateGameUi() {
        widgets.paused.isVisible = c.gamePaused
    }


    private fun processLocalInput() {
        val delta = Gdx.graphics.deltaTime
        val direction = vec2(0F, 0F)
        run {
            if (keyed(Input.Keys.W)) direction.add(0F, 1F)
            if (keyed(Input.Keys.S)) direction.add(0F, -1F)
            if (keyed(Input.Keys.A)) direction.add(-1F, 0F)
            if (keyed(Input.Keys.D)) direction.add(1F, 0F)
            direction.nor()
        }
        focus + (direction * (focusSpeed * delta))
    }

    private fun setupProjection() {

        projection.setToOrtho2DCentered(focus.x, focus.y, app.backBufferWidth() / zoom, app.backBufferHeight() / zoom)

        inverseProjection.set(projection)
        inverseProjection.inv()

        val gtl = vec3(-1F, -1F, 0F) * inverseProjection
        val gbr = vec3(1F, 1F, 0F) * inverseProjection


        val margin = 0
        top = Math.max(gtl.y.toInt() - margin, 0)
        left = Math.max(gtl.x.toInt() - margin, 0)
        bottom = Math.min(gbr.y.toInt() + 1 + margin, game.map.size)
        right = Math.min(gbr.x.toInt() + 1 + margin, game.map.size)

    }

    private fun renderSprites() {
        batch.projectionMatrix = projection
        batch.begin()

        for (y in top until bottom) {
            for (x in left until right) {
                val planted = game.map(x, y).planted
                game.map
                when (planted) {
                    is WallLike -> {
                        planted.type.textureAtlas
                    }
                }
            }
        }
        for (agent in game.agents) {
            batch.draw(textures.black, agent.position.x - 0.5F, agent.position.y - 0.5F, 1F, 1F)
            if (agent.lockingOnTarget != null) {
                val lockOnSize = agent.lockingOnTime / agent.totalLockOnTime
                batch.draw(textures.black, agent.position.x - lockOnSize / 2, agent.position.y - lockOnSize / 2, lockOnSize, lockOnSize)
            }
            if (agent.health != agent.maxHealth) {
                batch.color = Color.RED
                batch.draw(textures.white, agent.position.x - 0.5F, agent.position.y - 0.5F, 0.1F, agent.health / agent.maxHealth)
                batch.color = Color.WHITE
            }
        }
        for (bullet in game.bullets) {
            batch.draw(textures.black, bullet.position.x - 0.1F, bullet.position.y - 0.1F, 0.2F, 0.2F)
        }

        batch.end()
    }


    val waterSurface = object : Batched(
            // TODO how to animate WaterSurface?
            shaderOf("WaterSurface"),
            attrs(VertexAttribute(VertexAttributes.Usage.Position, 2, "position"),
                    VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 1, "in_index")),
            maxVertices = 4000,
            texture = textureArrayOf(WatersByDepth.map { it.texture.name }),
            primitiveType = GL20.GL_POINTS
    ) {
        // constants
        val paddingSize = 0.2F // in game coordinate
        val pointSize = 1 + paddingSize * 2

        override fun render() {
            GL11.glPointSize(zoom * pointSize)
            glEnableBlend()
            begin()
            shader.setUniformMatrix("projection", projection)
            shader.setUniformi("texture", 0)
            for (i in 0 until WatersByDepth.size) {
                val t = WatersByDepth[i]
                for (y in top until bottom) {
                    for (x in left until right) {
                        val tile = game.map(x, y)
                        if (tile.planted == t) {
                            put(tile.position.x + 0.5F,
                                    tile.position.y + 0.5F,
                                    i.toFloat())
                        }
                    }
                }
            }
            end()
        }

    }


    val TerrainTextureNames = NaturalTerrainsByGrainSizeInverse.map { it.texture.name }.toSet().toList()
    val TerrainTexturesToNamesIndex = NaturalTerrainsByGrainSizeInverse.map { a -> TerrainTextureNames.indexOfFirst { it == a.texture.name } }

    val terrain = object : Batched(
            shaderOf("Terrain"),
            attrs(VertexAttribute(VertexAttributes.Usage.Position, 2, "position"),
                    VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 1, "in_index"),
                    VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "in_tintColor")
            ),
            maxVertices = 4000,
            texture = textureArrayOf(TerrainTextureNames),
            primitiveType = GL20.GL_POINTS
    ) {

        // constants
        val paddingSize = 0.2F // in game coordinate
        val pointSize = 1 + paddingSize * 2

        override fun render() {
            GL11.glPointSize(zoom * pointSize)
            glEnableBlend()
            begin()
            shader.setUniformMatrix("projection", projection)
            shader.setUniformi("texture", 0)
            val terrainsByOrder = NaturalTerrainsByGrainSizeInverse
            for (i in 0 until terrainsByOrder.size) {
                val t = terrainsByOrder[i]
                val tint = t.texture.color
                for (y in top until bottom) {
                    for (x in left until right) {
                        val tile = game.map(x, y)
                        if (tile.terrain == t && !isDeepWaterSurface(tile.planted)) {
                            put(tile.position.x + 0.5F,
                                    tile.position.y + 0.5F,
                                    TerrainTexturesToNamesIndex[i].toFloat(),
                                    tint)
                        }
                    }
                }
            }
            end()
        }
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

    /**
     * we don't have a fully functioning save-file for multi-player now,
     */
    private fun debug_loadSaveFileAsGame() {
        val file = files.external("debug_save_file")
        val input = ObjectInputStream(file.read())
        registerGameSingleton(input.readObject() as BnwGame)
    }
    private fun debug_processSaveAndReloadInput() {
        if (keyed(Input.Keys.Q)) {
            timed("saving game state and re-loading") {
                val file = files.external("debug_save_file")
                // inspector for java standard serialization
                // https://code.google.com/archive/p/jdeserialize/downloads
                val javaStandard = {
                    // time 861ms, size 1200kb
                    val output = ObjectOutputStream(file.write(false))
                    output.writeObject(game)
                    output.close()
                    unregisterGameSingleton()
                    val input = ObjectInputStream(file.read())
                    registerGameSingleton(input.readObject() as BnwGame)
                }
                val fst = {
                    // time 245ms, size 839kb
                    val output = FSTObjectOutput(file.write(false))
                    output.writeObject(game)
                    output.close()
                    unregisterGameSingleton()
                    val input = FSTObjectInput(file.read())
                    registerGameSingleton(input.readObject() as BnwGame)
                }
                javaStandard.invoke()
            }
        }
    }

    private fun debug_renderDebugUi() {
        widgets.debug_info.setText("FPS: ${graphics.framesPerSecond}\nrender time: ${app.renderTime}")
    }

    val debug_shapeRenderer by lazy { ShapeRenderer() }
    private fun debug_renderVoronoiDiagram() {
        val renderer = debug_shapeRenderer
        renderer.projectionMatrix = projection
        renderer.begin(ShapeRenderer.ShapeType.Line)
        val size = game.map.size
        for (e in game.map.debug_mapGen.debug_edges) {
            renderer.line((e.start.x * size).toFloat(), (e.start.y * size).toFloat(), (e.end.x * size).toFloat(), (e.end.y * size).toFloat())
        }
        renderer.end()
        renderer.begin(ShapeRenderer.ShapeType.Filled)
        val dest = inputGameCoor(Gdx.input.x, Gdx.input.y).ivec2()
        for (p in game.map.debug_mapGen.debug_points) {
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
            if (game.map(dest).debug_inputPoint == p) {
                renderer.color = Color.RED
            }
            renderer.circle(p.x.toFloat(), p.y.toFloat(), 1F)
        }
        renderer.end()
    }


    fun debug_renderPathFindingResult() {

        for (y in top until bottom) {
            for (x in left until right) {
                val tile = game.map(x, y)
                if (game.map.findRoute.debug_counter == tile.temp_visited) {
                    batch.color = Color(1F, 1F, 1F, tile.temp_cost / 30)
                    batch.draw(textures.black, x.toFloat(), y.toFloat(), 1F, 1F)
                    batch.color = Color.WHITE
                }
            }
        }
    }

}


