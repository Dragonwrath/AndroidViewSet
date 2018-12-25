package com.joker.andoridviewset.main

import com.lzy.okgo.OkGo

import android.content.Context


class MainPresenterImpl(private val mView: MainContract.View) : MainContract.Presenter {

    private val mNetReqTag = Any()


    fun onDestroy() {
        OkGo.getInstance().cancelTag(mNetReqTag)
    }

}
