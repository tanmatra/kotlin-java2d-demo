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
import java2d.copy
import java2d.createSimilar
import java2d.hasBits
import java2d.use
import java.awt.AlphaComposite
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Color.BLACK
import java.awt.Color.WHITE
import java.awt.Color.YELLOW
import java.awt.Composite
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Paint
import java.awt.Point
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.TexturePaint
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.font.FontRenderContext
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import java.awt.geom.FlatteningPathIterator
import java.awt.geom.GeneralPath
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.util.ArrayList
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSlider
import javax.swing.JTable
import javax.swing.border.BevelBorder
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.EtchedBorder
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.TableModelEvent
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableModel

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
                        scenesTable = ScenesTable()
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
     * ScenesTable is the list of scenes known to the Director.
     * Scene participation, scene name and scene pause amount columns.
     * Global animation delay for scene's steps.
     */
    internal inner class ScenesTable : JPanel(), ActionListener, ChangeListener
    {
        private val table: JTable
        private val dataModel: TableModel

        init {
            background = WHITE
            layout = BorderLayout()
            val names = arrayOf("", "Scenes", "Pause")

            dataModel = object : AbstractTableModel()
            {
                override fun getColumnCount(): Int {
                    return names.size
                }

                override fun getRowCount(): Int {
                    return surface.director.size
                }

                override fun getValueAt(row: Int, col: Int): Any? {
                    val scene = surface.director[row]
                    return when (col) {
                        0 -> scene.participate
                        1 -> scene.name
                        2 -> scene.pauseAmt
                        else -> null
                    }
                }

                override fun getColumnName(col: Int): String {
                    return names[col]
                }

                override fun getColumnClass(c: Int): Class<*> {
                    return getValueAt(0, c)!!.javaClass
                }

                override fun isCellEditable(row: Int, col: Int): Boolean {
                    return col != 1
                }

                override fun setValueAt(aValue: Any?, row: Int, col: Int) {
                    val scene = surface.director[row]
                    when (col) {
                        0 -> scene.participate = aValue
                        1 -> scene.name = aValue as String
                        2 -> scene.pauseAmt = aValue as Long
                    }
                }
            }

            table = JTable(dataModel)
            var col = table.getColumn("")
            col.width = 16
            col.minWidth = 16
            col.maxWidth = 20
            col = table.getColumn("Pause")
            col.width = 60
            col.minWidth = 60
            col.maxWidth = 60
            table.sizeColumnsToFit(0)

            val scrollpane = JScrollPane(table)
            add(scrollpane)

            val panel = JPanel(BorderLayout())
            val b = JButton("Unselect All")
            b.horizontalAlignment = JButton.LEFT
            val font = Font("serif", Font.PLAIN, 10)
            b.font = font
            b.addActionListener(this)
            panel.add("West", b)

            val slider = JSlider(JSlider.HORIZONTAL, 0, 200, surface.sleepAmt.toInt())
            slider.addChangeListener(this)
            val tb = TitledBorder(EtchedBorder())
            tb.titleFont = font
            tb.title = ("Anim delay = " + surface.sleepAmt.toString()
                    + " ms")
            slider.border = tb
            slider.preferredSize = Dimension(140, 40)
            slider.minimumSize = Dimension(100, 40)
            slider.maximumSize = Dimension(180, 40)
            panel.add("East", slider)

            add("South", panel)
        }

        override fun actionPerformed(e: ActionEvent) {
            val b = e.source as JButton
            b.isSelected = !b.isSelected
            b.text = if (b.isSelected) "Select All" else "Unselect All"
            for (i in surface.director.indices) {
                val scene = surface.director[i]
                scene.participate = java.lang.Boolean.valueOf(!b.isSelected)
            }
            table.tableChanged(TableModelEvent(dataModel))
        }

        override fun stateChanged(e: ChangeEvent) {
            val slider = e.source as JSlider
            val value = slider.value
            val tb = slider.border as TitledBorder
            tb.title = "Anim delay = " + value.toString() + " ms"
            surface.sleepAmt = value.toLong()
            slider.repaint()
        }
    }  // End ScenesTable class

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
            if (thread != null) {
                thread!!.interrupt()
            }
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
                if (scene.participate as Boolean) {
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
         * Part is a piece of the scene.  Classes must implement Part
         * in order to participate in a scene.
         */
        internal interface Part
        {
            val begin: Int

            val end: Int

            fun reset(newWidth: Int, newHeight: Int)

            fun step(w: Int, h: Int)

            fun render(w: Int, h: Int, g2: Graphics2D)
        }

        /**
         * Scene is the manager of the parts.
         */
        internal class Scene(
            var name: String,
            var pauseAmt: Long,
            private val parts: Array<out Part>
        ) {
            var participate: Any? = java.lang.Boolean.TRUE
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
                for (i in parts.indices) {
                    parts[i].reset(w, h)
                }
            }

            fun step(w: Int, h: Int) {
                for (i in parts.indices) {
                    val part = parts[i]
                    if (index >= part.begin && index <= part.end) {
                        part.step(w, h)
                    }
                }
            }

            fun render(w: Int, h: Int, g2: Graphics2D) {
                for (i in parts.indices) {
                    val part = parts[i]
                    if (index >= part.begin && index <= part.end) {
                        part.render(w, h, g2)
                    }
                }
            }

            fun pause() {
                try {
                    Thread.sleep(java.lang.Long.parseLong((pauseAmt as String?)!!))
                } catch (ignored: Exception) {
                }
                System.gc()
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

        /**
         * GradientPaint Effect.  Burst, split, horizontal and
         * vertical gradient fill effects.
         */
        internal class GpE(
            private val type: Int,
            private val c1: Color,
            private val c2: Color,
            override val begin: Int,
            override val end: Int
        ) : Part
        {
            private var incr: Float = 0.0f
            private var index: Float = 0.0f
            private val rect = ArrayList<Rectangle2D>()
            private val grad = ArrayList<GradientPaint>()

            override fun reset(newWidth: Int, newHeight: Int) {
                incr = 1.0f / (end - begin)
                if (type and CNT != 0) {
                    incr /= 2.3f
                }
                if (type and CNT != 0 && type and INC != 0) {
                    index = 0.5f
                } else if (type and DEC != 0) {
                    index = 1.0f
                    incr = -incr
                } else {
                    index = 0.0f
                }
                index += incr
            }

            override fun step(w: Int, h: Int) {
                rect.clear()
                grad.clear()

                when {
                    type and WID != 0 -> {
                        val w2: Float
                        val x1: Float
                        val x2: Float
                        if (type and SPL != 0) {
                            w2 = w * 0.5f
                            x1 = w * (1.0f - index)
                            x2 = w * index
                        } else {
                            w2 = w * index
                            x2 = w2
                            x1 = x2
                        }
                        rect.add(Rectangle2D.Float(0f, 0f, w2, h.toFloat()))
                        rect.add(Rectangle2D.Float(w2, 0f, w - w2, h.toFloat()))
                        grad.add(GradientPaint(0f, 0f, c1, x1, 0f, c2))
                        grad.add(GradientPaint(x2, 0f, c2, w.toFloat(), 0f, c1))
                    }
                    type and HEI != 0 -> {
                        val h2: Float
                        val y1: Float
                        val y2: Float
                        if (type and SPL != 0) {
                            h2 = h * 0.5f
                            y1 = h * (1.0f - index)
                            y2 = h * index
                        } else {
                            h2 = h * index
                            y2 = h2
                            y1 = y2
                        }
                        rect.add(Rectangle2D.Float(0f, 0f, w.toFloat(), h2))
                        rect.add(Rectangle2D.Float(0f, h2, w.toFloat(), h - h2))
                        grad.add(GradientPaint(0f, 0f, c1, 0f, y1, c2))
                        grad.add(GradientPaint(0f, y2, c2, 0f, h.toFloat(), c1))
                    }
                    type and BUR != 0 -> {
                        val w2 = (w / 2).toFloat()
                        val h2 = (h / 2).toFloat()

                        rect.add(Rectangle2D.Float(0f, 0f, w2, h2))
                        rect.add(Rectangle2D.Float(w2, 0f, w2, h2))
                        rect.add(Rectangle2D.Float(0f, h2, w2, h2))
                        rect.add(Rectangle2D.Float(w2, h2, w2, h2))

                        val x1 = w * (1.0f - index)
                        val x2 = w * index
                        val y1 = h * (1.0f - index)
                        val y2 = h * index

                        grad.add(GradientPaint(0f, 0f, c1, x1, y1, c2))
                        grad.add(GradientPaint(w.toFloat(), 0f, c1, x2, y1, c2))
                        grad.add(GradientPaint(0f, h.toFloat(), c1, x1, y2, c2))
                        grad.add(GradientPaint(w.toFloat(), h.toFloat(), c1, x2, y2, c2))
                    }
                    type and NF != 0 -> {
                        val y = h * index
                        grad.add(GradientPaint(0f, 0f, c1, 0f, y, c2))
                    }
                }

                if (type and INC != 0 || type and DEC != 0) {
                    index += incr
                }
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
                for (i in grad.indices) {
                    g2.paint = grad[i]
                    if (type and NF == 0) {
                        g2.fill(rect[i])
                    }
                }
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            }

            @Suppress("unused")
            companion object
            {
                const val INC = 1               // increasing
                const val DEC = 2               // decreasing
                const val CNT = 4               // center
                const val WID = 8               // width
                const val WI = WID or INC
                const val WD = WID or DEC
                const val HEI = 16              // height
                const val HI = HEI or INC
                const val HD = HEI or DEC
                const val SPL = 32 or CNT       // split
                const val SIW = SPL or INC or WID
                const val SDW = SPL or DEC or WID
                const val SIH = SPL or INC or HEI
                const val SDH = SPL or DEC or HEI
                const val BUR = 64 or CNT        // burst
                const val BURI = BUR or INC
                const val BURD = BUR or DEC
                const val NF = 128               // no fill
            }
        } // End GpE class

        /**
         * TexturePaint Effect.  Expand and collapse a texture.
         */
        internal class TpE(
            private val type: Int,
            private val p1: Paint,
            private val p2: Paint,
            size: Int,
            override val begin: Int,
            override val end: Int
        ) : Part
        {
            private var incr: Float = 0.0f
            private var index: Float = 0.0f
            private var texture: TexturePaint? = null
            private var size: Int = 0
            private var bimg: BufferedImage? = null
            private var rect: Rectangle? = null

            init {
                setTextureSize(size)
            }

            private fun setTextureSize(size: Int) {
                this.size = size
                bimg = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
                rect = Rectangle(0, 0, size, size)
            }

            override fun reset(newWidth: Int, newHeight: Int) {
                incr = size.toFloat() / (end - begin).toFloat()
                if (type and HAF != 0) {
                    incr /= 2f
                }
                if (type and DEC != 0) {
                    index = size.toFloat()
                    if (type and HAF != 0) {
                        index /= 2f
                    }
                    incr = -incr
                } else {
                    index = 0.0f
                }
                index += incr
            }

            override fun step(w: Int, h: Int) {
                val g2 = bimg!!.createGraphics()
                g2.paint = p1
                g2.fillRect(0, 0, size, size)
                g2.paint = p2
                if (type and OVAL != 0) {
                    g2.fill(Ellipse2D.Float(0f, 0f, index, index))
                } else if (type and RECT != 0) {
                    g2.fill(Rectangle2D.Float(0f, 0f, index, index))
                }
                texture = TexturePaint(bimg, rect!!)
                g2.dispose()
                index += incr
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                g2.paint = texture
                if (type and NF == 0) {
                    g2.fillRect(0, 0, w, h)
                }
            }

            @Suppress("MemberVisibilityCanBePrivate")
            companion object
            {
                const val INC  =  1 // increasing
                const val DEC  =  2 // decreasing
                const val OVAL =  4 // oval
                const val RECT =  8 // rectangle
                const val HAF  = 16 // half oval or rect size
                const val NF   = 32 // no fill
                const val OI = OVAL or INC
                const val OD = OVAL or DEC
                const val RI = RECT or INC
                const val RD = RECT or DEC
            }
        } // End TpE class

        /**
         * Close out effect.  Close out the buffered image with different geometry shapes.
         */
        internal class CoE(
            private var type: Int,
            override val begin: Int,
            override val end: Int
        ) : Part
        {
            private var bimg: BufferedImage? = null
            private var shape: Shape? = null
            private var zoom: Double = 0.toDouble()
            private var extent: Double = 0.toDouble()
            private val zIncr: Double = - (2.0 / (end - begin))
            private val eIncr: Double = 360.0 / (end - begin)
            private val doRandom: Boolean = type and RAND != 0

            override fun reset(newWidth: Int, newHeight: Int) {
                if (doRandom) {
                    val num = (Math.random() * 5.0).toInt()
                    type = when (num) {
                        0 -> OVAL
                        1 -> RECT
                        2 -> RECT or WID
                        3 -> RECT or HEI
                        4 -> ARC
                        else -> OVAL
                    }
                }
                shape = null
                bimg = null
                extent = 360.0
                zoom = 2.0
            }

            override fun step(w: Int, h: Int) {
                if (bimg == null) {
                    bimg = bufferedImage!!.copy()
                }
                val z = Math.min(w, h) * zoom
                shape = when {
                    type hasBits OVAL -> Ellipse2D.Double(w / 2 - z / 2, h / 2 - z / 2, z, z)
                    type hasBits ARC -> Arc2D.Double(-100.0, -100.0, w + 200.0, h + 200.0, 90.0, extent, Arc2D.PIE)
                        .also { extent -= eIncr }
                    type hasBits RECT -> when {
                        type hasBits WID -> Rectangle2D.Double(w / 2 - z / 2, 0.0, z, h.toDouble())
                        type hasBits HEI -> Rectangle2D.Double(0.0, h / 2 - z / 2, w.toDouble(), z)
                        else -> Rectangle2D.Double(w / 2 - z / 2, h / 2 - z / 2, z, z)
                    }
                    else -> shape // ?
                }
                zoom += zIncr
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                g2.clip(shape)
                g2.drawImage(bimg, 0, 0, null)
            }

            companion object
            {
                const val WID = 1
                const val HEI = 2
                const val OVAL = 4
                const val RECT = 8
                const val RAND = 16
                const val ARC = 32
            }
        } // End CoE class

        /**
         * Dither Dissolve Effect. For each successive step in the animation,
         * a pseudo-random starting horizontal position is chosen using list,
         * and then the corresponding points created from xlist and ylist are
         * blacked out for the current "chunk".  The x and y chunk starting
         * positions are each incremented by the associated chunk size, and
         * this process is repeated for the number of "steps" in the
         * animation, causing an equal number of pseudo-randomly picked
         * "blocks" to be blacked out during each step of the animation.
         */
        internal class DdE(
            override val begin: Int,
            override val end: Int,
            private val blocksize: Int
        ) : Part
        {
            private var bimg: BufferedImage? = null
            private var big: Graphics2D? = null
            private lateinit var list: List<Int>
            private lateinit var xlist: List<Int>
            private lateinit var ylist: List<Int>
            private var xeNum: Int = 0
            private var yeNum: Int = 0    // element number
            private var xcSize: Int = 0
            private var ycSize: Int = 0  // chunk size
            private var inc: Int = 0

            private fun createShuffledLists() {
                xlist = MutableList(bimg!!.width) { i -> i }.apply { shuffle() }
                ylist = MutableList(bimg!!.height) { i -> i }.apply { shuffle() }
                list = MutableList(end - begin + 1) { i -> i }.apply { shuffle() }
            }

            override fun reset(newWidth: Int, newHeight: Int) {
                bimg = null
            }

            override fun step(w: Int, h: Int) {
                if (inc > end) {
                    bimg = null
                }
                if (bimg == null) {
                    bimg = bufferedImage!!.createSimilar().also { bimg ->
                        big = bimg.createGraphics().also { g ->
                            g.drawImage(bufferedImage, 0, 0, null)
                        }
                    }
                    createShuffledLists()
                    xcSize = xlist.size / (end - begin) + 1
                    ycSize = ylist.size / (end - begin) + 1
                    xeNum = 0
                    inc = 0
                }
                xeNum = xcSize * list[inc]
                yeNum = -ycSize
                inc++
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                big!!.color = myBlack

                for (k in 0..end - begin) {
                    if (xeNum + xcSize > xlist.size) {
                        xeNum = 0
                    } else {
                        xeNum += xcSize
                    }
                    yeNum += ycSize

                    var i = xeNum
                    while (i < xeNum + xcSize && i < xlist.size) {
                        var j = yeNum
                        while (j < yeNum + ycSize && j < ylist.size) {
                            val xval = xlist[i]
                            val yval = ylist[j]
                            if (xval % blocksize == 0 && yval % blocksize == 0) {
                                big!!.fillRect(xval, yval, blocksize, blocksize)
                            }
                            j++
                        }
                        i++
                    }
                }

                g2.drawImage(bimg, 0, 0, null)
            }
        } // End DdE class

        /**
         * Subimage effect.  Subimage the scene's buffered
         * image then rotate and scale down the subimages.
         */
        internal class SiE(
            private val siw: Int,
            private val sih: Int,
            override val begin: Int,
            override val end: Int
        ) : Part
        {
            private var bimg: BufferedImage? = null
            private val rIncr: Double = 360.0 / (end - begin)
            private val sIncr: Double = 1.0 / (end - begin)
            private var scale: Double = 0.0
            private var rotate: Double = 0.0
            private val subs = ArrayList<BufferedImage>(20)
            private val pts = ArrayList<Point>(20)

            override fun reset(newWidth: Int, newHeight: Int) {
                scale = 1.0
                rotate = 0.0
                bimg = null
                subs.clear()
                pts.clear()
            }

            override fun step(w: Int, h: Int) {
                if (bimg == null) {
                    bimg = bufferedImage!!.createSimilar().also { bimg ->
                        bimg.createGraphics().use { big ->
                            big.drawImage(bufferedImage, 0, 0, null)
                            run {
                                var x = 0
                                while (x < w && scale > 0.0) {
                                    val ww = if (x + siw < w) siw else w - x
                                    run {
                                        var y = 0
                                        while (y < h) {
                                            val hh = if (y + sih < h) sih else h - y
                                            subs.add(bimg.getSubimage(x, y, ww, hh))
                                            pts.add(Point(x, y))
                                            y += sih
                                        }
                                    }
                                    x += siw
                                }
                            }
                        }
                    }
                }

                rotate += rIncr
                scale -= sIncr
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                val saveTx = g2.transform
                g2.color = myBlue
                var i = 0
                while (i < subs.size && scale > 0.0) {
                    val bi = subs[i]
                    val p = pts[i]
                    val ww = bi.width
                    val hh = bi.height
                    val at = AffineTransform()
                    at.rotate(Math.toRadians(rotate), (p.x + ww / 2).toDouble(), (p.y + hh / 2).toDouble())
                    at.translate(p.x.toDouble(), p.y.toDouble())
                    at.scale(scale, scale)

                    val b1 = Rectangle(0, 0, ww, hh)
                    val shape = at.createTransformedShape(b1)
                    val b2 = shape.bounds2D
                    val xx = p.x + ww / 2 - (b2.x + b2.width / 2)
                    val yy = p.y + hh / 2 - (b2.y + b2.height / 2)
                    val toCenterAT = AffineTransform()
                    toCenterAT.translate(xx, yy)
                    toCenterAT.concatenate(at)

                    g2.transform = toCenterAT
                    g2.drawImage(bi, 0, 0, null)
                    g2.draw(b1)
                    i++
                }
                g2.transform = saveTx
            }
        } // End SiE class

        /**
         * Line Effect.  Flattened ellipse with lines from the center
         * to the edge.  Expand or collapse the ellipse.  Fade in or out
         * the lines.
         */
        internal class LnE(private val type: Int, override val begin: Int, override val end: Int) :
            Part
        {
            private var rIncr: Double = 0.0
            private var rotate: Double = 0.0
            private var zIncr: Double = 0.0
            private var zoom: Double = 0.0
            private val pts = ArrayList<Point2D.Double>()
            private var alpha: Float = 0.0f
            private var aIncr: Float = 0.0f

            init {
                val range = (this.end - begin).toFloat()
                rIncr = (360.0f / range).toDouble()
                aIncr = 0.9f / range
                zIncr = (2.0f / range).toDouble()
                if (type and DEC != 0) {
                    rIncr = -rIncr
                    aIncr = -aIncr
                    zIncr = -zIncr
                }
            }

            private fun generatePts(w: Int, h: Int, sizeF: Double) {
                pts.clear()
                val size = Math.min(w, h) * sizeF
                val ellipse = Ellipse2D.Double(w / 2 - size / 2, h / 2 - size / 2, size, size)
                val pi = ellipse.getPathIterator(null, 0.8)
                while (!pi.isDone) {
                    val pt = DoubleArray(6)
                    when (pi.currentSegment(pt)) {
                        FlatteningPathIterator.SEG_MOVETO, FlatteningPathIterator.SEG_LINETO ->
                            pts.add(Point2D.Double(pt[0], pt[1]))
                    }
                    pi.next()
                }
            }

            override fun reset(newWidth: Int, newHeight: Int) {
                if (type and DEC != 0) {
                    rotate = 360.0
                    alpha = 1.0f
                    zoom = 2.0
                } else {
                    alpha = 0f
                    rotate = alpha.toDouble()
                    zoom = 0.0
                }
                if (type and ZOOM == 0) {
                    generatePts(newWidth, newHeight, 0.5)
                }
            }

            override fun step(w: Int, h: Int) {
                if (type and ZOOM != 0) {
                    zoom += zIncr
                    generatePts(w, h, zoom)
                }
                if (type and RI != 0 || type and RI != 0) {
                    rotate += rIncr
                }
                if (type and ACI != 0 || type and ACD != 0) {
                    alpha += aIncr
                }
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                var saveAC: Composite? = null
                if (type and AC != 0 && alpha >= 0 && alpha <= 1) {
                    saveAC = g2.composite
                    g2.composite = AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, alpha
                                                             )
                }
                var saveTx: AffineTransform? = null
                if (type and R != 0) {
                    saveTx = g2.transform
                    val at = AffineTransform()
                    at.rotate(Math.toRadians(rotate), (w / 2).toDouble(), (h / 2).toDouble())
                    g2.transform = at
                }
                val p1 = Point2D.Double((w / 2).toDouble(), (h / 2).toDouble())
                g2.color = YELLOW
                for (pt in pts) {
                    g2.draw(Line2D.Float(p1, pt))
                }
                if (saveTx != null) {
                    g2.transform = saveTx
                }
                if (saveAC != null) {
                    g2.composite = saveAC
                }
            }

            @Suppress("MemberVisibilityCanBePrivate")
            companion object
            {
                const val INC = 1
                const val DEC = 2
                const val R = 4             // rotate
                const val ZOOM = 8             // zoom
                const val AC = 32             // AlphaComposite
                const val RI = R or INC
                const val RD = R or DEC
                const val ZOOMI = ZOOM or INC
                const val ZOOMD = ZOOM or DEC
                const val ACI = AC or INC
                const val ACD = AC or DEC
            }
        } // End LnE class

        /**
         * Template for Features & Contributors consisting of translating
         * blue and red rectangles and an image going from transparent to
         * opaque.
         */
        internal class Temp(
            private val type: Int,
            private val img: Image?,
            override val begin: Int,
            override val end: Int
        ) : Part {
            private var alpha: Float = 0.toFloat()
            private val aIncr: Float = 0.9f / (end - begin)
            private var rect1: Rectangle? = null
            private var rect2: Rectangle? = null
            private var x: Int = 0
            private var y: Int = 0
            private var xIncr: Int = 0
            private var yIncr: Int = 0

            init {
                if (type and NOANIM != 0) {
                    alpha = 1.0f
                }
            }

            override fun reset(newWidth: Int, newHeight: Int) {
                rect1 = Rectangle(8, 20, newWidth - 20, 30)
                rect2 = Rectangle(20, 8, 30, newHeight - 20)
                if (type and NOANIM == 0) {
                    alpha = 0.0f
                    xIncr = newWidth / (end - begin)
                    yIncr = newHeight / (end - begin)
                    x = newWidth + (xIncr * 1.4).toInt()
                    y = newHeight + (yIncr * 1.4).toInt()
                }
            }

            override fun step(w: Int, h: Int) {
                if (type and NOANIM != 0) {
                    return
                }
                if (type and RECT != 0) {
                    x -= xIncr
                    rect1!!.setLocation(x, 20)
                    y -= yIncr
                    rect2!!.setLocation(20, y)
                }
                if (type and IMG != 0) {
                    alpha += aIncr
                }
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                if (type and RECT != 0) {
                    g2.color = myBlue
                    g2.fill(rect1)
                    g2.color = myRed
                    g2.fill(rect2)
                }
                if (type and IMG != 0) {
                    val saveAC = g2.composite
                    if (alpha in 0.0 .. 1.0) {
                        g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
                    }
                    g2.drawImage(img, 30, 30, null)
                    g2.composite = saveAC
                }
            }

            companion object
            {
                const val NOANIM = 1
                const val RECT = 2
                const val IMG = 4
                const val RNA = RECT or NOANIM
                const val INA = IMG or NOANIM
            }
        } // End Temp class

        /**
         * Features of Java2D.  Single character advancement effect.
         */
        internal class Features(type: Int, override val begin: Int, override val end: Int) :
            Part
        {
            private val list: Array<String>
            private var strH: Int = 0
            private var endIndex: Int = 0
            private var listIndex: Int = 0
            private val v = ArrayList<String>()

            init {
                list = table[type]
            }

            override fun reset(newWidth: Int, newHeight: Int) {
                strH = fm2.ascent + fm2.descent
                endIndex = 1
                listIndex = 0
                v.clear()
                v.add(list[listIndex].substring(0, endIndex))
            }

            override fun step(w: Int, h: Int) {
                if (listIndex < list.size) {
                    if (++endIndex > list[listIndex].length) {
                        if (++listIndex < list.size) {
                            endIndex = 1
                            v.add(list[listIndex].substring(0, endIndex))
                        }
                    } else {
                        v[listIndex] = list[listIndex].substring(0, endIndex)
                    }
                }
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                g2.color = myWhite
                g2.font = font1
                g2.drawString(v[0], 90, 85)
                g2.font = font2
                run {
                    var i = 1
                    var y = 90
                    while (i < v.size) {
                        y += strH
                        g2.drawString(v[i], 120, y)
                        i++
                    }
                }
            }

            companion object
            {
                const val GRAPHICS = 0
                const val TEXT     = 1
                const val IMAGES   = 2
                const val COLOR    = 3

                val font1 = Font("serif", Font.BOLD, 38)
                val font2 = Font("serif", Font.PLAIN, 24)

                // var fm1 = Surface.getMetrics(font1)
                var fm2 = getMetrics(font2)

                var table = arrayOf(
                    arrayOf(
                        "Graphics",
                        "Antialiased rendering",
                        "Bezier paths",
                        "Transforms",
                        "Compositing",
                        "Stroking parameters"),
                    arrayOf(
                        "Text",
                        "Extended font support",
                        "Advanced text layout",
                        "Dynamic font loading",
                        "AttributeSets for font customization"),
                    arrayOf(
                        "Images",
                        "Flexible image layouts",
                        "Extended imaging operations",
                        "   Convolutions, Lookup Tables",
                        "RenderableImage interface"),
                    arrayOf(
                        "Color",
                        "ICC profile support",
                        "Color conversion",
                        "Arbitrary color spaces"))
            }
        } // End Features class

        /**
         * Scrolling text of Java2D contributors.
         */
        internal class Contributors(override val begin: Int, override val end: Int) : Part {
            private var nStrs: Int = 0
            private var strH: Int = 0
            private var index: Int = 0
            private var yh: Int = 0
            private var height: Int = 0
            private val v = ArrayList<String>()
            private val cast = ArrayList<String>(members.size + 3)
            private var counter: Int = 0
            private val cntMod: Int
            private var gp: GradientPaint? = null

            init {
                members.sort()
                cast.add("CONTRIBUTORS")
                cast.add(" ")
                cast.addAll(members)
                cast.add(" ")
                cast.add(" ")
                cntMod = (this.end - begin) / cast.size - 1
            }

            override fun reset(newWidth: Int, newHeight: Int) {
                v.clear()
                strH = fm.ascent + fm.descent
                nStrs = (newHeight - 40) / strH + 1
                height = strH * (nStrs - 1) + 48
                index = 0
                gp = GradientPaint(0f, (newHeight / 2).toFloat(), WHITE, 0f, (newHeight + 20).toFloat(), BLACK)
                counter = 0
            }

            override fun step(w: Int, h: Int) {
                if (counter++ % cntMod == 0) {
                    if (index < cast.size) {
                        v.add(cast[index])
                    }
                    if ((v.size == nStrs || index >= cast.size) && !v.isEmpty()) {
                        v.removeAt(0)
                    }
                    ++index
                }
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                g2.paint = gp
                g2.font = font
                val remainder = (counter % cntMod).toDouble()
                var incr = 1.0 - remainder / cntMod
                incr = if (incr == 1.0) 0.0 else incr
                var y = (incr * strH).toInt()

                if (index >= cast.size) {
                    y += yh
                } else {
                    yh = height - v.size * strH + y
                    y = yh
                }
                for (s in v) {
                    y += strH
                    g2.drawString(s, w / 2 - fm.stringWidth(s) / 2, y)
                }
            }

            companion object
            {
                var members = arrayOf(
                    "Brian Lichtenwalter",
                    "Jeannette Hung",
                    "Thanh Nguyen",
                    "Jim Graham",
                    "Jerry Evans",
                    "John Raley",
                    "Michael Peirce",
                    "Robert Kim",
                    "Jennifer Ball",
                    "Deborah Adair",
                    "Paul Charlton",
                    "Dmitry Feld",
                    "Gregory Stone",
                    "Richard Blanchard",
                    "Link Perry",
                    "Phil Race",
                    "Vincent Hardy",
                    "Parry Kejriwal",
                    "Doug Felt",
                    "Rekha Rangarajan",
                    "Paula Patel",
                    "Michael Bundschuh",
                    "Joe Warzecha",
                    "Joey Beheler",
                    "Aastha Bhardwaj",
                    "Daniel Rice",
                    "Chris Campbell",
                    "Shinsuke Fukuda",
                    "Dmitri Trembovetski",
                    "Chet Haase",
                    "Jennifer Godinez",
                    "Nicholas Talian",
                    "Raul Vera",
                    "Ankit Patel",
                    "Ilya Bagrak",
                    "Praveen Mohan",
                    "Rakesh Menon"
                                     )
                val font = Font("serif", Font.PLAIN, 26)
                var fm = getMetrics(font)
            }
        } // End Contributors class

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
