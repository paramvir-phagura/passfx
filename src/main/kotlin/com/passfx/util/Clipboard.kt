package com.passfx.util

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

object Clipboard {
    private val clipboard: java.awt.datatransfer.Clipboard

    init {
        val defaultToolkit = Toolkit.getDefaultToolkit()
        clipboard = defaultToolkit.systemClipboard
    }

    fun copy(text: String) {
        clipboard.setContents(StringSelection(text), null)
    }

    fun paste(): String? {
        val flavor = DataFlavor.stringFlavor

        if (clipboard.isDataFlavorAvailable(flavor)) {
            return clipboard.getData(flavor) as String
        }
        return null
    }
}