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
package java2d.demos.Arcs_Curves

import java2d.AnimatingControlsSurface
import java2d.CControl
import java2d.CustomControls
import java2d.Surface
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Paint
import java.awt.Rectangle
import java.awt.TexturePaint
import java.awt.geom.GeneralPath
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import javax.swing.Icon
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

/**
 * Animated Bezier Curve with controls for different draw & fill paints.
 */
class BezierAnim : AnimatingControlsSurface()
{
    private val animpts = FloatArray(POINTS_NUMBER * 2)
    private val deltas = FloatArray(POINTS_NUMBER * 2)
    private var gradient: GradientPaint = GradientPaint(0f, 0f, Color.RED, 200f, 200f, Color.YELLOW)
    private var fillPaint: Paint? = gradient
    private var drawPaint: Paint? = Color.BLUE
    private var stroke: BasicStroke = SOLID_STROKE

    init {
        background = Color.WHITE
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.NORTH)

    private fun animate(pts: FloatArray, deltas: FloatArray, index: Int, limit: Int) {
        var newpt = pts[index] + deltas[index]
        if (newpt <= 0) {
            newpt = -newpt
            deltas[index] = (Math.random() * 4.0 + 2.0).toFloat()
        } else if (newpt >= limit) {
            newpt = 2.0f * limit - newpt
            deltas[index] = -(Math.random() * 4.0 + 2.0).toFloat()
        }
        pts[index] = newpt
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        var i = 0
        while (i < animpts.size) {
            animpts[i + 0] = (Math.random() * newWidth).toFloat()
            animpts[i + 1] = (Math.random() * newHeight).toFloat()
            deltas[i + 0] = (Math.random() * 6.0 + 4.0).toFloat()
            deltas[i + 1] = (Math.random() * 6.0 + 4.0).toFloat()
            if (animpts[i + 0] > newWidth / 2.0f) {
                deltas[i + 0] = -deltas[i + 0]
            }
            if (animpts[i + 1] > newHeight / 2.0f) {
                deltas[i + 1] = -deltas[i + 1]
            }
            i += 2
        }
        gradient = GradientPaint(0f, 0f, Color.RED, newWidth * 0.7f, newHeight * 0.7f, Color.YELLOW)
    }

    override fun step(width: Int, height: Int) {
        var i = 0
        while (i < animpts.size) {
            animate(animpts, deltas, i + 0, width)
            animate(animpts, deltas, i + 1, height)
            i += 2
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val ctrlpts = animpts
        val len = ctrlpts.size
        var prevx = ctrlpts[len - 2]
        var prevy = ctrlpts[len - 1]
        var curx = ctrlpts[0]
        var cury = ctrlpts[1]
        var midx = (curx + prevx) / 2.0f
        var midy = (cury + prevy) / 2.0f
        val gp = GeneralPath(Path2D.WIND_NON_ZERO)
        gp.moveTo(midx, midy)
        var i = 2
        while (i <= ctrlpts.size) {
            val x1 = (midx + curx) / 2.0f
            val y1 = (midy + cury) / 2.0f
            prevx = curx
            prevy = cury
            if (i < ctrlpts.size) {
                curx = ctrlpts[i + 0]
                cury = ctrlpts[i + 1]
            } else {
                curx = ctrlpts[0]
                cury = ctrlpts[1]
            }
            midx = (curx + prevx) / 2.0f
            midy = (cury + prevy) / 2.0f
            val x2 = (prevx + midx) / 2.0f
            val y2 = (prevy + midy) / 2.0f
            gp.curveTo(x1, y1, x2, y2, midx, midy)
            i += 2
        }
        gp.closePath()
        drawPaint?.let { drawPaint ->
            g2.paint = drawPaint
            g2.stroke = stroke
            g2.draw(gp)
        }
        fillPaint?.let { fillPaint ->
            g2.paint = if (fillPaint is GradientPaint) gradient else fillPaint
            g2.fill(gp)
        }
    }

    internal class DemoControls(private val demo: BezierAnim) : CustomControls(demo.name)
    {
        private var fillMenu: JMenu
        private var drawMenu: JMenu
        private val fillMenuItems = arrayOfNulls<JMenuItem>(fillPaints.size)
        private val drawMenuItems = arrayOfNulls<JMenuItem>(drawPaints.size)
        private val fillIcons = arrayOfNulls<PaintedIcon>(fillPaints.size)
        private val drawIcons = arrayOfNulls<PaintedIcon>(drawPaints.size)

        init {
            val drawMenuBar = JMenuBar()
            add(drawMenuBar)

            val fillMenuBar = JMenuBar()
            add(fillMenuBar)

            drawMenu = drawMenuBar.add(JMenu("Draw Choice"))
            drawMenu.font = FONT

            for (i in drawPaints.indices) {
                drawIcons[i] = PaintedIcon(drawPaints[i])
                val menuItem = JMenuItem(drawName[i]).apply {
                    font = FONT
                    icon = drawIcons[i]
                    addActionListener {
                        demo.drawPaint = drawPaints[i]
                        if (text.endsWith("Dash")) {
                            demo.stroke = DASHED_STROKE
                        } else {
                            demo.stroke = SOLID_STROKE
                        }
                        drawMenu.icon = drawIcons[i]
                        checkRepaint()
                    }
                }
                drawMenuItems[i] = drawMenu.add(menuItem)
            }
            drawMenu.icon = drawIcons[1]

            fillMenu = fillMenuBar.add(JMenu("Fill Choice"))
            fillMenu.font = FONT
            for (i in fillPaints.indices) {
                fillIcons[i] = PaintedIcon(fillPaints[i])
                val menuItem = JMenuItem(fillName[i]).apply {
                    font = FONT
                    icon = fillIcons[i]
                    addActionListener {
                        demo.fillPaint = fillPaints[i]
                        fillMenu.icon = fillIcons[i]
                        checkRepaint()
                    }
                }
                fillMenuItems[i] = fillMenu.add(menuItem)
            }
            fillMenu.icon = fillIcons[fillPaints.size - 1]
        }

        private fun checkRepaint() {
            if (!demo.animating!!.isRunning) {
                demo.repaint()
            }
        }

        override fun getPreferredSize() = Dimension(200, 36)

        override fun run() {
            val me = Thread.currentThread()
            while (thread === me) {
                for (dmi in drawMenuItems) {
                    dmi!!.doClick()
                    for (fmi in fillMenuItems) {
                        fmi!!.doClick()
                        try {
                            Thread.sleep(3000 + (Math.random() * 3000).toLong())
                        } catch (e: InterruptedException) {
                            break
                        }
                    }
                }
            }
            thread = null
        }

        private class PaintedIcon(var paint: Paint?) : Icon
        {
            override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
                val g2 = g as Graphics2D
                g2.paint = paint ?: EMPTY_COLOR
                g2.fillRect(x, y, iconWidth, iconHeight)
                g2.color = Color.GRAY
                g2.draw3DRect(x, y, iconWidth - 1, iconHeight - 1, true)
            }

            override fun getIconWidth() = 12

            override fun getIconHeight() = 12

            companion object {
                private val EMPTY_COLOR = Color(0, 0, 0, 0)
            }
        }

        companion object
        {
            private val TEXTURE_PAINT_1: TexturePaint = run {
                val bufferedImage = BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB).apply {
                    setRGB(0, 0, 0xFF00FF00.toInt())
                    setRGB(1, 0, 0xFFFF0000.toInt())
                }
                TexturePaint(bufferedImage, Rectangle(0, 0, 2, 1))
            }

            private val TEXTURE_PAINT_2: TexturePaint = run {
                val bufferedImage = BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB).apply {
                    setRGB(0, 0, 0xFF0000FF.toInt())
                    setRGB(1, 0, 0xFFFF0000.toInt())
                }
                TexturePaint(bufferedImage, Rectangle(0, 0, 2, 1))
            }

            private val drawName = arrayOf("No Draw", "Blue", "Blue w/ Alpha", "Blue Dash", "Texture")

            private val drawPaints = arrayOf(
                null,
                Color.BLUE,
                Color(0, 0, 255, 126),
                Color.BLUE,
                TEXTURE_PAINT_2)

            private val fillName = arrayOf("No Fill", "Green", "Green w/ Alpha", "Texture", "Gradient")

            private val fillPaints = arrayOf(
                null,
                Color.GREEN,
                Color(0, 255, 0, 126),
                TEXTURE_PAINT_1,
                GradientPaint(0f, 0f, Color.RED, 30f, 30f, Color.YELLOW))

            private val FONT = Font(Font.SERIF, Font.PLAIN, 10)
        }
    }

    companion object
    {
        private const val POINTS_NUMBER = 6
        private val DASHED_STROKE =
            BasicStroke(10.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, floatArrayOf(5f), 0f)
        private val SOLID_STROKE =
            BasicStroke(10.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)

        @JvmStatic
        fun main(argv: Array<String>) {
            Surface.createDemoFrame(BezierAnim())
        }
    }
}
