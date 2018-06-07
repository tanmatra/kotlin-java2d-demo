package java2d.demos.Paint

internal class AnimVal(
    private var lowVal: Float,
    private var highVal: Float,
    private val lowRate: Float,
    private val highRate: Float
) {
    var value: Float = 0f
        private set

    private var curRate: Float = 0f

    val intValue: Int get() = value.toInt()

    init {
        this.value = random(lowVal, highVal)
        this.curRate = random(lowRate, highRate)
    }

    constructor(lowVal: Float, highVal: Float, lowRate: Float, highRate: Float, initial: Float)
        : this(lowVal, highVal, lowRate, highRate)
    {
        set(initial)
    }

    private fun random(low: Float, high: Float): Float {
        return (low + Math.random() * (high - low)).toFloat()
    }

    fun anim() {
        value += curRate
        clip()
    }

    fun set(value: Float) {
        this.value = value
        clip()
    }

    private fun clip() {
        if (value > highVal) {
            value = highVal - (value - highVal)
            if (value < lowVal) {
                value = highVal
            }
            curRate = -random(lowRate, highRate)
        } else if (value < lowVal) {
            value = lowVal + (lowVal - value)
            if (value > highVal) {
                value = lowVal
            }
            curRate = random(lowRate, highRate)
        }
    }

    fun newLimits(lowVal: Float, highVal: Float) {
        this.lowVal = lowVal
        this.highVal = highVal
        clip()
    }
}
