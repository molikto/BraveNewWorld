package org.snailya.bnw

import org.snailya.base.*
import org.snailya.bnw.ui.JoinServerPage
import org.snailya.bnw.ui.GeneralUi

/**
 * BASIC SETUP
 */
class BraveNewWorldWrapper(pdi: PlatformDependentInfo) : ApplicationWrapper({ BraveNewWorld(pdi) })

// our specific singleton
val bnw by lazy { app as BraveNewWorld }

/**
 * REAL THING
 */
class BraveNewWorld(pdi: PlatformDependentInfo) : ApplicationInner(pdi) {

    val ui = GeneralUi()

    init { page = JoinServerPage() }
}

