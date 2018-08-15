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
import java2d.CControl
import java2d.CustomControls
import java2d.createBooleanButton
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.IndexColorModel
import java.awt.image.Raster
import java.lang.Math.random
import java.lang.Math.sqrt
import javax.swing.AbstractButton
import javax.swing.JComboBox
import javax.swing.JToolBar

/**
 * Animated color bouncing balls with custom controls.
 */
class Balls private constructor() : AnimatingControlsSurface()
{
    private var now: Long = 0

    private var lasttime: Long = 0

    private val balls: Array<Ball> = COLORS.map { setting -> Ball(setting, 30) }.toTypedArray()

    private var clearToggle: Boolean = false

    private val combo = JComboBox<String>()

    init {
        background = Color.WHITE
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.NORTH)

    override fun reset(newWidth: Int, newHeight: Int) {
        if (newWidth > 400 && newHeight > 100) {
            combo.selectedIndex = 5
        }
    }

    override fun step(width: Int, height: Int) {
        if (lasttime == 0L) {
            lasttime = System.currentTimeMillis()
        }
        now = System.currentTimeMillis()
        val deltaT = now - lasttime
        var active = false
        for (ball in balls) {
            ball.step(deltaT, width, height)
            if (ball.vy > 0.02 || -ball.vy > 0.02 || ball.y + ball.size < height) {
                active = true
            }
        }
        if (!active) {
            for (ball in balls) {
                ball.vx = random().toFloat() / 4.0f - 0.125f
                ball.vy = -random().toFloat() / 4.0f - 0.2f
            }
            clearToggle = true
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        for (ball in balls) {
            if (ball.isSelected) {
                g2.drawImage(ball.imgs[ball.index], ball.x.toInt(), ball.y.toInt(), this)
            }
        }
        lasttime = now
    }

    internal class Ball(
        internal val setting: ColorSetting,
        size: Int
    ) {
        private val color = setting.color
        internal var isSelected = setting.isSelected
        internal var size: Int = 0
        var x: Float = 0.0f
        var y: Float = 0.0f
        internal var vx = 0.1f
        internal var vy = 0.05f

        internal var imgs: Array<BufferedImage> = createImages(size)

        // Pick a random starting image index, but not the last: we're going UP
        // and that would throw us off the end.
        var index = (random() * (IMAGES_COUNT - 1)).toInt()

        private var indexDirection = UP
        private var jitter: Float = 0.0f

        private fun createImages(size: Int): Array<BufferedImage> {
            this.size = size * 2
            val R = size
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

            val bg = 255
            val red = ByteArray(256)
            red[0] = bg.toByte()
            val green = ByteArray(256)
            green[0] = bg.toByte()
            val blue = ByteArray(256)
            blue[0] = bg.toByte()

            return Array(IMAGES_COUNT) { r ->
                val b = 0.5f + (r + 1f) / IMAGES_COUNT.toFloat() / 2f
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
                BufferedImage(icm, wr, icm.isAlphaPremultiplied, null)
            }
        }

        internal fun makeImages(size: Int) {
            imgs = createImages(size)
        }

        fun step(deltaT: Long, w: Int, h: Int) {
            jitter = random().toFloat() * 0.01f - 0.005f

            x += (vx * deltaT + Ax / 2.0 * deltaT.toDouble() * deltaT.toDouble()).toFloat()
            y += (vy * deltaT + Ay / 2.0 * deltaT.toDouble() * deltaT.toDouble()).toFloat()
            if (x <= 0.0f) {
                x = 0.0f
                vx = -vx * inelasticity + jitter
                //collision_x = true;
            }
            if (x + size >= w) {
                x = (w - size).toFloat()
                vx = -vx * inelasticity + jitter
                //collision_x = true;
            }
            if (y <= 0.0f) {
                y = 0.0f
                vy = -vy * inelasticity + jitter
                //collision_y = true;
            }
            if (y + size >= h) {
                y = (h - size).toFloat()
                vx *= inelasticity
                vy = -vy * inelasticity + jitter
                //collision_y = true;
            }
            vy += Ay * deltaT
            vx += Ax * deltaT

            when (indexDirection) {
                UP -> index++
                DOWN -> index--
            }
            when {
                index + 1 == IMAGES_COUNT -> indexDirection = DOWN
                index == 0 -> indexDirection = UP
            }
        }

        companion object
        {
            private const val IMAGES_COUNT = 5
            private const val inelasticity = 0.96f
            private const val Ax = 0.0f
            private const val Ay = 0.0002f
            private const val UP = 0
            private const val DOWN = 1

            private fun blend(fg: Int, bg: Int, fgfactor: Float): Int {
                return (bg + (fg - bg) * fgfactor).toInt()
            }
        }
    }

    internal inner class DemoControls(private val demo: Balls) : CustomControls(demo.name)
    {
        private val toolbar = JToolBar().apply { isFloatable = false }

        init {
            add(toolbar)
            toolbar.add(createBooleanButton(demo::clearSurface, "Clear"))

            for (ball in balls) {
                toolbar.add(createBooleanButton(ball::isSelected, ball.setting.name))
            }

            add(combo)
            for (n in 10 .. 80 step 10) {
                combo.addItem(n.toString())
            }
            combo.selectedIndex = 2
            combo.addActionListener {
                val size = (combo.selectedItem as String).toInt()
                for (ball in demo.balls) {
                    ball.makeImages(size)
                }
            }
        }

        override fun getPreferredSize() = Dimension(200, 40)

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
    }

    internal class ColorSetting(
        val name: String,
        val color: Color,
        val isSelected: Boolean)

    companion object
    {
        private val COLORS = arrayOf(
            ColorSetting("R", Color.RED,            true),
            ColorSetting("O", Color.ORANGE,         false),
            ColorSetting("Y", Color.YELLOW,         false),
            ColorSetting("G", Color.GREEN.darker(), true),
            ColorSetting("B", Color.BLUE,           true),
            ColorSetting("I", Color(75, 0, 82),     false),
            ColorSetting("V", Color(238, 130, 238), true))

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(Balls())
        }
    }
}
