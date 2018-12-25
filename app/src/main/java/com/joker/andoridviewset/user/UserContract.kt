package com.joker.andoridviewset.user

import vip.meilianhui.base.mvp.BasePresenter
import vip.meilianhui.base.mvp.BaseView


interface UserContract {

    interface Presenter : BasePresenter

    interface View : BaseView<UserContract.Presenter>
}