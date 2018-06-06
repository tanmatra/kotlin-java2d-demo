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
package java2d.demos.Clipping

import java2d.AnimatingControlsSurface
import java2d.CControl
import java2d.CustomControls
import java2d.createToolButton
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Rectangle
import java.awt.TexturePaint
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.lang.Math.random
import javax.swing.AbstractButton
import javax.swing.JToolBar

/**
 * Animated clipping of an image & composited shapes.
 */
class ClipAnim : AnimatingControlsSurface()
{
    private val animval = arrayOf(
        AnimVal(true),
        AnimVal(false),
        AnimVal(false))

    private var doObjects = true
    private val originalFont = Font(Font.SERIF, Font.PLAIN, 12)
    private var textFont: Font? = null
    private var gradient: GradientPaint? = null
    private var strX: Int = 0
    private var strY: Int = 0
    private var dukeX: Int = 0
    private var dukeY: Int = 0

    init {
        cimg = getImage("clouds.jpg")
        dimg = getImage("duke.gif")
        background = Color.WHITE
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.NORTH)

    override fun reset(newWidth: Int, newHeight: Int) {
        for (a in animval) {
            a.reset(newWidth, newHeight)
        }
        gradient = GradientPaint(0f, (newHeight / 2).toFloat(), Color.RED, newWidth * 0.4f, newHeight * 0.9f, Color.YELLOW)
        dukeX = (newWidth * 0.25 - dimg.getWidth(this) / 2).toInt()
        dukeY = (newHeight * 0.25 - dimg.getHeight(this) / 2).toInt()
        var fm = getFontMetrics(originalFont)
        val sw = fm.stringWidth(TEXT).toDouble()
        val sh = (fm.ascent + fm.descent).toDouble()
        val sx = (newWidth / 2 - 30) / sw
        val sy = (newHeight / 2 - 30) / sh
        textFont = originalFont.deriveFont(AffineTransform.getScaleInstance(sx, sy))
        fm = getFontMetrics(textFont)
        strX = (newWidth * 0.75 - fm.stringWidth(TEXT) / 2).toInt()
        strY = (newHeight * 0.72 + fm.ascent / 2).toInt()
    }

    override fun step(width: Int, height: Int) {
        for (a in animval) {
            if (a.isSelected) {
                a.step(width, height)
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val p1 = GeneralPath()
        val p2 = GeneralPath()

        for (a in animval) {
            if (a.isSelected) {
                val x = a.x
                val y = a.y
                val ew = a.ew
                val eh = a.eh
                p1.append(Ellipse2D.Double(x, y, ew, eh), false)
                p2.append(Rectangle2D.Double(x + 5, y + 5, ew - 10, eh - 10), false)
            }
        }
        if (animval.any { it.isSelected }) {
            g2.clip = p1
            g2.clip(p2)
        }

        if (doObjects) {
            val w2 = w / 2
            val h2 = h / 2
            g2.drawImage(cimg, 0, 0, w2, h2, null)
            g2.drawImage(dimg, dukeX, dukeY, null)

            g2.paint = texturePaint
            g2.fillRect(w2, 0, w2, h2)

            g2.paint = gradient
            g2.fillRect(0, h2, w2, h2)

            g2.color = Color.LIGHT_GRAY
            g2.fillRect(w2, h2, w2, h2)
            g2.color = Color.RED
            g2.drawOval(w2, h2, w2 - 1, h2 - 1)
            g2.font = textFont
            g2.drawString("CLIPPING", strX, strY)
        } else {
            g2.color = Color.LIGHT_GRAY
            g2.fillRect(0, 0, w, h)
        }
    }

    class AnimVal(internal var isSelected: Boolean)
    {
        private var ix = 5.0
        private var iy = 3.0
        private var iw = 5.0
        private var ih = 3.0
        internal var x: Double = 0.0
        internal var y: Double = 0.0
        internal var ew: Double = 0.0
        internal var eh: Double = 0.0 // ellipse width & height

        fun step(w: Int, h: Int) {
            x += ix
            y += iy
            ew += iw
            eh += ih

            if (ew > w / 2) {
                ew = (w / 2).toDouble()
                iw = random() * -w / 16 - 1
            }
            if (ew < w / 8) {
                ew = (w / 8).toDouble()
                iw = random() * w / 16 + 1
            }
            if (eh > h / 2) {
                eh = (h / 2).toDouble()
                ih = random() * -h / 16 - 1
            }
            if (eh < h / 8) {
                eh = (h / 8).toDouble()
                ih = random() * h / 16 + 1
            }

            if (x + ew > w) {
                x = w - ew - 1
                ix = random() * -w / 32 - 1
            }
            if (y + eh > h) {
                y = h - eh - 2
                iy = random() * -h / 32 - 1
            }
            if (x < 0) {
                x = 2.0
                ix = random() * w / 32 + 1
            }
            if (y < 0) {
                y = 2.0
                iy = random() * h / 32 + 1
            }
        }

        fun reset(w: Int, h: Int) {
            x = random() * w
            y = random() * h
            ew = random() * w / 2
            eh = random() * h / 2
        }
    }

    internal class DemoControls(private val demo: ClipAnim) : CustomControls(demo.name)/*, ActionListener*/
    {
        var toolbar: JToolBar  = JToolBar().apply { isFloatable = false }

        init {
            add(toolbar)
            toolbar.add(createToolButton("Objects", true) { selected ->
                demo.doObjects = selected
                checkRepaint()
            })
            demo.animval.forEachIndexed { index, animVal ->
                val initiallySelected = index == 0
                toolbar.add(createToolButton("Clip${index + 1}", initiallySelected) { selected ->
                    animVal.isSelected = selected
                    checkRepaint()
                })
            }
        }

        private fun checkRepaint() {
            if (!demo.animating!!.isRunning) {
                demo.repaint()
            }
        }

        override fun getPreferredSize() = Dimension(200, 40)

        override fun run() {
            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
                return
            }

            (toolbar.getComponentAtIndex(2) as AbstractButton).doClick()
            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
                return
            }

            if (size.width > 400) {
                (toolbar.getComponentAtIndex(3) as AbstractButton).doClick()
            }
            thread = null
        }
    }

    companion object
    {
        const val TEXT = "CLIPPING"
        private lateinit var dimg: Image
        private lateinit var cimg: Image

        internal val texturePaint: TexturePaint = run {
            val image = BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB)
            image.createGraphics().run {
                background = Color.YELLOW
                clearRect(0, 0, 5, 5)
                color = Color.RED
                fillRect(0, 0, 3, 3)
            }
            TexturePaint(image, Rectangle(0, 0, 5, 5))
        }

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(ClipAnim())
        }
    }
}
