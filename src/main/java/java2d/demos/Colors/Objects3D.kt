package java2d.demos.Colors

import java.awt.Color
import java.awt.Graphics2D

import java.lang.Math.PI
import java.lang.Math.abs
import java.lang.Math.cos
import java.lang.Math.min
import java.lang.Math.random
import java.lang.Math.sqrt

/**
 * 3D Objects : Solid Cube, Cube & Octahedron with polygonal faces.
 */
internal class Objects3D(
    private val polygons: Array<IntArray>,
    private val points: Array<DoubleArray>,
    private val faces: Array<IntArray>,
    w: Int,
    h: Int
) {
    private val npoint: Int
    private val nface: Int
    private val lightvec = doubleArrayOf(0.0, 1.0, 1.0)
    private val Zeye = 10.0
    private var angle: Double = 0.toDouble()
    private val orient: Matrix3D
    private val tmp: Matrix3D
    private val tmp2: Matrix3D
    private val tmp3: Matrix3D
    private var scaleDirection: Int = 0
    private var scale: Double = 0.toDouble()
    private val scaleAmt: Double
    private var ix = 3.0
    private var iy = 3.0
    private val rotPts: Array<DoubleArray>
    private val scrPts: Array<IntArray>
    private val xx = IntArray(20)
    private val yy = IntArray(20)
    private var x: Double = 0.toDouble()
    private var y: Double = 0.toDouble()

    init {
        npoint = points.size
        nface = faces.size

        x = w * random()
        y = h * random()

        ix = if (random() > 0.5) ix else -ix
        iy = if (random() > 0.5) iy else -iy

        rotPts = Array(npoint) { DoubleArray(3) }
        scrPts = Array(npoint) { IntArray(2) }

        var len = sqrt(lightvec[0] * lightvec[0] + lightvec[1] * lightvec[1] + lightvec[2] * lightvec[2])
        lightvec[0] = lightvec[0] / len
        lightvec[1] = lightvec[1] / len
        lightvec[2] = lightvec[2] / len

        var max = 0.0
        for (i in 0 until npoint) {
            len = sqrt(points[i][0] * points[i][0] + points[i][1] * points[i][1] + points[i][2] * points[i][2])
            if (len > max) {
                max = len
            }
        }

        for (i in 0 until nface) {
            len = sqrt(points[i][0] * points[i][0] + points[i][1] * points[i][1] + points[i][2] * points[i][2])
            points[i][0] = points[i][0] / len
            points[i][1] = points[i][1] / len
            points[i][2] = points[i][2] / len
        }

        orient = Matrix3D()
        tmp = Matrix3D()
        tmp2 = Matrix3D()
        tmp3 = Matrix3D()
        tmp.rotation(2, 0, PI / 50)
        calcScrPts(w.toDouble() / 3, h.toDouble() / 3, 0.0)

        scale = min(w.toDouble() / 3.0 / max / 1.2, h.toDouble() / 3.0 / max / 1.2)
        scaleAmt = scale
        scale *= random() * 1.5
        scaleDirection = if (scaleAmt < scale) DOWN else UP
    }

    private fun getColour(f: Int, index: Int): Color {
        var colour = ((rotPts[f][0] * lightvec[0] +
                       rotPts[f][1] * lightvec[1] +
                       rotPts[f][2] * lightvec[2]) * NCOLOUR).toInt()
        if (colour < 0) {
            colour = 0
        }
        if (colour > NCOLOUR - 1) {
            colour = NCOLOUR - 1
        }
        return COLOURS[colour][polygons[faces[f][index]][1]]
    }

    private fun calcScrPts(x: Double, y: Double, z: Double) {
        for (p in 0 until npoint) {
            rotPts[p][2] = points[p][0] * orient.M[2][0] + points[p][1] * orient.M[2][1] + points[p][2] * orient.M[2][2]
            rotPts[p][0] = points[p][0] * orient.M[0][0] + points[p][1] * orient.M[0][1] + points[p][2] * orient.M[0][2]
            rotPts[p][1] = -points[p][0] * orient.M[1][0] - points[p][1] * orient.M[1][1] - points[p][2] *
                orient.M[1][2]
        }
        for (p in nface until npoint) {
            rotPts[p][2] += z
            val persp = (Zeye - rotPts[p][2]) / (scale * Zeye)
            scrPts[p][0] = (rotPts[p][0] / persp + x).toInt()
            scrPts[p][1] = (rotPts[p][1] / persp + y).toInt()
        }
    }

    private fun faceUp(f: Int): Boolean {
        return rotPts[f][0] * rotPts[nface + f][0] +
            rotPts[f][1] * rotPts[nface + f][1] +
            rotPts[f][2] * (rotPts[nface + f][2] - Zeye) < 0
    }

    fun step(w: Int, h: Int) {
        x += ix
        y += iy
        if (x > w - scale) {
            x = w.toDouble() - scale - 1.0
            ix = -w / 100.0 - 1
        }
        if (x - scale < 0) {
            x = 2 + scale
            ix = w / 100.0 + random() * 3
        }
        if (y > h - scale) {
            y = h.toDouble() - scale - 2.0
            iy = -h / 100.0 - 1
        }
        if (y - scale < 0) {
            y = 2 + scale
            iy = h / 100.0 + random() * 3
        }

        angle += random() * 0.15
        tmp3.rotation(1, 2, angle)
        tmp2.rotation(1, 0, angle * sqrt(2.0) / 2)
        tmp.rotation(0, 2, angle * PI / 4)
        orient.M = tmp3.times(tmp2.times(tmp.M))
        val bounce = abs(cos(0.5 * angle)) * 2 - 1

        if (scale > scaleAmt * 1.4) {
            scaleDirection = DOWN
        } else if (scale < scaleAmt * 0.4) {
            scaleDirection = UP
        }
        if (scaleDirection == UP) {
            scale += random()
        } else if (scaleDirection == DOWN) {
            scale -= random()
        }

        calcScrPts(x, y, bounce)
    }

    fun render(g2: Graphics2D) {
        for (f in 0 until nface) {
            if (faceUp(f)) {
                for (j in 1 until faces[f][0] + 1) {
                    drawPoly(g2, faces[f][j], getColour(f, j))
                }
            }
        }
    }

    private fun drawPoly(g2: Graphics2D, poly: Int, colour: Color) {
        for (point in 2 until polygons[poly][0] + 2) {
            xx[point - 2] = scrPts[polygons[poly][point]][0]
            yy[point - 2] = scrPts[polygons[poly][point]][1]
        }
        g2.color = colour
        g2.fillPolygon(xx, yy, polygons[poly][0])
        g2.color = Color.black
        g2.drawPolygon(xx, yy, polygons[poly][0])
    }

    companion object
    {
        private const val UP = 0
        private const val DOWN = 1

        private const val NCOLOUR = 10

        private val COLOURS: Array<Array<Color>> = Array(NCOLOUR) { i ->
            val v = 255 - (NCOLOUR - i - 1) * 100 / NCOLOUR
            arrayOf(
                Color(v, v, v), // white
                Color(v, 0, 0), // red
                Color(0, v, 0), // green
                Color(0, 0, v), // blue
                Color(v, v, 0), // yellow
                Color(0, v, v), // cyan
                Color(v, 0, v)) // magenta
        }
    }
}
