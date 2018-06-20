package xyz.mcomella.mediasession

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
import android.media.AudioManager.STREAM_MUSIC
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.support.v4.media.session.MediaControllerCompat

const val SUPPORTED_ACTIONS = PlaybackStateCompat.ACTION_PLAY_PAUSE or
        PlaybackStateCompat.ACTION_PLAY or
        PlaybackStateCompat.ACTION_PAUSE or
        PlaybackStateCompat.ACTION_FAST_FORWARD or
        PlaybackStateCompat.ACTION_REWIND or
        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

class MainActivity : AppCompatActivity() {

    lateinit var mediaSessionCompat: MediaSessionCompat

    private var listener: AudioManager.OnAudioFocusChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        initWebView(wv)
        initMediaSessionCompat()
    }

    override fun onStart() {
        super.onStart()

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        listener = AudioManager.OnAudioFocusChangeListener { Log.d("lol", "audioChangeListener: $it") }
        val granted = audioManager.requestAudioFocus(listener, STREAM_MUSIC, AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
        Log.d("lol", "granted: $granted")

        mediaSessionCompat.isActive = true
    }

    override fun onStop() {
        super.onStop()
        (getSystemService(Context.AUDIO_SERVICE) as AudioManager).abandonAudioFocus(listener)

        mediaSessionCompat.isActive = false
    }

    private fun initMediaSessionCompat() {
        mediaSessionCompat = MediaSessionCompat(this, "lol")
        mediaSessionCompat.setMediaButtonReceiver(null)
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or
                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
        mediaSessionCompat.setPlaybackState(PlaybackStateCompat.Builder()
                .setActions(SUPPORTED_ACTIONS)
                .setState(PlaybackStateCompat.STATE_PAUSED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1.0f)
                .build()
        )
        mediaSessionCompat.setCallback(object : MediaSessionCompat.Callback() {
            override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                Log.d("lol", "mediaButtonEvent")
                return super.onMediaButtonEvent(mediaButtonEvent)
            }

            override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
                super.onCommand(command, extras, cb)
                Log.d("lol", "onCommand")
            }

            override fun onPrepare() {
                Log.d("lol", "onPrepare")
            }

            override fun onPlay() {
                Log.d("lol", "onPlay")
                mediaSessionCompat.setPlaybackState(PlaybackStateCompat.Builder()
                        .setActions(SUPPORTED_ACTIONS)
                        .setState(PlaybackStateCompat.STATE_PLAYING,
                                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                                1.0f)
                        .build())
            }

            override fun onPause() {
                Log.d("lol", "onPause")
                mediaSessionCompat.setPlaybackState(PlaybackStateCompat.Builder()
                        .setActions(SUPPORTED_ACTIONS)
                        .setState(PlaybackStateCompat.STATE_PAUSED,
                                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                                1.0f)
                        .build())
            }

            override fun onStop() {
                Log.d("lol", "onStop")
            }
        })

        val mediaController = MediaControllerCompat(this, mediaSessionCompat)
        MediaControllerCompat.setMediaController(this, mediaController)
    }

    private fun initWebView(wv: WebView) {
        wv.webChromeClient = WebChromeClient()
        wv.webViewClient = WebViewClient()
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        wv.loadUrl("https://youtube.com/tv")
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSessionCompat.release()
        // tODO: stop playback?
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        Log.d("lol", "event: ${event?.keyCode}")
        return super.dispatchKeyEvent(event)
    }
}
