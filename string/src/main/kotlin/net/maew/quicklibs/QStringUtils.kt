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

    /**
    // remove illegal characters and replace with a more friendly char ;)
     */
    @JvmStatic
    fun escapeFilename(
        fileName: String,
        singleSpaces: Boolean = true,
        trimLength: Int = Int.MAX_VALUE
    ): String {
        var safe = fileName.trim { it <= ' ' }

        // remove illegal characters
        safe = safe.replace(
            //"[\\/|\\\\|\\*|\\:|\\||\"|\'|\\<|\\>|\\{|\\}|\\?|\\%|,]".toRegex(),
            "[\\/|\\\\|\\*|\\:|\\||\"|\\<|\\>|\\{|\\}|\\?|\\%]".toRegex(),
            ""
        )

        // replace . dots with _
        //safe = safe.replace("\\.".toRegex(), "_")

        // remove the . if at the end
        while (safe.endsWith(".")) {
            safe = safe.substring(0, safe.length - 1)
        }

        // replace whitespace characters with _
        //safe = safe.replace("\\s+".toRegex(), "_")

        // replace double or more spaces with a single one
        if (singleSpaces) {
            //safe = safe.replace("_{2,}".toRegex(), "_")
            safe = safe.replace(" {2,}".toRegex(), " ")
        }

        // final trim
        safe = safe.trim()

        // limit long filename
        if (safe.length > trimLength) safe = safe.substring(0, trimLength - 3) + "..."

        return safe
    }

    @JvmStatic
    fun unescape(s: String): String {
        return StringEscapeUtils.unescapeXml(StringEscapeUtils.unescapeHtml4(s))
    }
}
