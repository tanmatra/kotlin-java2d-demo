package java2d.demos.Colors

import java.lang.Math.cos
import java.lang.Math.sin

/**
 * A 3D matrix object.
 */
internal class Matrix3D
{
    var M: Array<DoubleArray> = arrayOf(
        doubleArrayOf(1.0, 0.0, 0.0),
        doubleArrayOf(0.0, 1.0, 0.0),
        doubleArrayOf(0.0, 0.0, 1.0))

    private val tmp: Array<DoubleArray> = Array(3) { DoubleArray(3) }

    fun rotation(i: Int, j: Int, angle: Double) {
        for (row in 0 .. 2) {
            for (col in 0 .. 2) {
                if (row != col) {
                    M[row][col] = 0.0
                } else {
                    M[row][col] = 1.0
                }
            }
        }
        M[i][i] = cos(angle)
        M[j][j] = cos(angle)
        M[i][j] = sin(angle)
        M[j][i] = -sin(angle)
    }

    operator fun times(N: Array<DoubleArray>): Array<DoubleArray> {
        for (row in 0 .. 2) {
            for (col in 0 .. 2) {
                tmp[row][col] = 0.0
                for (k in 0 .. 2) {
                    tmp[row][col] += M[row][k] * N[k][col]
                }
            }
        }
        return tmp
    }
}
