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
import java2d.CControl
import java2d.CustomControls
import java2d.RepaintingProperty
import java2d.createBooleanButton
import java2d.getLogger
import java2d.systemTextAntialiasing
import java2d.textAntialiasing
import java2d.toBufferedImage
import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Image
import java.awt.geom.GeneralPath
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.image.BufferedImage
import java.io.FileInputStream
import java.util.LinkedList
import java.util.Random
import java.util.logging.Level
import javax.swing.AbstractButton
import javax.swing.JToolBar

/**
 * Animated Bezier Curve shape with images at the control points.
 * README.txt file scrolling up. Composited Image fading in and out.
 */
class BezierScroller : AnimatingControlsSurface()
{
    private val animpts = FloatArray(NUMPTS * 2)
    private val deltas = FloatArray(NUMPTS * 2)
    private var linesCount: Int = 0
    private var lineHeight: Int = 0
    private var textStartY: Int = 0
    private var imageX: Int = 0
    private var imageY: Int = 0
    private var subimageX: Int = 0 // position inside image strip
    private var alpha = 0.2f
    private var alphaDirection: Int = 0
    private var doImage: Boolean by RepaintingProperty(false)
    private var doShape: Boolean by RepaintingProperty(true)
    private var doText: Boolean by RepaintingProperty(true)
    @Volatile private var textCycled: Boolean = false
    private val hotJavaImg: Image = getImage("java-logo.gif")
    private val random = Random()

    private val image: BufferedImage = getImage("jumptojavastrip.png").toBufferedImage()

    private val lines: List<String> = try {
        FileInputStream(FILE_NAME).use {
            it.reader().readLines()
        }
    } catch (e: Exception) {
        getLogger<BezierScroller>().log(Level.SEVERE, null, e)
        FALLBACK_TEXT
    }

    private var linesIterator: Iterator<String> = lines.iterator()

    private val bufferLines = LinkedList<String>()

    init {
        background = Color.WHITE
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.NORTH)

    private fun animate(pts: FloatArray, deltas: FloatArray, index: Int, limit: Int) {
        var newpt = pts[index] + deltas[index]
        if (newpt <= 0) {
            newpt = -newpt
            deltas[index] = random.nextFloat() * 4.0f + 2.0f
        } else if (newpt >= limit) {
            newpt = 2.0f * limit - newpt
            deltas[index] = - (random.nextFloat() * 4.0f + 2.0f)
        }
        pts[index] = newpt
    }

    private fun randomizeImagePosition(width: Int, height: Int) {
        imageX = random.nextInt(width - STRIP_CELL_WIDTH)
        imageY = random.nextInt(height - STRIP_CELL_HEIGHT)
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        for (i in 0 until animpts.size step 2) {
            animpts[i + 0] = random.nextInt(newWidth).toFloat()
            animpts[i + 1] = random.nextInt(newHeight).toFloat()
            deltas[i + 0] = random.nextFloat() * 6.0f + 4.0f
            deltas[i + 1] = random.nextFloat() * 6.0f + 4.0f
            if (animpts[i + 0] > newWidth / 2.0f) {
                deltas[i + 0] = -deltas[i + 0]
            }
            if (animpts[i + 1] > newHeight / 2.0f) {
                deltas[i + 1] = -deltas[i + 1]
            }
        }

        val fontMetrics = getFontMetrics(FONT)
        lineHeight = fontMetrics.ascent + fontMetrics.descent
        linesCount = newHeight / lineHeight + 2
        bufferLines.clear()
        linesIterator = lines.iterator()
        textCycled = true

        randomizeImagePosition(newWidth, newHeight)
    }

    override fun step(width: Int, height: Int) {
        if (doText) {
            if (!linesIterator.hasNext()) {
                linesIterator = lines.iterator()
                textCycled = true
            }
            bufferLines += linesIterator.next()
            while (bufferLines.size > linesCount) {
                bufferLines.removeFirst()
            }
            textStartY = height - bufferLines.size * lineHeight
        }

        var i = 0
        while (i < animpts.size && doShape) {
            animate(animpts, deltas, i + 0, width)
            animate(animpts, deltas, i + 1, height)
            i += 2
        }
        if (doImage) {
            when (alphaDirection) {
                UP -> {
                    alpha += 0.025f
                    if (alpha > 0.99f) {
                        alphaDirection = DOWN
                        alpha = 1.0f
                    }
                }
                DOWN -> {
                    alpha -= 0.025f
                    if (alpha < 0.01f) {
                        alphaDirection = UP
                        alpha = 0.0f
                        randomizeImagePosition(width, height)
                    }
                }
            }
            subimageX += STRIP_CELL_WIDTH
            if (subimageX >= STRIP_WIDTH) {
                subimageX = 0
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        if (doText) {
            g2.color = TEXT_COLOR
            g2.font = FONT
            g2.textAntialiasing = systemTextAntialiasing
            var y = textStartY.toFloat()
            for (line in bufferLines) {
                g2.drawString(line, 1.0f, y)
                y += lineHeight
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

            g2.color = BLUE_BLEND
            g2.stroke = STROKE
            g2.draw(gp)
            g2.color = GREEN_BLEND
            g2.fill(gp)

            val pi = gp.getPathIterator(null)
            val pts = FloatArray(6)
            while (!pi.isDone) {
                if (pi.currentSegment(pts) == PathIterator.SEG_CUBICTO) {
                    g2.drawImage(hotJavaImg, pts[0].toInt(), pts[1].toInt(), this)
                }
                pi.next()
            }
        }

        if (doImage) {
            g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
            g2.drawImage(image.getSubimage(subimageX, 0, STRIP_CELL_WIDTH, STRIP_CELL_HEIGHT), imageX, imageY, this)
        }
    }

    internal class DemoControls(private val demo: BezierScroller) : CustomControls(demo.name)
    {
        private val toolbar = JToolBar().apply { isFloatable = false }

        init {
            add(toolbar)
            toolbar.add(createBooleanButton(demo::doImage, "Image"))
            toolbar.add(createBooleanButton(demo::doShape, "Shape"))
            toolbar.add(createBooleanButton(demo::doText, "Text"))
        }

        override fun getPreferredSize() = Dimension(200, 40)

        override fun run() {
            val me = Thread.currentThread()
            var i = 0
            while (thread === me) {
                try {
                    Thread.sleep(250)
                } catch (e: InterruptedException) {
                    return
                }

                if (demo.textCycled) {
                    (toolbar.getComponentAtIndex(i++ % 2) as AbstractButton).doClick()
                    demo.textCycled = false
                }
            }
            thread = null
        }
    }

    companion object
    {
        private val FALLBACK_TEXT = listOf(
            "Java2Demo",
            "BezierScroller - Animated Bezier Curve shape with images",
            "For README.txt file scrolling run in application mode",
            "")

        private const val NUMPTS = 6
        private val GREEN_BLEND = Color(0, 255, 0, 100)
        private val BLUE_BLEND = Color(0, 0, 255, 100)
        private val FONT = Font(Font.SERIF, Font.PLAIN, 12)
        private val STROKE = BasicStroke(3.0f)
        private const val UP = 0
        private const val DOWN = 1
        private const val STRIP_WIDTH = 800
        private const val STRIP_CELL_WIDTH = 80
        private const val STRIP_CELL_HEIGHT = 80
        private const val FILE_NAME = "README.txt"
        private val TEXT_COLOR = Color.LIGHT_GRAY

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(BezierScroller())
        }
    }
}
