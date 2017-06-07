package org.snailya.bnw

import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import org.snailya.base.PlatformDependentInfo
import java.lang.Exception

class AndroidLauncher : AndroidApplication() {
    private var wrapper: BraveNewWorldWrapper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        wrapper = BraveNewWorldWrapper(PlatformDependentInfo(null, null))
        initialize(wrapper, config)
    }

    override fun onDestroy() {
        try {
            wrapper?.dispose()
        } catch (e: Exception) {
            // TODO why this throws exception???
            // ignore
        }
        // TODO why this??
        // we do this because we used singletons in our app...
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
