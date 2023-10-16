package net.maew.quicklibs

import org.apache.commons.text.StringEscapeUtils
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

    @JvmStatic
    fun escapeFilename(
        name: String,
        singleSpaces: Boolean = true
    ): String {
        // remove illegal characters and replace with a more friendly char ;)
        var safe = name.trim { it <= ' ' }

        // remove illegal characters
        safe = safe.replace(
            "[\\/|\\\\|\\*|\\:|\\||\"|\'|\\<|\\>|\\{|\\}|\\?|\\%|,]".toRegex(),
            ""
        )

        // replace . dots with _ and remove the _ if at the end
        safe = safe.replace("\\.".toRegex(), "_")
        if (safe.endsWith("_")) {
            safe = safe.substring(0, safe.length - 1)
        }

        // replace whitespace characters with _
        safe = safe.replace("\\s+".toRegex(), "_")

        // replace double or more spaces with a single one
        if (singleSpaces) {
            safe = safe.replace("_{2,}".toRegex(), "_")
        }
        return safe
    }

    @JvmStatic
    fun unescape(s: String) : String {
        return StringEscapeUtils.unescapeXml(StringEscapeUtils.unescapeHtml4(s))
    }
}
