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

/**
 * Cubic & Quad curves implemented through GeneralPath.
 */
class CurveQuadTo : Surface()
{
    init {
        background = Color.WHITE
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val p = GeneralPath(Path2D.WIND_EVEN_ODD)
        p.moveTo(w * 0.2f, h * 0.25f)
        p.curveTo(w * 0.4f, h * 0.5f, w * 0.6f, 0.0f, w * 0.8f, h * 0.25f)
        p.moveTo(w * 0.2f, h * 0.6f)
        p.quadTo(w * 0.5f, h * 1.0f, w * 0.8f, h * 0.6f)
        g2.color = Color.LIGHT_GRAY
        g2.fill(p)
        g2.color = Color.BLACK
        g2.draw(p)
        g2.drawString("curveTo", (w * 0.2).toInt(), (h * 0.25f).toInt() - 5)
        g2.drawString("quadTo", (w * 0.2).toInt(), (h * 0.6f).toInt() - 5)
    }

    companion object {
        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(CurveQuadTo())
        }
    }
}
