package com.joker.andoridviewset.guide

import android.content.Context
import android.graphics.*
import android.support.annotation.DrawableRes
import android.support.annotation.IntDef
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import vip.meilianhui.R
import java.lang.ref.WeakReference
import java.util.concurrent.LinkedBlockingQueue

const val ANCHOR_GRAVITY_LEFT = 0x00000001
const val ANCHOR_GRAVITY_TOP = 0x00000010
const val ANCHOR_GRAVITY_RIGHT = 0x00000100
const val ANCHOR_GRAVITY_BOTTOM = 0x00001000

@Target(AnnotationTarget.VALUE_PARAMETER)
@IntDef(value = [ANCHOR_GRAVITY_LEFT,ANCHOR_GRAVITY_TOP,ANCHOR_GRAVITY_RIGHT,ANCHOR_GRAVITY_BOTTOM,
  ANCHOR_GRAVITY_LEFT.or(ANCHOR_GRAVITY_TOP),ANCHOR_GRAVITY_RIGHT.or(ANCHOR_GRAVITY_TOP),
  ANCHOR_GRAVITY_LEFT.or(ANCHOR_GRAVITY_BOTTOM),ANCHOR_GRAVITY_RIGHT.or(ANCHOR_GRAVITY_BOTTOM)])
annotation class AnchorGravity

class UserGuideSurfaceView(context : Context?,attrs : AttributeSet?,defStyleAttr : Int) :
    SurfaceView(context,attrs,defStyleAttr),SurfaceHolder.Callback {

  private val thread : GuideRenderThread = GuideRenderThread(this)
  private var lastJob : Job? = null


  override fun surfaceChanged(holder : SurfaceHolder?,format : Int,width : Int,height : Int) {
    lastJob?.apply { thread.queue.put(this) }
  }

  override fun surfaceDestroyed(holder : SurfaceHolder?) {
    thread.isRunning = false
    thread.interrupt()
    Log.e(UserGuideSurfaceView::class.java.simpleName,"surfaceDestroyed")

  }

  override fun surfaceCreated(holder : SurfaceHolder?) {
    thread.start()
    render(null,0)
    Log.e(UserGuideSurfaceView::class.java.simpleName,"surfaceCreated")
  }

  constructor(context : Context?) : this(context,null,0)
  constructor(context : Context?,attrs : AttributeSet?) : this(context,attrs,0)

  init {
    //不能设置该属性，无需关闭硬件加速
    //    setLayerType(View.LAYER_TYPE_SOFTWARE,null)
    setBackgroundColor(Color.argb(255,255,231,115));
    setZOrderOnTop(true)

  }


  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    holder.setFormat(PixelFormat.TRANSPARENT)
    holder.addCallback(this)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    holder.removeCallback(this)
  }


  fun render(holeRectF : RectF?,@DrawableRes drawable : Int) {
    render(holeRectF,true,ANCHOR_GRAVITY_TOP,drawable)
  }

  fun render(holeRectF : RectF?,isCircle : Boolean,anchorGravity : Int,@DrawableRes drawable : Int) {
    val job = Job(isCircle,anchorGravity,holeRectF,drawable)
    thread.queue.put(job)
    lastJob = job
  }

  class GuideRenderThread(view : SurfaceView) : Thread() {
    private val viewRef = WeakReference<SurfaceView>(view)
    internal val queue = LinkedBlockingQueue<Job>()
    internal var isRunning = true

    private val maskPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.FILL
          color = view.resources.getColor(R.color.translucent)
        }

    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cornerRadius = view.resources.getDimension(R.dimen.m_space)

    override fun run() {
      super.run()
      while (isRunning) {
        try {
          val job = queue.take()
          val view = viewRef.get()
          if (view != null) {
            val holder = view.holder
            val canvas = holder.lockCanvas(Rect(0,0,view.width,view.height))
            try {
              canvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR)


              //draw background
              Log.e(UserGuideSurfaceView::class.java.simpleName,"job ${view.width}---- ${view.height}")

              canvas.drawRect(0F,0F,view.width.toFloat(),view.height.toFloat(),maskPaint)
              if (job.maskRect != null) {
                //draw mask
                if (job.isCircle) {
                  canvas.drawOval(job.maskRect,holePaint)
                } else {
                  canvas.drawRoundRect(job.maskRect,cornerRadius,cornerRadius,holePaint)
                }

                //draw bitmap
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeResource(view.resources,job.drawable,options)
                var x = 0F
                var y = 0F
                var scaleX = 0F
                var scaleY = 0F
                if (job.direction.and(0x000F) == ANCHOR_GRAVITY_LEFT) {
                  x = job.maskRect.left - options.outWidth
                  if (x < 0) {
                    scaleX = job.maskRect.left / options.outWidth
                  }
                }

                if (job.direction.and(0x00F0) == ANCHOR_GRAVITY_TOP) {
                  y = job.maskRect.top - options.outHeight
                  if (y < 0) {
                    scaleY = job.maskRect.top / options.outHeight
                  }
                }

                if (job.direction.and(0x0F00) == ANCHOR_GRAVITY_RIGHT) {
                  x = job.maskRect.right + options.outWidth
                  if (x > view.width) {
                    scaleX = (view.width - job.maskRect.right - 0.5f) / options.outWidth
                  }
                  x = job.maskRect.right
                }

                if (job.direction.and(0xF000) == ANCHOR_GRAVITY_BOTTOM) {
                  y = job.maskRect.bottom + options.outHeight
                  if (y > view.height) {
                    scaleY = (view.height - job.maskRect.bottom - 0.5f) / options.outHeight
                  }
                  y = job.maskRect.bottom
                }

                val scale : Float = if (scaleX > 0 && scaleY > 0) {
                  Math.min(scaleX,scaleY)
                } else if (scaleX > 0) {
                  scaleX
                } else if (y > 0) {
                  scaleY
                } else {
                  1F
                }
                options.inJustDecodeBounds = false
                if (scale != 1F) {
                  options.inSampleSize = Math.ceil(Math.log((1 / scale).toDouble()) / Math.log(2.0)).toInt()
                }
                val bitmap = BitmapFactory.decodeResource(view.resources,job.drawable,options)
                canvas.drawBitmap(bitmap,x,y,bitmapPaint)
              }
            } finally {
              holder.unlockCanvasAndPost(canvas)
            }
          }
          Log.e(UserGuideSurfaceView::class.java.simpleName,"job   $job  finished")
        } catch (ex : Exception) {
          ex.printStackTrace()
          if (ex is InterruptedException) {
            isRunning = false
          } else if (ex is OutOfMemoryError) {
            System.gc()
          }
        }
      }
      Log.e(UserGuideSurfaceView::class.java.simpleName,"all is interrupted")

    }
  }

}

data class Job(
    val isCircle : Boolean = true,
    val direction : Int = ANCHOR_GRAVITY_TOP,
    val maskRect : RectF?,@DrawableRes val drawable : Int
)