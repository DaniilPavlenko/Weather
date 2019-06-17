package ru.dpav.weather

import android.support.v7.widget.RecyclerView
import android.view.View
import com.arellomobile.mvp.MvpDelegate

abstract class MvpViewHolder(private val mParentDelegate: MvpDelegate<*>, itemView: View)
	: RecyclerView.ViewHolder(itemView) {
	private var mMvpDelegate: MvpDelegate<*>? = null

	protected val mvpDelegate: MvpDelegate<*>?
		get() {
			if (getMvpChildId() == null) {
				return null
			}
			if (mMvpDelegate == null) {
				mMvpDelegate = MvpDelegate(this)
				mMvpDelegate!!.setParentDelegate(mParentDelegate, getMvpChildId())
			}
			return mMvpDelegate
		}

	protected fun destroyMvpDelegate() {
		if (mvpDelegate != null) {
			mvpDelegate!!.onSaveInstanceState()
			mvpDelegate!!.onDetach()
			mMvpDelegate = null
		}
	}

	protected fun createMvpDelegate() {
		if (mvpDelegate != null) {
			mvpDelegate!!.onCreate()
			mvpDelegate!!.onAttach()
		}
	}

	protected abstract fun getMvpChildId(): String?
}
