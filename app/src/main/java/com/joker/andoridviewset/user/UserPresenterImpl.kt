package com.joker.andoridviewset.user

import com.lzy.okgo.OkGo

import android.content.Context


class UserPresenterImpl(private val mView: UserContract.View) : UserContract.Presenter {

    private val mNetReqTag = Any()


    fun onDestroy() {
        OkGo.getInstance().cancelTag(mNetReqTag)
    }

}
