package org.snailya.base

/**
 * Created by molikto on 07/08/2017.
 */


inline val Int.dp: Float
    inline get() = app.dpiPixel * this

inline val Float.dp: Float
    inline get() = app.dpiPixel * this
