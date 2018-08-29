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

import java2d.Java2Demo.GroupInfo
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.LayoutManager
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JCheckBoxMenuItem
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.border.BevelBorder
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.SoftBevelBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * DemoGroup handles multiple demos inside of a panel.  Demos are loaded
 * from the demos[][] string as listed in Java2Demo.java.
 * Demo groups can be loaded individually, for example :
 * java DemoGroup Fonts
 * Loads all the demos found in the demos/Fonts directory.
 */
class DemoGroup internal constructor(
    private val java2Demo: Java2Demo? = null,
    groupInfo: GroupInfo
) : JPanel(), ChangeListener, ActionListener
{
    private val groupName = groupInfo.groupName
    lateinit var clonePanels: Array<JPanel>
    var tabbedPane: JTabbedPane? = null
    private var index: Int = 0

    val panel: JPanel
        get() = tabbedPane.let { tabbedPane ->
            if (tabbedPane != null) {
                tabbedPane.selectedComponent as JPanel
            } else {
                getComponent(0) as JPanel
            }
        }

    init {
        layout = BorderLayout()

        val demos = groupInfo.demos
        // If there are an odd number of demos, use GridBagLayout.
        val panelLayout: LayoutManager = if (demos.size % 2 == 1) GridBagLayout() else GridLayout(0, 2)
        val p = JPanel(panelLayout).apply { border = PANEL_BORDER }

        val mouseListener = object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                this@DemoGroup.mouseClicked(e.component)
            }
        }

        // For each demo in the group, prepare a DemoPanel.
        demos.forEachIndexed { i, demo ->
            val className = "java2d.demos.$groupName.$demo"
            val demoPanel = DemoPanel(className)
            demoPanel.setDemoBorder(p)
            demoPanel.surface?.run {
                addMouseListener(mouseListener)
                monitor = java2Demo?.performanceMonitor != null
            }
            if (panelLayout is GridBagLayout) {
                val x = p.componentCount % 2
                val y = p.componentCount / 2
                val w = if (i == demos.lastIndex) 2 else 1
                p.addToGridBag(demoPanel, x, y, w, 1, 1.0, 1.0)
            } else {
                p.add(demoPanel)
            }
        }

        add(p)
    }

    fun mouseClicked(component: Component) {
        var className = component.toString()

        if (tabbedPane == null) {
            shutDown(panel)
            val p = JPanel(BorderLayout()).apply { border = PANEL_BORDER }

            tabbedPane = JTabbedPane().apply {
                font = FONT
            }

            val tmpP = getComponent(0) as JPanel
            tabbedPane!!.addTab(groupName, tmpP)

//          clonePanels = arrayOfNulls(tmpP.componentCount)
            clonePanels = Array(tmpP.componentCount) { i ->
                JPanel(BorderLayout())
            }
            for (i in clonePanels.indices) {
//              clonePanels[i] = JPanel(BorderLayout())
                val dp = tmpP.getComponent(i) as DemoPanel
                val c = dp.clone()
                c.setDemoBorder(clonePanels[i])
                if (c.surface != null) {
                    c.surface.monitor = java2Demo?.performanceMonitor != null
                    val cloneImg = DemoImages.getImage("clone.gif", this)
                    val tools = c.tools!!
                    tools.cloneButton = tools.addTool(cloneImg, "Clone the Surface", this)
                    val d = tools.toolbar.preferredSize
                    tools.toolbar.preferredSize = Dimension(d.width + 27, d.height)
                    java2Demo?.backgroundColor?.let { backgroundColor ->
                        c.surface.background = backgroundColor
                    }
                }
                clonePanels[i].add(c)
                val s = dp.className.substring(dp.className.indexOf('.') + 1)
                tabbedPane!!.addTab(s, clonePanels[i])
            }
            p.add(tabbedPane)
            remove(tmpP)
            add(p)

            tabbedPane!!.addChangeListener(this)
            revalidate()
        }

        className = className.substring(0, className.indexOf('['))

        for (i in 0 until tabbedPane!!.tabCount) {
            val s1 = className.substring(className.indexOf('.') + 1)
            if (tabbedPane!!.getTitleAt(i) == s1) {
                tabbedPane!!.selectedIndex = i
                break
            }
        }

        revalidate()
    }

    override fun actionPerformed(e: ActionEvent) {
        val b = e.source as JButton
        if (b.toolTipText.startsWith("Clone")) {
            cloneDemo()
        } else {
            removeClone(b.parent.parent.parent.parent)
        }
    }

    override fun stateChanged(e: ChangeEvent) {
        shutDown(tabbedPane!!.getComponentAt(index) as JPanel)
        index = tabbedPane!!.selectedIndex
        setup(false)
    }

    fun setup(issueRepaint: Boolean) {
        val panel = panel

        // Let PerformanceMonitor know which demos are running
        java2Demo?.performanceMonitor?.run {
            surface.panel = panel
            surface.setSurfaceState()
        }

        val controls = java2Demo?.globalControls
        // .. tools check against global controls settings ..
        // .. & start demo & custom control thread if need be ..
        for (i in 0 until panel.componentCount) {
            val demoPanel = panel.getComponent(i) as DemoPanel
            if (demoPanel.surface != null && controls != null) {
                val tools = demoPanel.tools!!
                tools.isVisible = isValid
                tools.issueRepaint = issueRepaint
                val buttons = arrayOf(tools.toggleButton, tools.antialiasButton, tools.renderButton,
                                      tools.textureButton, tools.compositeB)
                val checkBoxes = arrayOf(controls.toolBarCheckBox, controls.antialiasingCheckBox,
                                         controls.renderCheckBox, controls.textureCheckBox, controls.compositeCheckBox)
                for (j in buttons.indices) {
                    if (controls.itemEventSource != null && controls.itemEventSource == checkBoxes[j]) {
                        if (buttons[j].isSelected != checkBoxes[j].isSelected) {
                            buttons[j].doClick()
                        }
                    } else if (controls.itemEventSource == null) {
                        if (buttons[j].isSelected != checkBoxes[j].isSelected) {
                            buttons[j].doClick()
                        }
                    }
                }
                tools.isVisible = true
                java2Demo?.globalControls?.selectedScreenIndex?.let { globalScreenIndex ->
                    if (globalScreenIndex != tools.screenCombo.selectedIndex) {
                        tools.screenCombo.selectedIndex = globalScreenIndex
                    }
                }
                if (Java2Demo.verboseCB.isSelected) {
                    demoPanel.surface.verbose(java2Demo)
                }
                demoPanel.surface.sleepAmount = controls.slider.value.toLong()
                java2Demo?.backgroundColor?.let { backgroundColor ->
                    demoPanel.surface.background = backgroundColor
                }
                tools.issueRepaint = true
            }
            demoPanel.start()
        }
        revalidate()
    }

    fun shutDown(p: JPanel) {
        for (i in 0 until p.componentCount) {
            (p.getComponent(i) as DemoPanel).stop()
        }
        System.gc()
    }

    fun cloneDemo() {
        val panel = clonePanels[tabbedPane!!.selectedIndex - 1]
        if (panel.componentCount == 1) {
            panel.invalidate()
            panel.layout = GridLayout(0, columns, 5, 5)
            panel.revalidate()
        }
        val original = panel.getComponent(0) as DemoPanel
        val clone = original.clone()
        if (columns == 2) {
            clone.setDemoBorder(panel)
        }
        val removeImg = DemoImages.getImage("remove.gif", this)
        val tools = clone.tools!!
        tools.cloneButton = tools.addTool(removeImg, "Remove the Surface", this)
        val d = tools.toolbar.preferredSize
        tools.toolbar.preferredSize = Dimension(d.width + 27, d.height)
        java2Demo?.backgroundColor?.let { backgroundColor ->
            clone.surface?.background = backgroundColor
        }
        if (java2Demo?.globalControls != null) {
            if (tools.isExpanded != java2Demo.globalControls.toolBarCheckBox.isSelected) {
                tools.toggleButton.doClick()
            }
        }
        clone.start()
        clone.surface!!.monitor = java2Demo?.performanceMonitor != null
        panel.add(clone)
        panel.repaint()
        panel.revalidate()
    }

    fun removeClone(theClone: Component) {
        val panel = clonePanels[tabbedPane!!.selectedIndex - 1]
        if (panel.componentCount == 2) {
            val cmp = panel.getComponent(0)
            panel.removeAll()
            panel.layout = BorderLayout()
            panel.revalidate()
            panel.add(cmp)
        } else {
            panel.remove(theClone)
            val cmpCount = panel.componentCount
            for (j in 1 until cmpCount) {
                val top = if (j + 1 >= 3) 0 else 5
                val left = if ((j + 1) % 2 == 0) 0 else 5
                val eb = EmptyBorder(top, left, 5, 5)
                val sbb = SoftBevelBorder(BevelBorder.RAISED)
                val p = panel.getComponent(j) as JPanel
                p.border = CompoundBorder(eb, sbb)
            }
        }
        panel.repaint()
        panel.revalidate()
    }

    companion object
    {
        var columns = 2
        private val FONT = Font(Font.SERIF, Font.PLAIN, 10)
        private val PANEL_BORDER = CompoundBorder(EmptyBorder(5, 5, 5, 5), BevelBorder(BevelBorder.LOWERED))

        @JvmStatic
        fun main(args: Array<String>) {
            val groupName = args.getOrNull(0) ?: return
            val groupDescriptor = Java2Demo.demos.find { it.groupName.equals(groupName, ignoreCase = true) } ?: return
            val group = DemoGroup(null, groupDescriptor)
            JFrame("Java2D Demo - DemoGroup").apply {
                addWindowListener(object : WindowAdapter() {
                    override fun windowClosing(e: WindowEvent?) {
                        System.exit(0)
                    }
                    override fun windowDeiconified(e: WindowEvent?) {
                        group.setup(false)
                    }
                    override fun windowIconified(e: WindowEvent?) {
                        group.shutDown(group.panel)
                    }
                })
                contentPane.add(group, BorderLayout.CENTER)
                pack()
                val FRAME_WIDTH = 620
                val FRAME_HEIGHT = 530
                setSize(FRAME_WIDTH, FRAME_HEIGHT)
                setLocationRelativeTo(null)  // centers f on screen
                isVisible = true
            }
            for (arg in args) {
                if (arg.startsWith("-ccthread")) {
                    Java2Demo.ccthreadCB = JCheckBoxMenuItem("CCThread", true)
                }
            }
            group.setup(false)
        }
    }
}
