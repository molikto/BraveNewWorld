package org.snailya.bnw.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import org.snailya.base.PlatformDependentInfo
import org.snailya.bnw.BraveNewWorldWrapper

object DesktopLauncher {
    @JvmStatic fun main(arg: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration()
//        val width = 736
//        val height = 410
        val width = 100
        val height = 100
        config.setWindowedMode(width, height)
        config.setHdpiMode(Lwjgl3ApplicationConfiguration.HdpiMode.Pixels)
        Lwjgl3Application(BraveNewWorldWrapper(PlatformDependentInfo(null, width)), config)
    }
}
