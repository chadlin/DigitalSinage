package com.example.loadhtml

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.indices
import com.example.loadhtml.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util
import java.util.*


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

	private val verticalWeight = arrayListOf(0f, 5f, 0.5f)
	private val midWeight = arrayListOf(2f, 1f)

	private val templateType =
		arrayListOf<String>(TYPE_DEFAULT, TYPE_VIDEO, TYPE_WEB, TYPE_IMAGE)

	private var layoutNaming = 0

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
//			switchMidConstraintSet()
			changeMidRowLayout(layoutNaming)
		}

		viewBinding.verticalChange.setOnClickListener {
			changeVerticalConstraint()
		}

		viewBinding.setPlayer.setOnClickListener {
			setView2Layout(getBroadcastView(templateType[0]), viewBinding.topRow)
			setView2Layout(getBroadcastView(templateType[1]), viewBinding.leftConstraintLayout)
			setView2Layout(getBroadcastView(templateType[2]), viewBinding.rightConstraintLayout)
//			setView2Layout(getBroadcastView(templateType[3]), viewBinding.bottomRow)

		}

		viewBinding.addView.setOnClickListener {
			addViewUseLayoutParams()
		}

	}


	private fun changeMidRowLayout(layout: Int) {
		val midRow = viewBinding.midRow
		val midConstraint = ConstraintSet().apply {
			clone(midRow)
			when (layout) {
				0 -> {
					Log.d("XXXXX", "changeMidConstraint: left 16.9")
					resetMidRow()
					setDimensionRatio(R.id.leftConstraintLayout, "16:9")
					constrainHeight(R.id.leftConstraintLayout, MATCH_PARENT)
					constrainWidth(R.id.leftConstraintLayout, MATCH_CONSTRAINT)
					layoutNaming = 1
				}
				1 -> {
					Log.d("XXXXX", "changeMidConstraint: right 16.9")
					resetMidRow()
					setDimensionRatio(R.id.rightConstraintLayout, "16:9")
					constrainHeight(R.id.rightConstraintLayout, MATCH_PARENT)
					constrainWidth(R.id.rightConstraintLayout, MATCH_CONSTRAINT)
					layoutNaming = 2
				}
				else -> {
					Log.d("XXXXXX", "changeMidConstraint: else")
					resetMidRow()
					setHorizontalWeight(R.id.leftConstraintLayout, 2f)
					setHorizontalWeight(R.id.rightConstraintLayout, 1f)
					layoutNaming = 0
				}
			}
		}
		midConstraint.applyTo(midRow)
	}

	private fun ConstraintSet.resetMidRow() {
		clear(R.id.leftConstraintLayout)
		clear(R.id.rightConstraintLayout)
		connect(R.id.rightConstraintLayout, ConstraintSet.TOP, R.id.midRow, ConstraintSet.TOP)
		connect(
			R.id.rightConstraintLayout,
			ConstraintSet.BOTTOM,
			R.id.midRow,
			ConstraintSet.BOTTOM
		)
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
			R.id.midRow,
			ConstraintSet.START
		)
		connect(
			R.id.leftConstraintLayout,
			ConstraintSet.END,
			R.id.rightConstraintLayout,
			ConstraintSet.START,
		)
		connect(
			R.id.rightConstraintLayout,
			ConstraintSet.END,
			R.id.midRow,
			ConstraintSet.END
		)
		connect(
			R.id.rightConstraintLayout,
			ConstraintSet.START,
			R.id.leftConstraintLayout,
			ConstraintSet.END,
		)
	}

	private val xArray = arrayOf(
		arrayOf(3), arrayOf(1)
	)

	private val yArray = arrayOf(
		arrayOf(1, 4), arrayOf(5)
	)

	fun getRandomColor(): Int {
		val rnd = Random()
		return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
	}


	private fun addViewUseLayoutParams() {
		val constraintLayout = ConstraintLayout(this)
		constraintLayout.id = getLayoutCounter()
		constraintLayout.background = getDrawable(R.color.teal_700)
		constraintLayout.layoutParams = ViewGroup.LayoutParams(
			LayoutParams.MATCH_PARENT,
			LayoutParams.MATCH_PARENT
		)
		//??????????????????
		setContentView(constraintLayout)

		setColumnLayout(constraintLayout, xArray)

		constraintLayout.forEachIndexed { index, view ->
			setRowLayout(view as ConstraintLayout, yArray[index])
		}

	}

	var counter = 0

	private fun getLayoutCounter(): Int {
		return counter++
	}

	private fun setColumnLayout(parentLayout: ConstraintLayout, weightArray: Array<*>) {


		for (i in weightArray.indices) {
			val constraintLayout = ConstraintLayout(this).apply {
				id = getLayoutCounter()
				background =
					getDrawable(if (i % 2 == 0) R.color.purple_500 else com.google.android.material.R.color.primary_dark_material_light)
			}

			val textView = TextView(this).apply {
				text = constraintLayout.id.toString()
				gravity = Gravity.CENTER_VERTICAL
				textSize = 50f
			}
			constraintLayout.addView(textView)

			val constraintLayoutParams = LayoutParams(200, LayoutParams.MATCH_PARENT)
			if (i == 0) {
				constraintLayoutParams.topToTop = parentLayout.id
				constraintLayoutParams.leftToLeft = parentLayout.id
			} else {
				constraintLayoutParams.topToTop = parentLayout.id
				constraintLayoutParams.startToEnd = constraintLayout.id - 1
			}

			constraintLayout.layoutParams = constraintLayoutParams
			Log.d(
				"XXXXX",
				"setColumnLayout: $i, ${constraintLayout.id}, ${constraintLayout.background} ${parentLayout.id}"
			)
			parentLayout.addView(constraintLayout)
		}
	}

	private fun setRowLayout(parentLayout: ConstraintLayout, weightArray: Array<*>) {
		for (j in weightArray.indices) {
			val constraintLayout = ConstraintLayout(this).apply {
				id = getLayoutCounter()
				background = getDrawable(if (j % 2 == 0) R.color.teal_200 else R.color.white)
			}

			val constraintLayoutParams: ConstraintLayout.LayoutParams =
				ConstraintLayout.LayoutParams(
					150, 200
				)


			if (j == 0) {
				constraintLayoutParams.rightToRight = parentLayout.id
				constraintLayoutParams.topToTop = parentLayout.id
			} else {
				constraintLayoutParams.topToBottom = constraintLayout.id - 1
				constraintLayoutParams.rightToRight = parentLayout.id
			}

			constraintLayout.layoutParams = constraintLayoutParams
			Log.d("XXXXX", "setRowLayout: ${constraintLayout.id}")
			parentLayout.addView(constraintLayout)

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
//			loadData("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>Document</title></head><body style=\"background-color: red;\">Hello World</body></html>", "text/html", "UTF-8")

		}
		return webLayout
	}

	private fun getEmbededWebView(): View {
//		checkWebView()
		return LayoutInflater.from(this).inflate(R.layout.layout_web_broadcast, null).apply {
			val webView = findViewById<WebView>(R.id.web_view)
			webView.apply {
				webViewClient = WebViewClient()
				scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
				isHorizontalScrollBarEnabled = false
				isVerticalScrollBarEnabled = false
				settings.apply {
					setAppCacheEnabled(true)
					cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
					javaScriptEnabled = true
					userAgentString =
						"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.137 Safari/537.36"
					mediaPlaybackRequiresUserGesture = false
				}
				loadUrl("https://www.youtube.com/watch?v=tq5pFhxZKFc")
				setOnTouchListener { _, _ -> true }
			}
		}
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

	private fun switchMidConstraintSet() {
		val midConstraintSet = ConstraintSet().apply {
			clone(viewBinding.midRow)
			resetMidRow()
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
			)
			setDimensionRatio(R.id.rightConstraintLayout, "16:9")
			constrainWidth(
				R.id.rightConstraintLayout,
				ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
			)
			constrainHeight(R.id.rightConstraintLayout, ConstraintLayout.LayoutParams.MATCH_PARENT)
			connect(
				R.id.leftConstraintLayout,
				ConstraintSet.START,
				R.id.rightConstraintLayout,
				ConstraintSet.END
			)
			connect(R.id.leftConstraintLayout, ConstraintSet.END, R.id.midRow, ConstraintSet.END)
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
				exoPlayer.addAnalyticsListener(EventLogger())
				val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp4))
				exoPlayer.setMediaItem(mediaItem)
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