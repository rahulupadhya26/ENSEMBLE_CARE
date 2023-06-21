package ensemblecare.csardent.com.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.Podcast
import ensemblecare.csardent.com.databinding.FragmentPodcastDetailBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

/**
 * A simple [Fragment] subclass.
 * Use the [PodcastDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PodcastDetailFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var podcast: Podcast? = null
    private var wellnessType: String? = null
    private var mediaUrl: String? = null
    private var mediaPlayer: MediaPlayer? = null
    private val audioHandler = Handler()
    var progressAlive = false
    var pDialog: ProgressDialog? = null
    private var isFavourite = false

    private lateinit var runnable1: Runnable
    private var handler1: Handler = Handler()
    private lateinit var binding: FragmentPodcastDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            podcast = it.getParcelable(ARG_PARAM1)
            wellnessType = it.getString(ARG_PARAM2)
            mediaUrl = it.getString(ARG_PARAM3)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPodcastDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_podcast_detail
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.black)
        mediaPlayer = MediaPlayer()

        if (podcast != null) {
            Glide.with(requireActivity())
                .load(BaseActivity.baseURL.dropLast(5) + podcast!!.podcast_image)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(binding.imgPodcastArtistLarge)

            Glide.with(requireActivity())
                .load(BaseActivity.baseURL.dropLast(5) + podcast!!.podcast_image)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(binding.imgPodcastArtistSmall)

            binding.txtPodcastTitle.text = podcast!!.name
            binding.txtPodcastArtist.text = podcast!!.artist
            binding.txtPodcastSubTitle.text = podcast!!.description
            isFavourite = podcast!!.is_favourite

            runOnUiThread {
                if (podcast!!.is_favourite) {
                    binding.imgFav.setImageResource(R.drawable.favorite)
                } else {
                    binding.imgFav.setImageResource(R.drawable.favourite_white)
                }
            }
            prepareMediaPlayer(podcast!!.podcast_url)
        } else {
            prepareMediaPlayer(mediaUrl!!)
        }

        onClickEvents()

        binding.podcastSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val playPos: Int = (mediaPlayer!!.duration / 100) * seekBar!!.progress
                    mediaPlayer!!.seekTo(playPos)
                    binding.currentDuration.text =
                        millisecondsToTimer(mediaPlayer!!.currentPosition.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        mediaPlayer!!.setOnBufferingUpdateListener { mp, percent ->
            binding.podcastSeekbar.secondaryProgress = percent
        }

        mediaPlayer!!.setOnCompletionListener {
            binding.podcastSeekbar.progress = 0
            binding.imgPlayPause.setImageResource(R.drawable.ic_action_play_arrow)
            binding.currentDuration.text = "00:00"
            binding.maxDuration.text = "00:00"
            mediaPlayer!!.reset()
        }

        // Seek bar change listener
        binding.podcastSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    mediaPlayer!!.seekTo(i * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

    }

    private fun onClickEvents() {
        binding.podcastPlayerBack.setOnClickListener {
            popBackStack()
        }

        binding.imgFav.setOnClickListener {
            if (wellnessType!!.isNotEmpty()) {
                sendFavoriteData(podcast!!.id, "Podcast", !isFavourite, wellnessType!!) {
                    runOnUiThread {
                        if (!isFavourite) {
                            binding.imgFav.setImageResource(R.drawable.favorite)
                        } else {
                            binding.imgFav.setImageResource(R.drawable.favourite_white)
                        }
                        isFavourite = !isFavourite
                    }
                }
            } else {
                sendResourceFavoriteData(podcast!!.id, "Podcast", !isFavourite) {
                    runOnUiThread {
                        if (!isFavourite) {
                            binding.imgFav.setImageResource(R.drawable.favorite)
                        } else {
                            binding.imgFav.setImageResource(R.drawable.favourite_white)
                        }
                        isFavourite = !isFavourite
                    }
                }
            }
        }

        binding.imgPrev.setOnClickListener {

        }

        binding.cardViewPlayButton.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                audioHandler.removeCallbacks(updater)
                handler1.removeCallbacks(runnable1)
                mediaPlayer!!.pause()
                binding.imgPlayPause.setImageResource(R.drawable.ic_action_play_arrow)
                mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition)
            } else {
                startMusic()
            }
        }

        binding.imgNext.setOnClickListener {

        }

        binding.imgSharePodcast.setOnClickListener {
            /*shareDetails(
                "",
                podcast!!.name,
                podcast!!.podcast_url,
                podcast!!.podcast_image,
                "Podcast"
            )*/
        }
    }

    private fun startMusic() {
        mediaPlayer!!.start()
        binding.imgPlayPause.setImageResource(R.drawable.pause)
        initializeSeekBar()
    }

    private fun showAudioProgress() {
        requireActivity().runOnUiThread {
            /*if (progressAlive) {
                pDialog!!.cancel()
                progressAlive = false
            }
            pDialog = ProgressDialog(context)
            pDialog!!.setMessage("Please wait...")
            if ("Please wait".contains("Please wait")) pDialog!!.setCanceledOnTouchOutside(false)
            progressAlive = true
            pDialog!!.show()*/
            showProgress()
        }
    }

    private fun hideAudioProgress() {
        requireActivity().runOnUiThread {
            hideProgress()
            /*if (progressAlive) {
                pDialog!!.dismiss()
                pDialog!!.cancel()
                progressAlive = false
            }*/
        }
    }

    private fun prepareMediaPlayer(audioLink: String) {
        try {
            showAudioProgress()
            mediaPlayer!!.setDataSource(requireActivity(), Uri.parse(audioLink))
            mediaPlayer!!.prepareAsync()
            mediaPlayer!!.setOnPreparedListener {
                hideAudioProgress()
                binding.maxDuration.text =
                    millisecondsToTimer(mediaPlayer!!.duration.toLong())
                startMusic()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private var updater: Runnable = Runnable {
        updateSeekBar()
        val currentDurationLength: Long = mediaPlayer!!.currentPosition.toLong()
        binding.currentDuration.text = millisecondsToTimer(currentDurationLength)
    }

    private fun updateSeekBar() {
        if (mediaPlayer!!.isPlaying) {
            binding.podcastSeekbar.progress =
                (mediaPlayer!!.currentPosition / max(1, mediaPlayer!!.duration)) * 100
            audioHandler.postDelayed(updater, 1000)
        }
    }

    // Method to initialize seek bar and audio stats
    private fun initializeSeekBar() {
        binding.podcastSeekbar.max = mediaPlayer!!.seconds

        runnable1 = Runnable {
            binding.podcastSeekbar.progress = mediaPlayer!!.currentSeconds

            val currentDurationLength: Long = mediaPlayer!!.currentPosition.toLong()
            binding.currentDuration.text = millisecondsToTimer(currentDurationLength)
            /*val diff = mediaPlayer.seconds - mediaPlayer.currentSeconds
            tv_due.text = "$diff sec"*/

            handler1.postDelayed(runnable1, 1000)
        }
        handler1.postDelayed(runnable1, 1000)
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

    override fun onPause() {
        super.onPause()
        if (mediaPlayer!!.isPlaying) {
            audioHandler.removeCallbacks(updater)
            handler1.removeCallbacks(runnable1)
            mediaPlayer!!.pause()
            binding.imgPlayPause.setImageResource(R.drawable.ic_action_play_arrow)
            mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mediaPlayer != null) {
            audioHandler.removeCallbacks(updater)
            if (::runnable1.isInitialized) {
                handler1.removeCallbacks(runnable1)
            }
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PodcastDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Podcast?, param2: String, param3: String = "") =
            PodcastDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                }
            }

        const val TAG = "Screen_podcast_detail"
    }
}