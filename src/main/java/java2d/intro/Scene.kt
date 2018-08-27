package java2d.intro

import java.awt.Graphics2D
import java.awt.image.BufferedImage

/**
 * Scene is the manager of the parts.
 */
internal class Scene(
    var name: String,
    var pauseAmt: Long,
    private val parts: Array<out Part>
) {
    var participate: Boolean = true
    var index: Int = 0
    var length: Int = 0

    init {
        for (part in parts) {
            val partLength = part.end
            if (partLength > length) {
                length = partLength
            }
        }
    }

    fun reset(surface: Intro.Surface, w: Int, h: Int) {
        index = 0
        parts.forEach { it.reset(surface, w, h) }
    }

    fun step(image: BufferedImage, w: Int, h: Int) {
        for (part in parts) {
            if (index in part.begin .. part.end) {
                part.step(image, w, h)
            }
        }
    }

    fun render(w: Int, h: Int, g2: Graphics2D) {
        for (part in parts) {
            if (index in part.begin .. part.end) {
                part.render(w, h, g2)
            }
        }
    }

    fun pause() {
        try {
            Thread.sleep(pauseAmt)
        } catch (ignored: Exception) {
        }
        // System.gc()
    }
}
