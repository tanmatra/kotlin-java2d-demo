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
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Image
import java.awt.geom.CubicCurve2D
import java.awt.geom.PathIterator
import java.awt.geom.Point2D

/**
 * Warps a image on a CubicCurve2D flattened path.
 */
class WarpImage : AnimatingSurface()
{
    private val img: Image = getImage("surfing.gif")
    private val iw: Int = img.getWidth(this)
    private val ih: Int = img.getHeight(this)
    private val iw2: Int = iw / 2
    private val ih2: Int = ih / 2
    private var points: Array<Point2D>? = null
    private var direction = FORWARD
    private var pointIndex: Int = 0
    private var ix: Int = 0
    private var iy: Int = 0

    init {
        background = Color.WHITE
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        pointIndex = 0
        direction = FORWARD
        val curve = CubicCurve2D.Float(newWidth * 0.2f, newHeight * 0.5f,
                                       newWidth * 0.4f, 0.0f,
                                       newWidth * 0.6f, newHeight.toFloat(),
                                       newWidth * 0.8f, newHeight * 0.5f)
        val pathIterator = curve.getPathIterator(null, 0.1)
        val list = mutableListOf<Point2D>()
        val coords = FloatArray(6)
        while (!pathIterator.isDone) {
            when (pathIterator.currentSegment(coords)) {
                PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> list += Point2D.Float(coords[0], coords[1])
            }
            pathIterator.next()
        }
        points = list.toTypedArray()
    }

    override fun step(width: Int, height: Int) {
        val points = points ?: return
        ix = points[pointIndex].x.toInt()
        iy = points[pointIndex].y.toInt()
        when (direction) {
            FORWARD -> {
                if (pointIndex < points.lastIndex) {
                    pointIndex++
                } else {
                    direction = BACK
                    pointIndex--
                }
            }
            BACK -> {
                if (pointIndex > 0) {
                    pointIndex--
                } else {
                    direction = FORWARD
                    pointIndex++
                }
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        g2.drawImage(img,  0,  0, ix, iy,   0,   0, iw2, ih2, this)
        g2.drawImage(img, ix,  0,  w, iy, iw2,   0,  iw, ih2, this)
        g2.drawImage(img,  0, iy, ix,  h,   0, ih2, iw2,  ih, this)
        g2.drawImage(img, ix, iy,  w,  h, iw2, ih2,  iw,  ih, this)
    }

    companion object
    {
        private const val FORWARD = 0
        private const val BACK = 1

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(WarpImage())
        }
    }
}
