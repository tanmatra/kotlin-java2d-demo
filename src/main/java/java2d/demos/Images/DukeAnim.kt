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
package java2d.demos.Images

import java2d.AnimatingSurface
import java2d.DemoPanel
import java2d.hasBits
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.ImageObserver
import javax.swing.AbstractButton

/**
 * Animated gif with a transparent background.
 */
class DukeAnim : AnimatingSurface(), ImageObserver
{
    private var ix: Int = 0
    private var startStopButton: AbstractButton? = null
    private val agif: Image = getImage("duke.running.gif")
    private val clouds: Image = getImage("clouds.jpg")
    private val aw: Int = agif.getWidth(this) / 2
    private val ah: Int = agif.getHeight(this) / 2
    private val cw: Int = clouds.getWidth(this)

    init {
        background = Color.WHITE
        dontThread = true
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        startStopButton = (parent as DemoPanel).tools?.startStopButton
    }

    override fun step(width: Int, height: Int) {}

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        ix -= 3
        if (ix <= -cw) {
            ix = w
        }
        g2.drawImage(clouds, ix, 10, cw, h - 20, this)
        g2.drawImage(agif, w / 2 - aw, h / 2 - ah, this)
    }


    override fun imageUpdate(
        img: Image?, infoflags: Int,
        x: Int, y: Int, width: Int, height: Int
    ): Boolean {
        startStopButton?.let { button ->
            if (button.isSelected) {
                if (infoflags hasBits (ImageObserver.ALLBITS or ImageObserver.FRAMEBITS)) {
                    repaint()
                }
            }
        }
        return isShowing
    }

    companion object {
        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(DukeAnim())
        }
    }
}
