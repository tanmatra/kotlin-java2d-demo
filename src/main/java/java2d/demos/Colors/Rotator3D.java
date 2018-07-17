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
package java2d.demos.Colors;


import java.awt.Color;
import java.awt.Graphics2D;
import java2d.AnimatingSurface;

/**
 * 3D objects with color & lighting translated, rotated and scaled.
 */
@SuppressWarnings("serial")
public class Rotator3D extends AnimatingSurface {

    private Objects3D objs[] = new Objects3D[3];
    private static final int[][][] polygons = {
        // Solid cube
        { { 5, 1, 15, 13, 21, 23, 15 },
            { 5, 2, 21, 13, 19, 27, 21 },
            { 5, 3, 23, 15, 17, 25, 23 },
            { 5, 4, 19, 13, 15, 17, 19 },
            { 5, 5, 27, 21, 23, 25, 27 },
            { 5, 6, 27, 19, 17, 25, 27 } },
        // Polygonal faces cube
        { { 5, 1, 21, 13, 19, 27, 21 },
            { 5, 5, 23, 15, 17, 25, 23 },
            { 4, 0, 15, 14, 16, 15 }, { 7, 6, 16, 14, 13, 12, 18, 17, 16 }, { 4,
                0, 12, 19, 18, 12 },
            { 4, 2, 22, 21, 20, 22 }, { 7, 0, 24, 23, 22, 20, 27, 26, 24 }, { 4,
                2, 24, 26, 25, 24 },
            { 4, 3, 15, 13, 23, 15 }, { 4, 0, 23, 13, 21, 23 },
            { 5, 0, 27, 26, 18, 19, 27 }, { 5, 4, 25, 17, 18, 26, 25 } },
        // Octahedron
        { { 4, 3, 18, 21, 16, 18 }, { 4, 1, 20, 16, 18, 20 },
            { 4, 1, 18, 21, 16, 18 }, { 4, 3, 20, 17, 19, 20 },
            { 4, 2, 20, 26, 27, 20 }, { 5, 3, 26, 18, 16, 27, 26 },
            { 5, 0, 17, 24, 25, 19, 17 }, { 4, 3, 21, 25, 24, 21 },
            { 4, 4, 18, 21, 22, 18 }, { 4, 2, 22, 21, 17, 22 },
            { 4, 5, 20, 23, 16, 20 }, { 4, 1, 20, 23, 19, 20 },
            { 4, 6, 21, 23, 16, 21 }, { 4, 4, 21, 23, 19, 21 },
            { 4, 5, 20, 18, 22, 20 }, { 4, 6, 20, 22, 17, 20 } }
    };
    private static final double[][][] points = {
        // Points for solid cube & polygonal faces cube
        { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 }, { 0, 0, 1 },
            { 0, 0, -1 }, { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 },
            { 0, 0, 1 }, { 0, 0, -1 }, { 1, 1, 0 }, { 1, 1, 1 }, { 0, 1, 1 },
            { -1, 1, 1 }, { -1, 1, 0 }, { -1, 1, -1 }, { 0, 1, -1 },
            { 1, 1, -1 },
            { 1, -1, 0 }, { 1, -1, 1 }, { 0, -1, 1 }, { -1, -1, 1 },
            { -1, -1, 0 },
            { -1, -1, -1 }, { 0, -1, -1 }, { 1, -1, -1 } },
        // Points for octahedron
        { { 0, 0, 1 }, { 0, 0, -1 }, { -0.8165, 0.4714, 0.33333 },
            { 0.8165, -0.4714, -0.33333 }, { 0.8165, 0.4714, 0.33333 },
            { -0.8165, -0.4714, -0.33333 }, { 0, -0.9428, 0.3333 },
            { 0, 0.9428, -0.33333 }, { 0, 0, 1 }, { 0, 0, -1 },
            { -0.8165, 0.4714, 0.33333 }, { 0.8165, -0.4714, -0.33333 },
            { 0.8165, 0.4714, 0.33333 }, { -0.8165, -0.4714, -0.33333 },
            { 0, -0.9428, 0.33333 }, { 0, 0.9428, -0.33333 },
            { -1.2247, -0.7071, 1 }, { 1.2247, 0.7071, -1 },
            { 0, 1.4142, 1 }, { 0, -1.4142, -1 }, { -1.2247, 0.7071, -1 },
            { 1.2247, -0.7071, 1 }, { 0.61237, 1.06066, 0 },
            { -0.61237, -1.06066, 0 }, { 1.2247, 0, 0 },
            { 0.61237, -1.06066, 0 }, { -0.61237, 1.06066, 0 },
            { -1.2247, 0, 0 } }
    };
    private static final int[][][] faces = {
        // Solid cube
        { { 1, 1 }, { 1, 2 }, { 1, 3 }, { 1, 4 }, { 1, 0 }, { 1, 5 } },
        // Polygonal faces cube
        { { 1, 0 }, { 1, 1 }, { 3, 2, 3, 4 }, { 3, 5, 6, 7 }, { 2, 8, 9 }, { 2,
                10, 11 } },
        // Octahedron
        { { 1, 2 }, { 1, 3 }, { 2, 4, 5 }, { 2, 6, 7 }, { 2, 8, 9 },
            { 2, 10, 11 }, { 2, 12, 13 }, { 2, 14, 15 } }, };

    public Rotator3D() {
        setBackground(Color.white);
    }

    @Override
    public void reset(int w, int h) {
        objs[0] = new Objects3D(polygons[0], points[0], faces[0], w, h);
        objs[1] = new Objects3D(polygons[1], points[0], faces[1], w, h);
        objs[2] = new Objects3D(polygons[2], points[1], faces[2], w, h);
    }

    @Override
    public void step(int w, int h) {
        for (Objects3D obj : objs) {
            if (obj != null) {
                obj.step(w, h);
            }
        }
    }

    @Override
    public void render(int w, int h, Graphics2D g2) {
        for (Objects3D obj : objs) {
            if (obj != null) {
                obj.render(g2);
            }
        }
    }

    public static void main(String argv[]) {
        createDemoFrame(new Rotator3D());
    }
} // End Rotator3D
