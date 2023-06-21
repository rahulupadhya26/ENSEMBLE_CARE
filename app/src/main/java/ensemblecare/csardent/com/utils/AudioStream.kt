package ensemblecare.csardent.com.utils

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
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.Podcast
import ensemblecare.csardent.com.databinding.DialogStreamAudioBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

class AudioStream(
    val context: Activity,
    private val audioDetails: Podcast,
    var mediaPlayer: MediaPlayer
) {

    private lateinit var dialogView: DialogStreamAudioBinding
    private val audioHandler = Handler()
    private var createStreamAudioDialog: BottomSheetDialog? = null

    var progressAlive = false
    var pDialog: ProgressDialog? = null

    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()

    @SuppressLint("SetTextI18n")
    fun streamAudio() {
        createStreamAudioDialog = BottomSheetDialog(context, R.style.SheetDialog)
        dialogView = DialogStreamAudioBinding.inflate(context.layoutInflater)
        /*dialogView = context.layoutInflater.inflate(
            R.layout.dialog_stream_audio, null
        )*/
        val view = dialogView.root
        createStreamAudioDialog!!.setContentView(view)
        createStreamAudioDialog!!.show()
        dialogView.txtAudioTitle.text = audioDetails.name
        dialogView.txtAudioArtist.text = audioDetails.artist

        createStreamAudioDialog!!.setOnCancelListener {
            dialogView.podcastSeekbar.progress = 0
            dialogView.imgAudioBtn.setImageResource(R.drawable.ic_play_arrow)
            dialogView.txtCurrentTime.text = "00:00"
            dialogView.txtDurationTime.text = "00:00"
            mediaPlayer.reset()
        }

        dialogView.podcastSeekbar.max = 100

        dialogView.imgAudioBtn.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                audioHandler.removeCallbacks(updater)
                handler.removeCallbacks(runnable)
                mediaPlayer.pause()
                dialogView.imgAudioBtn.setImageResource(R.drawable.ic_play_arrow)
                mediaPlayer.seekTo(mediaPlayer.currentPosition)
            } else {
                startMusic()
            }
        }

        prepareMediaPlayer(audioDetails.podcast_url)

        dialogView.podcastSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val playPos: Int = (mediaPlayer.duration / 100) * seekBar!!.progress
                    mediaPlayer.seekTo(playPos)
                    dialogView.txtCurrentTime.text =
                        millisecondsToTimer(mediaPlayer.currentPosition.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        mediaPlayer.setOnBufferingUpdateListener { mp, percent ->
            dialogView.podcastSeekbar.secondaryProgress = percent
        }

        mediaPlayer.setOnCompletionListener {
            dialogView.podcastSeekbar.progress = 0
            dialogView.imgAudioBtn.setImageResource(R.drawable.ic_play_arrow)
            dialogView.txtCurrentTime.text = "00:00"
            dialogView.txtDurationTime.text = "00:00"
            mediaPlayer.reset()
        }

        dialogView.imgAudioDownloadBtn.setOnClickListener {
            val async = DownloadAudioFromUrl(context, audioDetails.name)
            async.execute(audioDetails.podcast_url);
        }

        // Seek bar change listener
        dialogView.podcastSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    mediaPlayer.seekTo(i * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    private fun startMusic() {
        mediaPlayer.start()
        dialogView.imgAudioBtn.setImageResource(R.drawable.ic_pause)
        dialogView.imgAudioBtn.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primaryGreen))
        initializeSeekBar()
    }

    private fun showProgress() {
        context.runOnUiThread {
            if (progressAlive) {
                pDialog!!.cancel()
                progressAlive = false
            }
            pDialog = ProgressDialog(context)
            pDialog!!.setMessage("Please wait...")
            if ("Please wait".contains("Please wait")) pDialog!!.setCanceledOnTouchOutside(false)
            progressAlive = true
            pDialog!!.show()
        }
    }

    private fun hideProgress() {
        context.runOnUiThread {
            if (progressAlive) {
                pDialog!!.dismiss()
                pDialog!!.cancel()
                progressAlive = false
            }
        }
    }

    private fun prepareMediaPlayer(audioLink: String) {
        try {
            showProgress()
            mediaPlayer.setDataSource(context, Uri.parse(audioLink))
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                hideProgress()
                dialogView.txtDurationTime.text =
                    millisecondsToTimer(mediaPlayer.duration.toLong())
                startMusic()
            }
            //startMusic(view)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private var updater: Runnable = Runnable {
        updateSeekBar()
        val currentDuration: Long = mediaPlayer.currentPosition.toLong()
        dialogView.txtCurrentTime.text = millisecondsToTimer(currentDuration)
    }

    private fun updateSeekBar() {
        if (mediaPlayer.isPlaying) {
            dialogView.podcastSeekbar.progress =
                (mediaPlayer.currentPosition / max(1, mediaPlayer.duration)) * 100
            audioHandler.postDelayed(updater, 1000)
        }
    }

    // Method to initialize seek bar and audio stats
    private fun initializeSeekBar() {
        dialogView.podcastSeekbar.max = mediaPlayer.seconds

        runnable = Runnable {
            dialogView.podcastSeekbar.progress = mediaPlayer.currentSeconds

            val currentDuration: Long = mediaPlayer.currentPosition.toLong()
            dialogView.txtCurrentTime.text = millisecondsToTimer(currentDuration)
            /*val diff = mediaPlayer.seconds - mediaPlayer.currentSeconds
            tv_due.text = "$diff sec"*/

            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }

    // Creating an extension property to get the media player time duration in seconds
    private val MediaPlayer.seconds: Int
        get() {
            return this.duration / 1000
        }

    // Creating an extension property to get media player current position in seconds
    private val MediaPlayer.currentSeconds: Int
        get() {
            return this.currentPosition / 1000
        }

    private fun millisecondsToTimer(milliseconds: Long): String {
        var timerString = ""
        val secondsString: String
        val minString: String
        val hours: Int = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes: Int = ((milliseconds % (1000 * 60 * 60)) / (1000 * 60)).toInt()
        val seconds: Int = ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000).toInt()
        if (hours > 0) {
            timerString = "$hours:"
        }
        if (minutes < 10) {
            minString = "0$minutes"
        } else {
            minString = "" + minutes
        }
        if (seconds < 10) {
            secondsString = "0$seconds"
        } else {
            secondsString = "" + seconds
        }
        timerString = "$timerString$minString:$secondsString"
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