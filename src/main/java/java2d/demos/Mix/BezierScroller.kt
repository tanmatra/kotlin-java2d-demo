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
package java2d.demos.Mix

import java2d.AnimatingControlsSurface
import java2d.CustomControls
import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Color.LIGHT_GRAY
import java.awt.Color.WHITE
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Image
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.geom.GeneralPath
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.FileReader
import java.lang.Math.random
import java.util.ArrayList
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.AbstractButton
import javax.swing.JComboBox
import javax.swing.JToggleButton
import javax.swing.JToolBar

/**
 * Animated Bezier Curve shape with images at the control points.
 * README.txt file scrolling up. Composited Image fading in and out.
 */
class BezierScroller : AnimatingControlsSurface()
{
    private val animpts = FloatArray(NUMPTS * 2)
    private val deltas = FloatArray(NUMPTS * 2)
    private var reader: BufferedReader? = null
    private var nStrs: Int = 0
    private var strH: Int = 0
    private var yy: Int = 0
    private var ix: Int = 0
    private var iy: Int = 0
    private var imgX: Int = 0
    private var vector: MutableList<String>? = null
    private var appletVector: MutableList<String>? = null
    private var alpha = 0.2f
    private var alphaDirection: Int = 0
    private var doImage: Boolean = false
    private var doShape: Boolean = false
    private var doText: Boolean = false
    private var buttonToggle: Boolean = false

    val line: String?
        get() {
            var str: String? = null
            if (reader != null) {
                try {
                    str = reader!!.readLine()
                    if (str != null) {
                        if (str!!.length == 0) {
                            str = " "
                        }
                        vector!!.add(str)
                    }
                } catch (e: Exception) {
                    Logger.getLogger(BezierScroller::class.java.name).log(Level.SEVERE, null, e)
                    reader = null
                }
            } else {
                if (!appletVector!!.isEmpty()) {
                    str = appletVector!!.removeAt(0)
                    vector!!.add(str)
                }
            }
            return str
        }

    init {
        background = WHITE
        doText = true
        doShape = doText
        hotj_img = getImage("java-logo.gif")
        val image = getImage("jumptojavastrip.png")
        val iw = image.getWidth(this)
        val ih = image.getHeight(this)
        img = BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB)
        img.createGraphics().drawImage(image, 0, 0, this)
        controls = arrayOf(DemoControls(this))
    }

    fun animate(pts: FloatArray, deltas: FloatArray, index: Int, limit: Int) {
        var newpt = pts[index] + deltas[index]
        if (newpt <= 0) {
            newpt = -newpt
            deltas[index] = (random() * 4.0 + 2.0).toFloat()
        } else if (newpt >= limit) {
            newpt = 2.0f * limit - newpt
            deltas[index] = -(random() * 4.0 + 2.0).toFloat()
        }
        pts[index] = newpt
    }

    fun getFile() {
        try {
            val fName = "README.txt"
            reader = BufferedReader(FileReader(fName))
            line
        } catch (e: Exception) {
            reader = null
        }

        if (reader == null) {
            appletVector = ArrayList(100)
            for (i in 0 .. 99) {
                appletVector!!.add(appletStrs[i % appletStrs.size])
            }
            line
        }
        buttonToggle = true
    }

    override fun reset(w: Int, h: Int) {
        var i = 0
        while (i < animpts.size) {
            animpts[i + 0] = (random() * w).toFloat()
            animpts[i + 1] = (random() * h).toFloat()
            deltas[i + 0] = (random() * 6.0 + 4.0).toFloat()
            deltas[i + 1] = (random() * 6.0 + 4.0).toFloat()
            if (animpts[i + 0] > w / 2.0f) {
                deltas[i + 0] = -deltas[i + 0]
            }
            if (animpts[i + 1] > h / 2.0f) {
                deltas[i + 1] = -deltas[i + 1]
            }
            i += 2
        }
        val fm = getFontMetrics(FONT)
        strH = fm.ascent + fm.descent
        nStrs = h / strH + 2
        vector = ArrayList(nStrs)
        ix = (random() * (w - 80)).toInt()
        iy = (random() * (h - 80)).toInt()
    }

    override fun step(w: Int, h: Int) {
        if (doText && vector!!.isEmpty()) {
            getFile()
        }
        if (doText) {
            val s = line
            if (s == null || vector!!.size == nStrs && !vector!!.isEmpty()) {
                vector!!.removeAt(0)
            }
            yy = if (s == null) 0 else h - vector!!.size * strH
        }

        var i = 0
        while (i < animpts.size && doShape) {
            animate(animpts, deltas, i + 0, w)
            animate(animpts, deltas, i + 1, h)
            i += 2
        }
        if (doImage && alphaDirection == UP) {
            alpha += 0.025f
            if (alpha > .99) {
                alphaDirection = DOWN
                alpha = 1.0f
            }
        } else if (doImage && alphaDirection == DOWN) {
            alpha -= .02f
            if (alpha < 0.01) {
                alphaDirection = UP
                alpha = 0f
                ix = (random() * (w - 80)).toInt()
                iy = (random() * (h - 80)).toInt()
            }
        }
        if (doImage) {
            imgX += 80
            if (imgX == 800) {
                imgX = 0
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        if (doText) {
            g2.color = LIGHT_GRAY
            g2.font = FONT
            var y = yy.toFloat()
            //for (int i = 0; i < vector.size(); i++) {
            for (string in vector!!) {
                y += strH.toFloat()
                g2.drawString(string, 1f, y)
            }
        }

        if (doShape) {
            val ctrlpts = animpts
            val len = ctrlpts.size
            var prevx = ctrlpts[len - 2]
            var prevy = ctrlpts[len - 1]
            var curx = ctrlpts[0]
            var cury = ctrlpts[1]
            var midx = (curx + prevx) / 2.0f
            var midy = (cury + prevy) / 2.0f
            val gp = GeneralPath(Path2D.WIND_NON_ZERO)
            gp.moveTo(midx, midy)
            var i = 2
            while (i <= ctrlpts.size) {
                val x1 = (midx + curx) / 2.0f
                val y1 = (midy + cury) / 2.0f
                prevx = curx
                prevy = cury
                if (i < ctrlpts.size) {
                    curx = ctrlpts[i + 0]
                    cury = ctrlpts[i + 1]
                } else {
                    curx = ctrlpts[0]
                    cury = ctrlpts[1]
                }
                midx = (curx + prevx) / 2.0f
                midy = (cury + prevy) / 2.0f
                val x2 = (prevx + midx) / 2.0f
                val y2 = (prevy + midy) / 2.0f
                gp.curveTo(x1, y1, x2, y2, midx, midy)
                i += 2
            }
            gp.closePath()

            g2.color = blueBlend
            g2.stroke = bs
            g2.draw(gp)
            g2.color = greenBlend
            g2.fill(gp)

            val pi = gp.getPathIterator(null)
            val pts = FloatArray(6)
            while (!pi.isDone) {
                if (pi.currentSegment(pts) == PathIterator.SEG_CUBICTO) {
                    g2.drawImage(hotj_img, pts[0].toInt(), pts[1].toInt(), this)
                }
                pi.next()
            }
        }

        if (doImage) {
            val ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
            g2.composite = ac
            g2.drawImage(img.getSubimage(imgX, 0, 80, 80), ix, iy, this)
        }
    }

    internal class DemoControls(var demo: BezierScroller) : CustomControls(demo.name), ActionListener {
        var toolbar: JToolBar
        var combo: JComboBox<*>? = null

        init {
            toolbar = JToolBar()
            add(toolbar)
            toolbar.isFloatable = false
            addTool("Image", false)
            addTool("Shape", true)
            addTool("Text", true)
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
            val b = e.source as JToggleButton
            if (b.text == "Image") {
                demo.doImage = b.isSelected
            } else if (b.text == "Shape") {
                demo.doShape = b.isSelected
            } else {
                demo.doText = b.isSelected
            }
            if (!demo.animating!!.isRunning) {
                demo.repaint()
            }
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(200, 40)
        }

        override fun run() {
            val me = Thread.currentThread()
            var i = 0
            while (thread === me) {
                try {
                    Thread.sleep(250)
                } catch (e: InterruptedException) {
                    return
                }

                if (demo.buttonToggle) {
                    (toolbar.getComponentAtIndex(i++ % 2) as AbstractButton).doClick()
                    demo.buttonToggle = false
                }
            }
            thread = null
        }
    }

    companion object
    {
        private val appletStrs = arrayOf(
            " ",
            "Java2Demo",
            "BezierScroller - Animated Bezier Curve shape with images",
            "For README.txt file scrolling run in application mode",
            " ")

        private val NUMPTS = 6
        private val greenBlend = Color(0, 255, 0, 100)
        private val blueBlend = Color(0, 0, 255, 100)
        private val FONT = Font("serif", Font.PLAIN, 12)
        private val bs = BasicStroke(3.0f)
        private lateinit var hotj_img: Image
        private lateinit var img: BufferedImage
        private val UP = 0
        private val DOWN = 1

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(BezierScroller())
        }
    }
}
