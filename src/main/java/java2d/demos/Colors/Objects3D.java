package java2d.demos.Colors;

import java.awt.Color;
import java.awt.Graphics2D;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.random;
import static java.lang.Math.sqrt;

/**
 * 3D Objects : Solid Cube, Cube & Octahedron with polygonal faces.
 */
class Objects3D
{
    private static final int UP = 0;
    private static final int DOWN = 1;

    private static final int NCOLOUR = 10;

    private static final Color[][] COLOURS = new Color[NCOLOUR][7];

    static {
        initColours();
    }

    private static void initColours() {
        for (int i = 0; i < NCOLOUR; i++) {
            int val = 255 - (NCOLOUR - i - 1) * 100 / NCOLOUR;
            COLOURS[i] = new Color[] {
                    new Color(val, val, val), // white
                    new Color(val,   0,   0), // red
                    new Color(0,   val,   0), // green
                    new Color(0,     0, val), // blue
                    new Color(val, val,   0), // yellow
                    new Color(0,   val, val), // cyan
                    new Color(val,   0, val)  // magenta
            };
        }
    }

    private int[][] polygons;
    private double[][] points;
    private int npoint;
    private int[][] faces;
    private int nface;
    private double[] lightvec = { 0, 1, 1 };
    private double Zeye = 10;
    private double angle;
    private Matrix3D orient;
    private Matrix3D tmp;
    private Matrix3D tmp2;
    private Matrix3D tmp3;
    private int scaleDirection;
    private double scale;
    private double scaleAmt;
    private double ix = 3.0;
    private double iy = 3.0;
    private double[][] rotPts;
    private int[][] scrPts;
    private int[] xx = new int[20];
    private int[] yy = new int[20];
    private double x;
    private double y;

    public Objects3D(int[][] polygons,
                     double[][] points,
                     int[][] faces,
                     int w,
                     int h)
    {
        this.polygons = polygons;
        this.points = points;
        this.faces = faces;
        npoint = points.length;
        nface = faces.length;

        x = w * random();
        y = h * random();

        ix = random() > 0.5 ? ix : -ix;
        iy = random() > 0.5 ? iy : -iy;

        rotPts = new double[npoint][3];
        scrPts = new int[npoint][2];

        double len = sqrt(lightvec[0] * lightvec[0] + lightvec[1] * lightvec[1] + lightvec[2] * lightvec[2]);
        lightvec[0] = lightvec[0] / len;
        lightvec[1] = lightvec[1] / len;
        lightvec[2] = lightvec[2] / len;

        double max = 0;
        for (int i = 0; i < npoint; i++) {
            len = sqrt(points[i][0] * points[i][0] + points[i][1] * points[i][1] + points[i][2] * points[i][2]);
            if (len > max) {
                max = len;
            }
        }

        for (int i = 0; i < nface; i++) {
            len = sqrt(points[i][0] * points[i][0] + points[i][1] * points[i][1] + points[i][2] * points[i][2]);
            points[i][0] = points[i][0] / len;
            points[i][1] = points[i][1] / len;
            points[i][2] = points[i][2] / len;
        }

        orient = new Matrix3D();
        tmp = new Matrix3D();
        tmp2 = new Matrix3D();
        tmp3 = new Matrix3D();
        tmp.rotation(2, 0, PI / 50);
        calcScrPts((double) w / 3, (double) h / 3, 0);

        scale = min(w / 3.0 / max / 1.2, h / 3.0 / max / 1.2);
        scaleAmt = scale;
        scale *= random() * 1.5;
        scaleDirection = scaleAmt < scale ? DOWN : UP;
    }

    private Color getColour(int f, int index) {
        int colour = (int) ((rotPts[f][0] * lightvec[0] +
                             rotPts[f][1] * lightvec[1] +
                             rotPts[f][2] * lightvec[2]) * NCOLOUR);
        if (colour < 0) {
            colour = 0;
        }
        if (colour > NCOLOUR - 1) {
            colour = NCOLOUR - 1;
        }
        return COLOURS[colour][polygons[faces[f][index]][1]];
    }

    private void calcScrPts(double x, double y, double z) {
        for (int p = 0; p < npoint; p++) {
            rotPts[p][2] = points[p][0] * orient.M[2][0] + points[p][1] * orient.M[2][1] + points[p][2] * orient.M[2][2];
            rotPts[p][0] = points[p][0] * orient.M[0][0] + points[p][1] * orient.M[0][1] + points[p][2] * orient.M[0][2];
            rotPts[p][1] = -points[p][0] * orient.M[1][0] - points[p][1] * orient.M[1][1] - points[p][2] * orient.M[1][2];
        }
        for (int p = nface; p < npoint; p++) {
            rotPts[p][2] += z;
            final double persp = (Zeye - rotPts[p][2]) / (scale * Zeye);
            scrPts[p][0] = (int) (rotPts[p][0] / persp + x);
            scrPts[p][1] = (int) (rotPts[p][1] / persp + y);
        }
    }

    private boolean faceUp(int f) {
        return (rotPts[f][0] * rotPts[nface + f][0] +
                rotPts[f][1] * rotPts[nface + f][1] +
                rotPts[f][2] * (rotPts[nface + f][2] - Zeye)) < 0;
    }

    public void step(int w, int h) {
        x += ix;
        y += iy;
        if (x > w - scale) {
            x = w - scale - 1;
            ix = -w / 100.0 - 1;
        }
        if (x - scale < 0) {
            x = 2 + scale;
            ix = w / 100.0 + random() * 3;
        }
        if (y > h - scale) {
            y = h - scale - 2;
            iy = -h / 100.0 - 1;
        }
        if (y - scale < 0) {
            y = 2 + scale;
            iy = h / 100.0 + random() * 3;
        }

        angle += random() * 0.15;
        tmp3.rotation(1, 2, angle);
        tmp2.rotation(1, 0, angle * sqrt(2) / 2);
        tmp.rotation(0, 2, angle * PI / 4);
        orient.M = tmp3.times(tmp2.times(tmp.M));
        final double bounce = abs(cos(0.5 * (angle))) * 2 - 1;

        if (scale > scaleAmt * 1.4) {
            scaleDirection = DOWN;
        } else if (scale < scaleAmt * 0.4) {
            scaleDirection = UP;
        }
        if (scaleDirection == UP) {
            scale += random();
        } else if (scaleDirection == DOWN) {
            scale -= random();
        }

        calcScrPts(x, y, bounce);
    }

    public void render(Graphics2D g2) {
        for (int f = 0; f < nface; f++) {
            if (faceUp(f)) {
                for (int j = 1; j < faces[f][0] + 1; j++) {
                    drawPoly(g2, faces[f][j], getColour(f, j));
                }
            }
        }
    }

    private void drawPoly(Graphics2D g2, int poly, Color colour) {
        for (int point = 2; point < polygons[poly][0] + 2; point++) {
            xx[point - 2] = scrPts[polygons[poly][point]][0];
            yy[point - 2] = scrPts[polygons[poly][point]][1];
        }
        g2.setColor(colour);
        g2.fillPolygon(xx, yy, polygons[poly][0]);
        g2.setColor(Color.black);
        g2.drawPolygon(xx, yy, polygons[poly][0]);
    }
}
