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
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.Paint
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
class TextureChooser(var num: Int) : JPanel(GridLayout(0, 2, 5, 5))
{
    val imageTexture: TexturePaint
        get() {
            val img = DemoImages.getImage("java-logo.gif", this)
            val imgWidth = img.getWidth(this)
            val imgHeight = img.getHeight(this)
            val bufferedImage = BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB)
            bufferedImage.createGraphics().use { imgGr ->
                imgGr.drawImage(img, 0, 0, this)
            }
            return TexturePaint(bufferedImage, Rectangle(0, 0, imgWidth, imgHeight))
        }

    val textTexture: TexturePaint
        get() {
            val font = Font("Times New Roman", Font.BOLD, 10)
            val textLayout = TextLayout("Java2D", font, FontRenderContext(null, true, false))
            val textWidth = textLayout.bounds.width.toInt()
            val textHeight = (textLayout.ascent + textLayout.descent).toInt()
            val bufferedImage = BufferedImage(textWidth, textHeight, BufferedImage.TYPE_INT_RGB)
            bufferedImage.createGraphics().use { imgGr ->
                imgGr.background = Color.WHITE
                imgGr.clearRect(0, 0, textWidth, textHeight)
                imgGr.color = Color.LIGHT_GRAY
                textLayout.draw(imgGr, 0f, textLayout.ascent)
            }
            return TexturePaint(bufferedImage, Rectangle(0, 0, textWidth, textHeight))
        }

    val gradientPaint: GradientPaint
        get() = GradientPaint(0f, 0f, Color.WHITE, 80f, 0f, Color.GREEN)

    init {
        border = TitledBorder(EtchedBorder(), "Texture Chooser")
        add(Surface(geomTexture, this, 0))
        add(Surface(imageTexture, this, 1))
        add(Surface(textTexture, this, 2))
        add(Surface(gradientPaint, this, 3))
    }

    inner class Surface(
        private val paint: Paint,
        private val textureChooser: TextureChooser,
        private val num: Int)
    : JPanel()
    {
        var clickedFrame: Boolean = false
        private var enterExitFrame = false

        init {
            background = Color.WHITE
            clickedFrame = (num == textureChooser.num)
            if (num == textureChooser.num) {
                TextureChooser.texture = paint
            }
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    TextureChooser.texture = paint
                    clickedFrame = true
                    for (component in textureChooser.components) {
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
            if (paint is TexturePaint) {
                g2.paint = paint
            } else {
                g2.paint = paint as GradientPaint
            }
            g2.fill(Rectangle(0, 0, w, h))
            if (clickedFrame || enterExitFrame) {
                g2.color = Color.GRAY
                g2.stroke = BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)
                g2.drawRect(0, 0, w - 1, h - 1)
                textureChooser.num = num
            }
        }

        override fun getMinimumSize() = preferredSize

        override fun getMaximumSize() = preferredSize

        override fun getPreferredSize() = Dimension(30, 30)
    }

    companion object
    {
        var texture: Any = geomTexture

        val geomTexture: TexturePaint
            get() {
                val SIZE = 12
                val bufferedImage = BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB)
                bufferedImage.createGraphics().use { imgGr ->
                    imgGr.antialiasing = true
                    imgGr.background = Color.WHITE
                    imgGr.clearRect(0, 0, SIZE, SIZE)
                    imgGr.color = Color(211, 211, 211, 200)
                    val ellipseSize = (SIZE - 1).toFloat()
                    imgGr.fill(Ellipse2D.Float(1f, 1f, ellipseSize, ellipseSize))
                }
                return TexturePaint(bufferedImage, Rectangle(0, 0, SIZE, SIZE))
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
