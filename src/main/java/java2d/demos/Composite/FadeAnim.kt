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
import java2d.DemoImages
import java2d.createTitledSlider
import java2d.toBufferedImage
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
    private val objects = ArrayList<GraphicObject>(20)

    private var shapesCount: Int = 0
        set(value) {
            if (value < field) {
                val shapes = objects.filterIsInstance<ShapeObject>()
                objects.removeAll(shapes.subList(value, shapes.size))
            } else {
                for (i in field until value) {
                    objects.add(createShapeObject(i))
                }
            }
            field = value
            checkRepaint()
        }

    private fun createShapeObject(index: Int): GraphicObject {
        val shape: Shape = when (index % 7) {
            0 -> GeneralPath()
            1 -> Rectangle2D.Double()
            2 -> Ellipse2D.Double()
            3 -> Arc2D.Double()
            4 -> RoundRectangle2D.Double()
            5 -> CubicCurve2D.Double()
            6 -> QuadCurve2D.Double()
            else -> error(7)
        }
        return ShapeObject(shape, PAINTS[index % PAINTS.size]).apply {
            reset(width, height)
        }
    }

    private var stringsCount: Int = 0
        set(value) {
            if (value < field) {
                val texts = objects.filterIsInstance<TextObject>()
                objects.removeAll(texts.subList(value, texts.size))
            } else {
                for (i in field until value) {
                    objects.add(createStringObject(i))
                }
            }
            field = value
            checkRepaint()
        }

    private fun createStringObject(index: Int): GraphicObject {
        val string = STRINGS[index % STRINGS.size]
        val font = FONTS[index % FONTS.size]
        val paint = PAINTS[index % PAINTS.size]
        return TextObject(string, font, paint, this).apply {
            reset(width, height)
        }
    }

    private var imagesCount: Int = 0
        set(value) {
            if (value < field) {
                val images = objects.filter { (it is ImageObject) or (it is AnimatedImageObject) }
                objects.removeAll(images.subList(value, images.size))
            } else {
                for (i in field until value) {
                    objects.add(createImageObeject(i))
                }
            }
            field = value
            checkRepaint()
        }

    private fun createImageObeject(index: Int): GraphicObject {
        val imageInfo = IMAGE_INFOS[index % IMAGE_INFOS.size]
        return imageInfo.createGraphicObject(this).apply {
            reset(width, height)
        }
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
        for (graphicObject in objects) {
            graphicObject.paint(g2)
        }
    }

    internal class DemoControls(demo: FadeAnim) : CustomControls(demo.name)
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

        private val FONTS = arrayOf(
            Font("Times New Roman", Font.PLAIN, 64),
            Font(Font.SERIF, Font.BOLD + Font.ITALIC, 24),
            Font("Courier", Font.BOLD, 36),
            Font("Arial", Font.BOLD + Font.ITALIC, 48),
            Font("Helvetica", Font.PLAIN, 52))

        private val STRINGS = arrayOf(
            "Alpha", "Composite", "Src", "SrcOver", "SrcIn", "SrcOut", "Clear", "DstOver", "DstIn")

        private val IMAGE_INFOS = arrayOf(
            ImageInfo.Strip("jumptojavastrip.png", 80),
            ImageInfo.Basic("duke.gif"),
            ImageInfo.Basic("star7.gif"))

        private val PAINTS = arrayOf(
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.CYAN, TEXTURE_PAINT,
            Color.YELLOW, Color.LIGHT_GRAY, Color.WHITE)

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(FadeAnim())
        }
    }
}

sealed class ImageInfo(val name: String)
{
    abstract fun createGraphicObject(component: Component): GraphicObject

    class Basic(name: String) : ImageInfo(name) {
        override fun createGraphicObject(component: Component): GraphicObject =
            ImageObject(DemoImages.getImage(name, component))
    }
    class Strip(name: String, private val cellWidth: Int) : ImageInfo(name) {
        override fun createGraphicObject(component: Component): GraphicObject =
            AnimatedImageObject(DemoImages.getImage(name, component), cellWidth)
    }
}

abstract class GraphicObject
{
    protected var x: Double = 0.0
    protected var y: Double = 0.0
    private var alpha: Float = Math.random().toFloat()
    private var alphaDirection: Int = if (Math.random() > 0.5) UP else DOWN

    protected abstract fun randomizePosition(width: Int, height: Int)

    open fun reset(width: Int, height: Int) {
        randomizePosition(width, height)
    }

    open fun step(w: Int, h: Int) {
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
                    randomizePosition(w, h)
                }
            }
        }
    }

    fun paint(g2: Graphics2D) {
        g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        g2.translate(x, y)
        render(g2)
        g2.translate(-x, -y)
    }

    protected abstract fun render(g2: Graphics2D)

    companion object
    {
        const val UP = 0
        const val DOWN = 1
    }
}

class TextObject(
    private val string: String,
    private val font: Font,
    private val paint: Paint,
    component: Component
) : GraphicObject()
{
    private val textWidth: Int
    private val textHeight: Int

    init {
        val fontMetrics = component.getFontMetrics(font)
        textWidth = fontMetrics.stringWidth(string)
        textHeight = fontMetrics.height
    }

    override fun randomizePosition(width: Int, height: Int) {
        x = Math.random() * (width - textWidth)
        y = (Math.random() * height).coerceAtLeast(textHeight.toDouble())
    }

    override fun render(g2: Graphics2D) {
        g2.font = font
        g2.paint = paint
        g2.drawString(string, 0, 0)
    }
}

class ImageObject(private val image: Image) : GraphicObject()
{
    override fun randomizePosition(width: Int, height: Int) {
        x = Math.random() * (width - image.getWidth(null))
        y = Math.random() * (height - image.getHeight(null))
    }

    override fun render(g2: Graphics2D) {
        g2.drawImage(image, 0, 0, null) // need observer?
    }
}

class AnimatedImageObject(image: Image, private val cellWidth: Int) : GraphicObject()
{
    private val imageWidth = image.getWidth(null)
    private val imageHeight = image.getHeight(null)
    private val bufferedImage: BufferedImage = image.toBufferedImage()
    private var subimageX: Int = 0
    private var subimage: Image = createSubimage()

    private fun createSubimage(): Image = bufferedImage.getSubimage(subimageX, 0, cellWidth, imageHeight)

    override fun randomizePosition(width: Int, height: Int) {
        x = Math.random() * (width - subimage.getWidth(null))
        y = Math.random() * (height - subimage.getHeight(null))
    }

    override fun step(w: Int, h: Int) {
        subimageX += cellWidth
        if (subimageX >= imageWidth) {
            subimageX = 0
        }
        subimage = createSubimage()
        super.step(w, h)
    }

    override fun render(g2: Graphics2D) {
        g2.drawImage(subimage, 0, 0, null)
    }
}

class ShapeObject(private val shape: Shape, private val paint: Paint) : GraphicObject()
{
    override fun randomizePosition(width: Int, height: Int) {
        val bounds = shape.bounds
        x = Math.random() * (width - bounds.width)
        y = Math.random() * (height - bounds.height)
    }

    override fun reset(width: Int, height: Int) {
        super.reset(width, height)
        val frameWidth = 20 + Math.random() * ((if (width == 0) 400 else width) / 4)
        val frameHeight = 20 + Math.random() * ((if (height == 0) 300 else height) / 4)
        when (shape) {
            is Ellipse2D -> shape.setFrame(0.0, 0.0, frameWidth, frameHeight)
            is Rectangle2D -> shape.setRect(0.0, 0.0, frameWidth, frameWidth)
            is RoundRectangle2D -> shape.setRoundRect(0.0, 0.0, frameHeight, frameHeight, 20.0, 20.0)
            is Arc2D -> shape.setArc(0.0, 0.0, frameHeight, frameHeight, 45.0, 270.0, Arc2D.PIE)
            is QuadCurve2D -> shape.setCurve(0.0, 0.0, width * 0.2, height * 0.4, width * 0.4, 0.0)
            is CubicCurve2D -> shape.setCurve(0.0, 0.0, 30.0, -60.0, 60.0, 60.0, 90.0, 0.0)
            is GeneralPath -> shape.run {
                reset()
                val size = frameWidth.toFloat()
                moveTo(-size / 2.0f, -size / 8.0f)
                lineTo(+size / 2.0f, -size / 8.0f)
                lineTo(-size / 4.0f, +size / 2.0f)
                lineTo(+0.0f, -size / 2.0f)
                lineTo(+size / 4.0f, +size / 2.0f)
                closePath()
            }
        }
    }

    override fun render(g2: Graphics2D) {
        g2.paint = paint
        when (shape) {
            is QuadCurve2D, is CubicCurve2D -> {
                g2.stroke = STROKE
                g2.draw(shape)
            }
            else -> g2.fill(shape)
        }
    }

    companion object {
        private val STROKE = BasicStroke(6f)
    }
}
