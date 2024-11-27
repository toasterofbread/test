package dev.toastbits.lifelog.core.specification.model.entity

import dev.toastbits.lifelog.core.specification.model.UserContent

sealed interface LogDisplayText {
    data class OfString(val string: String): LogDisplayText
    data class OfUserContent(val userContent: UserContent): LogDisplayText
}