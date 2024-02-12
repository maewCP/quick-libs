package net.maew.quicklibs

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
                QConsoleUtils.println(ConsoleColors.YELLOW, "Cannot delete $file")
                if (e.message != null) println("Exception: " + e.message!!)
                e.printStackTrace()
                val sleepSec = Random.nextInt(5, 10)
                println("Try again in $sleepSec seconds...")
                Thread.sleep((sleepSec * 1000).toLong())
            }
        }
        QConsoleUtils.println(ConsoleColors.RED, "Stop trying.")
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
                QConsoleUtils.println(ConsoleColors.YELLOW, "Cannot rename $ori to $dst")
                if (e.message != null) println("Exception: " + e.message!!)
                e.printStackTrace()
                val sleepSec = Random.nextInt(5, 10)
                println("Try again in $sleepSec seconds...")
                Thread.sleep((sleepSec * 1000).toLong())
            }
        }
        QConsoleUtils.println(ConsoleColors.RED, "Stop trying.")
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
                QConsoleUtils.println(ConsoleColors.YELLOW, "Cannot copy $ori to $dst")
                if (e.message != null) println("Exception: " + e.message!!)
                e.printStackTrace()
                val sleepSec = Random.nextInt(5, 10)
                println("Try again in $sleepSec seconds...")
                Thread.sleep((sleepSec * 1000).toLong())
            }
        }
        QConsoleUtils.println(ConsoleColors.RED, "Stop trying.")
        return false
    }

    @JvmStatic
    fun getFileExtension(fileName: String): String {
        val extensionSeparatorIndex = fileName.lastIndexOf(".")
        if (extensionSeparatorIndex == -1) return ""
        else {
            val extension = fileName.substring(extensionSeparatorIndex + 1).lowercase()
            if (extension == "" || extension.contains(" ")) return ""
            return extension
        }
    }

    @JvmStatic
    fun addFileSubExtensionAtFirstPosition(pathName: String, subExtensionWithDot: String): String {
        val extensions = mutableListOf<String>()
        var targetPathName = getFileNameAndExtension(pathName)
        do {
            val extension = getFileExtension(targetPathName)
            if (extension != "") extensions.add(extension)
            targetPathName = getFileNameOnly(targetPathName)
        } while (extension != "")
        targetPathName += subExtensionWithDot
        extensions.reversed().forEach { extension -> targetPathName += ".$extension" }
        return getFolder(pathName, true) + targetPathName
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
        val extension = getFileExtension(fileName)
        return if (extension != "") fileName.substring(0, fileName.lastIndexOf(".")) else fileName
    }

    @JvmStatic
    fun getFolder(pathName: String, includeLastFolderSeparator: Boolean = false): String {
        val folderSeparatorIndex = pathName.lastIndexOf("/").coerceAtLeast(pathName.lastIndexOf("\\"))
        return if (folderSeparatorIndex == -1) "" else pathName.substring(0, folderSeparatorIndex + if (includeLastFolderSeparator) 1 else 0)
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
