package java2d

import java.applet.Applet

abstract class ProgramParameters
{
    abstract operator fun get(key: String): String?

    fun parse(demo: Java2Demo) {
        val globalControls = demo.globalControls
        val globalOptions = globalControls.options
        val runWindowOptions = RunWindow.Options()

        get("antialias")?.let { globalOptions.antialiasing = it.toBoolean() }
        get("buffers")?.let {
            // usage -buffers=3,10
            val (s1, s2) = it.split(',')
            runWindowOptions.setBuffers(s1.toInt(), s2.toInt())
        }
        get("ccthread")?.let { Java2Demo.ccthreadCB.isSelected = true }
        get("columns")?.let { DemoGroup.columns = it.toInt() }
        get("composite")?.let { globalOptions.composite = it.toBoolean() }
        get("delay")?.let { runWindowOptions.delay = it.toInt() }
        get("print")?.let { runWindowOptions.print = true }
        get("rendering")?.let { globalOptions.renderQuality = it.toBoolean() }
        get("runs")?.let {
            runWindowOptions.runs = it.toInt()
            demo.createRunWindow(runWindowOptions)
        }
        get("screen")?.let { globalControls.selectedScreenIndex = it.toInt() }
        get("texture")?.let { globalOptions.texture = it.toBoolean() }
        get("verbose")?.let { demo.isVerbose = true }
        get("zoom")?.let { runWindowOptions.zoom = true }
    }
}

class CommandLineParameters(private val args: Array<String>) : ProgramParameters()
{
    override fun get(key: String): String? {
        val prefix = "-$key"
        for (arg in args) {
            if (arg.startsWith(prefix)) {
                when {
                    arg.length == prefix.length -> return ""
                    arg[prefix.length] == '=' -> return arg.substring(prefix.length + 1)
                }
            }
        }
        return null
    }
}

class AppletParameters(private val applet: Applet) : ProgramParameters()
{
    override fun get(key: String): String? {
        return applet.getParameter(key)
    }
}
