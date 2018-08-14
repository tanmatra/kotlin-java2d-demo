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
package java2d.demos.Mix

import java2d.AnimatingControlsSurface
import java2d.CustomControls
import java.awt.Color
import java.awt.Color.BLUE
import java.awt.Color.GREEN
import java.awt.Color.ORANGE
import java.awt.Color.RED
import java.awt.Color.WHITE
import java.awt.Color.YELLOW
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.IndexColorModel
import java.awt.image.Raster
import java.lang.Math.random
import java.lang.Math.sqrt
import javax.swing.AbstractButton
import javax.swing.JComboBox
import javax.swing.JToggleButton
import javax.swing.JToolBar

/**
 * Animated color bouncing balls with custom controls.
 */
class Balls private constructor() : AnimatingControlsSurface()
{
    private var now: Long = 0

    private var lasttime: Long = 0

    private val balls: Array<Ball> = Array(colors.size) { i ->
        Ball(colors[i], 30)
    }

    private var clearToggle: Boolean = false
    private val combo = JComboBox<String>()

    init {
        background = WHITE
        balls[0].isSelected = true
        balls[3].isSelected = true
        balls[4].isSelected = true
        balls[6].isSelected = true
        controls = arrayOf(DemoControls(this))
    }

    override fun reset(w: Int, h: Int) {
        if (w > 400 && h > 100) {
            combo.selectedIndex = 5
        }
    }

    override fun step(w: Int, h: Int) {
        if (lasttime == 0L) {
            lasttime = System.currentTimeMillis()
        }
        now = System.currentTimeMillis()
        val deltaT = now - lasttime
        var active = false
        for (ball in balls) {
            if (ball == null) {
                return
            }
            ball.step(deltaT, w, h)
            if (ball.Vy > .02 || -ball.Vy > .02 || ball.y + ball.bsize < h) {
                active = true
            }
        }
        if (!active) {
            for (ball in balls) {
                ball.Vx = random().toFloat() / 4.0f - 0.125f
                ball.Vy = -random().toFloat() / 4.0f - 0.2f
            }
            clearToggle = true
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        for (b in balls) {
            if (b == null || b.imgs[b.index] == null || !b.isSelected) {
                continue
            }
            g2.drawImage(b.imgs[b.index], b.x.toInt(), b.y.toInt(), this)
        }
        lasttime = now
    }

    protected class Ball internal constructor(private val color: Color, bsize: Int)
    {
        internal var bsize: Int = 0
        var x: Float = 0.toFloat()
        var y: Float = 0.toFloat()
        internal var Vx = 0.1f
        internal var Vy = 0.05f

        internal lateinit var imgs: Array<BufferedImage?>

        // Pick a random starting image index, but not the last: we're going UP
        // and that would throw us off the end.
        var index = (random() * (nImgs - 1)).toInt()

        private var indexDirection = UP
        private var jitter: Float = 0.toFloat()
        internal var isSelected: Boolean = false

        init {
            makeImages(bsize)
        }

        internal fun makeImages(bsize: Int) {
            this.bsize = bsize * 2
            val R = bsize
            val data = ByteArray(R * 2 * R * 2)
            var maxr = 0
            var Y = 2 * R
            while (--Y >= 0) {
                val x0 = (sqrt((R * R - (Y - R) * (Y - R)).toDouble()) + 0.5).toInt()
                var p = Y * (R * 2) + R - x0
                for (X in -x0 until x0) {
                    val xx = X + 15
                    val yy = Y - R + 15
                    val r = (Math.hypot(xx.toDouble(), yy.toDouble()) + 0.5).toInt()
                    if (r > maxr) {
                        maxr = r
                    }
                    data[p++] = if (r <= 0) 1 else r.toByte()
                }
            }

            imgs = arrayOfNulls<BufferedImage>(nImgs)

            val bg = 255
            val red = ByteArray(256)
            red[0] = bg.toByte()
            val green = ByteArray(256)
            green[0] = bg.toByte()
            val blue = ByteArray(256)
            blue[0] = bg.toByte()

            for (r in imgs.indices) {
                val b = 0.5f + (r + 1f) / imgs.size.toFloat() / 2f
                for (i in maxr downTo 1) {
                    val d = i.toFloat() / maxr
                    red[i] = blend(blend(color.red, 255, d), bg, b).toByte()
                    green[i] = blend(blend(color.green, 255, d), bg, b).toByte()
                    blue[i] = blend(blend(color.blue, 255, d), bg, b).toByte()
                }
                val icm = IndexColorModel(8, maxr + 1, red, green, blue, 0)
                val dbb = DataBufferByte(data, data.size)
                val bandOffsets = intArrayOf(0)
                val wr = Raster.createInterleavedRaster(dbb, R * 2, R * 2, R * 2, 1, bandOffsets, null)
                imgs[r] = BufferedImage(icm, wr, icm.isAlphaPremultiplied, null)
            }
        }

        private fun blend(fg: Int, bg: Int, fgfactor: Float): Int {
            return (bg + (fg - bg) * fgfactor).toInt()
        }

        fun step(deltaT: Long, w: Int, h: Int) {

            jitter = random().toFloat() * .01f - .005f

            x += (Vx * deltaT + Ax / 2.0 * deltaT.toDouble() * deltaT.toDouble()).toFloat()
            y += (Vy * deltaT + Ay / 2.0 * deltaT.toDouble() * deltaT.toDouble()).toFloat()
            if (x <= 0.0f) {
                x = 0.0f
                Vx = -Vx * inelasticity + jitter
                //collision_x = true;
            }
            if (x + bsize >= w) {
                x = (w - bsize).toFloat()
                Vx = -Vx * inelasticity + jitter
                //collision_x = true;
            }
            if (y <= 0) {
                y = 0f
                Vy = -Vy * inelasticity + jitter
                //collision_y = true;
            }
            if (y + bsize >= h) {
                y = (h - bsize).toFloat()
                Vx *= inelasticity
                Vy = -Vy * inelasticity + jitter
                //collision_y = true;
            }
            Vy = Vy + Ay * deltaT
            Vx = Vx + Ax * deltaT

            if (indexDirection == UP) {
                index++
            }
            if (indexDirection == DOWN) {
                --index
            }
            if (index + 1 == nImgs) {
                indexDirection = DOWN
            }
            if (index == 0) {
                indexDirection = UP
            }
        }

        companion object
        {
            internal const val nImgs = 5
            private const val inelasticity = 0.96f
            private const val Ax = 0.0f
            private const val Ay = 0.0002f
            private const val UP = 0
            private const val DOWN = 1
        }
    }

    internal inner class DemoControls(var demo: Balls) : CustomControls(demo.name), ActionListener
    {
        private val toolbar = JToolBar()

        init {
            add(toolbar)
            toolbar.isFloatable = false
            addTool("Clear", true)
            addTool("R", demo.balls[0].isSelected)
            addTool("O", demo.balls[1].isSelected)
            addTool("Y", demo.balls[2].isSelected)
            addTool("G", demo.balls[3].isSelected)
            addTool("B", demo.balls[4].isSelected)
            addTool("I", demo.balls[5].isSelected)
            addTool("V", demo.balls[6].isSelected)
            add(combo)
            combo.addItem("10")
            combo.addItem("20")
            combo.addItem("30")
            combo.addItem("40")
            combo.addItem("50")
            combo.addItem("60")
            combo.addItem("70")
            combo.addItem("80")
            combo.selectedIndex = 2
            combo.addActionListener(this)
        }

        fun addTool(str: String, state: Boolean) {
            val b = toolbar.add(JToggleButton(str)) as JToggleButton
            b.isFocusPainted = false
            b.isSelected = state
            b.addActionListener(this)
            val width = b.preferredSize.width
            val prefSize = Dimension(width, 21)
            b.preferredSize = prefSize
            b.maximumSize = prefSize
            b.minimumSize = prefSize
        }

        override fun actionPerformed(e: ActionEvent) {
            if (e.source is JComboBox<*>) {
                val size = Integer.parseInt(combo.selectedItem as String)
                for (ball in demo.balls) {
                    ball.makeImages(size)
                }
                return
            }
            val b = e.source as JToggleButton
            if (b.text == "Clear") {
                demo.clearSurface = b.isSelected
            } else {
                val index = toolbar.getComponentIndex(b) - 1
                demo.balls[index].isSelected = b.isSelected
            }
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(200, 40)
        }

        override fun run() {
            try {
                Thread.sleep(999)
            } catch (e: Exception) {
                return
            }

            val me = Thread.currentThread()
            (toolbar.getComponentAtIndex(2) as AbstractButton).doClick()
            while (thread === me) {
                try {
                    Thread.sleep(222)
                } catch (e: InterruptedException) {
                    return
                }

                if (demo.clearToggle) {
                    if (demo.clearSurface) {
                        combo.selectedIndex = (random() * 5).toInt()
                    }
                    (toolbar.getComponentAtIndex(0) as AbstractButton).doClick()
                    demo.clearToggle = false
                }
            }
            thread = null
        }
    } // End DemoControls

    companion object
    {
        private val colors = arrayOf(RED, ORANGE, YELLOW, GREEN.darker(), BLUE, Color(75, 0, 82), Color(238, 130, 238))

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(Balls())
        }
    }
}
