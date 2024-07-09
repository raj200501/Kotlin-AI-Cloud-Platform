package security

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object DataEncryption {
    private val key: SecretKey

    init {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        key = keyGen.generateKey()
    }

    fun encrypt(data: String): ByteArray {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(data.toByteArray())
    }

    fun decrypt(encryptedData: ByteArray): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        return String(cipher.doFinal(encryptedData))
    }
}
