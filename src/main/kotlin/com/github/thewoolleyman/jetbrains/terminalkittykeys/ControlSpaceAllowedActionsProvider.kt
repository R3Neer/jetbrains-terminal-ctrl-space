package com.github.thewoolleyman.jetbrains.terminalkittykeys

import com.intellij.terminal.frontend.view.TerminalAllowedActionsProvider

/**
 * Registers the Ctrl+Space action so it is not blocked by the terminal's
 * "Override IDE shortcuts" feature in the Reworked Terminal.
 */
class ControlSpaceAllowedActionsProvider : TerminalAllowedActionsProvider {
    override fun getActionIds(): List<String> = listOf("TerminalCtrlSpace.Send")
}
