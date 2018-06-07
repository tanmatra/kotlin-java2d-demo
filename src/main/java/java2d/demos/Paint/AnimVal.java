package java2d.demos.Paint;

final class AnimVal
{
    private float curval;
    private float lowval;
    private float highval;
    private float currate;
    private float lowrate;
    private float highrate;

    public AnimVal(float lowval, float highval, float lowrate, float highrate) {
        this.lowval = lowval;
        this.highval = highval;
        this.lowrate = lowrate;
        this.highrate = highrate;
        this.curval = randval(lowval, highval);
        this.currate = randval(lowrate, highrate);
    }

    public AnimVal(float lowval, float highval, float lowrate, float highrate, float pos) {
        this(lowval, highval, lowrate, highrate);
        set(pos);
    }

    private float randval(float low, float high) {
        return (float) (low + Math.random() * (high - low));
    }

    public float getFlt() {
        return curval;
    }

    public int getInt() {
        return (int) curval;
    }

    public void anim() {
        curval += currate;
        clip();
    }

    public void set(float val) {
        curval = val;
        clip();
    }

    public void clip() {
        if (curval > highval) {
            curval = highval - (curval - highval);
            if (curval < lowval) {
                curval = highval;
            }
            currate = -randval(lowrate, highrate);
        } else if (curval < lowval) {
            curval = lowval + (lowval - curval);
            if (curval > highval) {
                curval = lowval;
            }
            currate = randval(lowrate, highrate);
        }
    }

    public void newlimits(float lowval, float highval) {
        this.lowval = lowval;
        this.highval = highval;
        clip();
    }
}
