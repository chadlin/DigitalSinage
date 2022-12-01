package com.example.loadhtml

import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.*
import com.example.loadhtml.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util


class MainActivity : AppCompatActivity() {

	private var firstPlayer: ExoPlayer? = null
//	private var secondPlayer: ExoPlayer? = null

	private val TYPE_IMAGE = "image"
	private val TYPE_VIDEO = "video"
	private val TYPE_WEB = "webpage"
	private val TYPE_DEFAULT = "default"


	private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
		ActivityMainBinding.inflate(layoutInflater)
	}

	private var playWhenReady = false
	private var currentItem = 0
	private var playbackPosition = 0L

	private val verticalWeight = arrayListOf(1f, 5f, 1f)
	private val midWeight = arrayListOf(2f, 1f)

	private val templateType =
		arrayListOf<String>(TYPE_WEB, TYPE_IMAGE, TYPE_VIDEO, TYPE_DEFAULT)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(viewBinding.root)

		val resetConstraintSet = ConstraintSet().apply {
			clone(viewBinding.mainLayout)
		}

		viewBinding.resetLayout.setOnClickListener {
			TransitionManager.beginDelayedTransition(viewBinding.mainLayout)
			resetConstraintSet.applyTo(viewBinding.mainLayout)
		}

		viewBinding.midChange.setOnClickListener {
			changeMidConstraintSet()
		}

		viewBinding.verticalChange.setOnClickListener {
			changeVerticalConstraint()
		}

		viewBinding.setPlayer.setOnClickListener {
			setView2Layout(getBroadcastView(templateType[0]), viewBinding.topRow)
			setView2Layout(getBroadcastView(templateType[1]), viewBinding.leftConstraintLayout)
			setView2Layout(getBroadcastView(templateType[2]), viewBinding.rightConstraintLayout)
			setView2Layout(getBroadcastView(templateType[3]), viewBinding.bottomRow)

		}

		viewBinding.setWebView.setOnClickListener {
			setView2Layout(getWebView(), viewBinding.leftConstraintLayout)
		}

	}

	private fun getBroadcastView(type: String): View {
		Log.d("XXXXX", "getBroadcastView: $type")
		return when (type) {
			TYPE_IMAGE -> getImageView()
			TYPE_VIDEO -> getVideoView()
			TYPE_WEB -> getWebView()
			else -> getDefaultView()
		}
	}

	private fun getDefaultView(): View {
		val imageLayout = LayoutInflater.from(this).inflate(R.layout.layout_image_broadcast, null)
		val imageView = imageLayout.findViewById<ImageView>(R.id.image)
		imageView.setBackgroundColor(getColor(R.color.teal_700))
		return imageLayout
	}

	private fun getVideoView(): View {
		val videoView = LayoutInflater.from(this).inflate(R.layout.layout_video_broadcast, null)
		val playerView = videoView.findViewById<PlayerView>(R.id.playerView)
		playerView.player = firstPlayer
		return videoView
	}

	private fun getImageView(): View {
		val imageLayout = LayoutInflater.from(this).inflate(R.layout.layout_image_broadcast, null)
		val imageView = imageLayout.findViewById<ImageView>(R.id.image)
		imageView.setImageDrawable(getDrawable(R.drawable.audio))
		return imageLayout
	}

	private fun getWebView(): View {
		val webLayout = LayoutInflater.from(this).inflate(R.layout.layout_web_broadcast, null)
		val webView = webLayout.findViewById<WebView>(R.id.web_view)
		webView.apply {
			settings.builtInZoomControls = true
			loadUrl("file:///android_asset/index.html")
//			loadData("<html><body>Hello, world!</body></html>", "text/html", "UTF-8")
		}
		return webLayout
	}

	private fun setView2Layout(view: View?, layout: ConstraintLayout) {
		Log.d("XXXXX", "setView2Layout: $view, $layout")
		view.removeSelf()
		Log.d("XXXXX", "setView2Layout: ${layout.indices}")
		layout.removeAllViews()
		layout.addView(view)
	}

	private fun View?.removeSelf() {
		this ?: return
		val parentView = parent as? ViewGroup ?: return
		parentView.removeView(this)
	}

	private fun changeMidConstraintSet() {
		val midConstraintSet = ConstraintSet().apply {
			clone(viewBinding.midRow)
			clear(R.id.leftConstraintLayout)
			clear(R.id.rightConstraintLayout)
			connect(R.id.rightConstraintLayout, ConstraintSet.TOP, R.id.midRow, ConstraintSet.TOP)
			connect(
				R.id.rightConstraintLayout,
				ConstraintSet.BOTTOM,
				R.id.midRow,
				ConstraintSet.BOTTOM
			)
			connect(
				R.id.rightConstraintLayout,
				ConstraintSet.START,
				R.id.midRow,
				ConstraintSet.START
			)
			connect(
				R.id.rightConstraintLayout,
				ConstraintSet.END,
				R.id.leftConstraintLayout,
				ConstraintSet.START,
				dp2px(4f).toInt()
			)
			setHorizontalWeight(R.id.rightConstraintLayout, midWeight[0])
			connect(R.id.leftConstraintLayout, ConstraintSet.TOP, R.id.midRow, ConstraintSet.TOP)
			connect(
				R.id.leftConstraintLayout,
				ConstraintSet.BOTTOM,
				R.id.midRow,
				ConstraintSet.BOTTOM
			)
			connect(
				R.id.leftConstraintLayout,
				ConstraintSet.START,
				R.id.rightConstraintLayout,
				ConstraintSet.END
			)
			connect(R.id.leftConstraintLayout, ConstraintSet.END, R.id.midRow, ConstraintSet.END)
			setHorizontalWeight(R.id.leftConstraintLayout, midWeight[1])
			setDimensionRatio(R.id.leftConstraintLayout, "16:9")
			setDimensionRatio(R.id.rightConstraintLayout, "16:9")
		}
		midConstraintSet.applyTo(viewBinding.midRow)
//		TransitionManager.beginDelayedTransition(viewBinding.midRow)
	}

	private fun changeVerticalConstraint() {
		val verticalConstraint = ConstraintSet().apply {
			clone(viewBinding.mainLayout)
			setVerticalWeight(R.id.topRow, verticalWeight[0])
			setVerticalWeight(R.id.midRow, verticalWeight[1])
			setVerticalWeight(R.id.bottomRow, verticalWeight[2])
		}
		verticalConstraint.applyTo(viewBinding.mainLayout)
		TransitionManager.beginDelayedTransition(viewBinding.mainLayout)
	}

	fun dp2px(dp: Float): Float {
		return dp * getDensity(applicationContext)
	}

	fun getDensity(context: Context): Float {
		val metrics = context.resources.displayMetrics
		return metrics.density
	}

	private fun initializePlayer() {
		firstPlayer = ExoPlayer.Builder(this)
			.build()
			.also { exoPlayer ->
				exoPlayer.playWhenReady = playWhenReady
				exoPlayer.seekTo(playbackPosition)
//				exoPlayer.setMediaSource(buildHlsMediaSource())
				exoPlayer.addAnalyticsListener(EventLogger())
				exoPlayer.setMediaItem(buildHlsMediaItem())
				exoPlayer.prepare()
			}

//		secondPlayer = ExoPlayer.Builder(this)
//			.build()
//			.also { exoPlayer ->
//				viewBinding.secondPlayer.player = exoPlayer
//				exoPlayer.playWhenReady = playWhenReady
//				exoPlayer.seekTo(playbackPosition)
//				val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp4))
//				exoPlayer.setMediaItem(mediaItem)
//				exoPlayer.addAnalyticsListener(EventLogger())
//				exoPlayer.prepare()
//			}

	}

	private fun buildHlsMediaSource(): MediaSource {
		val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
		return HlsMediaSource.Factory(dataSourceFactory)
			.createMediaSource(MediaItem.fromUri(getString(R.string.media_url_m3u8)))
	}

	private fun buildHlsMediaItem(): MediaItem {
		return MediaItem.Builder()
			.setUri(getString(R.string.media_url_m3u8))
//			.setMimeType(MimeTypes.APPLICATION_M3U8)
			.build()
	}

	private fun hideSystemUI() {
		WindowCompat.setDecorFitsSystemWindows(window, false)
//		WindowInsetsControllerCompat(window, viewBinding.firstPlayer).let { controller ->
//			controller.hide(WindowInsetsCompat.Type.systemBars())
//			controller.systemBarsBehavior =
//				WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//		}
	}

	override fun onStart() {
		super.onStart()
		if (Util.SDK_INT > 23) {
			initializePlayer()
		}
	}

	override fun onResume() {
		super.onResume()
		hideSystemUI()
		if (Util.SDK_INT <= 23 || firstPlayer == null) {
			initializePlayer()
		}
	}

	override fun onPause() {
		super.onPause()
		if (Util.SDK_INT <= 23) {
			releasePlayer()
		}
	}

	override fun onStop() {
		super.onStop()
		if (Util.SDK_INT > 23) {
			releasePlayer()
		}
	}

	private fun releasePlayer() {
		firstPlayer?.let { exoPlayer ->
			playbackPosition = exoPlayer.currentPosition
			currentItem = exoPlayer.currentMediaItemIndex
			playWhenReady = exoPlayer.playWhenReady
			exoPlayer.release()
		}
		firstPlayer = null

//		secondPlayer?.let { exoPlayer ->
//			playbackPosition = exoPlayer.currentPosition
//			currentItem = exoPlayer.currentMediaItemIndex
//			playWhenReady = exoPlayer.playWhenReady
//			exoPlayer.release()
//		}
//		secondPlayer = null
	}
}