package com.passfx.util

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginElement
import org.apache.logging.log4j.core.config.plugins.PluginFactory
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required
import org.apache.logging.log4j.core.layout.PatternLayout
import org.fusesource.jansi.Ansi
import java.io.Serializable

/**
 *
 * @author Paramvir Phagura (paramvir_phagura@outlook.com)
 */
@Plugin(name = "ColoredConsoleAppender", category = "Core", elementType = "appender", printObject = true)
class ColoredConsoleAppender(
    name: String,
    filter: Filter?,
    layout: Layout<out Serializable>,
    ignoreExceptions: Boolean
) : AbstractAppender(name, filter, layout, ignoreExceptions, null) {
    override fun append(e: LogEvent) {
        var className = e.source.className
        if (className.contains('$'))
            className = e.source.className.substringBefore('$')

        when (e.level) {
            Level.INFO -> println(Ansi.ansi().a("$className - ").fgGreen().a(e.message.formattedMessage).fgDefault())
            Level.WARN -> println(Ansi.ansi().a("$className - ").fgYellow().a(e.message.formattedMessage).fgDefault())
            Level.ERROR -> println(Ansi.ansi().a("$className - ").fgRed().a(e.message.formattedMessage).fgDefault())
            Level.DEBUG -> println(Ansi.ansi().a("$className - ${e.message.formattedMessage}"))
            // String(layout.toByteArray(e))
        }
    }

    companion object {
        init {
            //AnsiConsole.systemInstall()
        }

        @JvmStatic
        @PluginFactory
        fun createAppender(
            @PluginAttribute("name") @Required name: String,
            @PluginElement("Layout") layout: Layout<out Serializable>?,
            @PluginElement("Filter") filter: Filter?
        ): ColoredConsoleAppender =
            ColoredConsoleAppender(name, filter, layout ?: PatternLayout.createDefaultLayout(), true)
    }
}