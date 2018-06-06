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
package java2d.demos.Paths

import java2d.Surface
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.GeneralPath
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D

/**
 * Simple append of rectangle to path with & without the connect.
 */
class Append : Surface()
{
    init {
        background = Color.WHITE
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val p = GeneralPath(Path2D.WIND_NON_ZERO)
        p.moveTo(w * 0.25f, h * 0.2f)
        p.lineTo(w * 0.75f, h * 0.2f)
        p.closePath()
        p.append(Rectangle2D.Double(w * 0.4, h * 0.3, w * 0.2, h * 0.1), false)
        g2.color = Color.GRAY
        g2.fill(p)
        g2.color = Color.BLACK
        g2.draw(p)
        g2.drawString("Append rect to path", (w * 0.25).toInt(), (h * 0.2).toInt() - 5)

        p.reset()
        p.moveTo(w * 0.25f, h * 0.6f)
        p.lineTo(w * 0.75f, h * 0.6f)
        p.closePath()
        p.append(Rectangle2D.Double(w * 0.4, h * 0.7, w * 0.2, h * 0.1), true)
        g2.color = Color.GRAY
        g2.fill(p)
        g2.color = Color.BLACK
        g2.draw(p)
        g2.drawString("Append, connect", (w * 0.25).toInt(), (h * 0.6).toInt() - 5)
    }

    companion object
    {
        @JvmStatic
        fun main(s: Array<String>) {
            Surface.createDemoFrame(Append())
        }
    }
}
