package com.nyaa.saizeriya

import android.view.animation.Interpolator
import kotlin.math.pow
import kotlin.math.sin

class Easing {
    class EaseOutElasticInterpolator : Interpolator {
        override fun getInterpolation(t: Float): Float {
            val c4 = (2 * Math.PI) / 3
            return if (t == 0f) {
                0f
            } else if (t == 1f) {
                1f
            } else {
                (2.0).pow(-10 * t.toDouble()).toFloat() * sin((t - 0.075) * c4).toFloat() + 1
            }
        }
    }
}