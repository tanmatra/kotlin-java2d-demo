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
import java.awt.Dimension
import java.awt.Insets
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JApplet
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar

/**
 * A demo that shows Java2D features.
 *
 * Parameters that can be used in the Java2Demo.html file inside
 * the applet tag to customize demo runs :
 * <param name="runs" value="10"></param>
 * <param name="delay" value="10"></param>
 * <param name="ccthread" value=" "></param>
 * <param name="screen" value="5"></param>
 * <param name="antialias" value="true"></param>
 * <param name="rendering" value="true"></param>
 * <param name="texture" value="true"></param>
 * <param name="composite" value="true"></param>
 * <param name="verbose" value=" "></param>
 * <param name="buffers" value="3,10"></param>
 * <param name="verbose" value=" "></param>
 * <param name="zoom" value=" "></param>
 *
 * @author Brian Lichtenwalter  (Framework, Intro, demos)
 * @author Jim Graham           (demos)
 * @author Alexander Kouznetsov (code beautification)
 */
class Java2DemoApplet : JApplet()
{
    private lateinit var demo: Java2Demo

    override fun init() {
        val panel = JPanel()
        contentPane.add(panel, BorderLayout.CENTER)
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)

        val progressPanel = object : JPanel() {
            override fun getInsets() = Insets(40, 30, 20, 30)
        }
        progressPanel.layout = BoxLayout(progressPanel, BoxLayout.Y_AXIS)

        panel.add(Box.createGlue())
        panel.add(progressPanel)
        panel.add(Box.createGlue())

        progressPanel.add(Box.createGlue())

        val compMaxSize = Dimension(400, 20)
        val progressLabel = JLabel("Loading, please wait...").apply {
            maximumSize = compMaxSize
        }
        progressPanel.add(progressLabel)
        progressPanel.add(Box.createRigidArea(Dimension(1, 20)))

        val progressBar = JProgressBar().apply {
            isStringPainted = true
            alignmentX = Component.CENTER_ALIGNMENT
            maximumSize = compMaxSize
            minimum = 0
            value = 0
        }
        progressLabel.labelFor = progressBar
        progressPanel.add(progressBar)
        progressPanel.add(Box.createGlue())
        progressPanel.add(Box.createGlue())

        panel.preferredSize = contentPane.size
        contentPane.add(panel, BorderLayout.CENTER)
        validate()
        isVisible = true

        val demo = Java2Demo(progressLabel, progressBar, applet = true).also { this.demo = it }
        contentPane.remove(panel)
        contentPane.layout = BorderLayout()
        contentPane.add(demo, BorderLayout.CENTER)

        val globalControls = demo.globalControls
        val globalOptions = globalControls.options
        getParameter("delay")?.let { RunWindow.delay = it.toInt() }
        getParameter("ccthread")?.let { Java2Demo.ccthreadCB.isSelected = true }
        getParameter("screen")?.let { globalControls.selectedScreenIndex = it.toInt() }
        getParameter("antialias")?.let { globalOptions.antialiasing = it.toBoolean() }
        getParameter("rendering")?.let { globalOptions.renderQuality = it.toBoolean() }
        getParameter("texture")?.let { globalOptions.texture = it.toBoolean() }
        getParameter("composite")?.let { globalOptions.composite = it.toBoolean() }
        getParameter("verbose")?.let { demo.isVerbose = true }
        getParameter("columns")?.let { DemoGroup.columns = it.toInt() }
        getParameter("buffers")?.let {
            // usage -buffers=3,10
            RunWindow.buffersFlag = true
            val (s1, s2) = it.split(',')
            RunWindow.bufBeg = s1.toInt()
            RunWindow.bufEnd = s2.toInt()
        }
        getParameter("zoom")?.let { RunWindow.zoomCheckBox.isSelected = true }
        getParameter("runs")?.let {
            RunWindow.numRuns = it.toInt()
            demo.createRunWindow()
            RunWindow.runButton.doClick()
        }
        validate()
        repaint()
        requestDefaultFocus()
    }

    private fun requestDefaultFocus() {
        focusCycleRootAncestor?.let { nearestRoot ->
            nearestRoot.focusTraversalPolicy?.getDefaultComponent(nearestRoot)?.requestFocus()
        }
    }

    override fun start() {
        demo.start()
    }

    override fun stop() {
        demo.stop()
    }
}
