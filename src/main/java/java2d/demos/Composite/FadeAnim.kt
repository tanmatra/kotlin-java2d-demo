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
import java2d.CustomControls
import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.BorderLayout
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
import javax.swing.JSlider
import javax.swing.JToolBar
import javax.swing.SwingConstants
import javax.swing.border.EtchedBorder
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * Animation of compositing shapes, text and images fading in and out.
 */
class FadeAnim : AnimatingControlsSurface()
{
    private val objects = ArrayList<ObjectData>(20)
    private var numShapes: Int = 0
    private var numStrings: Int = 0
    private var numImages: Int = 0

    init {
        background = BLACK
        setStrings(2)
        setImages(3)
        setShapes(8)
        controls = arrayOf(DemoControls(this))
        constraints = arrayOf(BorderLayout.EAST)
    }

    fun setImages(num: Int) {

        if (num < numImages) {
            val images = ArrayList<ObjectData>(objects.size)
            for (obj in objects) {
                if (obj.`object` is Image) {
                    images.add(obj)
                }
            }
            objects.removeAll(images.subList(num, images.size))
        } else {
            val d = size
            for (i in numImages until num) {
                var obj: Any = getImage(imgs[i % imgs.size])
                if (imgs[i % imgs.size] == "jumptojavastrip.png") {
                    val iw = (obj as Image).getWidth(null)
                    val ih = obj.getHeight(null)
                    val bimage = BufferedImage(
                        iw, ih,
                        BufferedImage.TYPE_INT_RGB
                                              )
                    bimage.createGraphics().drawImage(obj, 0, 0, null)
                    obj = bimage
                }
                val od = ObjectData(obj, BLACK)
                od.reset(d.width, d.height)
                objects.add(od)
            }
        }
        numImages = num
    }

    fun setStrings(num: Int) {

        if (num < numStrings) {
            val textDatas = ArrayList<ObjectData>(
                objects.size
                                                 )
            //for (int i = 0; i < objects.size(); i++) {
            for (obj in objects) {
                if (obj.`object` is TextData) {
                    textDatas.add(obj)
                }
            }
            objects.removeAll(textDatas.subList(num, textDatas.size))
        } else {
            val d = size
            for (i in numStrings until num) {
                val j = i % fonts.size
                val k = i % strings.size
                val obj = TextData(strings[k], fonts[j], this)
                val od = ObjectData(obj, paints[i % paints.size])
                od.reset(d.width, d.height)
                objects.add(od)
            }
        }
        numStrings = num
    }

    fun setShapes(num: Int) {

        if (num < numShapes) {
            val shapes = ArrayList<ObjectData>(objects.size)
            //for (int i = 0; i < objects.size(); i++) {
            for (obj in objects) {
                if (obj.`object` is Shape) {
                    shapes.add(obj)
                }
            }
            objects.removeAll(shapes.subList(num, shapes.size))
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
                val od = ObjectData(obj, paints[i % paints.size])
                od.reset(d.width, d.height)
                objects.add(od)
            }
        }
        numShapes = num
    }

    override fun reset(w: Int, h: Int) {
        for (i in objects.indices) {
            objects[i].reset(w, h)
        }
    }

    override fun step(w: Int, h: Int) {
        for (i in objects.indices) {
            objects[i].step(w, h)
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        for (i in objects.indices) {
            val od = objects[i]
            val ac = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, od.alpha
                                               )
            g2.composite = ac
            g2.paint = od.paint
            g2.translate(od.x, od.y)

            if (od.`object` is Image) {
                g2.drawImage(od.`object` as Image, 0, 0, this)
            } else if (od.`object` is TextData) {
                g2.font = (od.`object` as TextData).font
                g2.drawString((od.`object` as TextData).string, 0, 0)
            } else if (od.`object` is QuadCurve2D || od.`object` is CubicCurve2D) {
                g2.stroke = bs
                g2.draw(od.`object` as Shape)
            } else if (od.`object` is Shape) {
                g2.fill(od.`object` as Shape)
            }
            g2.translate(-od.x, -od.y)
        }
    }

    internal class TextData(var string: String, var font: Font, cmp: Component) : Any() {
        var width: Int = 0
        var height: Int = 0

        init {
            val fm = cmp.getFontMetrics(font)
            width = fm.stringWidth(string)
            height = fm.height
        }
    }

    internal class ObjectData(`object`: Any, var paint: Paint) : Any()
    {
        val UP = 0
        val DOWN = 1
        var `object`: Any
        var bimg: BufferedImage?
        var x: Double = 0.toDouble()
        var y: Double = 0.toDouble()
        var alpha: Float = 0.toFloat()
        var alphaDirection: Int = 0
        var imgX: Int = 0

        init {
            this.`object` = `object`
            if (`object` is BufferedImage) {
                bimg = `object`
                this.`object` = `object`.getSubimage(0, 0, 80, 80)
            } else {
                bimg = null
            }
            getRandomXY(300, 250)
            alpha = Math.random().toFloat()
            alphaDirection = if (Math.random() > 0.5) UP else DOWN
        }

        private fun getRandomXY(w: Int, h: Int) {
            if (`object` is TextData) {
                x = Math.random() * (w - (`object` as TextData).width)
                y = Math.random() * h
                y = if (y < (`object` as TextData).height) (`object` as TextData).height.toDouble() else y
            } else if (`object` is Image) {
                x = Math.random() * (w - (`object` as Image).getWidth(null))
                y = Math.random() * (h - (`object` as Image).getHeight(null))
            } else if (`object` is Shape) {
                val bounds = (`object` as Shape).bounds
                x = Math.random() * (w - bounds.width)
                y = Math.random() * (h - bounds.height)
            }
        }

        fun reset(w: Int, h: Int) {
            getRandomXY(w, h)
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

        fun step(w: Int, h: Int) {
            if (`object` is BufferedImage) {
                imgX += 80
                if (imgX == 800) {
                    imgX = 0
                }
                `object` = bimg!!.getSubimage(imgX, 0, 80, 80)
            }
            if (alphaDirection == UP) {
                alpha += 0.05f
                if (alpha > .99) {
                    alphaDirection = DOWN
                    alpha = 1.0f
                }
            } else if (alphaDirection == DOWN) {
                alpha -= .05f
                if (alpha < 0.01) {
                    alphaDirection = UP
                    alpha = 0f
                    getRandomXY(w, h)
                }
            }
        }
    }

    internal class DemoControls(var demo: FadeAnim) : CustomControls(demo.name), ChangeListener {
        var shapeSlider: JSlider
        var stringSlider: JSlider
        var imageSlider: JSlider
        var FONT = Font("serif", Font.BOLD, 10)

        init {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(Box.createVerticalStrut(5))

            val toolbar = JToolBar(SwingConstants.VERTICAL)
            toolbar.isFloatable = false
            shapeSlider = JSlider(
                SwingConstants.HORIZONTAL, 0, 20,
                demo.numShapes
                                 )
            shapeSlider.addChangeListener(this)
            var tb = TitledBorder(EtchedBorder())
            tb.titleFont = FONT
            tb.title = demo.numShapes.toString() + " Shapes"
            shapeSlider.border = tb
            shapeSlider.preferredSize = Dimension(80, 45)
            shapeSlider.isOpaque = true
            toolbar.addSeparator()
            toolbar.add(shapeSlider)
            toolbar.addSeparator()

            stringSlider = JSlider(
                SwingConstants.HORIZONTAL, 0, 10,
                demo.numStrings
                                  )
            stringSlider.addChangeListener(this)
            tb = TitledBorder(EtchedBorder())
            tb.titleFont = FONT
            tb.title = demo.numStrings.toString() + " Strings"
            stringSlider.border = tb
            stringSlider.preferredSize = Dimension(80, 45)
            stringSlider.isOpaque = true
            toolbar.add(stringSlider)
            toolbar.addSeparator()

            imageSlider = JSlider(
                SwingConstants.HORIZONTAL, 0, 10,
                demo.numImages
                                 )
            imageSlider.addChangeListener(this)
            tb = TitledBorder(EtchedBorder())
            tb.titleFont = FONT
            tb.title = demo.numImages.toString() + " Images"
            imageSlider.border = tb
            imageSlider.preferredSize = Dimension(80, 45)
            imageSlider.isOpaque = true
            toolbar.add(imageSlider)
            toolbar.addSeparator()

            add(toolbar)
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
            slider.repaint()
            if (!demo.animating!!.isRunning) {
                demo.repaint()
            }
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(80, 0)
        }

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
    } // End DemoControls

    companion object {

        private val texturePaint: TexturePaint

        init {
            val w = 10
            val h = 10
            val bi = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
            val gi = bi.createGraphics()
            val oc = BLUE
            val ic = GREEN
            gi.paint = GradientPaint(0f, 0f, oc, w * .35f, h * .35f, ic)
            gi.fillRect(0, 0, w / 2, h / 2)
            gi.paint = GradientPaint(w.toFloat(), 0f, oc, w * .65f, h * .35f, ic)
            gi.fillRect(w / 2, 0, w / 2, h / 2)
            gi.paint = GradientPaint(0f, h.toFloat(), oc, w * .35f, h * .65f, ic)
            gi.fillRect(0, h / 2, w / 2, h / 2)
            gi.paint = GradientPaint(w.toFloat(), h.toFloat(), oc, w * .65f, h * .65f, ic)
            gi.fillRect(w / 2, h / 2, w / 2, h / 2)
            texturePaint = TexturePaint(bi, Rectangle(0, 0, w, h))
        }

        private val bs = BasicStroke(6f)
        private val fonts = arrayOf(
            Font("Times New Roman", Font.PLAIN, 64),
            Font("serif", Font.BOLD + Font.ITALIC, 24),
            Font("Courier", Font.BOLD, 36),
            Font("Arial", Font.BOLD + Font.ITALIC, 48),
            Font("Helvetica", Font.PLAIN, 52)
                                   )
        private val strings =
            arrayOf("Alpha", "Composite", "Src", "SrcOver", "SrcIn", "SrcOut", "Clear", "DstOver", "DstIn")
        private val imgs = arrayOf("jumptojavastrip.png", "duke.gif", "star7.gif")
        private val paints =
            arrayOf(RED, BLUE, GREEN, MAGENTA, ORANGE, PINK, CYAN, texturePaint, YELLOW, LIGHT_GRAY, WHITE)

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(FadeAnim())
        }
    }
}
