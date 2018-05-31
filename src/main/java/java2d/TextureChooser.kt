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
package java2d

import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Color.GRAY
import java.awt.Color.GREEN
import java.awt.Color.LIGHT_GRAY
import java.awt.Color.WHITE
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.Rectangle
import java.awt.TexturePaint
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.font.FontRenderContext
import java.awt.font.TextLayout
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.border.EtchedBorder
import javax.swing.border.TitledBorder

/**
 * Four types of Paint displayed: Geometry, Text & Image Textures and a Gradient Paint.
 * Paints can be selected with the Mouse.
 */
class TextureChooser(var num: Int) : JPanel()
{
    val imageTexture: TexturePaint
        get() {
            val img = DemoImages.getImage("java-logo.gif", this)
            val iw = img.getWidth(this)
            val ih = img.getHeight(this)
            val bi = BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB)
            val tG2 = bi.createGraphics()
            tG2.drawImage(img, 0, 0, this)
            val r = Rectangle(0, 0, iw, ih)
            return TexturePaint(bi, r)
        }

    val textTexture: TexturePaint
        get() {
            val f = Font("Times New Roman", Font.BOLD, 10)
            val tl = TextLayout("Java2D", f, FontRenderContext(null, false, false))
            val sw = tl.bounds.width.toInt()
            val sh = (tl.ascent + tl.descent).toInt()
            val bi = BufferedImage(sw, sh, BufferedImage.TYPE_INT_RGB)
            val tG2 = bi.createGraphics()
            tG2.background = WHITE
            tG2.clearRect(0, 0, sw, sh)
            tG2.color = LIGHT_GRAY
            tl.draw(tG2, 0f, tl.ascent)
            val r = Rectangle(0, 0, sw, sh)
            return TexturePaint(bi, r)
        }

    val gradientPaint: GradientPaint
        get() = GradientPaint(0f, 0f, WHITE, 80f, 0f, GREEN)

    init {
        layout = GridLayout(0, 2, 5, 5)
        border = TitledBorder(EtchedBorder(), "Texture Chooser")

        add(Surface(geomTexture, this, 0))
        add(Surface(imageTexture, this, 1))
        add(Surface(textTexture, this, 2))
        add(Surface(gradientPaint, this, 3))
    }

    inner class Surface(private val t: Any, private val tc: TextureChooser, private val num: Int) : JPanel()
    {
        var clickedFrame: Boolean = false
        private var enterExitFrame = false

        init {
            background = Color.WHITE
            clickedFrame = num == tc.num
            if (num == tc.num) {
                TextureChooser.texture = t
            }
            addMouseListener(object : MouseAdapter() {

                override fun mouseClicked(e: MouseEvent?) {
                    TextureChooser.texture = t
                    clickedFrame = true

                    for (component in tc.components) {
                        if (component is Surface) {
                            if (component != this@Surface && component.clickedFrame) {
                                component.clickedFrame = false
                                component.repaint()
                            }
                        }
                    }

                    // ABP
                    Java2Demo.controls.textureCheckBox.run {
                        if (isSelected) {
                            doClick()
                            doClick()
                        }
                    }
                }

                override fun mouseEntered(e: MouseEvent?) {
                    enterExitFrame = true
                    repaint()
                }

                override fun mouseExited(e: MouseEvent?) {
                    enterExitFrame = false
                    repaint()
                }
            })
        }

        public override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g as Graphics2D
            val w = size.width
            val h = size.height
            if (t is TexturePaint) {
                g2.paint = t
            } else {
                g2.paint = t as GradientPaint
            }
            g2.fill(Rectangle(0, 0, w, h))
            if (clickedFrame || enterExitFrame) {
                g2.color = GRAY
                val bs = BasicStroke(
                    3f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER
                                    )
                g2.stroke = bs
                g2.drawRect(0, 0, w - 1, h - 1)
                tc.num = num
            }
        }

        override fun getMinimumSize(): Dimension {
            return preferredSize
        }

        override fun getMaximumSize(): Dimension {
            return preferredSize
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(30, 30)
        }
    }

    companion object
    {
        var texture: Any = geomTexture

        val geomTexture: TexturePaint
            get() {
                val bi = BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB)
                val tG2 = bi.createGraphics()
                tG2.background = WHITE
                tG2.clearRect(0, 0, 5, 5)
                tG2.color = Color(211, 211, 211, 200)
                tG2.fill(Ellipse2D.Float(0f, 0f, 5f, 5f))
                val r = Rectangle(0, 0, 5, 5)
                return TexturePaint(bi, r)
            }

        @JvmStatic
        fun main(s: Array<String>) {
            JFrame("Java2D Demo - TextureChooser").apply {
                addWindowListener(object : WindowAdapter() {
                    override fun windowClosing(e: WindowEvent?) {
                        System.exit(0)
                    }
                })
                contentPane.add(TextureChooser(0), BorderLayout.CENTER)
                pack()
                size = Dimension(400, 400)
                isVisible = true
            }
        }
    }
}
