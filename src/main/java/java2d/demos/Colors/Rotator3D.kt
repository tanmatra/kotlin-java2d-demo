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

import java2d.AnimatingSurface
import java.awt.Color
import java.awt.Graphics2D

/**
 * 3D objects with color & lighting translated, rotated and scaled.
 */
class Rotator3D : AnimatingSurface()
{
    private val objs = arrayOfNulls<Objects3D>(3)

    init {
        background = Color.WHITE
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        objs[0] = Objects3D(polygons[0], points[0], faces[0], newWidth, newHeight)
        objs[1] = Objects3D(polygons[1], points[0], faces[1], newWidth, newHeight)
        objs[2] = Objects3D(polygons[2], points[1], faces[2], newWidth, newHeight)
    }

    override fun step(width: Int, height: Int) {
        for (obj in objs) {
            obj?.step(width, height)
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        for (obj in objs) {
            obj?.render(g2)
        }
    }

    companion object
    {
        private val polygons = arrayOf(
            // Solid cube
            arrayOf(
                intArrayOf(5, 1, 15, 13, 21, 23, 15),
                intArrayOf(5, 2, 21, 13, 19, 27, 21),
                intArrayOf(5, 3, 23, 15, 17, 25, 23),
                intArrayOf(5, 4, 19, 13, 15, 17, 19),
                intArrayOf(5, 5, 27, 21, 23, 25, 27),
                intArrayOf(5, 6, 27, 19, 17, 25, 27)),
            // Polygonal faces cube
            arrayOf(
                intArrayOf(5, 1, 21, 13, 19, 27, 21),
                intArrayOf(5, 5, 23, 15, 17, 25, 23),
                intArrayOf(4, 0, 15, 14, 16, 15),
                intArrayOf(7, 6, 16, 14, 13, 12, 18, 17, 16),
                intArrayOf(4, 0, 12, 19, 18, 12),
                intArrayOf(4, 2, 22, 21, 20, 22),
                intArrayOf(7, 0, 24, 23, 22, 20, 27, 26, 24),
                intArrayOf(4, 2, 24, 26, 25, 24),
                intArrayOf(4, 3, 15, 13, 23, 15),
                intArrayOf(4, 0, 23, 13, 21, 23),
                intArrayOf(5, 0, 27, 26, 18, 19, 27),
                intArrayOf(5, 4, 25, 17, 18, 26, 25)),
            // Octahedron
            arrayOf(
                intArrayOf(4, 3, 18, 21, 16, 18),
                intArrayOf(4, 1, 20, 16, 18, 20),
                intArrayOf(4, 1, 18, 21, 16, 18),
                intArrayOf(4, 3, 20, 17, 19, 20),
                intArrayOf(4, 2, 20, 26, 27, 20),
                intArrayOf(5, 3, 26, 18, 16, 27, 26),
                intArrayOf(5, 0, 17, 24, 25, 19, 17),
                intArrayOf(4, 3, 21, 25, 24, 21),
                intArrayOf(4, 4, 18, 21, 22, 18),
                intArrayOf(4, 2, 22, 21, 17, 22),
                intArrayOf(4, 5, 20, 23, 16, 20),
                intArrayOf(4, 1, 20, 23, 19, 20),
                intArrayOf(4, 6, 21, 23, 16, 21),
                intArrayOf(4, 4, 21, 23, 19, 21),
                intArrayOf(4, 5, 20, 18, 22, 20),
                intArrayOf(4, 6, 20, 22, 17, 20)))

        private val points = arrayOf(
            // Points for solid cube & polygonal faces cube
            arrayOf(
                doubleArrayOf(1.0, 0.0, 0.0),
                doubleArrayOf(-1.0, 0.0, 0.0),
                doubleArrayOf(0.0, 1.0, 0.0),
                doubleArrayOf(0.0, -1.0, 0.0),
                doubleArrayOf(0.0, 0.0, 1.0),
                doubleArrayOf(0.0, 0.0, -1.0),
                doubleArrayOf(1.0, 0.0, 0.0),
                doubleArrayOf(-1.0, 0.0, 0.0),
                doubleArrayOf(0.0, 1.0, 0.0),
                doubleArrayOf(0.0, -1.0, 0.0),
                doubleArrayOf(0.0, 0.0, 1.0),
                doubleArrayOf(0.0, 0.0, -1.0),
                doubleArrayOf(1.0, 1.0, 0.0),
                doubleArrayOf(1.0, 1.0, 1.0),
                doubleArrayOf(0.0, 1.0, 1.0),
                doubleArrayOf(-1.0, 1.0, 1.0),
                doubleArrayOf(-1.0, 1.0, 0.0),
                doubleArrayOf(-1.0, 1.0, -1.0),
                doubleArrayOf(0.0, 1.0, -1.0),
                doubleArrayOf(1.0, 1.0, -1.0),
                doubleArrayOf(1.0, -1.0, 0.0),
                doubleArrayOf(1.0, -1.0, 1.0),
                doubleArrayOf(0.0, -1.0, 1.0),
                doubleArrayOf(-1.0, -1.0, 1.0),
                doubleArrayOf(-1.0, -1.0, 0.0),
                doubleArrayOf(-1.0, -1.0, -1.0),
                doubleArrayOf(0.0, -1.0, -1.0),
                doubleArrayOf(1.0, -1.0, -1.0)),
            // Points for octahedron
            arrayOf(
                doubleArrayOf(0.0, 0.0, 1.0),
                doubleArrayOf(0.0, 0.0, -1.0),
                doubleArrayOf(-0.8165, 0.4714, 0.33333),
                doubleArrayOf(0.8165, -0.4714, -0.33333),
                doubleArrayOf(0.8165, 0.4714, 0.33333),
                doubleArrayOf(-0.8165, -0.4714, -0.33333),
                doubleArrayOf(0.0, -0.9428, 0.3333),
                doubleArrayOf(0.0, 0.9428, -0.33333),
                doubleArrayOf(0.0, 0.0, 1.0),
                doubleArrayOf(0.0, 0.0, -1.0),
                doubleArrayOf(-0.8165, 0.4714, 0.33333),
                doubleArrayOf(0.8165, -0.4714, -0.33333),
                doubleArrayOf(0.8165, 0.4714, 0.33333),
                doubleArrayOf(-0.8165, -0.4714, -0.33333),
                doubleArrayOf(0.0, -0.9428, 0.33333),
                doubleArrayOf(0.0, 0.9428, -0.33333),
                doubleArrayOf(-1.2247, -0.7071, 1.0),
                doubleArrayOf(1.2247, 0.7071, -1.0),
                doubleArrayOf(0.0, 1.4142, 1.0),
                doubleArrayOf(0.0, -1.4142, -1.0),
                doubleArrayOf(-1.2247, 0.7071, -1.0),
                doubleArrayOf(1.2247, -0.7071, 1.0),
                doubleArrayOf(0.61237, 1.06066, 0.0),
                doubleArrayOf(-0.61237, -1.06066, 0.0),
                doubleArrayOf(1.2247, 0.0, 0.0),
                doubleArrayOf(0.61237, -1.06066, 0.0),
                doubleArrayOf(-0.61237, 1.06066, 0.0),
                doubleArrayOf(-1.2247, 0.0, 0.0)))

        private val faces = arrayOf(
            // Solid cube
            arrayOf(
                intArrayOf(1, 1),
                intArrayOf(1, 2),
                intArrayOf(1, 3),
                intArrayOf(1, 4),
                intArrayOf(1, 0),
                intArrayOf(1, 5)),
            // Polygonal faces cube
            arrayOf(
                intArrayOf(1, 0),
                intArrayOf(1, 1),
                intArrayOf(3, 2, 3, 4),
                intArrayOf(3, 5, 6, 7),
                intArrayOf(2, 8, 9),
                intArrayOf(2, 10, 11)),
            // Octahedron
            arrayOf(
                intArrayOf(1, 2),
                intArrayOf(1, 3),
                intArrayOf(2, 4, 5),
                intArrayOf(2, 6, 7),
                intArrayOf(2, 8, 9),
                intArrayOf(2, 10, 11),
                intArrayOf(2, 12, 13),
                intArrayOf(2, 14, 15)))

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(Rotator3D())
        }
    }
}
