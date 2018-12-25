package com.joker.andoridviewset.main

import vip.meilianhui.base.mvp.BasePresenter
import vip.meilianhui.base.mvp.BaseView


interface MainContract {

    interface Presenter : BasePresenter

    interface View : BaseView<MainContract.Presenter>
}