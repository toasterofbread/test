package dev.toastbits.lifelog.core.specification.model.string

import dev.toastbits.lifelog.core.specification.extension.SpecificationExtension

interface StringId {
    suspend fun getString(locale: Locale, extension: SpecificationExtension?): String
}
