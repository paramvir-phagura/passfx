package net.upm.model

enum class Language {
    ENGLISH {
        override val code = "en"
    },
    CZECH {
        override val code = "cs"
    },
    GERMAN {
        override val code = "de"
    },
    GREEK {
        override val code = "el"
    },
    SPANISH {
        override val code = "es"
    },
    FRENCH {
        override val code = "fr"
    },
    ITALIAN {
        override val code = "it"
    },
    DUTCH {
        override val code = "nl"
    },
    POLISH {
        override val code = "cs"
    };

    abstract val code: String

    override fun toString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}