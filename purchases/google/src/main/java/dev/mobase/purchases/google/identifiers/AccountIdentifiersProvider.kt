package dev.mobase.purchases.google.identifiers

fun interface AccountIdentifiersProvider {
    operator fun invoke(): AccountIdentifiers?

    companion object {
        fun create(
            userId: String?,
            secretKey: String?,
            iv: String?
        ): AccountIdentifiersProvider {
            return DefaultAccountIdentifiersProvider(userId, secretKey, iv)
        }
    }
}