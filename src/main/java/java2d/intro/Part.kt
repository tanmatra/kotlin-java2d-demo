package java2d.intro

import java.awt.Graphics2D
import java.awt.image.BufferedImage

/**
 * Part is a piece of the scene.  Classes must implement Part in order to participate in a scene.
 */
internal interface Part
{
    val begin: Int

    val end: Int

    fun reset(surface: Intro.Surface, newWidth: Int, newHeight: Int)

    fun step(surfaceImage: BufferedImage, surface: Intro.Surface, w: Int, h: Int)

    fun render(w: Int, h: Int, g2: Graphics2D)
}
