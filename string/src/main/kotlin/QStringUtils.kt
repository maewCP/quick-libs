import java.security.MessageDigest

object QStringUtils {

    @JvmStatic
    fun md5(str: String): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(str.toByteArray())
        return encodeHexString(md.digest())
    }

    @JvmStatic
    fun encodeHexString(byteArray: ByteArray): String {
        val hexStringBuffer = StringBuffer()
        for (i in byteArray.indices) {
            hexStringBuffer.append(byteToHex(byteArray[i]))
        }
        return hexStringBuffer.toString()
    }

    @JvmStatic
    fun byteToHex(num: Byte): String {
        val hexDigits = CharArray(2)
        hexDigits[0] = Character.forDigit(num.toInt() shr 4 and 0xF, 16)
        hexDigits[1] = Character.forDigit(num.toInt() and 0xF, 16)
        return String(hexDigits)
    }
}
