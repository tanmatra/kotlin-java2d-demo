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
import java2d.unsafeLazy
import java2d.use
import java.awt.BorderLayout
import java.awt.Color
import java.awt.EventQueue
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
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
    private val surface = Surface()
    private val scenesTable by unsafeLazy { ScenesTable(surface) }
    private var showTable = false

    init {
        border = CompoundBorder(EmptyBorder(80, 110, 80, 110), BevelBorder(BevelBorder.LOWERED))
        background = Color.GRAY
        toolTipText = "click for scene table"
        add(surface)
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                removeAll()
                showTable = !showTable
                if (showTable) {
                    toolTipText = "click for animation"
                    surface.stop()
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
        if (!showTable) {
            surface.start()
        }
    }

    fun stop() {
        if (!showTable) {
            surface.stop()
        }
    }

    /**
     * Surface is the stage where the Director plays its scenes.
     */
    internal class Surface : JPanel(BorderLayout()), Runnable
    {
        var director: Director
        var index: Int = 0
        var sleepAmt: Long = 30
        @Volatile private var thread: Thread? = null

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
            cupanim = DemoImages.getImage("cupanim.gif", this)
            java_logo = DemoImages.getImage("java_logo.png", this)
            director = Director()
        }

        override fun paint(g: Graphics) {
            if (width <= 0 || height <= 0) {
                return
            }
            val image = bufferedImage?.takeIf { it.width == width && it.height == height }
                ?: graphicsConfiguration.createCompatibleImage(width, height).also {
                    bufferedImage = it
                    // reset future scenes
                    for (i in index + 1 until director.size) {
                        director[i].reset(this, width, height)
                    }
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
        }

        fun reset() {
            index = 0
            for (scene in director) {
                scene.reset(this, width, height)
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

            fun reset(surface: Surface, w: Int, h: Int) {
                index = 0
                parts.forEach { it.reset(surface, w, h) }
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

        companion object
        {
            lateinit var cupanim: Image
            lateinit var java_logo: Image
            var bufferedImage: BufferedImage? = null
        }
    } // End Surface class

    companion object
    {
        internal val BLACK = Color(20, 20, 20)
        internal val WHITE = Color(240, 240, 255)
        internal val RED = Color(149, 43, 42)
        internal val BLUE = Color(94, 105, 176)
        internal val YELLOW = Color(255, 255, 140)

        @JvmStatic
        fun main(argv: Array<String>) {
            EventQueue.invokeLater {
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
                    contentPane.add(intro, BorderLayout.CENTER)
                    pack()
                    setSize(720, 510)
                    setLocationRelativeTo(null)
                    isVisible = true
                }
                intro.start()
            }
        }
    }
}
