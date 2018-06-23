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
import java2d.CustomControls
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
import java.awt.Font.BOLD
import java.awt.Font.ITALIC
import java.awt.Font.PLAIN
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Paint
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.TexturePaint
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
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
import javax.swing.JSlider
import javax.swing.JToggleButton
import javax.swing.JToolBar
import javax.swing.SwingConstants
import javax.swing.border.EtchedBorder
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.plaf.metal.MetalBorders.ButtonBorder

/**
 * Animation of shapes, text and images rotating, scaling and translating
 * around a canvas.
 */
class TransformAnim : AnimatingControlsSurface()
{
    private val objDatas = ArrayList<ObjData>(13)
    private var numShapes: Int = 0
    private var numStrings: Int = 0
    private var numImages: Int = 0
    protected var doRotate = true
    protected var doTranslate = true
    protected var doScale = true
    protected var doShear: Boolean = false

    init {
        background = BLACK
        setStrings(1)
        setImages(2)
        setShapes(10)
        controls = arrayOf(DemoControls(this))
        constraints = arrayOf(BorderLayout.EAST)
    }

    fun setImages(num: Int) {
        if (num < numImages) {
            val v = ArrayList<ObjData>(objDatas.size)
            for (objData in objDatas) {
                if (objData.`object` is Image) {
                    v.add(objData)
                }
            }
            objDatas.removeAll(v.subList(num, v.size))
        } else {
            val d = size
            for (i in numImages until num) {
                val obj = getImage(imgs[i % imgs.size])
                val objData = ObjData(obj, BLACK)
                objData.reset(d.width, d.height)
                objDatas.add(objData)
            }
        }
        numImages = num
    }

    fun setStrings(num: Int) {
        if (num < numStrings) {
            val v = ArrayList<ObjData>(objDatas.size)
            for (objData in objDatas) {
                if (objData.`object` is TextData) {
                    v.add(objData)
                }
            }
            objDatas.removeAll(v.subList(num, v.size))
        } else {
            val d = size
            for (i in numStrings until num) {
                val j = i % fonts.size
                val k = i % strings.size
                val obj = TextData(strings[k], fonts[j])
                val objData = ObjData(obj, paints[i % paints.size])
                objData.reset(d.width, d.height)
                objDatas.add(objData)
            }
        }
        numStrings = num
    }

    fun setShapes(num: Int) {
        if (num < numShapes) {
            val v = ArrayList<ObjData>(objDatas.size)
            for (objData in objDatas) {
                if (objData.`object` is Shape) {
                    v.add(objData)
                }
            }
            objDatas.removeAll(v.subList(num, v.size))
        } else {
            val d = size
            for (i in numShapes until num) {
                var obj: Any
                when (i % 7) {
                    0 -> obj = GeneralPath()
                    1 -> obj = Rectangle2D.Double()
                    2 -> obj = Ellipse2D.Double()
                    3 -> obj = Arc2D.Double()
                    4 -> obj = RoundRectangle2D.Double()
                    5 -> obj = CubicCurve2D.Double()
                    6 -> obj = QuadCurve2D.Double()
                    else -> error(7)
                }
                val objData = ObjData(obj, paints[i % paints.size])
                objData.reset(d.width, d.height)
                objDatas.add(objData)
            }
        }
        numShapes = num
    }

    override fun reset(w: Int, h: Int) {
        for (objData in objDatas) {
            objData.reset(w, h)
        }
    }

    override fun step(w: Int, h: Int) {
        for (objData in objDatas) {
            objData.step(w, h, this)
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        for (objData in objDatas) {
            g2.transform = objData.at
            g2.paint = objData.paint
            if (objData.`object` is Image) {
                g2.drawImage(objData.`object` as Image, 0, 0, this)
            } else if (objData.`object` is TextData) {
                g2.font = (objData.`object` as TextData).font
                g2.drawString((objData.`object` as TextData).string, 0, 0)
            } else if (objData.`object` is QuadCurve2D || objData.`object` is CubicCurve2D) {
                g2.stroke = bs
                g2.draw(objData.`object` as Shape)
            } else if (objData.`object` is Shape) {
                g2.fill(objData.`object` as Shape)
            }
        }
    }

    internal class TextData(var string: String, var font: Font)

    internal class ObjData(var `object`: Any, var paint: Paint)
    {
        var x: Double = 0.toDouble()
        var y: Double = 0.toDouble()
        var ix = 5.0
        var iy = 3.0
        var rotate: Int = 0
        var scale: Double = 0.toDouble()
        var shear: Double = 0.toDouble()
        var scaleDirection: Int = 0
        var shearDirection: Int = 0
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
            if (`object` is Ellipse2D) {
                (`object` as Ellipse2D).setFrame(0.0, 0.0, ww, hh)
            } else if (`object` is Rectangle2D) {
                (`object` as Rectangle2D).setRect(0.0, 0.0, ww, ww)
            } else if (`object` is RoundRectangle2D) {
                (`object` as RoundRectangle2D).setRoundRect(0.0, 0.0, hh, hh, 20.0, 20.0)
            } else if (`object` is Arc2D) {
                (`object` as Arc2D).setArc(0.0, 0.0, hh, hh, 45.0, 270.0, Arc2D.PIE)
            } else if (`object` is QuadCurve2D) {
                (`object` as QuadCurve2D).setCurve(0.0, 0.0, w * .2, h * .4, w * .4, 0.0)
            } else if (`object` is CubicCurve2D) {
                (`object` as CubicCurve2D).setCurve(0.0, 0.0, 30.0, -60.0, 60.0, 60.0, 90.0, 0.0)
            } else if (`object` is GeneralPath) {
                val p = GeneralPath()
                val size = ww.toFloat()
                p.moveTo(-size / 2.0f, -size / 8.0f)
                p.lineTo(+size / 2.0f, -size / 8.0f)
                p.lineTo(-size / 4.0f, +size / 2.0f)
                p.lineTo(+0.0f, -size / 2.0f)
                p.lineTo(+size / 4.0f, +size / 2.0f)
                p.closePath()
                `object` = p
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

        companion object {
            val UP = 0
            val DOWN = 1
        }
    } // End ObjData class

    internal class DemoControls(var demo: TransformAnim) : CustomControls(demo.name), ActionListener, ChangeListener {
        var shapeSlider: JSlider
        var stringSlider: JSlider
        var imageSlider: JSlider
        var FONT = Font("serif", Font.BOLD, 10)
        var toolbar: JToolBar
        var buttonBorder = ButtonBorder()

        init {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(Box.createVerticalStrut(5))

            val bar = JToolBar(SwingConstants.VERTICAL)
            bar.isFloatable = false
            shapeSlider = JSlider(SwingConstants.HORIZONTAL, 0, 20, demo.numShapes)
            shapeSlider.addChangeListener(this)
            var tb = TitledBorder(EtchedBorder())
            tb.titleFont = FONT
            tb.title = demo.numShapes.toString() + " Shapes"
            shapeSlider.border = tb
            shapeSlider.isOpaque = true
            shapeSlider.preferredSize = Dimension(80, 44)
            bar.add(shapeSlider)
            bar.addSeparator()

            stringSlider = JSlider(SwingConstants.HORIZONTAL, 0, 10, demo.numStrings)
            stringSlider.addChangeListener(this)
            tb = TitledBorder(EtchedBorder())
            tb.titleFont = FONT
            tb.title = demo.numStrings.toString() + " Strings"
            stringSlider.border = tb
            stringSlider.isOpaque = true
            stringSlider.preferredSize = Dimension(80, 44)
            bar.add(stringSlider)
            bar.addSeparator()

            imageSlider = JSlider(SwingConstants.HORIZONTAL, 0, 10, demo.numImages)
            imageSlider.addChangeListener(this)
            tb = TitledBorder(EtchedBorder())
            tb.titleFont = FONT
            tb.title = demo.numImages.toString() + " Images"
            imageSlider.border = tb
            imageSlider.isOpaque = true
            imageSlider.preferredSize = Dimension(80, 44)
            bar.add(imageSlider)
            bar.addSeparator()
            add(bar)

            toolbar = JToolBar()
            toolbar.isFloatable = false
            addButton("T", "translate", demo.doTranslate)
            addButton("R", "rotate", demo.doRotate)
            addButton("SC", "scale", demo.doScale)
            addButton("SH", "shear", demo.doShear)
            add(toolbar)
        }

        fun addButton(s: String, tt: String, state: Boolean) {
            val b = toolbar.add(JToggleButton(s)) as JToggleButton
            b.font = FONT
            b.isSelected = state
            b.toolTipText = tt
            b.isFocusPainted = false
            b.border = buttonBorder
            b.addActionListener(this)
        }

        override fun actionPerformed(e: ActionEvent) {
            val b = e.source as JToggleButton
            if (b.text == "T") {
                demo.doTranslate = b.isSelected
            } else if (b.text == "R") {
                demo.doRotate = b.isSelected
            } else if (b.text == "SC") {
                demo.doScale = b.isSelected
            } else if (b.text == "SH") {
                demo.doShear = b.isSelected
            }
            if (!demo.animating!!.isRunning) {
                demo.repaint()
            }
        }

        override fun stateChanged(e: ChangeEvent) {
            val slider = e.source as JSlider
            val value = slider.value
            val tb = slider.border as TitledBorder
            if (slider == shapeSlider) {
                tb.title = value.toString() + " Shapes"
                demo.setShapes(value)
            } else if (slider == stringSlider) {
                tb.title = value.toString() + " Strings"
                demo.setStrings(value)
            } else if (slider == imageSlider) {
                tb.title = value.toString() + " Images"
                demo.setImages(value)
            }
            if (!demo.animating!!.isRunning) {
                demo.repaint()
            }
            slider.repaint()
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(80, 38)
        }

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
        private val texturePaint: TexturePaint

        init {
            val bi = BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)
            val gi = bi.createGraphics()
            gi.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            gi.color = RED
            gi.fillOval(0, 0, 9, 9)
            texturePaint = TexturePaint(bi, Rectangle(0, 0, 10, 10))
        }

        private val bs = BasicStroke(6f)

        private val fonts = arrayOf(
            Font("Times New Roman", PLAIN, 48),
            Font("serif", BOLD or ITALIC, 24),
            Font("Courier", BOLD, 36),
            Font("Arial", BOLD or ITALIC, 64),
            Font("Helvetica", PLAIN, 52))

        private val strings = arrayOf("Transformation", "Rotate", "Translate", "Shear", "Scale")

        private val imgs = arrayOf("duke.gif")

        private val paints = arrayOf(
            RED,
            BLUE,
            texturePaint,
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
