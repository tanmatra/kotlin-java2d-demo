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

import java2d.CControl
import java2d.ControlsSurface
import java2d.CustomControls
import java2d.RepaintingProperty
import java2d.createBooleanButton
import java2d.createToolButton
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Rectangle
import java.awt.TexturePaint
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.awt.geom.Line2D
import java.awt.image.BufferedImage
import javax.swing.AbstractButton
import javax.swing.ButtonGroup
import javax.swing.JToolBar

/**
 * Clipping an image, lines, text, texture and gradient with text.
 */
class Text : ControlsSurface()
{
    private enum class ClipType(val displayName: String, val description: String)
    {
        LINES("Lines", "Lines"),
        IMAGE("Image", "Image"),
        TEXTURE_PAINT("TP", "Texture paint"),
        GRADIENT_PAINT("GP", "Gradient paint"),
        TEXT("Text", "Text")
    }

    private var clipType: ClipType by RepaintingProperty(DEFAULT_CLIP_TYPE)

    private var doClip: Boolean by RepaintingProperty(true)

    private val image: Image = getImage("clouds.jpg")

    init {
        background = Color.WHITE
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.NORTH)

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val frc = g2.fontRenderContext
        val font = Font(Font.SANS_SERIF, Font.BOLD, 32)
        val string = "JAVA"
        var textLayout = TextLayout(string, font, frc)
        val sw1 = textLayout.bounds.width
        val sh1 = textLayout.bounds.height
        val sx = (w - 40) / sw1
        val sy = (h - 40) / sh1

        val shape1 = textLayout.getOutline(AffineTransform.getScaleInstance(sx, sy))
        val sw = shape1.bounds.getWidth()
        val sh = shape1.bounds.getHeight()

        val shape = AffineTransform.getTranslateInstance(w / 2 - sw / 2, h / 2 + sh / 2)
            .createTransformedShape(shape1)

        val r = shape.bounds

        if (doClip) {
            g2.clip(shape)
        }

        when (clipType) {
            ClipType.LINES -> {
                g2.color = Color.BLACK
                g2.fill(r)
                g2.color = Color.YELLOW
                g2.stroke = BasicStroke(1.5f)
                var j = r.y
                while (j < r.y + r.height) {
                    val line = Line2D.Float(
                        r.x.toFloat(), j.toFloat(),
                        (r.x + r.width).toFloat(), j.toFloat())
                    g2.draw(line)
                    j += 3
                }
            }
            ClipType.IMAGE -> g2.drawImage(image, r.x, r.y, r.width, r.height, null)
            ClipType.TEXTURE_PAINT -> {
                g2.paint = texturePaint
                g2.fill(r)
            }
            ClipType.GRADIENT_PAINT -> {
                g2.paint = GradientPaint(0f, 0f, Color.BLUE, w.toFloat(), h.toFloat(), Color.YELLOW)
                g2.fill(r)
            }
            ClipType.TEXT -> {
                g2.color = Color.BLACK
                g2.fill(shape.bounds)
                g2.color = Color.CYAN
                val font2 = Font(Font.SERIF, Font.BOLD, 14) // was 10
                textLayout = TextLayout("java", font2, frc)
                val sw2 = textLayout.bounds.width

                var x = r.x
                var y = (r.y + textLayout.ascent).toInt()
                val sh2 = (r.y + r.height).toDouble()
                while (y < sh2) {
                    textLayout.draw(g2, x.toFloat(), y.toFloat())
                    x += sw2.toInt()
                    if (x > r.x + r.width) {
                        x = r.x
                        y += textLayout.ascent.toInt()
                    }
                }
            }
        }
        g2.clip = Rectangle(0, 0, w, h)

        g2.color = Color.GRAY
        g2.draw(shape)
    }

    internal class DemoControls(private val demo: Text) : CustomControls(demo.name)
    {
        private val toolbar = JToolBar().apply { isFloatable = false }

        init {
            val buttonGroup = ButtonGroup()
            add(toolbar)
            toolbar.add(createBooleanButton(demo::doClip, "Clip"))
            for (clipType in enumValues<ClipType>()) {
                val initialState = (clipType == demo.clipType)
                val button = createToolButton(clipType.displayName, initialState, clipType.description) {
                    demo.clipType = clipType
                }
                toolbar.add(button)
                buttonGroup.add(button)
            }
        }

        override fun getPreferredSize() = Dimension(200, 40)

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
        private val DEFAULT_CLIP_TYPE = ClipType.LINES

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
        fun main(args: Array<String>) {
            createDemoFrame(Text())
        }
    }
}
