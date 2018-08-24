package java2d.intro

import java.awt.Graphics2D

/**
 * Part is a piece of the scene.  Classes must implement Part in order to participate in a scene.
 */
internal interface Part
{
    val begin: Int

    val end: Int

    fun reset(newWidth: Int, newHeight: Int)

    fun step(w: Int, h: Int)

    fun render(w: Int, h: Int, g2: Graphics2D)
}
