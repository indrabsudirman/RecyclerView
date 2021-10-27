package com.example.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recyclerview.databinding.DetailItemBinding

class EpisodeViewHolderKotlin(val binding: DetailItemBinding): RecyclerView.ViewHolder(binding.root){

    fun bind(episode: Episode){
        binding.imageTitle.text = episode.title

        binding.imageDownload.isVisible = (episode.downloadStatus == DownloadStatus.NOT_DOWNLOADED)
        binding.imageDownload.setOnClickListener {
            /***
             * TODO :
             * 1. call vidioSDK : VidioSDK.downloadEpisode(episodeId: Long) in another thread so it won't block UI
             * 2. when download in progress set `binding.progressView.isVisible = true`
             * 3. when download finish set `binding.downloadButton.isVisible = false` `binding.progressView.isVisible = false`
             *
             * Notes :
             * - Be carefull on accessing view in another thread, since recycleview can be recycled
             */
        }

        Glide.with(co)
                .load(episode.thumbnailUrl)
                .into(binding.image)
    }
}

class EpisodeAdapterKotlin (private val episodes: List<Episode>): RecyclerView.Adapter<EpisodeViewHolderKotlin>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolderKotlin {
        return EpisodeViewHolderKotlin(DetailItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holderKotlin: EpisodeViewHolderKotlin, position: Int) {
        holderKotlin.bind(episodes[position])
    }

    override fun getItemCount(): Int = episodes.size
}

data class Episode(
        val id: Long,
        val title: String,
        val duration: String,
        val thumbnailUrl: String,
        val description: String,
        val isFree: Boolean,
        val downloadStatus: DownloadStatus
)

enum class DownloadStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOAD_FINISH
}
