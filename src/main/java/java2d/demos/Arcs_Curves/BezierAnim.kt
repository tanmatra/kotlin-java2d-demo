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
import java2d.CustomControls
import java2d.Surface
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Color.BLUE
import java.awt.Color.GRAY
import java.awt.Color.GREEN
import java.awt.Color.RED
import java.awt.Color.WHITE
import java.awt.Color.YELLOW
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Paint
import java.awt.Rectangle
import java.awt.TexturePaint
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
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
class BezierAnim : AnimatingControlsSurface() {
    protected var solid = BasicStroke(
        10.0f,
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND
                                     )
    protected var dashed = BasicStroke(
        10.0f,
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, floatArrayOf(5f),
        0f
                                      )
    private val animpts = FloatArray(NUMPTS * 2)
    private val deltas = FloatArray(NUMPTS * 2)
    protected var fillPaint: Paint
    protected var drawPaint: Paint
    protected var doFill = true
    protected var doDraw = true
    protected var gradient: GradientPaint
    protected var stroke: BasicStroke

    init {
        background = WHITE
        gradient = GradientPaint(0f, 0f, RED, 200f, 200f, YELLOW)
        fillPaint = gradient
        drawPaint = BLUE
        stroke = solid
        controls = arrayOf(DemoControls(this))
    }

    fun animate(pts: FloatArray, deltas: FloatArray, index: Int, limit: Int) {
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

    override fun reset(w: Int, h: Int) {
        var i = 0
        while (i < animpts.size) {
            animpts[i + 0] = (Math.random() * w).toFloat()
            animpts[i + 1] = (Math.random() * h).toFloat()
            deltas[i + 0] = (Math.random() * 6.0 + 4.0).toFloat()
            deltas[i + 1] = (Math.random() * 6.0 + 4.0).toFloat()
            if (animpts[i + 0] > w / 2.0f) {
                deltas[i + 0] = -deltas[i + 0]
            }
            if (animpts[i + 1] > h / 2.0f) {
                deltas[i + 1] = -deltas[i + 1]
            }
            i += 2
        }
        gradient = GradientPaint(0f, 0f, RED, w * .7f, h * .7f, YELLOW)
    }

    override fun step(w: Int, h: Int) {
        var i = 0
        while (i < animpts.size) {
            animate(animpts, deltas, i + 0, w)
            animate(animpts, deltas, i + 1, h)
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
        if (doDraw) {
            g2.paint = drawPaint
            g2.stroke = stroke
            g2.draw(gp)
        }
        if (doFill) {
            if (fillPaint is GradientPaint) {
                fillPaint = gradient
            }
            g2.paint = fillPaint
            g2.fill(gp)
        }
    }

    internal class DemoControls(var demo: BezierAnim) : CustomControls(demo.name), ActionListener {
        var fillName = arrayOf("No Fill", "Green", "Green w/ Alpha", "Texture", "Gradient")
        var fillMenu: JMenu
        var drawMenu: JMenu
        var fillMI = arrayOfNulls<JMenuItem>(fillPaints.size)
        var drawMI = arrayOfNulls<JMenuItem>(drawPaints.size)
        var fillIcons = arrayOfNulls<PaintedIcon>(fillPaints.size)
        var drawIcons = arrayOfNulls<PaintedIcon>(drawPaints.size)

        init {

            val drawMenuBar = JMenuBar()
            add(drawMenuBar)

            val fillMenuBar = JMenuBar()
            add(fillMenuBar)

            drawMenu = drawMenuBar.add(JMenu("Draw Choice"))
            drawMenu.font = FONT

            for (i in drawPaints.indices) {
                drawIcons[i] = PaintedIcon(drawPaints[i])
                drawMI[i] = drawMenu.add(JMenuItem(drawName[i]))
                drawMI[i]!!.setFont(FONT)
                drawMI[i]!!.setIcon(drawIcons[i])
                drawMI[i]!!.addActionListener(this)
            }
            drawMenu.icon = drawIcons[1]

            fillMenu = fillMenuBar.add(JMenu("Fill Choice"))
            fillMenu.font = FONT
            for (i in fillPaints.indices) {
                fillIcons[i] = PaintedIcon(fillPaints[i])
                fillMI[i] = fillMenu.add(JMenuItem(fillName[i]))
                fillMI[i]!!.setFont(FONT)
                fillMI[i]!!.setIcon(fillIcons[i])
                fillMI[i]!!.addActionListener(this)
            }
            fillMenu.icon = fillIcons[fillPaints.size - 1]
        }

        override fun actionPerformed(e: ActionEvent) {
            val obj = e.source
            for (i in fillPaints.indices) {
                if (obj == fillMI[i]) {
                    demo.doFill = true
                    demo.fillPaint = fillPaints[i]
                    fillMenu.icon = fillIcons[i]
                    break
                }
            }
            for (i in drawPaints.indices) {
                if (obj == drawMI[i]) {
                    demo.doDraw = true
                    demo.drawPaint = drawPaints[i]
                    if ((obj as JMenuItem).text.endsWith("Dash")) {
                        demo.stroke = demo.dashed
                    } else {
                        demo.stroke = demo.solid
                    }
                    drawMenu.icon = drawIcons[i]
                    break
                }
            }
            if (obj == fillMI[0]) {
                demo.doFill = false
            } else if (obj == drawMI[0]) {
                demo.doDraw = false
            }
            if (!demo.animating!!.isRunning) {
                demo.repaint()
            }
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(200, 36)
        }

        override fun run() {
            val me = Thread.currentThread()
            while (thread === me) {
                for (dmi in drawMI) {
                    dmi!!.doClick()
                    for (fmi in fillMI) {
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

        internal class PaintedIcon(var paint: Paint) : Icon {

            override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
                val g2 = g as Graphics2D
                g2.paint = paint
                g2.fillRect(x, y, iconWidth, iconHeight)
                g2.color = GRAY
                g2.draw3DRect(
                    x, y, iconWidth - 1, iconHeight - 1,
                    true
                             )
            }

            override fun getIconWidth(): Int {
                return 12
            }

            override fun getIconHeight(): Int {
                return 12
            }
        } // End PaintedIcon class

        companion object {

            val tp1: TexturePaint
            val tp2: TexturePaint

            init {
                var bi = BufferedImage(
                    2, 1,
                    BufferedImage.TYPE_INT_RGB
                                      )
                bi.setRGB(0, 0, -0xff0100)
                bi.setRGB(1, 0, -0x10000)
                tp1 = TexturePaint(bi, Rectangle(0, 0, 2, 1))
                bi = BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB)
                bi.setRGB(0, 0, -0xffff01)
                bi.setRGB(1, 0, -0x10000)
                tp2 = TexturePaint(bi, Rectangle(0, 0, 2, 1))
            }

            var drawPaints = arrayOf(
                Color(0, 0, 0, 0), BLUE, Color(
                    0,
                    0, 255, 126
                                              ), BLUE, tp2
                                    )
            var drawName = arrayOf("No Draw", "Blue", "Blue w/ Alpha", "Blue Dash", "Texture")
            var fillPaints = arrayOf(
                Color(0, 0, 0, 0), GREEN, Color(
                    0,
                    255, 0, 126
                                               ), tp1, GradientPaint(0f, 0f, RED, 30f, 30f, YELLOW)
                                    )
            val FONT = Font("serif", Font.PLAIN, 10)
        }
    } // End DemoControls class

    companion object {

        private val NUMPTS = 6

        @JvmStatic
        fun main(argv: Array<String>) {
            Surface.createDemoFrame(BezierAnim())
        }
    }
} // End BezierAnim class

