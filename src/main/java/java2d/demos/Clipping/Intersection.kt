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
import java2d.RepaintingProperty
import java2d.createBooleanButton
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.Shape
import java.awt.font.FontRenderContext
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import java.awt.geom.Rectangle2D
import javax.swing.AbstractButton
import javax.swing.JToolBar

/**
 * Animated intersection clipping of lines, an image and a textured rectangle.
 */
class Intersection : AnimatingControlsSurface()
{
    private var xx: Int = 0
    private var yy: Int = 0
    private var ww: Int = 0
    private var hh: Int = 0
    private var direction = HEIGHT_DECREASE
    private var angdeg: Int = 0
    private lateinit var textshape: Shape
    private var sw: Double = 0.0
    private var sh: Double = 0.0
    private lateinit var ovals: GeneralPath
    private lateinit var rectshape: Rectangle2D

    private var doIntersection: Boolean by RepaintingProperty(true)
    private var doOvals: Boolean by RepaintingProperty(true)
    private var doText: Boolean by RepaintingProperty(false)

    private var threeSixty: Boolean = false

    init {
        background = Color.WHITE
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.NORTH)

    override fun reset(newWidth: Int, newHeight: Int) {
        yy = 0
        xx = yy
        ww = newWidth - 1
        hh = newHeight
        direction = HEIGHT_DECREASE
        angdeg = 0
        val frc = FontRenderContext(null, true, false)
        val f = Font("serif", Font.BOLD, 32)
        val tl = TextLayout("J2D", f, frc)
        sw = tl.bounds.width
        sh = tl.bounds.height
        val size = Math.min(newWidth, newHeight)
        val sx = (size - 40) / sw
        val sy = (size - 100) / sh
        val tx = AffineTransform.getScaleInstance(sx, sy)
        textshape = tl.getOutline(tx)
        rectshape = textshape.bounds
        sw = rectshape.width
        sh = rectshape.height
        ovals = GeneralPath().apply {
            append(Ellipse2D.Double(10.0, 10.0, 20.0, 20.0), false)
            append(Ellipse2D.Double((newWidth - 30).toDouble(), 10.0, 20.0, 20.0), false)
            append(Ellipse2D.Double(10.0, (newHeight - 30).toDouble(), 20.0, 20.0), false)
            append(Ellipse2D.Double((newWidth - 30).toDouble(), (newHeight - 30).toDouble(), 20.0, 20.0), false)
        }
    }

    override fun step(width: Int, height: Int) {
        when (direction) {
            HEIGHT_DECREASE -> {
                yy += 2
                hh -= 4
                if (yy >= height / 2) {
                    direction = HEIGHT_INCREASE
                }
            }
            HEIGHT_INCREASE -> {
                yy -= 2
                hh += 4
                if (yy <= 0) {
                    direction = WIDTH_DECREASE
                    hh = height - 1
                    yy = 0
                }
            }
            WIDTH_DECREASE -> {
                xx += 2
                ww -= 4
                if (xx >= width / 2) {
                    direction = WIDTH_INCREASE
                }
            }
            WIDTH_INCREASE -> {
                xx -= 2
                ww += 4
                if (xx <= 0) {
                    direction = HEIGHT_DECREASE
                    ww = width - 1
                    xx = 0
                }
            }
        }
        angdeg += 5
        if (angdeg == 360) {
            angdeg = 0
            threeSixty = true
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val rect = Rectangle(xx, yy, ww, hh)

        val tx = AffineTransform().apply {
            rotate(Math.toRadians(angdeg.toDouble()), (w / 2).toDouble(), (h / 2).toDouble())
            translate(w / 2 - sw / 2, sh + (h - sh) / 2)
        }

        val path = GeneralPath()
        if (doOvals) {
            path.append(ovals, false)
        }
        if (doText) {
            path.append(tx.createTransformedShape(textshape), false)
        } else {
            path.append(tx.createTransformedShape(rectshape), false)
        }

        if (doIntersection) {
            g2.clip(rect)
            g2.clip(path)
        }

        g2.color = Color.GREEN
        g2.fill(rect)

        g2.clip = Rectangle(0, 0, w, h)

        g2.color = Color.LIGHT_GRAY
        g2.draw(rect)
        g2.color = Color.BLACK
        g2.draw(path)
    }

    internal class DemoControls(var demo: Intersection) : CustomControls(demo.name)
    {
        private val toolbar = JToolBar().apply { isFloatable = false }

        init {
            add(toolbar)
            toolbar.add(createBooleanButton(demo::doIntersection, "Intersect"))
            toolbar.add(createBooleanButton(demo::doText, "Text"))
            toolbar.add(createBooleanButton(demo::doOvals, "Ovals"))
        }

        override fun getPreferredSize() = Dimension(200, 40)

        override fun run() {
            val me = Thread.currentThread()
            while (thread === me) {
                if (demo.threeSixty) {
                    (toolbar.getComponentAtIndex(1) as AbstractButton).doClick()
                    demo.threeSixty = false
                }
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    return
                }
            }
            thread = null
        }
    }

    companion object
    {
        private const val HEIGHT_DECREASE = 0
        private const val HEIGHT_INCREASE = 1
        private const val WIDTH_DECREASE = 2
        private const val WIDTH_INCREASE = 3

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(Intersection())
        }
    }
}
