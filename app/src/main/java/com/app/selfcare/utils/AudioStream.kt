package com.app.selfcare.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.app.selfcare.R
import com.app.selfcare.data.Podcast
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_stream_audio.view.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

class AudioStream(val context: Activity, private val audioDetails: Podcast, var mediaPlayer: MediaPlayer) {

    var dialogView: View? = null
    private val audioHandler = Handler()
    private var createStreamAudioDialog: BottomSheetDialog? = null

    @SuppressLint("SetTextI18n")
    fun streamAudio() {
        createStreamAudioDialog = BottomSheetDialog(context)
        dialogView = context.layoutInflater.inflate(
            R.layout.dialog_stream_audio, null
        )
        createStreamAudioDialog!!.setContentView(dialogView!!)
        createStreamAudioDialog!!.show()
        dialogView!!.txt_audio_title.text = audioDetails.title
        dialogView!!.txt_audio_artist.text = audioDetails.artist

        createStreamAudioDialog!!.setOnCancelListener {
            dialogView!!.podcast_seekbar.progress = 0
            dialogView!!.img_audio_btn.setImageResource(R.drawable.ic_play_arrow)
            dialogView!!.txtCurrentTime.text = "00:00"
            dialogView!!.txtDurationTime.text = "00:00"
            mediaPlayer.reset()
        }

        dialogView!!.podcast_seekbar.max = 100

        dialogView!!.img_audio_btn.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                audioHandler.removeCallbacks(updater)
                mediaPlayer.pause()
                dialogView!!.img_audio_btn.setImageResource(R.drawable.ic_play_arrow)
            } else {
                startMusic()
            }
        }

        prepareMediaPlayer(audioDetails.audio)

        dialogView!!.podcast_seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val playPos: Int = (mediaPlayer.duration / 100) * seekBar!!.progress
                    mediaPlayer.seekTo(playPos)
                    dialogView!!.txtCurrentTime.text =
                        millisecondsToTimer(mediaPlayer.currentPosition.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        mediaPlayer.setOnBufferingUpdateListener { mp, percent ->
            dialogView!!.podcast_seekbar.secondaryProgress = percent
        }

        mediaPlayer.setOnCompletionListener {
            dialogView!!.podcast_seekbar.progress = 0
            dialogView!!.img_audio_btn.setImageResource(R.drawable.ic_play_arrow)
            dialogView!!.txtCurrentTime.text = "00:00"
            dialogView!!.txtDurationTime.text = "00:00"
            mediaPlayer.reset()
        }

        dialogView!!.img_audio_download_btn.setOnClickListener {
            val async = DownloadAudioFromUrl(context, audioDetails.title)
            async.execute(audioDetails.audio);

        }
    }

    private fun startMusic() {
        mediaPlayer.start()
        dialogView!!.img_audio_btn.setImageResource(R.drawable.ic_pause)
        dialogView!!.img_audio_btn.imageTintList=
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primaryGreen))
        updateSeekBar()
    }

    private fun prepareMediaPlayer(audioLink: String) {
        try {
            mediaPlayer.setDataSource(context, Uri.parse(audioLink))
            mediaPlayer.prepare()
            dialogView!!.txtDurationTime.text = millisecondsToTimer(mediaPlayer.duration.toLong())
            //startMusic(view)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private var updater: Runnable = Runnable {
        updateSeekBar()
        val currentDuration: Long = mediaPlayer.currentPosition.toLong()
        dialogView!!.txtCurrentTime.text = millisecondsToTimer(currentDuration)
    }

    private fun updateSeekBar() {
        if (mediaPlayer.isPlaying) {
            dialogView!!.podcast_seekbar.progress =
                (mediaPlayer.currentPosition / max(1, mediaPlayer.duration)) * 100
            audioHandler.postDelayed(updater, 1000)
        }
    }

    private fun millisecondsToTimer(milliseconds: Long): String {
        var timerString = ""
        val secondsString: String
        val hours: Int = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes: Int = ((milliseconds % (1000 * 60 * 60)) / (1000 * 60)).toInt()
        val seconds: Int = ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000).toInt()
        if (hours > 0) {
            timerString = "$hours:"
        }
        if (seconds < 10) {
            secondsString = "0$seconds"
        } else {
            secondsString = "" + seconds
        }
        timerString = "$timerString$minutes:$secondsString"
        return timerString
    }

    class DownloadAudioFromUrl(val context: Activity, private val audioTitle: String) :
        AsyncTask<String, Int, String>() {

        // declare the dialog as a member field of your activity
        var mProgressDialog: ProgressDialog? = null

        @Deprecated("Deprecated in Java")
        override fun onPreExecute() {
            super.onPreExecute()
            // instantiate it within the onCreate method
            mProgressDialog = ProgressDialog(context)
            mProgressDialog!!.setMessage("Audio file downloading");
            mProgressDialog!!.isIndeterminate = true;
            mProgressDialog!!.setCanceledOnTouchOutside(false)
            mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog!!.setCancelable(false);
            mProgressDialog!!.show();
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            mProgressDialog!!.dismiss();
        }

        @Deprecated("Deprecated in Java")
        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            Log.d("ptg", "onProgressUpdate: " + values[0])
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog!!.isIndeterminate = false;
            mProgressDialog!!.max = 100;
            mProgressDialog!!.progress = values[0]!!;
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg sUrl: String?): String? {
            var input: InputStream? = null
            var output: OutputStream? = null
            var connection: HttpURLConnection? = null
            try {
                val url = URL(sUrl[0])
                connection = url.openConnection() as HttpURLConnection
                connection.connect()

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.responseCode !== HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.responseCode
                        .toString() + " " + connection.responseMessage
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                val fileLength: Int = connection.contentLength

                // download the file
                input = connection.inputStream
//            output = new FileOutputStream("/data/data/com.example.vadym.test1/textfile.txt");
                output = context.openFileOutput("$audioTitle.mp3", Context.MODE_PRIVATE)
                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    // allow canceling with back button
                    if (isCancelled) {
                        input.close()
                        return null
                    }
                    total += count.toLong()
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((total * 100 / fileLength).toInt())
                    output.write(data, 0, count)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            } finally {
                try {
                    output?.close()
                    input?.close()
                } catch (ignored: IOException) {
                }
                connection?.disconnect()
            }
            return null
        }

    }
}