package com.example.livetv.ui

import android.os.Bundle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.example.livetv.Channel
import com.example.livetv.ui.presenter.ChannelPresenter


class MainFragment : BrowseSupportFragment() {

    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupUI()
        loadChannels()
    }

    private fun setupUI() {
        title = "TVPlus"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
    }

    private fun loadChannels() {
        val categories = listOf("Filmes", "Notícias", "Infantil")

        for (category in categories) {
            val channels = listOf(
                Channel("Canal 1", "http://stream1.m3u8", "https://via.placeholder.com/300x200.png?text=Canal+1", category),
                Channel("Canal 2", "http://stream2.m3u8", "https://via.placeholder.com/300x200.png?text=Canal+2", category)
            )

            val listRowAdapter = ArrayObjectAdapter(ChannelPresenter<Any>())
            channels.forEach { listRowAdapter.add(it) }

            val header = HeaderItem(category)
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }

        adapter = rowsAdapter
    }
}
