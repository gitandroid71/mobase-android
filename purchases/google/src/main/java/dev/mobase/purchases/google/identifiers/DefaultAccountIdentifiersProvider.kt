package dev.mobase.purchases.google.identifiers

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal class DefaultAccountIdentifiersProvider(
    private val userId: String?,
    private val secretKey: String?,
    private val iv: String?,
) : AccountIdentifiersProvider {
    override operator fun invoke(): AccountIdentifiers? {
        if (userId == null || secretKey == null || iv == null) return null

        return AccountIdentifiers(
            obfuscatedAccountId = getObfuscatedAccountId(userId),
            obfuscatedProfileId = getObfuscatedProfileId(userId, secretKey, iv)
        )
    }

    private fun getObfuscatedAccountId(accountId: String): String {
        val hashBytes = MessageDigest.getInstance("SHA-256")
            .digest(accountId.toByteArray())
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    private fun getObfuscatedProfileId(profileId: String, secretKey: String, iv: String): String {
        val keyBytes = secretKey.toByteArray()
        require(keyBytes.size in setOf(16, 24, 32)) {
            "AES secretKey must be 16, 24, or 32 bytes, got ${keyBytes.size}"
        }

        val ivBytes = iv.toByteArray()
        require(ivBytes.size == 16) {
            "AES/CBC IV must be 16 bytes, got ${ivBytes.size}"
        }

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyBytes, "AES"), IvParameterSpec(ivBytes))
        val encryptedBytes = cipher.doFinal(profileId.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }
}