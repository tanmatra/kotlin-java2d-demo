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
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.JPanel
import javax.swing.Timer
import javax.swing.border.EtchedBorder
import javax.swing.border.TitledBorder

/**
 * Displays the time for a Surface to paint. Displays the number
 * of frames per second on animated demos.  Up to four surfaces fit
 * in the display area.
 */
class PerformanceMonitor : JPanel(BorderLayout())
{
    val surface = Surface()

    init {
        border = TitledBorder(EtchedBorder(), "Performance")
        add(surface)
    }

    val isRunning get() = surface.isRunning

    fun start() = surface.start()

    fun stop() = surface.stop()

    inner class Surface : JPanel()
    {
        private var bufferedImage: BufferedImage? = null
        var panel: JPanel? = null
        private var fontMetrics: FontMetrics? = null
        private var timer: Timer? = null

        internal val isRunning: Boolean get() = (timer != null)

        init {
            background = Color.BLACK
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (timer == null) {
                        start()
                    } else {
                        stop()
                    }
                }
            })
        }

        override fun getMinimumSize(): Dimension = preferredSize

        override fun getMaximumSize(): Dimension = preferredSize

        override fun getPreferredSize(): Dimension {
            val textHeight = getFontMetrics(FONT).height
            return Dimension(135, 2 + textHeight * 4)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            if (bufferedImage != null) {
                g.drawImage(bufferedImage, 0, 0, null)
            }
        }

        internal fun start() {
            if (timer == null) {
                timer = Timer(990) {
                    if (isShowing && width != 0 && height != 0) {
                        render()
                        repaint()
                    }
                }.apply {
                    initialDelay = 0
                    start()
                }
            }
        }

        internal fun stop() {
            timer?.let { timer ->
                timer.stop()
                this.timer = null
            }
        }

        fun setSurfaceState() {
            panel?.let { panel ->
                for (component in panel.components) {
                    if ((component as DemoPanel).surface != null) {
                        component.surface!!.monitor = (timer != null)
                    }
                }
            }
        }

        private fun render() {
            val image = bufferedImage?.takeIf { it.width == width && it.height == height }
                ?: BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).also {
                    bufferedImage = it
                    fontMetrics = null
                    setSurfaceState()
                }
            image.createGraphics().use { imgGr ->
                imgGr.textAntialiasing = systemTextAntialiasing
                imgGr.font = FONT
                val fontMetrics = fontMetrics ?: imgGr.fontMetrics.also { fontMetrics = it }
                imgGr.background = background
                imgGr.clearRect(0, 0, image.width, image.height)
                panel?.let { panel ->
                    imgGr.color = Color.GREEN
                    var y = 1
                    val ascent = fontMetrics.ascent
                    val descent = fontMetrics.descent
                    for (component in panel.components) {
                        (component as? DemoPanel)?.surface?.performanceString?.let { performanceString ->
                            y += ascent
                            imgGr.drawString(performanceString, 4, y + 1)
                            y += descent
                        }
                    }
                }
            }
        }
    }

    companion object
    {
        private val FONT = Font(Font.DIALOG, Font.PLAIN, 12)
    }
}
