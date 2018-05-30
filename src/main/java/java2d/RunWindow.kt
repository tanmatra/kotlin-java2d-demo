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
package java2d

import java.awt.Color.BLACK
import java.awt.Color.GREEN
import java.awt.Color.RED
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.Date
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.border.BevelBorder
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder

/**
 * A separate window for running the Java2Demo.  Go from tab to tab or demo to
 * demo.
 */
class RunWindow : JPanel(), Runnable, ActionListener
{
    private var delayTextField: JTextField? = null
    private var runsTextField: JTextField? = null
    private var thread: Thread? = null
    private val pb: JProgressBar
    private var dg: DemoGroup? = null
    private var dp: DemoPanel? = null

    init {
        layout = GridBagLayout()
        val eb = EmptyBorder(5, 5, 5, 5)
        border = CompoundBorder(eb, BevelBorder(BevelBorder.LOWERED))

        val font = Font("serif", Font.PLAIN, 10)

        runB = JButton("Run")
        runB.background = GREEN
        runB.addActionListener(this)
        runB.minimumSize = Dimension(70, 30)
        Java2Demo.addToGridBag(this, runB, 0, 0, 1, 1, 0.0, 0.0)

        pb = JProgressBar()
        pb.preferredSize = Dimension(100, 30)
        pb.minimum = 0
        Java2Demo.addToGridBag(this, pb, 1, 0, 2, 1, 1.0, 0.0)

        val p1 = JPanel(GridLayout(2, 2))
        var p2 = JPanel()
        var l = JLabel("Runs:")
        l.font = font
        l.foreground = BLACK
        p2.add(l)
        runsTextField = JTextField(numRuns.toString())
        p2.add(runsTextField)
        runsTextField!!.preferredSize = Dimension(30, 20)
        runsTextField!!.addActionListener(this)
        p1.add(p2)
        p2 = JPanel()
        l = JLabel("Delay:")
        l.font = font
        l.foreground = BLACK
        p2.add(l)
        delayTextField = JTextField(delay.toString())
        p2.add(delayTextField)
        delayTextField!!.preferredSize = Dimension(30, 20)
        delayTextField!!.addActionListener(this)
        p1.add(p2)

        zoomCB.horizontalAlignment = SwingConstants.CENTER
        zoomCB.font = font
        printCB.font = font
        p1.add(zoomCB)
        p1.add(printCB)
        printCB.addActionListener(this)
        Java2Demo.addToGridBag(this, p1, 0, 1, 3, 1, 1.0, 1.0)
    }

    override fun actionPerformed(e: ActionEvent) {
        if (e.source == printCB) {
            Java2Demo.printCB.isSelected = printCB.isSelected
        } else if (e.source == delayTextField) {
            delay = Integer.parseInt(delayTextField!!.text.trim { it <= ' ' })
        } else if (e.source == runsTextField) {
            numRuns = Integer.parseInt(runsTextField!!.text.trim { it <= ' ' })
        } else if ("Run" == e.actionCommand) {
            doRunAction()
        } else if ("Stop" == e.actionCommand) {
            stop()
        }
    }

    fun doRunAction() {
        runB.text = "Stop"
        runB.background = RED
        start()
    }

    fun start() {
        thread = Thread(this)
        thread!!.priority = Thread.NORM_PRIORITY + 1
        thread!!.name = "RunWindow"
        thread!!.start()
    }

    @Synchronized
    fun stop() {
        if (thread != null) {
            thread!!.interrupt()
        }
        thread = null
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        (this as java.lang.Object).notifyAll()
    }

    fun sleepPerTab() {
        var j = 0
        while (j < delay + 1 && thread != null) {
            var k = 0
            while (k < 10 && thread != null) {
                try {
                    Thread.sleep(100)
                } catch (e: Exception) {
                }

                k++
            }
            val pbUpdateRunnable = Runnable { pb.value = pb.value + 1 }
            SwingUtilities.invokeLater(pbUpdateRunnable)
            j++
        }
    }

    private fun printDemo(dg: DemoGroup?) {
        val printDemoRunnable = Runnable {
            if (!Java2Demo.controls.toolBarCB.isSelected) {
                Java2Demo.controls.toolBarCB.isSelected = true
                dg!!.invalidate()
            }
            for (comp in dg!!.panel.components) {
                val dp = comp as DemoPanel
                if (dp.tools != null) {
                    if (dp.surface.animating != null) {
                        if (dp.surface.animating.running()) {
                            dp.tools.startStopButton!!.doClick()
                        }
                    }
                    dp.tools.printButton.doClick()
                }
            }
        }
        invokeAndWait(printDemoRunnable)
    }

    override fun run() {

        println(
            "\nJava2D Demo RunWindow : " + numRuns + " Runs, "
                    + delay + " second delay between tabs\n" + "java version: " + System.getProperty("java.version") + "\n" + System.getProperty(
                "os.name"
                                                                                                                                                ) + " " + System.getProperty(
                "os.version"
                                                                                                                                                                            ) + "\n"
               )
        val r = Runtime.getRuntime()

        var runNum = 0
        while (runNum < numRuns && thread != null) {

            val d = Date()
            print("#" + runNum + " " + d.toString() + ", ")
            r.gc()
            val freeMemory = r.freeMemory().toFloat()
            val totalMemory = r.totalMemory().toFloat()
            println(((totalMemory - freeMemory) / 1024).toString() + "K used")

            var i = 0
            while (i < Java2Demo.tabbedPane.tabCount && thread != null) {

                val mainTabIndex = i
                val initDemoRunnable = Runnable {
                    pb.value = 0
                    pb.maximum = delay
                    if (mainTabIndex != 0) {
                        dg = Java2Demo.group[mainTabIndex - 1]
                        dg!!.invalidate()
                    }
                    Java2Demo.tabbedPane.selectedIndex = mainTabIndex
                }
                invokeAndWait(initDemoRunnable)

                if (i != 0 && (zoomCB.isSelected || buffersFlag)) {
                    dp = dg!!.panel.getComponent(0) as DemoPanel
                    if (dg!!.tabbedPane == null && dp!!.surface != null) {
                        val mouseClickedRunnable = Runnable { dg!!.mouseClicked(dp!!.surface) }
                        invokeAndWait(mouseClickedRunnable)
                    }
                    var j = 1
                    while (j < dg!!.tabbedPane!!.tabCount && thread != null) {

                        val subTabIndex = j

                        val initPanelRunnable = Runnable {
                            pb.value = 0
                            pb.maximum = delay
                            dg!!.tabbedPane!!.selectedIndex = subTabIndex
                        }
                        invokeAndWait(initPanelRunnable)

                        val p = dg!!.panel
                        if (buffersFlag && p.componentCount == 1) {
                            dp = p.getComponent(0) as DemoPanel
                            if (dp!!.surface.animating != null) {
                                dp!!.surface.animating.stop()
                            }
                            var k = bufBeg
                            while (k <= bufEnd && thread != null) {

                                val cloneIndex = k
                                val cloneRunnable = Runnable {
                                    dp!!.tools.cloneButton!!.doClick()
                                    val n = p.componentCount
                                    val clone = p.getComponent(n - 1) as DemoPanel
                                    if (clone.surface.animating != null) {
                                        clone.surface.animating.stop()
                                    }
                                    clone.tools.issueRepaint = true
                                    clone.tools.screenCombo.selectedIndex = cloneIndex
                                    clone.tools.issueRepaint = false
                                }
                                invokeAndWait(cloneRunnable)
                                k++
                            }
                        }
                        if (printCB.isSelected) {
                            printDemo(dg)
                        }
                        sleepPerTab()
                        j++
                    }
                } else if (i != 0 && printCB.isSelected) {
                    printDemo(dg)
                    sleepPerTab()
                } else {
                    sleepPerTab()
                }
                i++
            }
            if (runNum + 1 == numRuns) {
                println("Finished.")
                if (exit && thread != null) {
                    println("System.exit(0).")
                    System.exit(0)
                }
            }
            runNum++
        }
        val resetRunnable = Runnable {
            runB.text = "Run"
            runB.background = GREEN
            pb.value = 0
        }
        invokeAndWait(resetRunnable)

        thread = null
        dg = null
        dp = null
    }

    companion object
    {
        lateinit var runB: JButton
        var delay = 10
        var numRuns = 20
        var exit: Boolean = false
        var zoomCB = JCheckBox("Zoom")
        var printCB = JCheckBox("Print")
        var buffersFlag: Boolean = false
        var bufBeg: Int = 0
        var bufEnd: Int = 0

        private fun invokeAndWait(run: Runnable) {
            try {
                SwingUtilities.invokeAndWait(run)
            } catch (e: Exception) {
                Logger.getLogger(RunWindow::class.java.name).log(
                    Level.SEVERE,
                    "ERROR in invokeAndWait", e
                                                                )
            }
        }
    }
}
