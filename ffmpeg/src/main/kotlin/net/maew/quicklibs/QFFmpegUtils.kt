package net.maew.quicklibs

import net.bramp.ffmpeg.*
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.job.FFmpegJob
import net.bramp.ffmpeg.probe.FFmpegFormat
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import net.bramp.ffmpeg.probe.FFmpegStream
import net.bramp.ffmpeg.progress.Progress
import net.bramp.ffmpeg.progress.ProgressListener
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.math.abs


object QFFmpegUtils {

    @JvmStatic
    fun quickProbe(ffprobePath: String, input: String): Pair<FFmpegProbeResult?, FFmpegStream?> {
        val ffprobe = FFprobe(ffprobePath)
        val probeResult: FFmpegProbeResult
        try {
            probeResult = ffprobe.probe(input)
        } catch (e: Exception) {
            QConsoleUtils.println(ConsoleColors.RED, "FFProbe.exe Cannot read file $input -> Skip")
            if (e.message != null) e.printStackTrace()
            return Pair(null, null)
        }
        val stream: FFmpegStream = probeResult.getStreams().first { it.codec_type == FFmpegStream.CodecType.VIDEO }
        if (stream.bit_rate == 0L) {
            stream.bit_rate = probeResult.format.bit_rate - probeResult.getStreams().sumOf { it.bit_rate }
        }
        val (w, h) = getBoxByDisplayAspectRatio(stream.width, stream.height, stream.display_aspect_ratio)
        stream.width = w
        stream.height = h
        return probeResult to stream
    }

    @JvmStatic
    fun isStreamRotated(stream: FFmpegStream): Boolean {
        stream.side_data_list?.forEach { sideData ->
            if (sideData.rotation in listOf(-90, 90, -270, 270)) return true
        }
        return false
    }

    fun quickCompareDuration(ffprobePath: String, input: String, output: String): Boolean {
        val (inputProbeResult, _) = quickProbe(ffprobePath, input)
        return inputProbeResult == null || quickCompareDuration(ffprobePath, inputProbeResult.getFormat().duration, output)
    }

    fun quickCompareDuration(ffprobePath: String, inputDuration: Double, output: String): Boolean {
        val (outputProbeResult, _) = quickProbe(ffprobePath, output)
        return outputProbeResult == null || abs(inputDuration - outputProbeResult.getFormat().duration) < 3
    }

    @JvmStatic
    fun transcode(
        ffmpegPath: String, ffprobePath: String, input: String, output: String, oWidth: Int,
        oHeight: Int, x265VideoQuality: Double, x265Preset: String, progressReportIntervalPercentage: Int
    ): Boolean {
        val ffmpeg = FFmpeg(ffmpegPath, object : RunProcessFunction() {
            @Throws(IOException::class)
            override fun run(args: List<String>): Process {
                val process: Process = super.run(args)
                Runtime.getRuntime().addShutdownHook(Thread({ killFFmpeg(process, output) }, "FFmpeg process destroyer"))
                return process
            }
        })
        val (inputProbeResult, inputStream) = quickProbe(ffprobePath, input)
        if (inputProbeResult == null || inputStream == null) return false
        val inputFormat: FFmpegFormat = inputProbeResult.getFormat()

        val (ow, oh) = downScale(inputStream.width, inputStream.height, oWidth, oHeight, isStreamRotated(inputStream))
        println()
        println("Target File: $output")
        println("Target URL: ${QFileUtils.getUrlFromFilename(output)}")
        println("Target Resolution: $ow x $oh")
        println("Target x265 Quality: $x265VideoQuality")
        println("Target x265 Preset: $x265Preset")
        println()

        val builder = FFmpegBuilder()
            .setInput(inputProbeResult) // Filename, or a FFmpegProbeResult
            .overrideOutputFiles(true) // Override the output if it exists
            .addOutput(output) // Filename for the destination
//        .setFormat("mkv") // Format is inferred from filename, or can be set
//        .setTargetSize(250000) // Aim for a 250KB file
//        .disableSubtitle() // No subtiles
            .setAudioChannels(2) // Stereo audio
            .setAudioCodec("mp3") // using the mp3 codec
            .setAudioSampleRate(44100) // at 44.1KHz
            .setAudioBitRate(131072) // at 128 kbit/s
//        .setVideoCodec("h264_nvenc") // Video using x264
            .setVideoCodec("libx265") // Video using x265
            .setVideoFrameRate(30, 1) // at 30 frames per second
//        .setVideoResolution(640, 480)// at 640x480 resolution
            .setVideoResolution(ow, oh)
            .setVideoQuality(x265VideoQuality)
            .setPreset(x265Preset)
            .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
            .done()

        val executor = FFmpegExecutor(ffmpeg, FFprobe(ffprobePath))

        var nextReportingPercentage = 0
        var startTime: LocalDateTime? = null
        val job: FFmpegJob = executor.createJob(builder, object : ProgressListener {
            // Using the FFmpegProbeResult determine the duration of the input
            val duration_ns: Double = inputFormat.duration * TimeUnit.SECONDS.toNanos(1)
            override fun progress(progress: Progress) {
                val percentage: Double = progress.out_time_ns / duration_ns * 100
                // Print out interesting information about the progress
                if (percentage >= nextReportingPercentage) {
                    nextReportingPercentage = percentage.toInt() / progressReportIntervalPercentage * progressReportIntervalPercentage + progressReportIntervalPercentage
                    val now = LocalDateTime.now()
                    val etaTime = if (startTime != null) startTime!!.plusNanos((ChronoUnit.NANOS.between(startTime, now) * 100L / percentage).toLong()) else null
                    val etaSec = if (etaTime != null) ChronoUnit.SECONDS.between(now, etaTime) else null
                    if (startTime == null) startTime = now

                    println(
                        java.lang.String.format(
                            "[%.0f%%] fps:%.0f speed:%.2fx now:%s eta:%s %s",
                            percentage,
                            progress.fps.toDouble(),
                            progress.speed,
                            now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            etaTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) ?: "",
                            if (etaSec != null) "(" + formatSecHHmmss(etaSec.toDouble()) + ")" else ""
                        )
                    )

//                    println(
//                        java.lang.String.format(
//                            "[%.0f%%] status:%s frame:%d t:%s ms fps:%.0f speed:%.2fx",
//                            percentage,
//                            progress.status,
//                            progress.frame,
//                            FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
//                            progress.fps.toDouble(),
//                            progress.speed
//                        )
//                    )
                }
            }
        })

        println("Begin transcoding at ${LocalDateTime.now()}")
        if (File(output).exists()) {
            throw OutputFileExistsFFmpegException("$output exists")
        }
        job.run()
        return quickCompareDuration(ffprobePath, inputFormat.duration, output)
    }

    @JvmStatic
    fun generateThumbnail(ffmpegPath: String, ffprobePath: String, input: String, output: String, atSecond: Int, width: Int, height: Int = -1): Boolean {
        val ffmpeg = FFmpeg(ffmpegPath)
        val builder = FFmpegBuilder()
            .addExtraArgs("-ss", atSecond.toString())
            .setInput(input)
            .overrideOutputFiles(true) // Override the output if it exists
            .addOutput(output)
            .setFrames(1)
            .setVideoFilter("select='gt(n\\,0)',scale=$width:$height")
            .done()
        val executor = FFmpegExecutor(ffmpeg, FFprobe(ffprobePath))
        val job: FFmpegJob = executor.createJob(builder)
        job.run()
        return true
    }

    @JvmStatic
    fun getBoxByDisplayAspectRatio(iw: Int, ih: Int, displayAspectRatio: String?): Pair<Int, Int> {
        if (displayAspectRatio == null) return iw to ih
        val dar = displayAspectRatio.split(":".toRegex())
        if (dar.size != 2) throw RuntimeException("Display Aspect Ratio: $displayAspectRatio is in valid.")
        val dw = dar[0].toInt()
        val dh = dar[1].toInt()
        var ow: Int = iw
        var oh: Int = ih
        // assumption is ih is correct
        ow = dw * oh / dh
        if (ow % 2 != 0) ow -= 1
        if (oh % 2 != 0) oh -= 1
        return ow to oh
    }

    @JvmStatic
    fun downScale(iw: Int, ih: Int, boxw: Int, boxh: Int, rotated: Boolean): Pair<Int, Int> {
        var ow: Int = if (!rotated) iw else ih
        var oh: Int = if (!rotated) ih else iw
        if (ow > boxw && boxw != -1) {
            oh = oh * boxw / ow
            ow = boxw
        }
        if (oh > boxh && boxh != -1) {
            ow = ow * boxh / oh
            oh = boxh
        }
        if (ow % 2 != 0) ow -= 1
        if (oh % 2 != 0) oh -= 1
        return ow to oh
    }

    /**
     * @return the exit code of the terminated process as an `unsigned
     * byte` (0..255 range), or -1 if the current thread has been
     * interrupted.
     */
    @JvmStatic
    fun killFFmpeg(process: Process, output: String): Int {
        if (!process.isAlive) {
            /*
			 * ffmpeg -version, do nothing
			 */
            return process.exitValue()
        }

        /*
		 * ffmpeg -f x11grab
		 */

        QConsoleUtils.println(ConsoleColors.BLUE_BOLD, "About to destroy the child process...")
        try {
            OutputStreamWriter(process.outputStream).use { out -> out.write('q'.code) }
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
        return try {
            if (!process.waitFor(5L, TimeUnit.SECONDS)) {
                process.destroy()
                process.waitFor()
            }
            if (QFileUtils.deleteWithRetry(File(output))) {
                QConsoleUtils.println(ConsoleColors.GREEN, "Delete temp file successfully $output")
            } else {
                QConsoleUtils.println(ConsoleColors.RED, "Cannot delete temp file $output")
            }
            process.exitValue()
        } catch (ie: InterruptedException) {
            println("Interrupted")
            ie.printStackTrace()
            Thread.currentThread().interrupt()
            -1
        }
    }

    @JvmStatic
    fun formatSecHHmmss(sec: Double): String {
        val h = sec.toInt() / 60 / 60
        val m = sec.toInt() % (60 * 60) / 60
        val s = sec - (h * 60 * 60) - (m * 60)
        return "${"%02d".format(h)}:${"%02d".format(m)}:${"%02.0f".format(s)}"
    }

    @JvmStatic
    fun formatFileSize(byte: Long): String {
        return if (byte > 1024 * 1024 * 1024) {
            "${"%.1f".format(byte / 1024.0 / 1024.0 / 1024.0)} GiB"
        } else if (byte > 1024 * 1024) {
            "${"%.1f".format(byte / 1024.0 / 1024.0)} MiB"
        } else {
            "${"%.1f".format(byte / 1024.0)} KiB"
        }
    }
}

class OutputFileExistsFFmpegException(message: String) : RuntimeException(message)
