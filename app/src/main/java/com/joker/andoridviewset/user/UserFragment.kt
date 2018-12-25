package com.joker.andoridviewset.user

import vip.meilianhui.R
import vip.meilianhui.base.fragment.BaseFragment
import android.os.Bundle


class UserFragment : BaseFragment(), UserContract.View {

    private var mPresenter: UserContract.Presenter? = null

    protected val layoutResource: Int
        get() = R.layout.fragment_user

    protected fun inflateView(savedInstanceState: Bundle) {

    }

    protected fun inflateDate(savedInstanceState: Bundle) {
        mPresenter = UserPresenterImpl(this)
        wrapPresenter(mPresenter)
    }


}
