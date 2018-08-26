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
package java2d.intro

import java2d.DemoImages
import java2d.use
import java.awt.AlphaComposite
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Composite
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Paint
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.font.FontRenderContext
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.border.BevelBorder
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder

/**
 * Introduction to the Java2Demo.
 *
 * @author Brian Lichtenwalter
 * @author Alexander Kouznetsov
 */
class Intro : JPanel(BorderLayout())
{
    private var scenesTable: ScenesTable? = null
    private var doTable: Boolean = false
    internal var surface: Surface

    init {
        border = CompoundBorder(EmptyBorder(80, 110, 80, 110), BevelBorder(BevelBorder.LOWERED))
        background = Color.GRAY
        toolTipText = "click for scene table"
        surface = Surface()
        add(surface)
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                removeAll()
                doTable = !doTable
                if (doTable) {
                    toolTipText = "click for animation"
                    surface.stop()
                    if (scenesTable == null) {
                        scenesTable = ScenesTable(surface)
                    }
                    add(scenesTable)
                } else {
                    toolTipText = "click for scene table"
                    surface.start()
                    add(surface)
                }
                revalidate()
                repaint()
            }
        })
    }

    fun start() {
        if (!doTable) {
            surface.start()
        }
    }

    fun stop() {
        if (!doTable) {
            surface.stop()
        }
    }

    /**
     * Surface is the stage where the Director plays its scenes.
     */
    internal class Surface : JPanel(), Runnable
    {
        var director: Director
        var index: Int = 0
        var sleepAmt: Long = 30
        private var thread: Thread? = null

        init {
            surf = this
            background = myBlack
            layout = BorderLayout()
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (thread == null) {
                        start()
                    } else {
                        stop()
                    }
                }
            })
            cupanim = DemoImages.getImage("cupanim.gif", this)
            java_logo = DemoImages.getImage("java_logo.png", this)
            director = Director()
        }

        override fun paint(g: Graphics) {
            if (width <= 0 || height <= 0) {
                return
            }
            val image = bufferedImage?.takeIf { it.width == width && it.height == height } ?: run {
                val newImage = graphicsConfiguration.createCompatibleImage(width, height)
                bufferedImage = newImage
                // reset future scenes
                for (i in index + 1 until director.size) {
                    director[i].reset(width, height)
                }
                newImage
            }

            val scene = director[index]
            if (scene.index <= scene.length) {
                if (thread != null) {
                    scene.step(width, height)
                }
                image.createGraphics().use { g2 ->
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                    g2.background = background
                    g2.clearRect(0, 0, width, height)

                    scene.render(width, height, g2)

                    if (thread != null) {
                        // increment scene.index after scene.render
                        scene.index++
                    }
                }
            }
            g.drawImage(image, 0, 0, this)
        }

        fun start() {
            if (thread == null) {
                thread = Thread(this, "Intro").apply {
                    priority = Thread.MIN_PRIORITY
                    start()
                }
            }
        }

        @Synchronized
        fun stop() {
            thread?.interrupt()
            thread = null
            @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            (this as java.lang.Object).notifyAll()
        }

        fun reset() {
            index = 0
            val d = size
            for (scene in director) {
                scene.reset(d.width, d.height)
            }
        }

        override fun run() {
            val me = Thread.currentThread()
            while (thread === me && !isShowing || size.width <= 0) {
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    return
                }
            }

            if (index == 0) {
                reset()
            }

            while (thread === me) {
                val scene = director[index]
                if (scene.participate) {
                    repaint()
                    try {
                        Thread.sleep(sleepAmt)
                    } catch (e: InterruptedException) {
                        break
                    }

                    if (scene.index > scene.length) {
                        scene.pause()
                        if (++index >= director.size) {
                            reset()
                        }
                    }
                } else {
                    if (++index >= director.size) {
                        reset()
                    }
                }
            }
            thread = null
        }

        /**
         * Scene is the manager of the parts.
         */
        internal class Scene(
            var name: String,
            var pauseAmt: Long,
            private val parts: Array<out Part>
        ) {
            var participate: Boolean = true
            var index: Int = 0
            var length: Int = 0

            init {
                for (part in parts) {
                    val partLength = part.end
                    if (partLength > length) {
                        length = partLength
                    }
                }
            }

            fun reset(w: Int, h: Int) {
                index = 0
                parts.forEach { it.reset(w, h) }
            }

            fun step(w: Int, h: Int) {
                for (part in parts) {
                    if (index in part.begin .. part.end) {
                        part.step(w, h)
                    }
                }
            }

            fun render(w: Int, h: Int, g2: Graphics2D) {
                for (part in parts) {
                    if (index in part.begin .. part.end) {
                        part.render(w, h, g2)
                    }
                }
            }

            fun pause() {
                try {
                    Thread.sleep(pauseAmt)
                } catch (ignored: Exception) {
                }
                // System.gc()
            }
        } // End Scene class

        /**
         * Text Effect.  Transformation of characters.  Clip or fill.
         */
        internal class TxE(
            text: String,
            font: Font,
            private val type: Int,
            private val paint: Paint?,
            override val begin: Int,
            override val end: Int
        ) : Part {
            private var rIncr: Double = 0.toDouble()
            private var sIncr: Double = 0.toDouble()
            private var sx: Double = 0.toDouble()
            private var sy: Double = 0.toDouble()
            private var rotate: Double = 0.toDouble()
            private val shapes: Array<Shape>
            private val txShapes: Array<Shape?>
            private val sw: Int
            private var numRev: Int = 0

            init {
                setIncrements(2.0)
                val chars = text.toCharArray()
                txShapes = arrayOfNulls(chars.size)
                val frc = FontRenderContext(null, true, true)
                val tl = TextLayout(text, font, frc)
                sw = tl.getOutline(null).bounds.getWidth().toInt()
                shapes = Array(chars.size) { j ->
                    val s = chars[j].toString()
                    TextLayout(s, font, frc).getOutline(null)
                }
            }

            private fun setIncrements(numRevolutions: Double) {
                this.numRev = numRevolutions.toInt()
                rIncr = 360.0 / ((end - begin) / numRevolutions)
                sIncr = 1.0 / (end - begin)
                if (type and SCX != 0 || type and SCY != 0) {
                    sIncr *= 2.0
                }
                if (type and DEC != 0) {
                    rIncr = -rIncr
                    sIncr = -sIncr
                }
            }

            override fun reset(newWidth: Int, newHeight: Int) {
                when (type) {
                    SCXI -> {
                        sx = -1.0
                        sy = 1.0
                    }
                    SCYI -> {
                        sx = 1.0
                        sy = -1.0
                    }
                    else -> {
                        sy = if (type and DEC != 0) 1.0 else 0.0
                        sx = sy
                    }
                }
                rotate = 0.0
            }

            override fun step(w: Int, h: Int) {
                var charWidth = (w / 2 - sw / 2).toFloat()

                for (i in shapes.indices) {
                    val at = AffineTransform()
                    val maxBounds = shapes[i].bounds
                    at.translate(charWidth.toDouble(), h / 2 + maxBounds.getHeight() / 2)
                    charWidth += maxBounds.getWidth().toFloat() + 1
                    var shape = at.createTransformedShape(shapes[i])
                    val b1 = shape.bounds2D

                    if (type and R != 0) {
                        at.rotate(Math.toRadians(rotate))
                    }
                    if (type and SC != 0) {
                        at.scale(sx, sy)
                    }
                    shape = at.createTransformedShape(shapes[i])
                    val b2 = shape.bounds2D

                    val xx = b1.x + b1.width / 2 - (b2.x + b2.width / 2)
                    val yy = b1.y + b1.height / 2 - (b2.y + b2.height / 2)
                    val toCenterAT = AffineTransform()
                    toCenterAT.translate(xx, yy)
                    toCenterAT.concatenate(at)
                    txShapes[i] = toCenterAT.createTransformedShape(shapes[i])
                }
                // avoid over rotation
                if (Math.abs(rotate) <= numRev * 360) {
                    rotate += rIncr
                    when {
                        type and SCX != 0 -> sx += sIncr
                        type and SCY != 0 -> sy += sIncr
                        else -> {
                            sx += sIncr
                            sy += sIncr
                        }
                    }
                }
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                var saveAC: Composite? = null
                if (type and AC != 0 && sx > 0 && sx < 1) {
                    saveAC = g2.composite
                    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sx.toFloat())
                }
                var path: GeneralPath? = null
                if (type and CLIP != 0) {
                    path = GeneralPath()
                }
                if (paint != null) {
                    g2.paint = paint
                }
                for (i in txShapes.indices) {
                    if (type and CLIP != 0) {
                        path!!.append(txShapes[i], false)
                    } else {
                        g2.fill(txShapes[i])
                    }
                }
                if (type and CLIP != 0) {
                    g2.clip(path)
                }
                if (saveAC != null) {
                    g2.composite = saveAC
                }
            }

            @Suppress("unused", "MemberVisibilityCanBePrivate")
            companion object
            {
                const val INC = 1
                const val DEC = 2
                const val R = 4            // rotate
                const val RI = R or INC
                const val RD = R or DEC
                const val SC = 8            // scale
                const val SCI = SC or INC
                const val SCD = SC or DEC
                const val SCX = 16           // scale invert x
                const val SCXI = SCX or SC or INC
                const val SCXD = SCX or SC or DEC
                const val SCY = 32           // scale invert y
                const val SCYI = SCY or SC or INC
                const val SCYD = SCY or SC or DEC
                const val AC = 64           // AlphaComposite
                const val CLIP = 128          // Clipping
                const val NOP = 512          // No Paint
            }
        } // End TxE class

        companion object
        {
            lateinit var surf: Surface
            lateinit var cupanim: Image
            lateinit var java_logo: Image
            var bufferedImage: BufferedImage? = null

            fun getMetrics(font: Font): FontMetrics {
                return surf.getFontMetrics(font)
            }
        }
    } // End Surface class

    companion object
    {
        internal val myBlack = Color(20, 20, 20)
        internal val myWhite = Color(240, 240, 255)
        internal val myRed = Color(149, 43, 42)
        internal val myBlue = Color(94, 105, 176)
        internal val myYellow = Color(255, 255, 140)

        @JvmStatic
        fun main(argv: Array<String>) {
            val intro = Intro()
            JFrame("Java2D Demo - Intro").apply {
                addWindowListener(object : WindowAdapter() {
                    override fun windowClosing(e: WindowEvent?) {
                        System.exit(0)
                    }
                    override fun windowDeiconified(e: WindowEvent?) {
                        intro.start()
                    }
                    override fun windowIconified(e: WindowEvent?) {
                        intro.stop()
                    }
                })
                contentPane.add("Center", intro)
                pack()
                setSize(720, 510)
                setLocationRelativeTo(null)
                isVisible = true
            }
            intro.start()
        }
    }
}
