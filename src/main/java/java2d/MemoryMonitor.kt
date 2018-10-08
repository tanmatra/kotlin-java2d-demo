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

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Color.GREEN
import java.awt.Color.YELLOW
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagLayout
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.geom.Line2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.util.Date
import javax.swing.BorderFactory
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.border.TitledBorder

/**
 * Tracks Memory allocated & used, displayed in graph form.
 */
class MemoryMonitor : JPanel(BorderLayout())
{
    val surface = Surface()
    private val controls: JPanel
    private var doControls: Boolean = false
    private val textField: JTextField
    private val dateStampCheckBox = JCheckBox("Output Date Stamp")

    init {
        border = TitledBorder(BorderFactory.createEtchedBorder(), "Memory Monitor")
        add(surface)
        controls = JPanel(GridBagLayout()).apply {
            preferredSize = PREFERRED_SIZE
        }
        controls.add(JLabel("Sample Rate"), GBC(0, 0))
        textField = JTextField("1000").apply {
            preferredSize = Dimension(45, preferredSize.height)
        }
        controls.add(textField, GBC(1, 0).insets(0, 2, 0, 2))
        controls.add(JLabel("ms"), GBC(2, 0))
        controls.add(dateStampCheckBox, GBC(0, 1).span(3, 1))

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                removeAll()
                doControls = !doControls
                if (doControls) {
                    surface.stop()
                    add(controls)
                } else {
                    try {
                        surface.sleepAmount = textField.text.trim().toLong()
                    } catch (ex: Exception) {
                        // ignore
                    }
                    surface.start()
                    add(surface)
                }
                revalidate()
                repaint()
            }
        })
    }

    inner class Surface : JPanel(), Runnable
    {
        var thread: Thread? = null
        var sleepAmount: Long = 1000
        private var w: Int = 0
        private var h: Int = 0
        private var bimg: BufferedImage? = null
        private var imgGr: Graphics2D? = null
        private val runtime = Runtime.getRuntime()
        private var columnInc: Int = 0
        private var pts: IntArray? = null
        private var ptNum: Int = 0
        private var ascent: Int = 0
        private var descent: Int = 0
        private val graphOutlineRect = Rectangle()
        private val mfRect = Rectangle2D.Float()
        private val muRect = Rectangle2D.Float()
        private val graphLine = Line2D.Float()
        private var usedStr: String? = null

        init {
            background = Color.BLACK
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (thread == null) {
                        start()
                    } else {
                        stop()
                    }
                }
            })
        }

        override fun getMinimumSize(): Dimension = PREFERRED_SIZE

        override fun getMaximumSize(): Dimension = PREFERRED_SIZE

        override fun getPreferredSize(): Dimension = PREFERRED_SIZE

        override fun paint(g: Graphics) {
            val imgGr = imgGr ?: return

            imgGr.textAntialiasing = systemTextAntialiasing
            imgGr.background = background
            imgGr.clearRect(0, 0, w, h)

            val freeMemory = runtime.freeMemory().toFloat()
            val totalMemory = runtime.totalMemory().toFloat()

            // .. Draw allocated and used strings ..
            imgGr.color = GREEN
            imgGr.drawString("%,d K allocated".format(totalMemory.toInt() / 1024), 4.0f, ascent + 0.5f)
            usedStr = "%,d K used".format((totalMemory - freeMemory).toInt() / 1024)
            imgGr.drawString(usedStr, 4, h - descent)

            // Calculate remaining size
            val textHeight = (ascent + descent).toFloat()
            val remainingHeight = h.toFloat() - textHeight * 2 - 0.5f
            val blockHeight = remainingHeight / 10
            val visibleBlockHeight = (blockHeight - 1).toDouble()
            val blockWidth = 20.0
            fun blockY(i: Int) = (textHeight + i * blockHeight).toDouble()

            // .. Memory Free ..
            imgGr.color = FREE_MEMORY_COLOR
            val memUsage = (freeMemory / totalMemory * 10).toInt()
            for (i in 0 until memUsage) {
                mfRect.setRect(5.0, blockY(i), blockWidth, visibleBlockHeight)
                imgGr.fill(mfRect)
            }

            // .. Memory Used ..
            imgGr.color = GREEN
            for (i in memUsage until 10) {
                muRect.setRect(5.0, blockY(i), blockWidth, visibleBlockHeight)
                imgGr.fill(muRect)
            }

            // .. Draw History Graph ..
            imgGr.color = GRAPH_COLOR
            val graphX = 30
            val graphY = textHeight.toInt()
            val graphW = w - graphX - 5
            val graphH = remainingHeight.toInt()
            graphOutlineRect.setRect(graphX.toDouble(), graphY.toDouble(), graphW.toDouble(), graphH.toDouble())
            imgGr.draw(graphOutlineRect)

            val graphRow = graphH / 10

            // .. Draw row ..
            for (j in graphY .. graphH + graphY step graphRow) {
                graphLine.setLine(graphX.toDouble(), j.toDouble(), (graphX + graphW).toDouble(), j.toDouble())
                imgGr.draw(graphLine)
            }

            // .. Draw animated column movement ..
            val graphColumn = graphW / 15

            if (columnInc == 0) {
                columnInc = graphColumn
            }

            for (j in graphX + columnInc until graphW + graphX step graphColumn) {
                graphLine.setLine(j.toDouble(), graphY.toDouble(), j.toDouble(), (graphY + graphH).toDouble())
                imgGr.draw(graphLine)
            }

            --columnInc

            if (pts == null) {
                pts = IntArray(graphW)
                ptNum = 0
            } else if (pts!!.size != graphW) {
                val tmp: IntArray
                if (ptNum < graphW) {
                    tmp = IntArray(ptNum)
                    System.arraycopy(pts!!, 0, tmp, 0, tmp.size)
                } else {
                    tmp = IntArray(graphW)
                    System.arraycopy(pts!!, pts!!.size - tmp.size, tmp, 0, tmp.size)
                    ptNum = tmp.size - 2
                }
                pts = IntArray(graphW)
                System.arraycopy(tmp, 0, pts!!, 0, tmp.size)
            } else {
                imgGr.color = YELLOW
                pts!![ptNum] = (graphY + graphH * (freeMemory / totalMemory)).toInt()
                run {
                    var j = graphX + graphW - ptNum
                    var k = 0
                    while (k < ptNum) {
                        if (k != 0) {
                            if (pts!![k] != pts!![k - 1]) {
                                imgGr.drawLine(j - 1, pts!![k - 1], j, pts!![k])
                            } else {
                                imgGr.fillRect(j, pts!![k], 1, 1)
                            }
                        }
                        k++
                        j++
                    }
                }
                if (ptNum + 2 == pts!!.size) {
                    // throw out oldest point
                    for (j in 1 until ptNum) {
                        pts!![j - 1] = pts!![j]
                    }
                    --ptNum
                } else {
                    ptNum++
                }
            }
            g.drawImage(bimg, 0, 0, this)
        }

        fun start() {
            thread = Thread(this, "MemoryMonitor").apply {
                priority = Thread.MIN_PRIORITY
                start()
            }
        }

        @Synchronized
        fun stop() {
            thread = null
            @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            (this as Object).notify()
        }

        override fun run() {
            val me = Thread.currentThread()

            while (thread === me && !isShowing || size.width == 0) {
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    return
                }
            }

            while (thread === me && isShowing) {
                val d = size
                if (d.width != w || d.height != h) {
                    w = d.width
                    h = d.height
                    bimg = createImage(w, h) as BufferedImage
                    imgGr = bimg!!.createGraphics().apply {
                        font = FONT
                    }
                    val fm = imgGr!!.getFontMetrics(FONT)
                    ascent = fm.ascent
                    descent = fm.descent
                }
                repaint()
                try {
                    Thread.sleep(sleepAmount)
                } catch (e: InterruptedException) {
                    break
                }

                if (dateStampCheckBox.isSelected) {
                    println("${Date()}: $usedStr")
                }
            }
            thread = null
        }
    }

    companion object
    {
        private val FONT = Font(Font.DIALOG, Font.PLAIN, 11)
        private val PREFERRED_SIZE = Dimension(150, 80)
        private val GRAPH_COLOR = Color(46, 139, 87)
        private val FREE_MEMORY_COLOR = Color(0, 100, 0)

        @JvmStatic
        fun main(s: Array<String>) {
            runInFrame("Java2D Demo - MemoryMonitor") {
                val demo = MemoryMonitor()
                addWindowListener(object : WindowAdapter() {
                    override fun windowDeiconified(e: WindowEvent?) {
                        demo.surface.start()
                    }
                    override fun windowIconified(e: WindowEvent?) {
                        demo.surface.stop()
                    }
                })
                contentPane.add(demo, BorderLayout.CENTER)
                pack()
                demo.surface.start()
            }
        }
    }
}
