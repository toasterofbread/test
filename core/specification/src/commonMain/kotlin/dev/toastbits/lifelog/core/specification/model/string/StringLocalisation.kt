package dev.toastbits.lifelog.core.specification.model.string

import dev.toastbits.composekit.util.model.Locale
import dev.toastbits.lifelog.core.specification.extension.SpecificationExtension

abstract class StringLocalisation<E: SpecificationExtension, T: StringId>(val locale: Locale) {
    @Suppress("UNCHECKED_CAST")
    suspend fun getString(id: T, extension: SpecificationExtension?): String =
        getStringImpl(id, (extension ?: SpecificationExtension.EMPTY) as E)

    protected abstract suspend fun getStringImpl(id: T, extension: E): String
}
