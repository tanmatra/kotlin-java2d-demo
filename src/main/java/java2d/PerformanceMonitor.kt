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
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.JPanel
import javax.swing.border.EtchedBorder
import javax.swing.border.TitledBorder

/**
 * Displays the time for a Surface to paint. Displays the number
 * of frames per second on animated demos.  Up to four surfaces fit
 * in the display area.
 */
class PerformanceMonitor : JPanel()
{
    internal var surface: Surface

    init {
        layout = BorderLayout()
        border = TitledBorder(EtchedBorder(), "Performance")
        surface = Surface()
        add(surface)
    }

    inner class Surface : JPanel(), Runnable
    {
        var thread: Thread? = null
        private var bimg: BufferedImage? = null
        private val FONT = Font("Times New Roman", Font.PLAIN, 12)
        private var panel: JPanel? = null

        init {
            background = Color.black
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
            val textH = getFontMetrics(FONT).height
            return Dimension(135, 2 + textH * 4)
        }

        override fun paint(g: Graphics?) {
            if (bimg != null) {
                g!!.drawImage(bimg, 0, 0, this)
            }
        }

        fun start() {
            thread = Thread(this)
            thread!!.priority = Thread.MIN_PRIORITY
            thread!!.name = "PerformanceMonitor"
            thread!!.start()
        }

        @Synchronized
        fun stop() {
            thread = null
            setSurfaceState()
            @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            (this as java.lang.Object).notify()
        }

        fun setSurfaceState() {
            if (panel != null) {
                for (comp in panel!!.components) {
                    if ((comp as DemoPanel).surface != null) {
                        comp.surface.monitor = thread != null
                    }
                }
            }
        }

        fun setPanel(panel: JPanel) {
            this.panel = panel
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

            var d = Dimension(0, 0)
            var big: Graphics2D? = null
            var fm: FontMetrics? = null
            var ascent = 0
            var descent = 0

            while (thread === me && isShowing) {

                if (width != d.width || height != d.height) {
                    d = size
                    bimg = createImage(d.width, d.height) as BufferedImage
                    big = bimg!!.createGraphics()
                    big!!.font = FONT
                    fm = big.fontMetrics
                    ascent = fm!!.ascent
                    descent = fm.descent
                    setSurfaceState()
                }

                big!!.background = background
                big.clearRect(0, 0, d.width, d.height)
                if (panel == null) {
                    continue
                }
                big.color = Color.green
                var ssH = 1
                for (comp in panel!!.components) {
                    if ((comp as DemoPanel).surface != null) {
                        val pStr = comp.surface.perfStr
                        if (pStr != null) {
                            ssH += ascent
                            big.drawString(pStr, 4, ssH + 1)
                            ssH += descent
                        }
                    }
                }
                repaint()

                try {
                    Thread.sleep(999)
                } catch (e: InterruptedException) {
                    break
                }
            }
            thread = null
        }
    }
}
