package com.framework.views.blur

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import com.framework.R
import com.framework.views.blur.BlockingBlurController.Companion.TRANSPARENT

/**
 * FrameLayout that blurs its underlying content.
 * Can have children and draw them over blurred background.
 */
class BlurView : FrameLayout {
  var blurController: BlurController = NoOpController()

  @ColorInt
  private var overlayColor = 0

  constructor(context: Context) : super(context) {
    init(null, 0)
  }

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    init(attrs, 0)
  }

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  ) {
    init(attrs, defStyleAttr)
  }

  private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
    val a = context.obtainStyledAttributes(attrs, R.styleable.BlurView, defStyleAttr, 0)
    overlayColor = a.getColor(R.styleable.BlurView_blurOverlayColor, TRANSPARENT)
    a.recycle()
  }

  override fun draw(canvas: Canvas) {
    val shouldDraw: Boolean = blurController.draw(canvas)
    if (shouldDraw) {
      super.draw(canvas)
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    blurController.updateBlurViewSize()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    blurController.setBlurAutoUpdate(false)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    if (!isHardwareAccelerated) {
      Log.e(TAG, "BlurView can't be used in not hardware-accelerated window!")
    } else {
      blurController.setBlurAutoUpdate(true)
    }
  }

  /**
   * @param rootView root to start blur from.
   * Can be Activity's root content layout (android.R.id.content)
   * or (preferably) some of your layouts. The lower amount of Views are in the root, the better for performance.
   * @return [BlurView] to setup needed params.
   */
  fun setupWith(@NonNull rootView: ViewGroup?): BlockingBlurController? {
    val blurController: BlockingBlurController? =
      rootView?.let { BlockingBlurController(this, it, overlayColor) }
    this.blurController.destroy()
    if (blurController != null) {
      this.blurController = blurController
    }
    return blurController
  }
  // Setters duplicated to be able to conveniently change these settings outside of setupWith chain
  /**
   * @see BlurViewFacade.setBlurRadius
   */
  fun setBlurRadius(radius: Float): BlurViewFacade? {
    return blurController.setBlurRadius(radius)
  }

  /**
   * @see BlurViewFacade.setOverlayColor
   */
  fun setOverlayColor(@ColorInt overlayColor: Int): BlurViewFacade? {
    this.overlayColor = overlayColor
    return blurController.setOverlayColor(overlayColor)
  }

  /**
   * @see BlurViewFacade.setBlurAutoUpdate
   */
  fun setBlurAutoUpdate(enabled: Boolean): BlurViewFacade? {
    return blurController.setBlurAutoUpdate(enabled)
  }

  /**
   * @see BlurViewFacade.setBlurEnabled
   */
  fun setBlurEnabled(enabled: Boolean): BlurViewFacade? {
    return blurController.setBlurEnabled(enabled)
  }

  companion object {
    private val TAG = BlurView::class.java.simpleName
  }
}

fun BlurView.setBlur(activity: Activity, value: Float) {
  val decorView: View? = activity.window?.decorView
  val rootView: ViewGroup = decorView?.findViewById(android.R.id.content) as ViewGroup
  val windowBackground: Drawable = decorView.background
  this.setupWith(rootView)?.setFrameClearDrawable(windowBackground)
    ?.setBlurAlgorithm(RenderScriptBlur(activity))?.setBlurRadius(value)
    ?.setHasFixedTransformationMatrix(true)
}