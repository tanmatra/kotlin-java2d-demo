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

/**
 * Demos that animate extend this class.
 */
abstract class AnimatingSurface : Surface(), Runnable
{
    @Volatile
    var isRunning = false
        private set

    @Volatile
    private var thread: Thread? = null

    abstract fun step(width: Int, height: Int)

    abstract fun reset(newWidth: Int, newHeight: Int)

    fun start() {
        if (!isRunning && !dontThread) {
            thread = Thread(this, "$name Demo").apply {
                priority = Thread.MIN_PRIORITY
                start()
            }
            isRunning = true
        }
    }

    @Synchronized
    fun stop() {
        thread?.let { thread ->
            isRunning = false
            thread.interrupt()
        }
        thread = null
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        (this as java.lang.Object).notifyAll()
    }

    override fun run() {
        while (isRunning && !isShowing || size.width == 0) {
            try {
                Thread.sleep(200)
            } catch (ignored: InterruptedException) {
            }
        }

        while (isRunning) {
            repaint()
            try {
                Thread.sleep(sleepAmount)
            } catch (ignored: InterruptedException) {
            }
        }
        isRunning = false
    }

    /**
     * Causes surface to repaint immediately
     */
    fun doRepaint() {
        if (isRunning) {
            thread?.interrupt()
        }
    }
}
