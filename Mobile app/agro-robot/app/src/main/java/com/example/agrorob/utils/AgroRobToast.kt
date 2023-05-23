package com.example.agrorob.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.Toast

class AgroRobToast(context: Context) : Toast(context) {

    private val mContext = context

    override fun show() {
        super.show()
        val view = this.view
        if (view != null) {
            val iconView = view.findViewById<View>(android.R.id.icon)
            if (iconView is ImageView) {
                iconView.setImageDrawable(mContext.applicationInfo.loadIcon(mContext.packageManager))
            }
        }
    }
}