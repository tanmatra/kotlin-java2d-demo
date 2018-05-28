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

import java2d.ControlsSurface
import java2d.CustomControls
import java2d.Surface
import java.awt.BasicStroke
import java.awt.Color.BLACK
import java.awt.Color.BLUE
import java.awt.Color.CYAN
import java.awt.Color.GRAY
import java.awt.Color.RED
import java.awt.Color.WHITE
import java.awt.Color.YELLOW
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Rectangle
import java.awt.TexturePaint
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.awt.geom.Line2D
import java.awt.image.BufferedImage
import javax.swing.AbstractButton
import javax.swing.JToggleButton
import javax.swing.JToolBar

/**
 * Clipping an image, lines, text, texture and gradient with text.
 */
class Text : ControlsSurface()
{
    private var clipType = "Lines"
    protected var doClip = true

    init {
        background = WHITE
        img = getImage("clouds.jpg")
        controls = arrayOf(DemoControls(this))
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val frc = g2.fontRenderContext
        var f = Font("sansserif", Font.BOLD, 32)
        val s = "JAVA"
        var tl = TextLayout(s, f, frc)
        var sw = tl.bounds.width
        var sh = tl.bounds.height
        val sx = (w - 40) / sw
        val sy = (h - 40) / sh
        var tx = AffineTransform.getScaleInstance(sx, sy)
        var shape = tl.getOutline(tx)
        sw = shape.bounds.getWidth()
        sh = shape.bounds.getHeight()
        tx = AffineTransform.getTranslateInstance(w / 2 - sw / 2, h / 2 + sh / 2)
        shape = tx.createTransformedShape(shape)
        val r = shape.bounds

        if (doClip) {
            g2.clip(shape)
        }

        if (clipType == "Lines") {
            g2.color = BLACK
            g2.fill(r)
            g2.color = YELLOW
            g2.stroke = BasicStroke(1.5f)
            var j = r.y
            while (j < r.y + r.height) {
                val line = Line2D.Float(
                    r.x.toFloat(), j.toFloat(),
                    (r.x + r.width).toFloat(), j.toFloat()
                                       )
                g2.draw(line)
                j = j + 3
            }
        } else if (clipType == "Image") {
            g2.drawImage(img, r.x, r.y, r.width, r.height, null)
        } else if (clipType == "TP") {
            g2.paint = texturePaint
            g2.fill(r)
        } else if (clipType == "GP") {
            g2.paint = GradientPaint(0f, 0f, BLUE, w.toFloat(), h.toFloat(), YELLOW)
            g2.fill(r)
        } else if (clipType == "Text") {
            g2.color = BLACK
            g2.fill(shape.bounds)
            g2.color = CYAN
            f = Font("serif", Font.BOLD, 10)
            tl = TextLayout("java", f, frc)
            sw = tl.bounds.width

            var x = r.x
            var y = (r.y + tl.ascent).toInt()
            sh = (r.y + r.height).toDouble()
            while (y < sh) {
                tl.draw(g2, x.toFloat(), y.toFloat())
                x += sw.toInt()
                if (x > r.x + r.width) {
                    x = r.x
                    y += tl.ascent.toInt()
                }
            }
        }
        g2.clip = Rectangle(0, 0, w, h)

        g2.color = GRAY
        g2.draw(shape)
    }

    internal class DemoControls(var demo: Text) : CustomControls(demo.name), ActionListener
    {
        private val toolbar = JToolBar().apply { isFloatable = false }

        init {
            add(toolbar)
            addTool("Clip", true)
            addTool("Lines", true)
            addTool("Image", false)
            addTool("TP", false)
            addTool("GP", false)
            addTool("Text", false)
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
            if (e.source == toolbar.getComponentAtIndex(0)) {
                val b = e.source as JToggleButton
                demo.doClip = b.isSelected
            } else {
                for (comp in toolbar.components) {
                    (comp as JToggleButton).isSelected = false
                }
                val b = e.source as JToggleButton
                b.isSelected = true
                demo.clipType = b.text
            }
            demo.repaint()
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(200, 40)
        }

        override fun run() {
            try {
                Thread.sleep(1111)
            } catch (e: Exception) {
                return
            }

            val me = Thread.currentThread()
            while (thread === me) {
                for (i in 1 until toolbar.componentCount - 1) {
                    (toolbar.getComponentAtIndex(i) as AbstractButton).doClick()
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
        internal lateinit var img: Image
        internal var texturePaint: TexturePaint

        init {
            val bi = BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB)
            val big = bi.createGraphics()
            big.background = YELLOW
            big.clearRect(0, 0, 5, 5)
            big.color = RED
            big.fillRect(0, 0, 3, 3)
            texturePaint = TexturePaint(bi, Rectangle(0, 0, 5, 5))
        }

        @JvmStatic
        fun main(s: Array<String>) {
            Surface.createDemoFrame(Text())
        }
    }
}
