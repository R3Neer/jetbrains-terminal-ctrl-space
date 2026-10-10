package com.github.thewoolleyman.jetbrains.terminalkittykeys

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.terminal.frontend.view.TerminalView
import com.jediterm.terminal.ui.JediTermWidget
import java.awt.Component
import java.awt.KeyboardFocusManager
import javax.swing.SwingUtilities

/** Windows ConPTY compatibility action: the only bytes emitted describe Space+CONTROL.
 * No selected-tab fallback: a failed Reworked write must never reach another PTY.
 */
class SendControlSpaceAction(
    private val focusOwner: () -> Component? = { KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner },
    private val enabled: () -> Boolean = { CtrlSpaceSettings.instance.state.enabled && SystemInfo.isWindows },
    private val diagnostics: () -> Boolean = { CtrlSpaceSettings.instance.state.diagnostics },
    private val targetResolver: (AnActionEvent, Component?) -> ((String) -> Unit)? = ::focusedTarget,
) : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.EDT
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = enabled() && targetResolver(e, focusOwner()) != null
    }
    override fun actionPerformed(e: AnActionEvent) {
        if (!enabled()) return
        val send = targetResolver(e, focusOwner()) ?: return
        try {
            // Only press: AnAction does not receive physical keyup. No fake release.
            send("\u001b[32;57;32;1;8;1_")
            if (diagnostics()) LOG.info("CtrlSpace: focused PTY write, Win32 SPACE+CONTROL press")
        } catch (ex: Exception) { LOG.warn("CtrlSpace: focused PTY write failed; no fallback", ex) }
    }
    companion object { private val LOG = Logger.getInstance(SendControlSpaceAction::class.java) }
}

internal fun focusedTarget(e: AnActionEvent, focus: Component?): ((String) -> Unit)? {
    if (focus == null) return null
    val reworked = e.getData(TerminalView.DATA_KEY)
    if (reworked != null) {
        val input = reworked.preferredFocusableComponent
        return if (focus === input || SwingUtilities.isDescendingFrom(focus, input)) ({ text -> reworked.sendText(text) }) else null
    }
    // Resolve the widget from actual focused component, including split terminals.
    var ancestor: Component? = focus
    while (ancestor != null) {
        if (ancestor is JediTermWidget) {
            val widget = ancestor
            if (focus !== widget.terminalPanel && !SwingUtilities.isDescendingFrom(focus, widget.terminalPanel)) return null
            val connector = widget.ttyConnector ?: return null
            return { text -> connector.write(text) }
        }
        ancestor = ancestor.parent
    }
    return null
}
