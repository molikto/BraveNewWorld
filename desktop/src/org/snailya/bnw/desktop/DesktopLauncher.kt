package org.snailya.bnw.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.log.info
import org.snailya.base.PlatformDependentInfo
import org.snailya.base.logger
import org.snailya.bnw.BraveNewWorldWrapper

object DesktopLauncher {
    @JvmStatic fun main(arg: Array<String>) {
        // TODO this should be in every launcher, but we currently only using desktop...
        logger = {s -> info{s}}
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle("Brave New World")
        config.useOpenGL3(true, 3, 3)
        //config.setIdleFPS(120)
        val width = 736
        val height = 410
//        val width = 100
//        val height = 100
        config.setWindowedMode(width, height)
        config.setHdpiMode(Lwjgl3ApplicationConfiguration.HdpiMode.Pixels)
        Lwjgl3Application(BraveNewWorldWrapper(PlatformDependentInfo(null, width)), config)
    }
}
