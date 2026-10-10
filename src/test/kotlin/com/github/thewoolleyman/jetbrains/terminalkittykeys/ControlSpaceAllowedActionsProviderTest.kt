package com.github.thewoolleyman.jetbrains.terminalkittykeys

import org.junit.Assert.assertEquals
import org.junit.Test

class ControlSpaceAllowedActionsProviderTest {

    @Test
    fun `returns correct action ID`() {
        val provider = ControlSpaceAllowedActionsProvider()
        val actionIds = provider.getActionIds()
        assertEquals("TerminalCtrlSpace.Send", actionIds.first())
    }

    @Test
    fun `returns exactly one action ID`() {
        val provider = ControlSpaceAllowedActionsProvider()
        val actionIds = provider.getActionIds()
        assertEquals(1, actionIds.size)
    }
}
