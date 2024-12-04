package dev.toastbits.lifelog.core.specification.model.string

data class Locale(
    val language: String,
    val region: String?
) {
    override fun equals(other: Any?): Boolean =
        other is Locale
            && language.equals(other.language, ignoreCase = true)
            && region.equals(other.region, ignoreCase = true)

    override fun hashCode(): Int {
        var result = language.lowercase().hashCode()
        result = 31 * result + (region?.lowercase()?.hashCode() ?: 0)
        return result
    }

    companion object {
        val DEFAULT: Locale = Locale("en", "GB")

        fun parse(stringLocale: String): Locale {
            val split: List<String> = stringLocale.split('-')
            if (split.size == 1) {
                return Locale(split[0], null)
            }
            else if (split.size == 2) {
                return Locale(split[0], split[1])
            }
            else {
                throw IllegalArgumentException("Invalid split size of string '$stringLocale' (${split.size})")
            }
        }
    }
}
