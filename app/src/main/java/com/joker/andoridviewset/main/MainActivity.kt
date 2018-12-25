package com.joker.andoridviewset.main

import vip.meilianhui.R
import vip.meilianhui.base.activity.BaseActivity
import android.os.Bundle


class MainActivity : BaseActivity(), MainContract.View {

    private var mPresenter: MainContract.Presenter? = null

    protected val layoutResource: Int
        get() = R.layout.activity_main

    protected fun inflateView(savedInstanceState: Bundle) {

    }

    protected fun inflateDate(savedInstanceState: Bundle) {
        mPresenter = MainPresenterImpl(this)
        wrapPresenter(mPresenter)
    }


}
