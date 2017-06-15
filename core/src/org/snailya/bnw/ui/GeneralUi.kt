package org.snailya.bnw.ui

import com.badlogic.gdx.graphics.Color
import ktx.scene2d.Scene2DSkin
import ktx.style.label
import ktx.style.skin
import ktx.style.textButton
import ktx.style.textField
import org.snailya.base.dp
import org.snailya.base.fontGenerator
import org.snailya.base.ofSize

/**
 * Created by molikto on 14/06/2017.
 */



class GeneralUi() {
    val RobotoMono = fontGenerator("RobotoMono-Regular")
    val RobotoMono14 = RobotoMono.ofSize(14.dp)

    val defaultSkin = skin {
        label {
            font = RobotoMono14
            fontColor = Color.WHITE
        }
        textButton {
            font = RobotoMono14
            fontColor = Color.WHITE
        }
        textField {
            font = RobotoMono14
            fontColor = Color.WHITE
        }
    }

    init { Scene2DSkin.defaultSkin = defaultSkin }
}
