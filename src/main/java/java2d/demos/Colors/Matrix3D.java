package java2d.demos.Colors;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * A 3D matrix object.
 */
class Matrix3D
{

    public double[][] M = { { 1, 0, 0 },
        { 0, 1, 0 },
        { 0, 0, 1 } };
    private double[][] tmp = new double[3][3];
    private int row, col, k;

    public void Rotation(int i, int j, double angle) {
        for (row = 0; row < 3; row++) {
            for (col = 0; col < 3; col++) {
                if (row != col) {
                    M[row][col] = 0.0;
                } else {
                    M[row][col] = 1.0;
                }
            }
        }
        M[i][i] = cos(angle);
        M[j][j] = cos(angle);
        M[i][j] = sin(angle);
        M[j][i] = -sin(angle);
    }

    public double[][] Times(double[][] N) {
        for (row = 0; row < 3; row++) {
            for (col = 0; col < 3; col++) {
                tmp[row][col] = 0.0;
                for (k = 0; k < 3; k++) {
                    tmp[row][col] += M[row][k] * N[k][col];
                }
            }
        }
        return tmp;
    }
} // End Matrix3D
