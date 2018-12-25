package com.joker.andoridviewset

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.View


class CountdownTextView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int):
    AppCompatTextView(context, attrs, defStyleAttr), View.OnClickListener {

    override fun onClick(v: View?) {
        mFinishedListener?.onCountDownFinished()
        removeCallbacks(countdownRunnable)
    }

    constructor(context: Context?):this(context,null,0)

    constructor(context: Context?,attrs: AttributeSet?):this(context,attrs,0)

    private var defaultCount=5

    //定义
    private var mFinishedListener:FinishedListener?=null

    private val countdownRunnable= Runnable{}.apply {
        --defaultCount
        if (defaultCount>1) {
            this@CountdownTextView.text = "$defaultCount 秒 | 跳过"
            postDelayed(this, 1000)
        } else {
            mFinishedListener?.onCountDownFinished()
            removeCallbacks(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(countdownRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(countdownRunnable)
    }

    fun setFinishListener(listener: FinishedListener){
        mFinishedListener=listener
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(this)
    }

    public interface FinishedListener{
        fun onCountDownFinished()
    }
}