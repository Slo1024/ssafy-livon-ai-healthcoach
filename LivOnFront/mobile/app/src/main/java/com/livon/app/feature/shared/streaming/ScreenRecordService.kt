package com.livon.app.feature.shared.streaming

import android.app.*
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.core.app.NotificationCompat
import android.media.MediaScannerConnection
import com.livon.app.R
import java.io.File

class ScreenRecordService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var videoFile: File? = null
    private var isRecording = false

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
    }

    private fun startForegroundWithNotification() {
        val channelId = "screen_record_channel"
        val channelName = "Screen Recording"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("📹 화면 녹화 중")
            .setContentText("녹화가 진행 중입니다...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED) ?: return START_NOT_STICKY
        val data = intent.getParcelableExtra<Intent>("data") ?: return START_NOT_STICKY

        startRecording(resultCode, data)
        return START_STICKY
    }

    private fun startRecording(resultCode: Int, data: Intent) {
        if (isRecording) return

        val projectionManager = getSystemService(MediaProjectionManager::class.java)
        val dm: DisplayMetrics = resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        val density = dm.densityDpi

        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        if (!moviesDir.exists()) moviesDir.mkdirs()
        videoFile = File(moviesDir, "record_${System.currentTimeMillis()}.mp4")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(videoFile!!.absolutePath)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoSize(width, height)
            setVideoEncodingBitRate(5 * 1024 * 1024)
            setVideoFrameRate(30)
            prepare()
        }

        mediaProjection = projectionManager.getMediaProjection(resultCode, data)

// Callback 등록
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                stopScreenRecording()
            }
        }, null)

// 3. VirtualDisplay 생성 (MediaRecorder.surface 연결)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenRecording",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder!!.surface,
            null, null
        )

// 4. 녹화 시작
        mediaRecorder?.start()

        isRecording = true
        Toast.makeText(this, "📹 화면 녹화 시작!", Toast.LENGTH_SHORT).show()
    }


    private fun stopScreenRecording() {
        if (!isRecording) return

        try {
            mediaRecorder?.apply {
                try { stop() } catch (e: RuntimeException) {
                    videoFile?.delete()
                }
                reset()
                release()
            }
            virtualDisplay?.release()
            mediaProjection?.stop()

            videoFile?.let { file ->
                MediaScannerConnection.scanFile(this, arrayOf(file.absolutePath), null) { path, uri ->
                    Toast.makeText(this, "💾 영상 저장 완료: $path", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            videoFile?.delete()
        } finally {
            mediaRecorder = null
            virtualDisplay = null
            mediaProjection = null
            isRecording = false
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScreenRecording()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
