package com.joker.andoridviewset.guide;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import vip.meilianhui.R;

/**
 * 支持垂直滚动的GuideView
 */
public class UserGuideView extends View implements Handler.Callback{

  public final static int ANCHOR_GRAVITY_LEFT = 0x00000001;
  public final static int ANCHOR_GRAVITY_TOP = 0x00000010;
  public final static int ANCHOR_GRAVITY_RIGHT = 0x00000100;
  public final static int ANCHOR_GRAVITY_BOTTOM = 0x00001000;

  private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private final Paint lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private final static int CODE_ENQUEUE_JOB = 1;

  private HandlerThread thread = new HandlerThread("GuideView");

  private Handler handler;

  private float cornerRadius;

  private Job lastJob;

  private final Matrix matrix = new Matrix();
  float x = 0F;
  float y = 0F;
  float scaleX = 0F;
  float scaleY = 0F;

  public UserGuideView(Context context){
    this(context,null,0);
  }

  public UserGuideView(Context context,
                       @Nullable AttributeSet attrs){
    this(context,attrs,0);
  }

  public UserGuideView(Context context,@Nullable AttributeSet attrs,int defStyleAttr){
    super(context,attrs,defStyleAttr);
    maskPaint.setColor(getResources().getColor(R.color.translucent));
    lightPaint.setColor(Color.TRANSPARENT);
    lightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    cornerRadius = getResources().getDimension(R.dimen.m_space);
    setLayerType(View.LAYER_TYPE_SOFTWARE,null);
  }

  @Override protected void onAttachedToWindow(){
    super.onAttachedToWindow();
    thread.start();
    handler = new Handler(thread.getLooper(),this);
  }

  @Override protected void onDetachedFromWindow(){
    super.onDetachedFromWindow();
    thread.quitSafely();
    handler = null;
  }

  public void enqueueNewGuideMask(@NonNull RectF maskRectF,@DrawableRes int drawable){
    enqueueNewGuideMask(true,ANCHOR_GRAVITY_TOP,maskRectF,drawable);
  }

  public void enqueueNewGuideMask(boolean isCircle,int direction,@NonNull RectF maskRectF,
                                  @DrawableRes int drawable){
    if(handler != null){
      Message message = Message.obtain();
      message.what = CODE_ENQUEUE_JOB;
      message.obj = new Job(isCircle,direction,maskRectF,drawable);
      handler.sendMessage(message);
    }
  }

  @Override public boolean handleMessage(Message msg){
    if(msg.what == CODE_ENQUEUE_JOB){
      Job job = (Job)msg.obj;
      try{
        if(job != null){
          job.bitmap = BitmapFactory.decodeResource(getResources(),job.drawableId);
          if(job.bitmap != null){
//            boolean leftAnchor = isLeftAnchor(job);

            boolean topAnchor = isTopAnchor(job);

//            boolean rightAnchor = isRightAnchor(job);

            boolean bottomAnchor = isBottomAnchor(job);

//            float left = job.maskRectF.centerX() - job.bitmap.getWidth() / 2;
//            float right = job.maskRectF.centerX() + job.bitmap.getWidth() / 2;
//            if(left < 0){
//              x = 0;
//              if(right > getWidth()){
//                scaleX = 1 - (right - getWidth()) / job.bitmap.getWidth();
//              }
//            }else if(right > getWidth()){
//              if(job.bitmap.getWidth() > getWidth()){
//                scaleX = job.bitmap.getWidth()/ getWidth();
//                x = 0;
//              } else {
//                x= job.maskRectF.left-right+getWidth();
//              }
//            }

//            supportHeightScale(job,topAnchor,bottomAnchor);

//            float scale = 1F;
//            if(scaleX > 0 && scaleY > 0){
//              scale = Math.min(scaleX,scaleY);
//            }else if(scaleX > 0){
//              scale = scaleX;
//            }else if(scaleY > 0){
//              scale = scaleY;
//            }
//            if(scale != 0){
//              matrix.reset();
//              matrix.postScale(scale,scale);
//            }

            matrix.postTranslate(x,y);
            lastJob = job;
            postInvalidate();
            return true;
          }
        }
      }catch(Exception ex){
        //nothing
      }
    }
    return false;
  }

  private void validateWidthScale(Job job){
    //如果没有设置 左右的锚点
    float left = job.maskRectF.centerX() - job.bitmap.getWidth() / 2;
    float right = job.maskRectF.centerX() + job.bitmap.getWidth() / 2;
    if(left < 0){
      x = 0;
      if(right > getWidth()){
        scaleX = 1 - (right - getWidth()) / job.bitmap.getWidth();
      }
    }else if(right > getWidth()){
      scaleX = 1 - (right - getWidth()) / job.bitmap.getWidth();
    }
  }

  private void supportHeightScale(Job job,boolean topAnchor,boolean bottomAnchor){
    //如果没有设置 上下的锚点
    if(!topAnchor && !bottomAnchor){
      float top = job.maskRectF.centerY() - job.bitmap.getHeight() / 2;
      float bottom = job.maskRectF.centerY() + job.bitmap.getHeight() / 2;
      if(top < 0){
        y = 0;
      }
    }
  }

  private boolean isBottomAnchor(Job job){
    boolean bottomAnchor = (job.direction & 0x00001000) == ANCHOR_GRAVITY_BOTTOM;
    if(bottomAnchor){
      float totalHeight = job.maskRectF.bottom + job.bitmap.getHeight();
      if(totalHeight > getHeight()){
        scaleY = 1 - (totalHeight - getHeight() - 0.5F) * 1F / getHeight();
      }
      y = job.maskRectF.bottom;
    }
    return bottomAnchor;
  }

  private boolean isRightAnchor(Job job){
    boolean rightAnchor = (job.direction & 0x00000100) == ANCHOR_GRAVITY_RIGHT;
    if(rightAnchor){
      x = job.maskRectF.right + job.bitmap.getWidth();
      if(x > getWidth()){
        scaleX = 1 - (x - getWidth() - 0.5f) * 1F / job.bitmap.getWidth();
      }
      x = job.maskRectF.right;
    }
    return rightAnchor;
  }

  private boolean isTopAnchor(Job job){
    boolean topAnchor = (job.direction & 0x00000010) == ANCHOR_GRAVITY_TOP;
    if(topAnchor){
      y = job.maskRectF.top - job.bitmap.getHeight();
      if(y < 0){
        scaleY = 1 - (y + 0.5F) * 1F / job.bitmap.getHeight();
        y = 0;
      }
    }
    return topAnchor;
  }

  private boolean isLeftAnchor(Job job){
    boolean leftAnchor = (job.direction & 0x00000001) == ANCHOR_GRAVITY_LEFT;
    if(leftAnchor){
      x = job.maskRectF.left - job.bitmap.getWidth();
      if(x < 0){
        scaleX = 1 + (job.maskRectF.left - job.bitmap.getWidth() - 0.5F) * 1F / job.bitmap.getWidth();
        x = 0;
      }
    }
    return leftAnchor;
  }

  @Override protected void onDraw(Canvas canvas){
    super.onDraw(canvas);
    final Job job = lastJob;
    if(job != null){
      //draw background
      canvas.drawRect(0F,0F,getWidth(),getHeight(),maskPaint);
      if(job.maskRectF != null){
        //draw mask
        if(job.isCircle){
          canvas.drawOval(job.maskRectF,lightPaint);
        }else{
          canvas.drawRoundRect(job.maskRectF,cornerRadius,cornerRadius,lightPaint);
        }
        //draw bitmap
        canvas.drawBitmap(job.bitmap,matrix,bitmapPaint);
      }
    }
  }

  private static class Job{
    private final boolean isCircle;
    private final int direction;
    private final RectF maskRectF;
    private final @DrawableRes int drawableId;
    private Bitmap bitmap;

    Job(boolean isCircle,int direction,RectF maskRectF,int drawable){
      this.isCircle = isCircle;
      this.direction = direction;
      this.maskRectF = maskRectF;
      this.drawableId = drawable;
    }

  }
}
