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
package java2d.demos.Composite

import java2d.AnimatingControlsSurface
import java2d.CControl
import java2d.CustomControls
import java2d.createTitledSlider
import java2d.use
import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Paint
import java.awt.Rectangle
import java.awt.Shape
import java.awt.TexturePaint
import java.awt.geom.Arc2D
import java.awt.geom.CubicCurve2D
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import java.awt.geom.QuadCurve2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.util.ArrayList
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JToolBar
import javax.swing.SwingConstants

/**
 * Animation of compositing shapes, text and images fading in and out.
 */
class FadeAnim : AnimatingControlsSurface()
{
    private val objects = ArrayList<ObjectData>(20)

    private var shapesCount: Int = 0
        set(value) {
            if (value < field) {
                val shapes = objects.filter { it.item is Shape }
                objects.removeAll(shapes.subList(value, shapes.size))
            } else {
                for (i in field until value) {
                    objects.add(createShapeObject(i))
                }
            }
            field = value
            checkRepaint()
        }

    private fun createShapeObject(index: Int): ObjectData {
        val item: Shape = when (index % 7) {
            0 -> GeneralPath()
            1 -> Rectangle2D.Double()
            2 -> Ellipse2D.Double()
            3 -> Arc2D.Double()
            4 -> RoundRectangle2D.Double()
            5 -> CubicCurve2D.Double()
            6 -> QuadCurve2D.Double()
            else -> error(7)
        }
        val objectData = ObjectData(item, PAINTS[index % PAINTS.size])
        objectData.reset(width, height)
        return objectData
    }

    private var stringsCount: Int = 0
        set(value) {
            if (value < field) {
                val textDatas = objects.filter { it.item is TextData }
                objects.removeAll(textDatas.subList(value, textDatas.size))
            } else {
                for (i in field until value) {
                    objects.add(createStringObject(i))
                }
            }
            field = value
            checkRepaint()
        }

    private fun createStringObject(index: Int): ObjectData {
        val textData = TextData(STRINGS[index % STRINGS.size], FONTS[index % FONTS.size], this)
        val objectData = ObjectData(textData, PAINTS[index % PAINTS.size])
        objectData.reset(width, height)
        return objectData
    }

    private var imagesCount: Int = 0
        set(value) {
            if (value < field) {
                val images = objects.filter { it.item is Image }
                objects.removeAll(images.subList(value, images.size))
            } else {
                for (i in field until value) {
                    objects.add(createImageObeject(i))
                }
            }
            field = value
            checkRepaint()
        }

    private fun createImageObeject(index: Int): ObjectData {
        val name = IMAGE_NAMES[index % IMAGE_NAMES.size]
        var image = getImage(name)
        if (name == "jumptojavastrip.png") {
            val iw = image.getWidth(null)
            val ih = image.getHeight(null)
            image = BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB).apply {
                createGraphics().use { gr -> gr.drawImage(image, 0, 0, null) }
            }
        }
        val objectData = ObjectData(image, Color.BLACK)
        objectData.reset(width, height)
        return objectData
    }

    init {
        background = Color.BLACK
        stringsCount = 2
        imagesCount = 3
        shapesCount = 8
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.EAST)

    override fun reset(newWidth: Int, newHeight: Int) {
        for (obj in objects) {
            obj.reset(newWidth, newHeight)
        }
    }

    override fun step(width: Int, height: Int) {
        for (obj in objects) {
            obj.step(width, height)
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        for (objectData in objects) {
            g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, objectData.alpha)
            g2.paint = objectData.paint
            g2.translate(objectData.x, objectData.y)

            val item = objectData.item
            when (item) {
                is Image -> g2.drawImage(item, 0, 0, this)
                is TextData -> {
                    g2.font = item.font
                    g2.drawString(item.string, 0, 0)
                }
                is QuadCurve2D, is CubicCurve2D -> {
                    g2.stroke = BASIC_STROKE
                    g2.draw(item as Shape)
                }
                is Shape -> g2.fill(item)
            }
            g2.translate(-objectData.x, -objectData.y)
        }
    }

    internal class TextData(val string: String, val font: Font, cmp: Component)
    {
        val width: Int
        val height: Int

        init {
            val fontMetrics = cmp.getFontMetrics(font)
            width = fontMetrics.stringWidth(string)
            height = fontMetrics.height
        }
    }

    internal class ObjectData(item: Any, val paint: Paint)
    {
        var item: Any
        var bufferedImage: BufferedImage?
        var x: Double = 0.0
        var y: Double = 0.0
        var alpha: Float = 0.0f
        var alphaDirection: Int = 0
        var imgX: Int = 0

        init {
            this.item = item
            if (item is BufferedImage) {
                bufferedImage = item
                this.item = item.getSubimage(0, 0, 80, 80)
            } else {
                bufferedImage = null
            }
            getRandomXY(300, 250)
            alpha = Math.random().toFloat()
            alphaDirection = if (Math.random() > 0.5) UP else DOWN
        }

        private fun getRandomXY(w: Int, h: Int) {
            val item = item
            when (item) {
                is TextData -> {
                    x = Math.random() * (w - item.width)
                    y = Math.random() * h
                    y = if (y < item.height) item.height.toDouble() else y
                }
                is Image -> {
                    x = Math.random() * (w - item.getWidth(null))
                    y = Math.random() * (h - item.getHeight(null))
                }
                is Shape -> {
                    val bounds = item.bounds
                    x = Math.random() * (w - bounds.width)
                    y = Math.random() * (h - bounds.height)
                }
            }
        }

        fun reset(width: Int, height: Int) {
            getRandomXY(width, height)
            val ww = 20 + Math.random() * ((if (width == 0) 400 else width) / 4)
            val hh = 20 + Math.random() * ((if (height == 0) 300 else height) / 4)
            val item = item
            when (item) {
                is Ellipse2D -> item.setFrame(0.0, 0.0, ww, hh)
                is Rectangle2D -> item.setRect(0.0, 0.0, ww, ww)
                is RoundRectangle2D -> item.setRoundRect(0.0, 0.0, hh, hh, 20.0, 20.0)
                is Arc2D -> item.setArc(0.0, 0.0, hh, hh, 45.0, 270.0, Arc2D.PIE)
                is QuadCurve2D -> item.setCurve(0.0, 0.0, width * 0.2, height * 0.4, width * 0.4, 0.0)
                is CubicCurve2D -> item.setCurve(0.0, 0.0, 30.0, -60.0, 60.0, 60.0, 90.0, 0.0)
                is GeneralPath -> item.run {
                    reset()
                    val size = ww.toFloat()
                    moveTo(-size / 2.0f, -size / 8.0f)
                    lineTo(+size / 2.0f, -size / 8.0f)
                    lineTo(-size / 4.0f, +size / 2.0f)
                    lineTo(+0.0f, -size / 2.0f)
                    lineTo(+size / 4.0f, +size / 2.0f)
                    closePath()
                }
            }
        }

        fun step(w: Int, h: Int) {
            if (item is BufferedImage) {
                imgX += 80
                if (imgX == 800) {
                    imgX = 0
                }
                item = bufferedImage!!.getSubimage(imgX, 0, 80, 80)
            }
            when (alphaDirection) {
                UP -> {
                    alpha += 0.05f
                    if (alpha > 0.99f) {
                        alphaDirection = DOWN
                        alpha = 1.0f
                    }
                }
                DOWN -> {
                    alpha -= 0.05f
                    if (alpha < 0.01) {
                        alphaDirection = UP
                        alpha = 0.0f
                        getRandomXY(w, h)
                    }
                }
            }
        }

        companion object {
            const val UP = 0
            const val DOWN = 1
        }
    }

    internal class DemoControls(var demo: FadeAnim) : CustomControls(demo.name)
    {
        private val shapeSlider = createTitledSlider("Shapes", 20, demo::shapesCount)
        private val stringSlider = createTitledSlider("Strings", 10, demo::stringsCount)
        private val imageSlider = createTitledSlider("Images", 10, demo::imagesCount)

        init {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(Box.createVerticalStrut(5))

            val toolbar = JToolBar(SwingConstants.VERTICAL).apply { isFloatable = false }
            toolbar.add(shapeSlider)
            toolbar.add(stringSlider)
            toolbar.add(imageSlider)

            add(toolbar)
        }

        override fun getPreferredSize() = Dimension(120, 0)

        override fun run() {
            try {
                Thread.sleep(999)
            } catch (e: InterruptedException) {
                return
            }

            shapeSlider.value = (Math.random() * 5).toInt()
            stringSlider.value = 10
            thread = null
        }
    }

    companion object
    {
        private val TEXTURE_PAINT: TexturePaint = run {
            val w = 10
            val h = 10
            val bufferedImage = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
            bufferedImage.createGraphics().use { gfx ->
                val oc = Color.BLUE
                val ic = Color.GREEN
                gfx.paint = GradientPaint(0f, 0f, oc, w * 0.35f, h * 0.35f, ic)
                gfx.fillRect(0, 0, w / 2, h / 2)
                gfx.paint = GradientPaint(w.toFloat(), 0f, oc, w * 0.65f, h * 0.35f, ic)
                gfx.fillRect(w / 2, 0, w / 2, h / 2)
                gfx.paint = GradientPaint(0f, h.toFloat(), oc, w * 0.35f, h * 0.65f, ic)
                gfx.fillRect(0, h / 2, w / 2, h / 2)
                gfx.paint = GradientPaint(w.toFloat(), h.toFloat(), oc, w * 0.65f, h * 0.65f, ic)
                gfx.fillRect(w / 2, h / 2, w / 2, h / 2)
            }
            TexturePaint(bufferedImage, Rectangle(0, 0, w, h))
        }

        private val BASIC_STROKE = BasicStroke(6f)

        private val FONTS = arrayOf(
            Font("Times New Roman", Font.PLAIN, 64),
            Font(Font.SERIF, Font.BOLD + Font.ITALIC, 24),
            Font("Courier", Font.BOLD, 36),
            Font("Arial", Font.BOLD + Font.ITALIC, 48),
            Font("Helvetica", Font.PLAIN, 52))

        private val STRINGS = arrayOf(
            "Alpha", "Composite", "Src", "SrcOver", "SrcIn", "SrcOut", "Clear", "DstOver", "DstIn")

        private val IMAGE_NAMES = arrayOf("jumptojavastrip.png", "duke.gif", "star7.gif")

        private val PAINTS = arrayOf(
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.CYAN, TEXTURE_PAINT,
            Color.YELLOW, Color.LIGHT_GRAY, Color.WHITE)

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(FadeAnim())
        }
    }
}
