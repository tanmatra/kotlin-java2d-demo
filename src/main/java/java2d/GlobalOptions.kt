package java2d

interface GlobalOptions
{
    var isDefaultPrinter: Boolean
    var isCustomControlThread: Boolean
    var isVerbose: Boolean

    class Basic : GlobalOptions
    {
        override var isDefaultPrinter: Boolean = true
        override var isCustomControlThread: Boolean = false
        override var isVerbose: Boolean = false
    }
}
