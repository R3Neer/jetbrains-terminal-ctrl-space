package com.github.thewoolleyman.jetbrains.terminalkittykeys

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.terminal.frontend.view.TerminalView
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test
import java.awt.Component
import com.jediterm.terminal.ui.JediTermWidget
import com.jediterm.terminal.ui.TerminalPanel
import com.jediterm.terminal.TtyConnector
import javax.swing.JPanel

class SendControlSpaceActionTest {
    private fun event(view: TerminalView? = null): AnActionEvent = AnActionEvent(
        DataContext { id -> if (id == TerminalView.DATA_KEY.name) view else null }, Presentation(),
        ActionPlaces.UNKNOWN, ActionUiKind.NONE, null, 0, mockk<ActionManager>(relaxed = true),
    )
    @Test fun `no terminal means disabled`() {
        val e=event(); val action=SendControlSpaceAction({null},{true},{false},{_,_->null}); action.update(e); assertFalse(e.presentation.isEnabled)
    }
    @Test fun `disabled profile never sends`() {
        var calls=0; val e=event(); val action=SendControlSpaceAction({null},{false},{false},{_,_ -> {calls++}})
        action.update(e); action.actionPerformed(e); assertFalse(e.presentation.isEnabled); assertEquals(0,calls)
    }
    @Test fun `one invocation sends one press without fabricated release`() {
        val writes=mutableListOf<String>(); val action=SendControlSpaceAction({null},{true},{false},{_,_ -> {s -> writes.add(s)}})
        action.actionPerformed(event()); assertEquals(listOf("\u001b[32;57;32;1;8;1_"),writes)
    }
    @Test fun `write failure does not resolve another terminal`() {
        var resolves=0; val action=SendControlSpaceAction({null},{true},{false},{_,_ -> resolves++; {_:String -> throw RuntimeException("fail")}})
        action.actionPerformed(event()); assertEquals(1,resolves)
    }
    @Test fun `Reworked input focus resolves correct view`() {
        val input=JPanel(); val focus=JPanel(); input.add(focus)
        val view=mockk<TerminalView>(); every {view.preferredFocusableComponent} returns input
        every {view.sendText(any())} just runs
        focusedTarget(event(view),focus)!!.invoke("test")
        verify(exactly=1) {view.sendText("test")}
    }
    @Test fun `Reworked view present with editor focus is rejected`() {
        val view=mockk<TerminalView>(); every {view.preferredFocusableComponent} returns JPanel()
        assertNull(focusedTarget(event(view),JPanel())); verify(exactly=0) {view.sendText(any())}
    }
    @Test fun `no focused component is rejected`() { assertNull(focusedTarget(event(),null)) }
    @Test fun `editor without view is rejected`() { assertNull(focusedTarget(event(),JPanel())) }
    @Test fun `Classic uses focused widget connector without selected tab fallback`() {
        val focus=mockk<Component>(); val panel=mockk<TerminalPanel>(); val widget=mockk<JediTermWidget>(); val connector=mockk<TtyConnector>()
        every {focus.parent} returns panel; every {panel.parent} returns widget
        every {widget.terminalPanel} returns panel; every {widget.ttyConnector} returns connector
        every {connector.write(any<String>())} just runs
        focusedTarget(event(),focus)!!.invoke("\u001b[32;57;32;1;8;1_")
        verify(exactly=1) {connector.write("\u001b[32;57;32;1;8;1_")}
    }
    @Test fun `Classic search field in widget is rejected`() {
        val focus=mockk<Component>(); val widget=mockk<JediTermWidget>(); val panel=mockk<TerminalPanel>()
        every {focus.parent} returns widget; every {widget.parent} returns null; every {widget.terminalPanel} returns panel
        assertNull(focusedTarget(event(),focus))
    }
    @Test fun `profile state is reversible`() {
        val settings=CtrlSpaceSettings(); assertFalse(settings.state.enabled)
        settings.loadState(CtrlSpaceSettings.Options(true,true)); assertTrue(settings.state.enabled)
        settings.loadState(CtrlSpaceSettings.Options(false,false)); assertFalse(settings.state.enabled)
    }
}
