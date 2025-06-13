package com.skysinc.tvplus.ui.presenter

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.skysinc.tvplus.Channel

class ChannelPresenter<T> : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val context = parent.context

        val cardView = ImageCardView(context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(313, 176) // largura x altura padrão dos cards
        }

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val channel = item as Channel
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = channel.name
        cardView.contentText = channel.category
        Glide.with(cardView.context)
            .load(channel.thumbnailUrl)
            .centerCrop()
            .into(cardView.mainImageView)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.mainImage = null
    }
}
