package net.upm.view

import net.upm.model.Account

enum class AccountSort
{
    AtoZ
    {
        override val text = "A - Z"
        override val comparator = Comparator<Account> { a1, a2 ->
            if (a1.name.value[0] < a2.name.value[0])
                return@Comparator -1
            else if (a1.name.value[0] > a2.name.value[0])
                return@Comparator 1
            return@Comparator 0
        }
    },
    ZtoA
    {
        override val text = "Z - A"
        override val comparator: Comparator<Account> = AtoZ.comparator.reversed()
    };

    abstract val text: String

    abstract val comparator: Comparator<Account>

    override fun toString() = text
}