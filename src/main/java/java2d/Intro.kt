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
package java2d

import java.awt.AlphaComposite
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Color.BLACK
import java.awt.Color.GRAY
import java.awt.Color.RED
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
import java.awt.Toolkit
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
import java.util.Arrays
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
class Intro : JPanel()
{
    private var scenesTable: ScenesTable? = null
    private var doTable: Boolean = false

    init {
        val eb = EmptyBorder(80, 110, 80, 110)
        val bb = BevelBorder(BevelBorder.LOWERED)
        border = CompoundBorder(eb, bb)
        layout = BorderLayout()
        background = GRAY
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
    internal class ScenesTable : JPanel(), ActionListener, ChangeListener {

        private val table: JTable
        private val dataModel: TableModel

        init {
            background = WHITE
            layout = BorderLayout()
            val names = arrayOf("", "Scenes", "Pause")

            dataModel = object : AbstractTableModel() {

                override fun getColumnCount(): Int {
                    return names.size
                }

                override fun getRowCount(): Int {
                    return surface.director.size
                }

                override fun getValueAt(row: Int, col: Int): Any? {
                    val scene = surface.director[row]
                    return if (col == 0) {
                        scene.participate
                    } else if (col == 1) {
                        scene.name
                    } else {
                        scene.pauseAmt
                    }
                }

                override fun getColumnName(col: Int): String {
                    return names[col]
                }

                override fun getColumnClass(c: Int): Class<*> {
                    return getValueAt(0, c)!!.javaClass
                }

                override fun isCellEditable(row: Int, col: Int): Boolean {
                    return if (col != 1) true else false
                }

                override fun setValueAt(aValue: Any?, row: Int, col: Int) {
                    val scene = surface.director[row]
                    if (col == 0) {
                        scene.participate = aValue
                    } else if (col == 1) {
                        scene.name = aValue
                    } else {
                        scene.pauseAmt = aValue
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

            val slider = JSlider(
                JSlider.HORIZONTAL, 0, 200,
                surface.sleepAmt.toInt()
                                )
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
    internal class Surface : JPanel(), Runnable {
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

        override fun paint(g: Graphics?) {
            val d = size
            if (d.width <= 0 || d.height <= 0) {
                return
            }
            if (bimg == null || bimg!!.width != d.width || bimg!!.height != d.height) {
                bimg = graphicsConfiguration.createCompatibleImage(
                    d.width,
                    d.height
                                                                  )
                // reset future scenes
                for (i in index + 1 until director.size) {
                    director[i].reset(d.width, d.height)
                }
            }

            val scene = director[index]
            if (scene.index <= scene.length) {
                if (thread != null) {
                    scene.step(d.width, d.height)
                }

                val g2 = bimg!!.createGraphics()
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                                   )
                g2.background = background
                g2.clearRect(0, 0, d.width, d.height)

                scene.render(d.width, d.height, g2)

                if (thread != null) {
                    // increment scene.index after scene.render
                    scene.index++
                }
                g2.dispose()
            }
            g!!.drawImage(bimg, 0, 0, this)
        }

        fun start() {
            if (thread == null) {
                thread = Thread(this)
                thread!!.priority = Thread.MIN_PRIORITY
                thread!!.name = "Intro"
                thread!!.start()
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
        internal interface Part {

            val begin: Int

            val end: Int

            fun reset(newwidth: Int, newheight: Int)

            fun step(w: Int, h: Int)

            fun render(w: Int, h: Int, g2: Graphics2D)
        }

        /**
         * Director is the holder of the scenes, their names & pause amounts
         * between scenes.
         */
        internal class Director : ArrayList<Scene>() {

            var gp = GradientPaint(0f, 40f, myBlue, 38f, 2f, myBlack)
            var f1 = Font("serif", Font.PLAIN, 200)
            var f2 = Font("serif", Font.PLAIN, 120)
            var f3 = Font("serif", Font.PLAIN, 72)
            var partsInfo = arrayOf(
                arrayOf(
                    arrayOf<Any>("J  -  scale text on gradient", "0"),
                    arrayOf<Any>(GpE(GpE.BURI, myBlack, myBlue, 0, 20), TxE("J", f1, TxE.SCI, myYellow, 2, 20))
                       ),
                arrayOf(
                    arrayOf<Any>("2  -  scale & rotate text on gradient", "0"),
                    arrayOf<Any>(
                        GpE(GpE.BURI, myBlue, myBlack, 0, 22),
                        TxE("2", f1, TxE.RI or TxE.SCI, myYellow, 2, 22)
                                )
                       ),
                arrayOf(
                    arrayOf<Any>("D  -  scale text on gradient", "0"),
                    arrayOf<Any>(GpE(GpE.BURI, myBlack, myBlue, 0, 20), TxE("D", f1, TxE.SCI, myYellow, 2, 20))
                       ),
                arrayOf(
                    arrayOf<Any>("Java2D  -  scale & rotate text on gradient", "1000"),
                    arrayOf<Any>(
                        GpE(GpE.SIH, myBlue, myBlack, 0, 40),
                        TxE("Java2D", f2, TxE.RI or TxE.SCI, myYellow, 0, 40)
                                )
                       ),
                arrayOf(arrayOf<Any>("Previous scene dither dissolve out", "0"), arrayOf<Any>(DdE(0, 20, 1))),
                arrayOf(
                    arrayOf<Any>("Graphics Features", "999"),
                    arrayOf<Any>(
                        Temp(Temp.RECT, null, 0, 15),
                        Temp(Temp.IMG, java_logo, 2, 15),
                        Temp(Temp.RNA or Temp.INA, java_logo, 16, 130),
                        Features(Features.GRAPHICS, 16, 130)
                                )
                       ),
                arrayOf(
                    arrayOf<Any>("Java2D  -  texture text on gradient", "1000"), arrayOf<Any>(
                        GpE(GpE.WI, myBlue, myBlack, 0, 20),
                        GpE(GpE.WD, myBlue, myBlack, 21, 40),
                        TpE(TpE.OI or TpE.NF, myBlack, myYellow, 4, 0, 10),
                        TpE(TpE.OD or TpE.NF, myBlack, myYellow, 4, 11, 20),
                        TpE(
                            TpE.OI or TpE.NF or TpE.HAF, myBlack, myYellow, 5,
                            21, 40
                           ),
                        TxE("Java2D", f2, 0, null, 0, 40)
                                                                                             )
                       ),
                arrayOf(arrayOf<Any>("Previous scene random close out", "0"), arrayOf<Any>(CoE(CoE.RAND, 0, 20))),
                arrayOf(
                    arrayOf<Any>("Text Features", "999"),
                    arrayOf<Any>(
                        Temp(Temp.RECT, null, 0, 15),
                        Temp(Temp.IMG, java_logo, 2, 15),
                        Temp(Temp.RNA or Temp.INA, java_logo, 16, 130),
                        Features(Features.TEXT, 16, 130)
                                )
                       ),
                arrayOf(
                    arrayOf<Any>("Java2D  -  composite text on texture", "1000"),
                    arrayOf<Any>(
                        TpE(TpE.RI, myBlack, gp, 40, 0, 20),
                        TpE(TpE.RD, myBlack, gp, 40, 21, 40),
                        TpE(TpE.RI, myBlack, gp, 40, 41, 60),
                        TxE("Java2D", f2, TxE.AC, myYellow, 0, 60)
                                )
                       ),
                arrayOf(arrayOf<Any>("Previous scene dither dissolve out", "0"), arrayOf<Any>(DdE(0, 20, 4))),
                arrayOf(
                    arrayOf<Any>("Imaging Features", "999"),
                    arrayOf<Any>(
                        Temp(Temp.RECT, null, 0, 15),
                        Temp(Temp.IMG, java_logo, 2, 15),
                        Temp(Temp.RNA or Temp.INA, java_logo, 16, 130),
                        Features(Features.IMAGES, 16, 130)
                                )
                       ),
                arrayOf(
                    arrayOf<Any>("Java2D  -  text on gradient", "1000"),
                    arrayOf<Any>(
                        GpE(GpE.SDH, myBlue, myBlack, 0, 20),
                        GpE(GpE.SIH, myBlue, myBlack, 21, 40),
                        GpE(GpE.SDH, myBlue, myBlack, 41, 50),
                        GpE(GpE.INC or GpE.NF, myRed, myYellow, 0, 50),
                        TxE("Java2D", f2, TxE.NOP, null, 0, 50)
                                )
                       ),
                arrayOf(arrayOf<Any>("Previous scene ellipse close out", "0"), arrayOf<Any>(CoE(CoE.OVAL, 0, 20))),
                arrayOf(
                    arrayOf<Any>("Color Features", "999"),
                    arrayOf<Any>(
                        Temp(Temp.RECT, null, 0, 15),
                        Temp(Temp.IMG, java_logo, 2, 15),
                        Temp(Temp.RNA or Temp.INA, java_logo, 16, 99),
                        Features(Features.COLOR, 16, 99)
                                )
                       ),
                arrayOf(
                    arrayOf<Any>("Java2D  -  composite and rotate text on paints", "2000"),
                    arrayOf<Any>(
                        GpE(GpE.BURI, myBlack, myBlue, 0, 20),
                        GpE(GpE.BURD, myBlack, myBlue, 21, 30),
                        TpE(TpE.OI or TpE.HAF, myBlack, myBlue, 10, 31, 40),
                        TxE("Java2D", f2, TxE.AC or TxE.RI, myYellow, 0, 40)
                                )
                       ),
                arrayOf(arrayOf<Any>("Previous scene subimage transform out", "0"), arrayOf<Any>(SiE(60, 60, 0, 40))),
                arrayOf(
                    arrayOf<Any>("CREDITS  -  transform in", "1000"),
                    arrayOf<Any>(
                        LnE(LnE.ACI or LnE.ZOOMI or LnE.RI, 0, 60),
                        TxE("CREDITS", f3, TxE.AC or TxE.SCI, RED, 20, 30),
                        TxE("CREDITS", f3, TxE.SCXD, RED, 31, 38),
                        TxE("CREDITS", f3, TxE.SCXI, RED, 39, 48),
                        TxE("CREDITS", f3, TxE.SCXD, RED, 49, 54),
                        TxE("CREDITS", f3, TxE.SCXI, RED, 55, 60)
                                )
                       ),
                arrayOf(
                    arrayOf<Any>("CREDITS  -  transform out", "0"),
                    arrayOf<Any>(
                        LnE(LnE.ACD or LnE.ZOOMD or LnE.RD, 0, 45),
                        TxE("CREDITS", f3, 0, RED, 0, 9),
                        TxE("CREDITS", f3, TxE.SCD or TxE.RD, RED, 10, 30)
                                )
                       ),
                arrayOf(
                    arrayOf<Any>("Contributors", "1000"),
                    arrayOf<Any>(
                        Temp(Temp.RECT, null, 0, 30),
                        Temp(Temp.IMG, cupanim, 4, 30),
                        Temp(Temp.RNA or Temp.INA, cupanim, 31, 200),
                        Contributors(34, 200)
                                )
                       )
                                   )

            init {
                for (partInfo in partsInfo) {
                    val parts = ArrayList<Part>()
                    for (part in partInfo[1]) {
                        parts.add(part as Part)
                    }
                    add(Scene(parts, partInfo[0][0], partInfo[0][1]))
                }
            }
        }

        /**
         * Scene is the manager of the parts.
         */
        internal class Scene(var parts: List<Part>, var name: Any?, var pauseAmt: Any?) : Any() {
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

            fun setIncrements(numRevolutions: Double) {
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

            override fun reset(w: Int, h: Int) {
                if (type == SCXI) {
                    sx = -1.0
                    sy = 1.0
                } else if (type == SCYI) {
                    sx = 1.0
                    sy = -1.0
                } else {
                    sy = if (type and DEC != 0) 1.0 else 0.0
                    sx = sy
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
                    if (type and SCX != 0) {
                        sx += sIncr
                    } else if (type and SCY != 0) {
                        sy += sIncr
                    } else {
                        sx += sIncr
                        sy += sIncr
                    }
                }
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                var saveAC: Composite? = null
                if (type and AC != 0 && sx > 0 && sx < 1) {
                    saveAC = g2.composite
                    g2.composite = AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, sx.toFloat()
                                                             )
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

            companion object {
                val INC = 1
                val DEC = 2
                val R = 4            // rotate
                val RI = R or INC
                val RD = R or DEC
                val SC = 8            // scale
                val SCI = SC or INC
                val SCD = SC or DEC
                val SCX = 16           // scale invert x
                val SCXI = SCX or SC or INC
                val SCXD = SCX or SC or DEC
                val SCY = 32           // scale invert y
                val SCYI = SCY or SC or INC
                val SCYD = SCY or SC or DEC
                val AC = 64           // AlphaComposite
                val CLIP = 128          // Clipping
                val NOP = 512          // No Paint
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
                          ) : Part {
            private var incr: Float = 0.toFloat()
            private var index: Float = 0.toFloat()
            private val rect = ArrayList<Rectangle2D>()
            private val grad = ArrayList<GradientPaint>()

            override fun reset(w: Int, h: Int) {
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

                if (type and WID != 0) {
                    var w2 = 0f
                    var x1 = 0f
                    var x2 = 0f
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
                } else if (type and HEI != 0) {
                    var h2 = 0f
                    var y1 = 0f
                    var y2 = 0f
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
                } else if (type and BUR != 0) {

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
                } else if (type and NF != 0) {
                    val y = h * index
                    grad.add(GradientPaint(0f, 0f, c1, 0f, y, c2))
                }

                if (type and INC != 0 || type and DEC != 0) {
                    index += incr
                }
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF
                                   )
                for (i in grad.indices) {
                    g2.paint = grad[i]
                    if (type and NF == 0) {
                        g2.fill(rect[i])
                    }
                }
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                                   )
            }

            companion object {

                val INC = 1               // increasing
                val DEC = 2               // decreasing
                val CNT = 4               // center
                val WID = 8               // width
                val WI = WID or INC
                val WD = WID or DEC
                val HEI = 16              // height
                val HI = HEI or INC
                val HD = HEI or DEC
                val SPL = 32 or CNT        // split
                val SIW = SPL or INC or WID
                val SDW = SPL or DEC or WID
                val SIH = SPL or INC or HEI
                val SDH = SPL or DEC or HEI
                val BUR = 64 or CNT        // burst
                val BURI = BUR or INC
                val BURD = BUR or DEC
                val NF = 128             // no fill
            }
        } // End GpE class

        /**
         * TexturePaint Effect.  Expand and collapse a texture.
         */
        internal class TpE(
            private val type: Int, private val p1: Paint, private val p2: Paint, size: Int,
            override val begin: Int, override val end: Int
                          ) : Part {
            private var incr: Float = 0.toFloat()
            private var index: Float = 0.toFloat()
            private var texture: TexturePaint? = null
            private var size: Int = 0
            private var bimg: BufferedImage? = null
            private var rect: Rectangle? = null

            init {
                setTextureSize(size)
            }

            fun setTextureSize(size: Int) {
                this.size = size
                bimg = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
                rect = Rectangle(0, 0, size, size)
            }

            override fun reset(w: Int, h: Int) {
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

            companion object {

                val INC = 1             // increasing
                val DEC = 2             // decreasing
                val OVAL = 4             // oval
                val RECT = 8             // rectangle
                val HAF = 16             // half oval or rect size
                val NF = 32             // no fill
                val OI = OVAL or INC
                val OD = OVAL or DEC
                val RI = RECT or INC
                val RD = RECT or DEC
            }
        } // End TpE class

        /**
         * Close out effect.  Close out the buffered image with different
         * geometry shapes.
         */
        internal class CoE(private var type: Int, override val begin: Int, override val end: Int) : Part {
            private var bimg: BufferedImage? = null
            private var shape: Shape? = null
            private var zoom: Double = 0.toDouble()
            private var extent: Double = 0.toDouble()
            private val zIncr: Double
            private val eIncr: Double
            private val doRandom: Boolean

            init {
                zIncr = -(2.0 / (this.end - begin))
                eIncr = 360.0 / (this.end - begin)
                doRandom = type and RAND != 0
            }

            override fun reset(w: Int, h: Int) {
                if (doRandom) {
                    val num = (Math.random() * 5.0).toInt()
                    when (num) {
                        0 -> type = OVAL
                        1 -> type = RECT
                        2 -> type = RECT or WID
                        3 -> type = RECT or HEI
                        4 -> type = ARC
                        else -> type = OVAL
                    }
                }
                shape = null
                bimg = null
                extent = 360.0
                zoom = 2.0
            }

            override fun step(w: Int, h: Int) {
                if (bimg == null) {
                    val biw = Surface.bimg!!.width
                    val bih = Surface.bimg!!.height
                    bimg = BufferedImage(
                        biw, bih,
                        BufferedImage.TYPE_INT_RGB
                                        )
                    val big = bimg!!.createGraphics()
                    big.drawImage(Surface.bimg, 0, 0, null)
                }
                val z = Math.min(w, h) * zoom
                if (type and OVAL != 0) {
                    shape = Ellipse2D.Double(
                        w / 2 - z / 2, h / 2 - z / 2, z,
                        z
                                            )
                } else if (type and ARC != 0) {
                    shape = Arc2D.Double(
                        -100.0, -100.0, (w + 200).toDouble(), (h + 200).toDouble(), 90.0,
                        extent, Arc2D.PIE
                                        )
                    extent -= eIncr
                } else if (type and RECT != 0) {
                    if (type and WID != 0) {
                        shape = Rectangle2D.Double(w / 2 - z / 2, 0.0, z, h.toDouble())
                    } else if (type and HEI != 0) {
                        shape = Rectangle2D.Double(0.0, h / 2 - z / 2, w.toDouble(), z)
                    } else {
                        shape = Rectangle2D.Double(w / 2 - z / 2, h / 2 - z / 2, z, z)
                    }
                }
                zoom += zIncr
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                g2.clip(shape)
                g2.drawImage(bimg, 0, 0, null)
            }

            companion object {

                val WID = 1
                val HEI = 2
                val OVAL = 4
                val RECT = 8
                val RAND = 16
                val ARC = 32
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
        internal class DdE(override val begin: Int, override val end: Int, private val blocksize: Int) : Part {
            private var bimg: BufferedImage? = null
            private var big: Graphics2D? = null
            private var list: MutableList<Int>? = null
            private var xlist: MutableList<Int>? = null
            private var ylist: MutableList<Int>? = null
            private var xeNum: Int = 0
            private var yeNum: Int = 0    // element number
            private var xcSize: Int = 0
            private var ycSize: Int = 0  // chunk size
            private var inc: Int = 0

            private fun createShuffledLists() {
                val width = bimg!!.width
                val height = bimg!!.height
                xlist = ArrayList(width)
                ylist = ArrayList(height)
                list = ArrayList(end - begin + 1)
                for (i in 0 until width) {
                    xlist!!.add(i, i)
                }
                for (i in 0 until height) {
                    ylist!!.add(i, i)
                }
                for (i in 0 until end - begin + 1) {
                    list!!.add(i, i)
                }
                java.util.Collections.shuffle(xlist!!)
                java.util.Collections.shuffle(ylist!!)
                java.util.Collections.shuffle(list!!)
            }

            override fun reset(w: Int, h: Int) {
                bimg = null
            }

            override fun step(w: Int, h: Int) {
                if (inc > end) {
                    bimg = null
                }
                if (bimg == null) {
                    val biw = Surface.bimg!!.width
                    val bih = Surface.bimg!!.height
                    bimg = BufferedImage(
                        biw, bih,
                        BufferedImage.TYPE_INT_RGB
                                        )
                    createShuffledLists()
                    big = bimg!!.createGraphics()
                    big!!.drawImage(Surface.bimg, 0, 0, null)
                    xcSize = xlist!!.size / (end - begin) + 1
                    ycSize = ylist!!.size / (end - begin) + 1
                    xeNum = 0
                    inc = 0
                }
                xeNum = xcSize * list!![inc]
                yeNum = -ycSize
                inc++
            }

            override fun render(w: Int, h: Int, g2: Graphics2D) {
                big!!.color = myBlack

                for (k in 0..end - begin) {
                    if (xeNum + xcSize > xlist!!.size) {
                        xeNum = 0
                    } else {
                        xeNum += xcSize
                    }
                    yeNum += ycSize

                    var i = xeNum
                    while (i < xeNum + xcSize && i < xlist!!.size) {
                        var j = yeNum
                        while (j < yeNum + ycSize && j < ylist!!.size) {
                            val xval = xlist!![i]
                            val yval = ylist!![j]
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
        internal class SiE(private val siw: Int, private val sih: Int, override val begin: Int, override val end: Int) :
            Part {
            private var bimg: BufferedImage? = null
            private val rIncr: Double
            private val sIncr: Double
            private var scale: Double = 0.toDouble()
            private var rotate: Double = 0.toDouble()
            private val subs = ArrayList<BufferedImage>(20)
            private val pts = ArrayList<Point>(20)

            init {
                rIncr = 360.0 / (this.end - begin)
                sIncr = 1.0 / (this.end - begin)
            }

            override fun reset(w: Int, h: Int) {
                scale = 1.0
                rotate = 0.0
                bimg = null
                subs.clear()
                pts.clear()
            }

            override fun step(w: Int, h: Int) {
                if (bimg == null) {
                    val biw = Surface.bimg!!.width
                    val bih = Surface.bimg!!.height
                    bimg = BufferedImage(
                        biw, bih,
                        BufferedImage.TYPE_INT_RGB
                                        )
                    val big = bimg!!.createGraphics()
                    big.drawImage(Surface.bimg, 0, 0, null)
                    run {
                        var x = 0
                        while (x < w && scale > 0.0) {
                            val ww = if (x + siw < w) siw else w - x
                            run {
                                var y = 0
                                while (y < h) {
                                    val hh = if (y + sih < h) sih else h - y
                                    subs.add(bimg!!.getSubimage(x, y, ww, hh))
                                    pts.add(Point(x, y))
                                    y += sih
                                }
                            }
                            x += siw
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
        internal class LnE(private val type: Int, override val begin: Int, override val end: Int) : Part {
            private var rIncr: Double = 0.toDouble()
            private var rotate: Double = 0.toDouble()
            private var zIncr: Double = 0.toDouble()
            private var zoom: Double = 0.toDouble()
            private val pts = ArrayList<Point2D.Double>()
            private var alpha: Float = 0.toFloat()
            private var aIncr: Float = 0.toFloat()

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

            fun generatePts(w: Int, h: Int, sizeF: Double) {
                pts.clear()
                val size = Math.min(w, h) * sizeF
                val ellipse = Ellipse2D.Double(w / 2 - size / 2, h / 2 - size / 2, size, size)
                val pi = ellipse.getPathIterator(null, 0.8)
                while (!pi.isDone) {
                    val pt = DoubleArray(6)
                    when (pi.currentSegment(pt)) {
                        FlatteningPathIterator.SEG_MOVETO, FlatteningPathIterator.SEG_LINETO -> pts.add(
                            Point2D.Double(
                                pt[0],
                                pt[1]
                                          )
                                                                                                       )
                    }
                    pi.next()
                }
            }

            override fun reset(w: Int, h: Int) {
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
                    generatePts(w, h, 0.5)
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

            companion object {

                val INC = 1
                val DEC = 2
                val R = 4             // rotate
                val ZOOM = 8             // zoom
                val AC = 32             // AlphaComposite
                val RI = R or INC
                val RD = R or DEC
                val ZOOMI = ZOOM or INC
                val ZOOMD = ZOOM or DEC
                val ACI = AC or INC
                val ACD = AC or DEC
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
            private val aIncr: Float
            private var rect1: Rectangle? = null
            private var rect2: Rectangle? = null
            private var x: Int = 0
            private var y: Int = 0
            private var xIncr: Int = 0
            private var yIncr: Int = 0

            init {
                aIncr = 0.9f / (this.end - begin)
                if (type and NOANIM != 0) {
                    alpha = 1.0f
                }
            }

            override fun reset(w: Int, h: Int) {
                rect1 = Rectangle(8, 20, w - 20, 30)
                rect2 = Rectangle(20, 8, 30, h - 20)
                if (type and NOANIM == 0) {
                    alpha = 0.0f
                    xIncr = w / (end - begin)
                    yIncr = h / (end - begin)
                    x = w + (xIncr * 1.4).toInt()
                    y = h + (yIncr * 1.4).toInt()
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
                    if (alpha >= 0 && alpha <= 1) {
                        g2.composite = AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER, alpha
                                                                 )
                    }
                    g2.drawImage(img, 30, 30, null)
                    g2.composite = saveAC
                }
            }

            companion object {

                val NOANIM = 1
                val RECT = 2
                val IMG = 4
                val RNA = RECT or NOANIM
                val INA = IMG or NOANIM
            }
        } // End Temp class

        /**
         * Features of Java2D.  Single character advancement effect.
         */
        internal class Features(type: Int, override val begin: Int, override val end: Int) : Part {
            private val list: Array<String>
            private var strH: Int = 0
            private var endIndex: Int = 0
            private var listIndex: Int = 0
            private val v = ArrayList<String>()

            init {
                list = table[type]
            }

            override fun reset(w: Int, h: Int) {
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

            companion object {

                val GRAPHICS = 0
                val TEXT = 1
                val IMAGES = 2
                val COLOR = 3
                val font1 = Font("serif", Font.BOLD, 38)
                val font2 = Font("serif", Font.PLAIN, 24)
                var fm1 = Surface.getMetrics(font1)
                var fm2 = Surface.getMetrics(font2)
                var table = arrayOf(
                    arrayOf(
                        "Graphics",
                        "Antialiased rendering",
                        "Bezier paths",
                        "Transforms",
                        "Compositing",
                        "Stroking parameters"
                           ),
                    arrayOf(
                        "Text",
                        "Extended font support",
                        "Advanced text layout",
                        "Dynamic font loading",
                        "AttributeSets for font customization"
                           ),
                    arrayOf(
                        "Images",
                        "Flexible image layouts",
                        "Extended imaging operations",
                        "   Convolutions, Lookup Tables",
                        "RenderableImage interface"
                           ),
                    arrayOf("Color", "ICC profile support", "Color conversion", "Arbitrary color spaces")
                                   )
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
                java.util.Arrays.sort(members)
                cast.add("CONTRIBUTORS")
                cast.add(" ")
                cast.addAll(Arrays.asList(*members))
                cast.add(" ")
                cast.add(" ")
                cntMod = (this.end - begin) / cast.size - 1
            }

            override fun reset(w: Int, h: Int) {
                v.clear()
                strH = fm.ascent + fm.descent
                nStrs = (h - 40) / strH + 1
                height = strH * (nStrs - 1) + 48
                index = 0
                gp = GradientPaint(0f, (h / 2).toFloat(), WHITE, 0f, (h + 20).toFloat(), BLACK)
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
                    y = yh + y
                } else {
                    yh = height - v.size * strH + y
                    y = yh
                }
                for (s in v) {
                    y += strH
                    g2.drawString(s, w / 2 - fm.stringWidth(s) / 2, y)
                }
            }

            companion object {

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
                var fm = Surface.getMetrics(font)
            }
        } // End Contributors class

        companion object {
            lateinit var surf: Surface
            lateinit var cupanim: Image
            lateinit var java_logo: Image
            var bimg: BufferedImage? = null

            fun getMetrics(font: Font): FontMetrics {
                return surf.getFontMetrics(font)
            }
        }
    } // End Surface class

    companion object {

        private val myBlack = Color(20, 20, 20)
        private val myWhite = Color(240, 240, 255)
        private val myRed = Color(149, 43, 42)
        private val myBlue = Color(94, 105, 176)
        private val myYellow = Color(255, 255, 140)
        internal lateinit var surface: Surface

        @JvmStatic
        fun main(argv: Array<String>) {
            val intro = Intro()
            val l = object : WindowAdapter() {

                override fun windowClosing(e: WindowEvent?) {
                    System.exit(0)
                }

                override fun windowDeiconified(e: WindowEvent?) {
                    intro.start()
                }

                override fun windowIconified(e: WindowEvent?) {
                    intro.stop()
                }
            }
            val f = JFrame("Java2D Demo - Intro")
            f.addWindowListener(l)
            f.contentPane.add("Center", intro)
            f.pack()
            val screenSize = Toolkit.getDefaultToolkit().screenSize
            val w = 720
            val h = 510
            f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2)
            f.setSize(w, h)
            f.isVisible = true
            intro.start()
        }
    }
} // End Intro class

