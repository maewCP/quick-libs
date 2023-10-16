package net.maew.quicklibs

import net.maew.quicklibs.QConsoleUtils.consolePrintln
import java.io.File
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.random.Random

object QFileUtils {
    @JvmStatic
    fun deleteWithRetry(file: File): Boolean {
        if (!file.exists()) return true
        var retryCount = 0
        while (retryCount++ <= 10) {
            try {
                Files.delete(Paths.get(file.path))
                return true
            } catch (e: Exception) {
                consolePrintln(ConsoleColors.YELLOW, "Cannot delete $file")
                if (e.message != null) println("Exception: " + e.message!!)
                e.printStackTrace()
                val sleepSec = Random.nextInt(5, 10)
                println("Try again in $sleepSec seconds...")
                Thread.sleep((sleepSec * 1000).toLong())
            }
        }
        consolePrintln(ConsoleColors.RED, "Stop trying.")
        return false
    }

    @JvmStatic
    fun renameWithRetry(ori: File, dst: File, replaceExisting: Boolean = false): Boolean {
        var retryCount = 0
        while (retryCount++ <= 10) {
            try {
                if (replaceExisting) {
                    Files.move(Paths.get(ori.path), Paths.get(dst.path), StandardCopyOption.REPLACE_EXISTING)
                } else {
                    Files.move(Paths.get(ori.path), Paths.get(dst.path))
                }
                return true
            } catch (e: Exception) {
                consolePrintln(ConsoleColors.YELLOW, "Cannot rename $ori to $dst")
                if (e.message != null) println("Exception: " + e.message!!)
                e.printStackTrace()
                val sleepSec = Random.nextInt(5, 10)
                println("Try again in $sleepSec seconds...")
                Thread.sleep((sleepSec * 1000).toLong())
            }
        }
        consolePrintln(ConsoleColors.RED, "Stop trying.")
        return false
    }

    @JvmStatic
    fun copyWithRetry(ori: File, dst: File, replaceExisting: Boolean = false): Boolean {
        var retryCount = 0
        while (retryCount++ <= 10) {
            try {
                if (replaceExisting) {
                    Files.copy(Paths.get(ori.path), Paths.get(dst.path), StandardCopyOption.REPLACE_EXISTING)
                } else {
                    Files.copy(Paths.get(ori.path), Paths.get(dst.path))
                }
                return true
            } catch (e: Exception) {
                consolePrintln(ConsoleColors.YELLOW, "Cannot copy $ori to $dst")
                if (e.message != null) println("Exception: " + e.message!!)
                e.printStackTrace()
                val sleepSec = Random.nextInt(5, 10)
                println("Try again in $sleepSec seconds...")
                Thread.sleep((sleepSec * 1000).toLong())
            }
        }
        consolePrintln(ConsoleColors.RED, "Stop trying.")
        return false
    }

    @JvmStatic
    fun getFileExtension(fileName: String): String {
        return fileName.substring(fileName.lastIndexOf(".") + 1).lowercase()
    }

    @JvmStatic
    fun getFileNameAndExtension(pathName: String): String {
        val folderSeparatorIndex = pathName.lastIndexOf("/").coerceAtLeast(pathName.lastIndexOf("\\"))
        return if (folderSeparatorIndex == -1) pathName else pathName.substring(folderSeparatorIndex + 1)
    }

    @JvmStatic
    fun getFileNameOnly(pathName: String): String {
        val folderSeparatorIndex = pathName.lastIndexOf("/").coerceAtLeast(pathName.lastIndexOf("\\"))
        val fileName = if (folderSeparatorIndex == -1) pathName else pathName.substring(folderSeparatorIndex)
        return fileName.substring(0, pathName.lastIndexOf("."))
    }

    @JvmStatic
    fun getFolder(pathName: String): String {
        val folderSeparatorIndex = pathName.lastIndexOf("/").coerceAtLeast(pathName.lastIndexOf("\\"))
        return if (folderSeparatorIndex == -1) "" else pathName.substring(0, folderSeparatorIndex)
    }

    @JvmStatic
    fun getUrlFromFilename(pathName: String): String {
        val fileName = pathName.replace("\\", "/")
        val urlString = if (fileName.substring(0, 2) == "//") {
            "file:$fileName"
        } else {
            "file:///$fileName"
        }
        return encodeUrlProperly(urlString)
    }
    @JvmStatic
    fun encodeUrlProperly(urlString: String): String {
        return try {
            val decodedURL = URLDecoder.decode(urlString, "UTF-8")
            val url = URL(decodedURL)
            val uri = URI(
                url.getProtocol(),
                url.getUserInfo(),
                url.getHost(),
                url.getPort(),
                url.getPath(),
                url.getQuery(),
                url.getRef()
            )
            uri.toASCIIString()
        } catch (_: Exception) {
            ""
        }
    }
}
