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
import java.awt.Color.BLACK
import java.awt.Color.GREEN
import java.awt.Color.YELLOW
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.geom.Line2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.util.Date
import javax.swing.JCheckBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.border.EtchedBorder
import javax.swing.border.TitledBorder

/**
 * Tracks Memory allocated & used, displayed in graph form.
 */
class MemoryMonitor : JPanel()
{
    var surface: Surface
    internal var controls: JPanel
    internal var doControls: Boolean = false
    internal var tf: JTextField

    init {
        layout = BorderLayout()
        border = TitledBorder(EtchedBorder(), "Memory Monitor")
        surface = Surface()
        add(surface)
        controls = JPanel()
        controls.preferredSize = Dimension(135, 80)
        val font = Font("serif", Font.PLAIN, 10)
        var label = JLabel("Sample Rate")
        label.font = font
        label.foreground = BLACK
        controls.add(label)
        tf = JTextField("1000")
        tf.preferredSize = Dimension(45, 20)
        controls.add(tf)
        label = JLabel("ms")
        controls.add(label)
        label.font = font
        label.foreground = BLACK
        controls.add(dateStampCB)
        dateStampCB.font = font
        addMouseListener(object : MouseAdapter() {

            override fun mouseClicked(e: MouseEvent?) {
                removeAll()
                doControls = !doControls
                if (doControls) {
                    surface.stop()
                    add(controls)
                } else {
                    try {
                        surface.sleepAmount = java.lang.Long.parseLong(tf.text.trim { it <= ' ' })
                    } catch (ex: Exception) {
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
        private var big: Graphics2D? = null
        private val r = Runtime.getRuntime()
        private var columnInc: Int = 0
        private var pts: IntArray? = null
        private var ptNum: Int = 0
        private var ascent: Int = 0
        private var descent: Int = 0
        private val graphOutlineRect = Rectangle()
        private val mfRect = Rectangle2D.Float()
        private val muRect = Rectangle2D.Float()
        private val graphLine = Line2D.Float()
        private val graphColor = Color(46, 139, 87)
        private val mfColor = Color(0, 100, 0)
        private var usedStr: String? = null

        init {
            background = BLACK
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

        override fun getMinimumSize(): Dimension {
            return preferredSize
        }

        override fun getMaximumSize(): Dimension {
            return preferredSize
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(135, 80)
        }

        override fun paint(g: Graphics?) {

            if (big == null) {
                return
            }

            big!!.background = background
            big!!.clearRect(0, 0, w, h)

            val freeMemory = r.freeMemory().toFloat()
            val totalMemory = r.totalMemory().toFloat()

            // .. Draw allocated and used strings ..
            big!!.color = GREEN
            big!!.drawString((totalMemory.toInt() / 1024).toString() + "K allocated", 4.0f, ascent + 0.5f)
            usedStr = ((totalMemory - freeMemory).toInt() / 1024).toString() + "K used"
            big!!.drawString(usedStr, 4, h - descent)

            // Calculate remaining size
            val ssH = (ascent + descent).toFloat()
            val remainingHeight = h.toFloat() - ssH * 2 - 0.5f
            val blockHeight = remainingHeight / 10
            val blockWidth = 20.0f

            // .. Memory Free ..
            big!!.color = mfColor
            val memUsage = (freeMemory / totalMemory * 10).toInt()
            var i = 0
            while (i < memUsage) {
                mfRect.setRect(
                    5.0, (ssH + i * blockHeight).toDouble(),
                    blockWidth.toDouble(), (blockHeight - 1).toDouble())
                big!!.fill(mfRect)
                i++
            }

            // .. Memory Used ..
            big!!.color = GREEN
            while (i < 10) {
                muRect.setRect(
                    5.0, (ssH + i * blockHeight).toDouble(),
                    blockWidth.toDouble(), (blockHeight - 1).toDouble())
                big!!.fill(muRect)
                i++
            }

            // .. Draw History Graph ..
            big!!.color = graphColor
            val graphX = 30
            val graphY = ssH.toInt()
            val graphW = w - graphX - 5
            val graphH = remainingHeight.toInt()
            graphOutlineRect.setRect(graphX.toDouble(), graphY.toDouble(), graphW.toDouble(), graphH.toDouble())
            big!!.draw(graphOutlineRect)

            val graphRow = graphH / 10

            // .. Draw row ..
            run {
                var j = graphY
                while (j <= graphH + graphY) {
                    graphLine.setLine(graphX.toDouble(), j.toDouble(), (graphX + graphW).toDouble(), j.toDouble())
                    big!!.draw(graphLine)
                    j += graphRow
                }
            }

            // .. Draw animated column movement ..
            val graphColumn = graphW / 15

            if (columnInc == 0) {
                columnInc = graphColumn
            }

            run {
                var j = graphX + columnInc
                while (j < graphW + graphX) {
                    graphLine.setLine(j.toDouble(), graphY.toDouble(), j.toDouble(), (graphY + graphH).toDouble())
                    big!!.draw(graphLine)
                    j += graphColumn
                }
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
                big!!.color = YELLOW
                pts!![ptNum] = (graphY + graphH * (freeMemory / totalMemory)).toInt()
                run {
                    var j = graphX + graphW - ptNum
                    var k = 0
                    while (k < ptNum) {
                        if (k != 0) {
                            if (pts!![k] != pts!![k - 1]) {
                                big!!.drawLine(j - 1, pts!![k - 1], j, pts!![k])
                            } else {
                                big!!.fillRect(j, pts!![k], 1, 1)
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
            g!!.drawImage(bimg, 0, 0, this)
        }

        fun start() {
            thread = Thread(this)
            thread!!.priority = Thread.MIN_PRIORITY
            thread!!.name = "MemoryMonitor"
            thread!!.start()
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
                    big = bimg!!.createGraphics()
                    big!!.font = FONT
                    val fm = big!!.getFontMetrics(FONT)
                    ascent = fm.ascent
                    descent = fm.descent
                }
                repaint()
                try {
                    Thread.sleep(sleepAmount)
                } catch (e: InterruptedException) {
                    break
                }

                if (MemoryMonitor.dateStampCB.isSelected) {
                    println(Date().toString() + " " + usedStr)
                }
            }
            thread = null
        }
    }

    companion object
    {
        internal var dateStampCB = JCheckBox("Output Date Stamp")

        private val FONT = Font("Times New Roman", Font.PLAIN, 11)

        @JvmStatic
        fun main(s: Array<String>) {
            val demo = MemoryMonitor()
            val l = object : WindowAdapter() {

                override fun windowClosing(e: WindowEvent?) {
                    System.exit(0)
                }

                override fun windowDeiconified(e: WindowEvent?) {
                    demo.surface.start()
                }

                override fun windowIconified(e: WindowEvent?) {
                    demo.surface.stop()
                }
            }
            val f = JFrame("Java2D Demo - MemoryMonitor")
            f.addWindowListener(l)
            f.contentPane.add("Center", demo)
            f.pack()
            f.size = Dimension(200, 200)
            f.isVisible = true
            demo.surface.start()
        }
    }
}
