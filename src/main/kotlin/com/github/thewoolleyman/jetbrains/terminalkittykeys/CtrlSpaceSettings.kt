package com.github.thewoolleyman.jetbrains.terminalkittykeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.options.Configurable
import javax.swing.JCheckBox
import javax.swing.JPanel
import java.awt.GridLayout

@Service(Service.Level.APP)
@State(name = "TerminalCtrlSpace", storages = [Storage("terminal-ctrl-space.xml")])
class CtrlSpaceSettings : PersistentStateComponent<CtrlSpaceSettings.Options> {
    data class Options(var enabled: Boolean = false, var diagnostics: Boolean = false)
    private var options = Options()
    override fun getState() = options
    override fun loadState(state: Options) { options = state }
    companion object { val instance: CtrlSpaceSettings get() = ApplicationManager.getApplication().getService(CtrlSpaceSettings::class.java) }
}
/** Explicit ConPTY profile: the public TerminalView API exposes no raw mode 9001 state.
 * Disabled until the user selects local Windows ConPTY, never assume SSH/WSL speaks Win32.
 */
class CtrlSpaceConfigurable : Configurable {
    private val enabled = JCheckBox("Enable Ctrl+Space for local Windows ConPTY terminals")
    private val diagnostics = JCheckBox("Log Ctrl+Space routing metadata")
    override fun getDisplayName() = "Terminal Ctrl+Space (ConPTY)"
    override fun createComponent() = JPanel(GridLayout(0,1)).apply { add(enabled); add(diagnostics) }
    override fun isModified() = enabled.isSelected != CtrlSpaceSettings.instance.state.enabled || diagnostics.isSelected != CtrlSpaceSettings.instance.state.diagnostics
    override fun apply() { CtrlSpaceSettings.instance.state.enabled = enabled.isSelected; CtrlSpaceSettings.instance.state.diagnostics = diagnostics.isSelected }
    override fun reset() { enabled.isSelected = CtrlSpaceSettings.instance.state.enabled; diagnostics.isSelected = CtrlSpaceSettings.instance.state.diagnostics }
}
