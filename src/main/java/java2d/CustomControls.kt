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

import java.awt.Color
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.border.EtchedBorder

/**
 * A convenience class for demos that use Custom Controls.  This class
 * sets up the thread for running the custom control.  A notifier thread
 * is started as well, a flashing 2x2 rect is drawn in the upper right corner
 * while the custom control thread continues to run.
 */
abstract class CustomControls(name: String? = null) : JPanel(), Runnable
{
    @Volatile
    protected var thread: Thread? = null

    protected var doNotifier: Boolean = false

    private var ccnt: CCNotifierThread? = null

    init {
        this.name = if (name == null) "Demo" else "$name Demo"
        border = EtchedBorder()
        @Suppress("LeakingThis")
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

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.color = if (doNotifier) BLUE else Color.GRAY
        g.fillRect(size.width - 2, 0, 2, 2)
    }

    fun start() {
        if (thread == null) {
            thread = Thread(this, "$name ccthread").apply {
                priority = Thread.MIN_PRIORITY
                start()
            }
            ccnt = CCNotifierThread().apply {
                name = "$name ccthread notifier"
                start()
            }
        }
    }

    @Synchronized
    fun stop() {
        thread?.let { thread ->
            thread.interrupt()
            ccnt?.interrupt()
        }
        thread = null
    }

    // Custom Controls override the run method
    override fun run() {}

    /**
     * Notifier that the custom control thread is running.
     */
    internal inner class CCNotifierThread : Thread()
    {
        override fun run() {
            while (thread != null) {
                doNotifier = !doNotifier
                repaint()
                try {
                    Thread.sleep(444)
                } catch (ex: Exception) {
                    break
                }
            }
            doNotifier = false
            repaint()
        }
    }

    companion object {
        private val BLUE = Color(204, 204, 255)
    }
}
