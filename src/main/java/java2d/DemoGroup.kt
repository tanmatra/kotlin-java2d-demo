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

import java.awt.BorderLayout
import java.awt.Component
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import kotlin.reflect.KProperty

/**
 * DemoGroup handles multiple demos inside of a panel.  Demos are loaded
 * from the demos[][] string as listed in Java2Demo.java.
 * Demo groups can be loaded individually, for example :
 * java DemoGroup Fonts
 * Loads all the demos found in the demos/Fonts directory.
 */
class DemoGroup internal constructor(
    groupInfo: GroupInfo,
    private val globalOptions: GlobalOptions,
    private val java2Demo: Java2Demo? = null
) : JPanel(BorderLayout()), ChangeListener
{
    internal constructor(groupInfo: GroupInfo, java2Demo: Java2Demo) : this(groupInfo, java2Demo, java2Demo)

    private val groupName = groupInfo.groupName
    lateinit var clonePanels: Array<JPanel>
    var tabbedPane: JTabbedPane? = null
    private var index: Int = 0

    val activePanel: JPanel
        get() = tabbedPane.let { tabbedPane ->
            if (tabbedPane != null) {
                tabbedPane.selectedComponent as JPanel
            } else {
                getComponent(0) as JPanel
            }
        }

    init {
        val classes = groupInfo.classes
        val gridPanel = JPanel(GridBagLayout()).apply { border = PANEL_BORDER }

        val mouseListener = object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                scatterDemos(event.component)
            }
        }

        // For each demo in the group, prepare a DemoPanel.
        classes.forEachIndexed { i, cls ->
            val demoPanel = DemoPanel(globalOptions, cls)
            demoPanel.setDemoBorder(gridPanel)
            demoPanel.surface?.run {
                addMouseListener(mouseListener)
                monitor = java2Demo?.performanceMonitor != null
            }
            gridPanel.add(demoPanel, createGridConstraint(gridPanel, i, classes.lastIndex))
        }

        add(gridPanel)
    }

    fun scatterDemos(eventSource: Component) {
        val tabbedPane = tabbedPane ?: run {
            shutDown(activePanel)
            val newPanel = JPanel(BorderLayout()).apply { border = PANEL_BORDER }

            val newTabbedPane = JTabbedPane().also { tabbedPane = it }

            val oldGridPanel = getComponent(0) as JPanel
            newTabbedPane.addTab(groupName, oldGridPanel)

            clonePanels = oldGridPanel.components.map { oldComponent ->
                val demoPanel = oldComponent as DemoPanel
                val clonesPanel = JPanel(BorderLayout())
                val demoPanelClone: DemoPanel = demoPanel.clone()
                demoPanelClone.setDemoBorder(clonesPanel) // fixme
                demoPanelClone.surface?.let { surface ->
                    surface.monitor = java2Demo?.performanceMonitor != null
                    demoPanelClone.tools?.let { tools ->
                        val cloneImg = DemoImages.getImage("clone.gif", this)
                        tools.cloneButton = tools.addTool(cloneImg, "Clone the Surface") {
                            cloneDemo()
                        }
                        tools.toolbar.increasePreferredWidth(27)
                        java2Demo?.backgroundColor?.let { surface.background = it }
                    }
                }
                clonesPanel.add(demoPanelClone)
                newTabbedPane.addTab(demoPanel.componentName, clonesPanel)
                clonesPanel
            }.toTypedArray()

            newPanel.add(newTabbedPane)
            remove(oldGridPanel)
            add(newPanel)

            newTabbedPane.addChangeListener(this)
            revalidate()
            newTabbedPane
        }

        val indexOfTab = tabbedPane.indexOfTab(eventSource.javaClass.simpleName)
        if (indexOfTab > 0) {
            tabbedPane.selectedIndex = indexOfTab
        }

        revalidate()
    }

    override fun stateChanged(e: ChangeEvent) {
        shutDown(tabbedPane!!.getComponentAt(index) as JPanel)
        index = tabbedPane!!.selectedIndex
        setup(false)
    }

    fun setup(issueRepaint: Boolean, sourceProperty: KProperty<*>? = null) {
        val activePanel = activePanel

        // Let PerformanceMonitor know which demos are running
        java2Demo?.performanceMonitor?.run {
            surface.panel = activePanel
            surface.setSurfaceState()
        }

        val globalControls = java2Demo?.globalControls
        // .. tools check against global controls settings ..
        // .. & start demo & custom control thread if need be ..
        activePanel.forEachComponent<DemoPanel> { demoPanel ->
            if (demoPanel.surface != null && globalControls != null) {
                val tools = demoPanel.tools!!
                tools.isVisible = isValid
                tools.issueRepaint = issueRepaint
                tools.options.copyFrom(globalControls.options, sourceProperty)
                tools.isVisible = true
                globalControls.selectedScreenIndex.let { globalScreenIndex ->
                    if (globalScreenIndex != tools.selectedScreenIndex) {
                        tools.selectedScreenIndex = globalScreenIndex
                    }
                }
                if (globalOptions.isVerbose) {
                    demoPanel.surface.verbose(java2Demo)
                }
                demoPanel.surface.sleepAmount = globalControls.sleepAmount
                java2Demo?.backgroundColor?.let { backgroundColor ->
                    demoPanel.surface.background = backgroundColor
                }
                tools.issueRepaint = true
            }
            demoPanel.start()
        }
        revalidate()
    }

    fun shutDown(panel: JPanel) {
        panel.forEachComponent<DemoPanel> { demoPanel ->
            demoPanel.stop()
        }
        System.gc()
    }

    private fun cloneDemo() {
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
        tools.cloneButton = tools.addTool(removeImg, "Remove the Surface") {
            removeClone(clone)
        }
        tools.toolbar.increasePreferredWidth(27)
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

    private fun removeClone(theClone: Component) {
        val panel = clonePanels[tabbedPane!!.selectedIndex - 1]
        if (panel.componentCount == 2) {
            val cmp = panel.getComponent(0)
            panel.removeAll()
            panel.layout = BorderLayout()
            panel.revalidate()
            panel.add(cmp)
        } else {
            panel.remove(theClone)
            for (j in 1 until panel.componentCount) {
                val top = if (j + 1 >= 3) 0 else 5
                val left = if ((j + 1) % 2 == 0) 0 else 5
                val eb = EmptyBorder(top, left, 5, 5)
                val sbb = BorderFactory.createRaisedSoftBevelBorder()
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
        private val PANEL_BORDER = CompoundBorder(EmptyBorder(5, 5, 5, 5), BorderFactory.createLoweredBevelBorder())
        private const val FRAME_WIDTH = 620
        private const val FRAME_HEIGHT = 530

        @JvmStatic
        fun main(args: Array<String>) {
            val options = GlobalOptions.Basic()
            options.isCustomControlThread = args.any { it.startsWith("-ccthread") }

            val groupName = args.getOrNull(0) ?: return
            val groupInfo = GroupInfo.findByName(groupName) ?: return
            val group = DemoGroup(groupInfo, options)
            JFrame("Java2D Demo - DemoGroup").apply {
                addWindowListener(object : WindowAdapter() {
                    override fun windowClosing(e: WindowEvent?) {
                        System.exit(0)
                    }
                    override fun windowDeiconified(e: WindowEvent?) {
                        group.setup(false)
                    }
                    override fun windowIconified(e: WindowEvent?) {
                        group.shutDown(group.activePanel)
                    }
                })
                contentPane.add(group, BorderLayout.CENTER)
                pack()
                setSize(FRAME_WIDTH, FRAME_HEIGHT)
                setLocationRelativeTo(null)  // centers f on screen
                isVisible = true
            }
            group.setup(false)
        }

        private fun createGridConstraint(panel: JPanel, index: Int, lastIndex: Int): GBC {
            val x = panel.componentCount % 2
            val y = panel.componentCount / 2
            val w = if (index == lastIndex) 2 else 1
            return GBC(x, y).span(w, 1).fill().grow()
        }
    }
}
