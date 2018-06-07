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
package java2d.demos.Paint

import java2d.AnimatingControlsSurface
import java2d.CustomControls
import java2d.Surface
import java.awt.Color
import java.awt.Dimension
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.LinearGradientPaint
import java.awt.MultipleGradientPaint.CycleMethod
import java.awt.Paint
import java.awt.RadialGradientPaint
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.geom.Point2D
import javax.swing.JComboBox

/**
 * GradientPaint animation.
 */
class GradAnim : AnimatingControlsSurface()
{
    private val x1: AnimVal
    private val y1: AnimVal
    private val x2: AnimVal
    private val y2: AnimVal
    private var hue = (Math.random() * MAX_HUE).toInt()
    private var gradientType: Int = 0

    init {
        background = Color.WHITE
        controls = arrayOf(DemoControls(this))
        x1 = AnimVal(0f, 300f, 2f, 10f)
        y1 = AnimVal(0f, 300f, 2f, 10f)
        x2 = AnimVal(0f, 300f, 2f, 10f)
        y2 = AnimVal(0f, 300f, 2f, 10f)
        gradientType = BASIC_GRADIENT
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        x1.newlimits(0f, newWidth.toFloat())
        y1.newlimits(0f, newHeight.toFloat())
        x2.newlimits(0f, newWidth.toFloat())
        y2.newlimits(0f, newHeight.toFloat())
    }

    override fun step(width: Int, height: Int) {
        x1.anim()
        y1.anim()
        x2.anim()
        y2.anim()
        hue = (hue + (Math.random() * 10).toInt()) % MAX_HUE
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val fx1 = x1.flt
        val fy1 = y1.flt
        var fx2 = x2.flt
        var fy2 = y2.flt

        if (fx1 == fx2 && fy1 == fy2) {
            // just to prevent the points from being coincident
            fx2++
            fy2++
        }

        val c1 = getColor(hue)
        val c2 = getColor(hue + 256 * 3)
        val gp: Paint

        when (gradientType) {
            BASIC_GRADIENT -> gp = GradientPaint(fx1, fy1, c1,
                                                 fx2, fy2, c2,
                                                 true)
            LINEAR_GRADIENT -> {
                val fractions = floatArrayOf(0.0f, 0.2f, 1.0f)
                val c3 = getColor(hue + 256 * 2)
                val colors = arrayOf(c1, c2, c3)
                gp = LinearGradientPaint(fx1, fy1,
                                         fx2, fy2,
                                         fractions, colors,
                                         CycleMethod.REFLECT)
            }

            RADIAL_GRADIENT -> {
                val fractions = floatArrayOf(0.0f, 0.2f, 0.8f, 1.0f)
                val c3 = getColor(hue + 256 * 2)
                val c4 = getColor(hue + 256 * 4)
                val colors = arrayOf(c1, c2, c3, c4)
                val radius = Point2D.distance(fx1.toDouble(), fy1.toDouble(), fx2.toDouble(), fy2.toDouble()).toFloat()
                gp = RadialGradientPaint(fx1, fy1, radius,
                                         fractions, colors,
                                         CycleMethod.REFLECT)
            }

            FOCUS_GRADIENT -> {
                val fractions = floatArrayOf(0.0f, 0.2f, 0.8f, 1.0f)
                val c3 = getColor(hue + 256 * 4)
                val c4 = getColor(hue + 256 * 2)
                val colors = arrayOf(c1, c2, c3, c4)
                var radius = Point2D.distance(fx1.toDouble(), fy1.toDouble(), fx2.toDouble(), fy2.toDouble()).toFloat()
                val max = Math.max(w, h).toFloat()
                // This function will map the smallest radius to
                // max/10 when the points are next to each other,
                // max when the points are max distance apart,
                // and >max when they are further apart (in which
                // case the focus clipping code in RGP will clip
                // the focus to be inside the radius).
                radius = max * (radius / max * 0.9f + 0.1f)
                gp = RadialGradientPaint(fx2, fy2, radius,
                                         fx1, fy1,
                                         fractions, colors,
                                         CycleMethod.REPEAT)
            }
            else -> gp = GradientPaint(fx1, fy1, c1, fx2, fy2, c2, true)
        }
        g2.paint = gp
        g2.fillRect(0, 0, w, h)
        g2.color = Color.yellow
        g2.drawLine(x1.int, y1.int, x2.int, y2.int)
    }

    internal inner class DemoControls(var demo: GradAnim) : CustomControls(demo.name), ActionListener
    {
        private var combo: JComboBox<String> = JComboBox()

        init {
            combo.addActionListener(this)
            combo.addItem("2-color GradientPaint")
            combo.addItem("3-color LinearGradientPaint")
            combo.addItem("4-color RadialGradientPaint")
            combo.addItem("4-color RadialGradientPaint with focus")
            combo.selectedIndex = 0
            add(combo)
        }

        override fun actionPerformed(e: ActionEvent) {
            val index = combo.selectedIndex
            if (index >= 0) {
                demo.gradientType = index
            }
            if (!demo.isRunning) {
                demo.repaint()
            }
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(200, 41)
        }

        override fun run() {
            val me = Thread.currentThread()
            while (thread === me) {
                for (i in 0 until combo.itemCount) {
                    combo.selectedIndex = i
                    try {
                        Thread.sleep(4444)
                    } catch (e: InterruptedException) {
                        return
                    }
                }
            }
            thread = null
        }
    }

    companion object
    {
        private const val BASIC_GRADIENT = 0
        private const val LINEAR_GRADIENT = 1
        private const val RADIAL_GRADIENT = 2
        private const val FOCUS_GRADIENT = 3
        private const val MAX_HUE = 256 * 6

        private fun getColor(hue: Int): Color {
            val leg = hue / 256 % 6
            val step = hue % 256 * 2
            val falling = if (step < 256) 255 else 511 - step
            val rising = if (step < 256) step else 255
            var r: Int
            var g: Int
            var b: Int
            b = 0
            g = b
            r = g
            when (leg) {
                0 -> r = 255
                1 -> {
                    r = falling
                    g = rising
                }
                2 -> g = 255
                3 -> {
                    g = falling
                    b = rising
                }
                4 -> b = 255
                5 -> {
                    b = falling
                    r = rising
                }
            }
            return Color(r, g, b)
        }

        @JvmStatic
        fun main(argv: Array<String>) {
            Surface.createDemoFrame(GradAnim())
        }
    }
}
