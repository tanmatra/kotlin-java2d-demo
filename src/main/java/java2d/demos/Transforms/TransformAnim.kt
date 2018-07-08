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

package java2d.demos.Transforms

import java2d.AnimatingControlsSurface
import java2d.CControl
import java2d.CustomControls
import java2d.antialiasing
import java2d.createTitledSlider
import java2d.createToolButton
import java2d.use
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Color.BLACK
import java.awt.Color.BLUE
import java.awt.Color.CYAN
import java.awt.Color.GREEN
import java.awt.Color.LIGHT_GRAY
import java.awt.Color.MAGENTA
import java.awt.Color.ORANGE
import java.awt.Color.PINK
import java.awt.Color.RED
import java.awt.Color.WHITE
import java.awt.Color.YELLOW
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Paint
import java.awt.Rectangle
import java.awt.Shape
import java.awt.TexturePaint
import java.awt.geom.AffineTransform
import java.awt.geom.Arc2D
import java.awt.geom.CubicCurve2D
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import java.awt.geom.QuadCurve2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.util.ArrayList
import javax.swing.AbstractButton
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JToolBar
import javax.swing.SwingConstants
import kotlin.reflect.KMutableProperty0

/**
 * Animation of shapes, text and images rotating, scaling and translating
 * around a canvas.
 */
class TransformAnim : AnimatingControlsSurface()
{
    private val objDatas = ArrayList<ObjData>(13)
    private var doRotate = true
    private var doTranslate = true
    private var doScale = true
    private var doShear: Boolean = false

    private var shapesCount: Int = 0
        set(value) {
            if (value < field) {
                val v = objDatas.filter { it.shape is Shape }
                objDatas.removeAll(v.subList(value, v.size))
            } else {
                for (i in field until value) {
                    val obj: Shape = when (i % 7) {
                        0 -> GeneralPath()
                        1 -> Rectangle2D.Double()
                        2 -> Ellipse2D.Double()
                        3 -> Arc2D.Double()
                        4 -> RoundRectangle2D.Double()
                        5 -> CubicCurve2D.Double()
                        6 -> QuadCurve2D.Double()
                        else -> error(7)
                    }
                    val objData = ObjData(obj, PAINTS[i % PAINTS.size])
                    objData.reset(width, height)
                    objDatas.add(objData)
                }
            }
            field = value
            checkRepaint()
        }

    private var imagesCount: Int = 0
        set(value) {
            if (value < field) {
                val v = objDatas.filter { it.shape is Image }
                objDatas.removeAll(v.subList(value, v.size))
            } else {
                for (i in field until value) {
                    val obj = getImage(IMAGES[i % IMAGES.size])
                    val objData = ObjData(obj, Color.BLACK)
                    objData.reset(width, height)
                    objDatas.add(objData)
                }
            }
            field = value
            checkRepaint()
        }

    private var stringsCount: Int = 0
        set(value) {
            if (value < field) {
                val v = objDatas.filter { it.shape is TextData }
                objDatas.removeAll(v.subList(value, v.size))
            } else {
                for (i in field until value) {
                    val obj = TextData(STRINGS[i % STRINGS.size], FONTS[i % FONTS.size])
                    val objData = ObjData(obj, PAINTS[i % PAINTS.size])
                    objData.reset(width, height)
                    objDatas.add(objData)
                }
            }
            field = value
            checkRepaint()
        }

    init {
        background = BLACK
        stringsCount = 1
        imagesCount = 2
        shapesCount = 10
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.EAST)

    override fun reset(newWidth: Int, newHeight: Int) {
        for (objData in objDatas) {
            objData.reset(newWidth, newHeight)
        }
    }

    override fun step(width: Int, height: Int) {
        for (objData in objDatas) {
            objData.step(width, height, this)
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        for (objData in objDatas) {
            g2.transform = objData.at
            g2.paint = objData.paint
            val obj = objData.shape
            when (obj) {
                is Image -> g2.drawImage(obj, 0, 0, this)
                is TextData -> {
                    g2.font = obj.font
                    g2.drawString(obj.string, 0, 0)
                }
                is QuadCurve2D, is CubicCurve2D -> {
                    g2.stroke = BASIC_STROKE
                    g2.draw(obj as Shape)
                }
                is Shape -> g2.fill(obj)
            }
        }
    }

    private fun checkRepaint() {
        if (!isRunning) {
            repaint()
        }
    }

    internal class TextData(var string: String, var font: Font)

    internal class ObjData(var shape: Any, var paint: Paint)
    {
        var x: Double = 0.0
        var y: Double = 0.0
        private var ix = 5.0
        private var iy = 3.0
        var rotate: Int = 0
        private var scale: Double = 0.0
        private var shear: Double = 0.0
        private var scaleDirection: Int = 0
        private var shearDirection: Int = 0
        var at = AffineTransform()

        init {
            rotate = (Math.random() * 360).toInt()
            scale = Math.random() * 1.5
            scaleDirection = if (Math.random() > 0.5) UP else DOWN
            shear = Math.random() * 0.5
            shearDirection = if (Math.random() > 0.5) UP else DOWN
        }

        fun reset(w: Int, h: Int) {
            x = Math.random() * w
            y = Math.random() * h
            val ww = 20 + Math.random() * ((if (w == 0) 400 else w) / 4)
            val hh = 20 + Math.random() * ((if (h == 0) 300 else h) / 4)
            val shape = shape
            when (shape) {
                is Ellipse2D -> shape.setFrame(0.0, 0.0, ww, hh)
                is Rectangle2D -> shape.setRect(0.0, 0.0, ww, ww)
                is RoundRectangle2D -> shape.setRoundRect(0.0, 0.0, hh, hh, 20.0, 20.0)
                is Arc2D -> shape.setArc(0.0, 0.0, hh, hh, 45.0, 270.0, Arc2D.PIE)
                is QuadCurve2D -> shape.setCurve(0.0, 0.0, w * 0.2, h * 0.4, w * 0.4, 0.0)
                is CubicCurve2D -> shape.setCurve(0.0, 0.0, 30.0, -60.0, 60.0, 60.0, 90.0, 0.0)
                is GeneralPath -> {
                    this.shape = GeneralPath().apply {
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
        }

        fun step(w: Int, h: Int, demo: TransformAnim) {
            at.setToIdentity()
            if (demo.doRotate) {
                rotate += 5
                if (rotate == 360) {
                    rotate = 0
                }
                at.rotate(Math.toRadians(rotate.toDouble()), x, y)
            }
            at.translate(x, y)
            if (demo.doTranslate) {
                x += ix
                y += iy
                if (x > w) {
                    x = (w - 1).toDouble()
                    ix = Math.random() * -w / 32 - 1
                }
                if (x < 0) {
                    x = 2.0
                    ix = Math.random() * w / 32 + 1
                }
                if (y > h) {
                    y = (h - 2).toDouble()
                    iy = Math.random() * -h / 32 - 1
                }
                if (y < 0) {
                    y = 2.0
                    iy = Math.random() * h / 32 + 1
                }
            }
            if (demo.doScale && scaleDirection == UP) {
                scale += 0.05
                if (scale > 1.5) {
                    scaleDirection = DOWN
                }
            } else if (demo.doScale && scaleDirection == DOWN) {
                scale -= .05
                if (scale < 0.5) {
                    scaleDirection = UP
                }
            }
            if (demo.doScale) {
                at.scale(scale, scale)
            }
            if (demo.doShear && shearDirection == UP) {
                shear += 0.05
                if (shear > 0.5) {
                    shearDirection = DOWN
                }
            } else if (demo.doShear && shearDirection == DOWN) {
                shear -= .05
                if (shear < -0.5) {
                    shearDirection = UP
                }
            }
            if (demo.doShear) {
                at.shear(shear, shear)
            }
        }

        companion object
        {
            const val UP = 0
            const val DOWN = 1
        }
    }

    internal class DemoControls(private val demo: TransformAnim) : CustomControls(demo.name)
    {
        private val toolbar = JToolBar().apply { isFloatable = false }

        init {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(Box.createVerticalStrut(5))

            add(JToolBar(SwingConstants.VERTICAL).apply {
                isFloatable = false
                add(createTitledSlider("Shapes", 20, demo::shapesCount))
                add(createTitledSlider("Strings", 10, demo::stringsCount))
                add(createTitledSlider("Images", 10, demo::imagesCount))
            })

            toolbar.add(createButton("T", "Translate", demo::doTranslate))
            toolbar.add(createButton("R", "Rotate", demo::doRotate))
            toolbar.add(createButton("SC", "Scale", demo::doScale))
            toolbar.add(createButton("SH", "Shear", demo::doShear))
            add(toolbar)
        }

        private fun createButton(text: String, toolTip: String, property: KMutableProperty0<Boolean>): AbstractButton {
            return createToolButton(text, property.get(), toolTip) { selected ->
                property.set(selected)
                demo.checkRepaint()
            }
        }

        override fun getPreferredSize() = Dimension(150, 38) // (80, 38)

        override fun run() {
            val me = Thread.currentThread()
            while (thread === me) {
                for (i in 1 until toolbar.componentCount) {
                    try {
                        Thread.sleep(4444)
                    } catch (e: InterruptedException) {
                        return
                    }
                    (toolbar.getComponentAtIndex(i) as AbstractButton).doClick()
                }
            }
            thread = null
        }
    }

    companion object
    {
        private val TEXTURE_PAINT: TexturePaint = run {
            val bufferedImage = BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)
            bufferedImage.createGraphics().use { gfx ->
                gfx.antialiasing = true
                gfx.color = Color.RED
                gfx.fillOval(0, 0, 9, 9)
            }
            TexturePaint(bufferedImage, Rectangle(0, 0, 10, 10))
        }

        private val BASIC_STROKE = BasicStroke(6f)

        private val FONTS = arrayOf(
            Font("Times New Roman", Font.PLAIN, 48),
            Font(Font.SERIF, Font.BOLD or Font.ITALIC, 24),
            Font("Courier", Font.BOLD, 36),
            Font("Arial", Font.BOLD or Font.ITALIC, 64),
            Font("Helvetica", Font.PLAIN, 52))

        private val STRINGS = arrayOf("Transformation", "Rotate", "Translate", "Shear", "Scale")

        private val IMAGES = arrayOf("duke.gif")

        private val PAINTS = arrayOf(
            RED,
            BLUE,
            TEXTURE_PAINT,
            GREEN,
            MAGENTA,
            ORANGE,
            PINK,
            CYAN,
            Color(0, 255, 0, 128),
            Color(0, 0, 255, 128),
            YELLOW,
            LIGHT_GRAY,
            WHITE)

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(TransformAnim())
        }
    }
}
