package dev.toastbits.lifelog.core.specification.model.string

import dev.toastbits.lifelog.core.specification.extension.SpecificationExtension

abstract class StringLocalisations<E: SpecificationExtension, T: StringId>(vararg localisations: StringLocalisation<E, T>) {
    init {
        require(localisations.isNotEmpty())
    }

    private val localisations: Map<Locale, StringLocalisation<E, T>> =
        localisations.associateBy { it.locale }

    private val alternateLocalisations: MutableMap<Locale, StringLocalisation<E, T>> = mutableMapOf()

    fun getBestLocalisation(locale: Locale): StringLocalisation<E, T> {
        localisations[locale]?.also { return it }
        alternateLocalisations[locale]?.also { return it }

        localisations.entries
            .firstOrNull { it.key.language == locale.language }
            ?.value
            ?.also {
                alternateLocalisations[locale] = it
                return it
            }

        if (locale != Locale.DEFAULT) {
            try {
                return getBestLocalisation(Locale.DEFAULT)
            }
            catch (_: NoSuchElementException) {}
        }

        val ret: StringLocalisation<E, T> = localisations.values.first()
        alternateLocalisations[locale] = ret

        return ret
    }
}
