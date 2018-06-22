/*
 *
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package java2d.demos.Transforms

import java2d.AnimatingControlsSurface
import java2d.CControl
import java2d.CustomControls
import java2d.createToolButton
import java2d.use
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Image
import java.awt.font.TextLayout
import javax.swing.AbstractButton
import javax.swing.ButtonGroup
import javax.swing.JToolBar

/**
 * Scaling or Shearing or Rotating an image & rectangle.
 */
class SelectTx : AnimatingControlsSurface()
{
    private val originalImage: Image = getImage("painting.gif")
    private var image: Image? = null
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var transformType: TransformType = TransformType.SHEAR
    private var sx: Double = 0.0
    private var sy: Double = 0.0
    private var angdeg: Double = 0.0
    private var direction = Direction.RIGHT
    private var transformToggle: TransformType = TransformType.SCALE

    init {
        background = Color.WHITE
        imageWidth = originalImage.getWidth(this)
        imageHeight = originalImage.getHeight(this)
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.NORTH)

    override fun reset(newWidth: Int, newHeight: Int) {
        imageWidth = if (newWidth > 3) newWidth / 3 else 1
        imageHeight = if (newHeight > 3) newHeight / 3 else 1

        image = createImage(imageWidth, imageHeight).apply {
            graphics.use { gfx ->
                gfx.drawImage(originalImage, 0, 0, imageWidth, imageHeight, Color.ORANGE, null)
            }
        }
        when (transformType) {
            TransformType.SCALE -> {
                direction = Direction.RIGHT
                sx = 1.0
                sy = 1.0
            }
            TransformType.SHEAR -> {
                direction = Direction.RIGHT
                sx = 0.0
                sy = 0.0
            }
            else -> angdeg = 0.0
        }
    }

    override fun step(width: Int, height: Int) {
        val rw = imageWidth + 10
        val rh = imageHeight + 10

        when {
            transformType == TransformType.SCALE && direction == Direction.RIGHT -> {
                sx += 0.05
                if (width * 0.5 - imageWidth * 0.5 + rw * sx + 10.0 > width) {
                    direction = Direction.DOWN
                }
            }
            transformType == TransformType.SCALE && direction == Direction.DOWN -> {
                sy += 0.05
                if (height * 0.5 - imageHeight * 0.5 + rh * sy + 20.0 > height) {
                    direction = Direction.LEFT
                }
            }
            transformType == TransformType.SCALE && direction == Direction.LEFT -> {
                sx -= 0.05
                if (rw * sx - 10 <= -(width * 0.5 - imageWidth * 0.5)) {
                    direction = Direction.UP
                }
            }
            transformType == TransformType.SCALE && direction == Direction.UP -> {
                sy -= 0.05
                if (rh * sy - 20 <= -(height * 0.5 - imageHeight * 0.5)) {
                    direction = Direction.RIGHT
                    transformToggle = TransformType.SHEAR
                }
            }
            transformType == TransformType.SHEAR && direction == Direction.RIGHT -> {
                sx += 0.05
                if (rw.toDouble() + 2.0 * rh.toDouble() * sx + 20.0 > width) {
                    direction = Direction.LEFT
                    sx -= 0.1
                }
            }
            transformType == TransformType.SHEAR && direction == Direction.LEFT -> {
                sx -= 0.05
                if (rw - 2.0 * rh.toDouble() * sx + 20 > width) {
                    direction = Direction.XMIDDLE
                }
            }
            transformType == TransformType.SHEAR && direction == Direction.XMIDDLE -> {
                sx += 0.05
                if (sx > 0) {
                    direction = Direction.DOWN
                    sx = 0.0
                }
            }
            transformType == TransformType.SHEAR && direction == Direction.DOWN -> {
                sy -= 0.05
                if (rh - 2.0 * rw.toDouble() * sy + 20 > height) {
                    direction = Direction.UP
                    sy += 0.1
                }
            }
            transformType == TransformType.SHEAR && direction == Direction.UP -> {
                sy += 0.05
                if (rh.toDouble() + 2.0 * rw.toDouble() * sy + 20.0 > height) {
                    direction = Direction.YMIDDLE
                }
            }
            transformType == TransformType.SHEAR && direction == Direction.YMIDDLE -> {
                sy -= 0.05
                if (sy < 0) {
                    direction = Direction.XUPYUP
                    sy = 0.0
                }
            }
            transformType == TransformType.SHEAR && direction == Direction.XUPYUP -> {
                sx += 0.05
                sy += 0.05
                if (rw.toDouble() + 2.0 * rh.toDouble() * sx + 30.0 > width ||
                    rh.toDouble() + 2.0 * rw.toDouble() * sy + 30.0 > height)
                {
                    direction = Direction.XDOWNYDOWN
                }
            }
            transformType == TransformType.SHEAR && direction == Direction.XDOWNYDOWN -> {
                sy -= 0.05
                sx -= 0.05
                if (sy < 0) {
                    direction = Direction.RIGHT
                    sx = 0.0
                    sy = 0.0
                    transformToggle = TransformType.ROTATE
                }
            }
            transformType == TransformType.ROTATE -> {
                angdeg += 5.0
                if (angdeg >= 360.0) {
                    angdeg -= 360.0
                    transformToggle = TransformType.SCALE
                }
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val font = g2.font
        val frc = g2.fontRenderContext
        val tl = TextLayout(transformType.title, font, frc)
        g2.color = Color.BLACK
        tl.draw(g2, (w / 2 - tl.bounds.width / 2).toFloat(), tl.ascent + tl.descent)

        when (transformType) {
            TransformType.ROTATE -> {
                val s = java.lang.Double.toString(angdeg)
                g2.drawString("angdeg=$s", 2, h - 4)
            }
            else -> {
                var s = java.lang.Double.toString(sx)
                s = if (s.length < 5) s else s.substring(0, 5)
                val tlsx = TextLayout("sx=$s", font, frc)
                tlsx.draw(g2, 2f, (h - 4).toFloat())

                s = java.lang.Double.toString(sy)
                s = if (s.length < 5) s else s.substring(0, 5)
                g2.drawString("sy=$s", (tlsx.bounds.width + 4).toInt(), h - 4)
            }
        }

        when (transformType) {
            TransformType.SCALE -> {
                g2.translate(w / 2 - imageWidth / 2, h / 2 - imageHeight / 2)
                g2.scale(sx, sy)
            }
            TransformType.SHEAR -> {
                g2.translate(w / 2 - imageWidth / 2, h / 2 - imageHeight / 2)
                g2.shear(sx, sy)
            }
            else -> {
                g2.rotate(Math.toRadians(angdeg), (w / 2).toDouble(), (h / 2).toDouble())
                g2.translate(w / 2 - imageWidth / 2, h / 2 - imageHeight / 2)
            }
        }

        g2.color = Color.ORANGE
        g2.fillRect(0, 0, imageWidth + 10, imageHeight + 10)
        g2.drawImage(image, 5, 5, this)
    }

    internal class DemoControls(private val demo: SelectTx) : CustomControls(demo.name)
    {
        private val toolbar = JToolBar().apply { isFloatable = false }

        init {
            add(toolbar)

            val buttonGroup = ButtonGroup()
            fun addTool(button: AbstractButton) {
                toolbar.add(button)
                buttonGroup.add(button)
            }

            addTool(createToolButton("Scale", false) {
                demo.transformType = TransformType.SCALE
                demo.direction = Direction.RIGHT
                demo.sy = 1.0
                demo.sx = demo.sy
            })
            addTool(createToolButton("Shear", true) {
                demo.transformType = TransformType.SHEAR
                demo.direction = Direction.RIGHT
                demo.sy = 0.0
                demo.sx = demo.sy
            })
            addTool(createToolButton("Rotate", false) {
                demo.transformType = TransformType.ROTATE
                demo.angdeg = 0.0
            })
        }

        override fun getPreferredSize() = Dimension(200, 39)

        override fun run() {
            val me = Thread.currentThread()
            demo.transformToggle = demo.transformType
            while (thread === me) {
                try {
                    Thread.sleep(222)
                } catch (e: InterruptedException) {
                    return
                }
                if (demo.transformToggle != demo.transformType) {
                    (toolbar.getComponent(demo.transformToggle.ordinal) as AbstractButton).doClick()
                }
            }
            thread = null
        }
    }

    private enum class TransformType(val title: String) {
        SCALE("Scale"),
        SHEAR("Shear"),
        ROTATE("Rotate")
    }

    private enum class Direction {
        RIGHT,
        LEFT,
        XMIDDLE,
        DOWN,
        UP,
        YMIDDLE,
        XUPYUP,
        XDOWNYDOWN
    }

    companion object
    {
        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(SelectTx())
        }
    }
}
