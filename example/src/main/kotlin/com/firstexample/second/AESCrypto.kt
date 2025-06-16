package com.firstexample.second

import javax.crypto.Cipher

class AESCrypto(val key: ByteArray) {
    fun encrypt(bytes: ByteArray): ByteArray {
        return ByteArray(0)
    }

    fun decrypt(bytes: ByteArray): ByteArray {
        return ByteArray(0)
    }

    companion object {
        fun encrypt(key: ByteArray, bytes: ByteArray): ByteArray = AESCrypto(key).encrypt(bytes)
        fun decrypt(key: ByteArray, bytes: ByteArray): ByteArray = AESCrypto(key).decrypt(bytes)
    }
}