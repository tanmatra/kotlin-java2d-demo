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
package java2d.demos.Colors

import java2d.Surface
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D

/**
 * Creating colors with an alpha value.
 */
class BullsEye : Surface()
{
    init {
        background = Color.WHITE
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        for (n in 0 .. 17) {
            val i = (n + 2) / 2.0f
            val x = 5 + i * (w / 2 / 10)
            val y = 5 + i * (h / 2 / 10)
            val ew = w - 10 - i * w / 10
            val eh = h - 10 - i * h / 10
            val alpha = if (n == 0) 0.1f else 1.0f / (19.0f - n)
            if (n >= 16) {
                g2.color = reds[n - 16]
            } else {
                g2.color = Color(0f, 0f, 0f, alpha)
            }
            g2.fill(Ellipse2D.Float(x, y, ew, eh))
        }
    }

    companion object
    {
        private val reds = arrayOf(Color.RED.darker(), Color.RED)

        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(BullsEye())
        }
    }
}
