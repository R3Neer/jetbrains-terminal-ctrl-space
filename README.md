# JetBrains Terminal Ctrl+Space

Fixes Ctrl+Space in focused local Windows ConPTY terminals in JetBrains IDEs.
It sends Space + CONTROL to Nushell, preserving your existing shell bindings.
Both Classic and Reworked terminals are supported; editor shortcuts stay outside
the plugin's scope. It sends a press event and does not synthesize a key release.

## Install

Download the plugin ZIP from Releases. In your IDE, open **Settings → Plugins →
gear → Install Plugin from Disk**, select the ZIP, and restart. Then enable
**Settings → Terminal Ctrl+Space (ConPTY) → Enable Ctrl+Space for local Windows
ConPTY terminals**. Install once per IDE, not per project.

Supports JetBrains platform builds 253–262. Enable the profile only for local
Windows terminals, not SSH or WSL. Check with `keybindings listen` in Nushell:
Ctrl+Space should report Space with CONTROL. Your original shell bindings then
handle the key. If IDE completion captures it, adjust that IDE action's shortcut
in Keymap; keep Nushell bindings unchanged.

To revert, disable the profile or uninstall the plugin and restart.

Based on MIT-licensed Terminal Kitty Keys; attribution is retained in NOTICE.txt.
