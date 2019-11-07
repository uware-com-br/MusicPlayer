package br.com.uware.musicplayer.adapter

import android.content.Context
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.uware.musicplayer.R
import kotlinx.android.synthetic.main.content_musicas.view.*
import java.text.SimpleDateFormat

class MusicAdapter (musicList: ArrayList<Uri>,
          private var ctx: Context,
                    private val callback: (Uri,Int) -> Unit,
                    private val callbackMusic: () -> Int,
                    private val callbackDelete: (Int) -> Unit): RecyclerView.Adapter<MusicAdapter.ViewHolder>(){

    private var musicList: ArrayList<Uri> = ArrayList<Uri>()
    init {
        this.musicList = musicList
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.content_musicas, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val music = musicList[position]
        var mmr = MediaMetadataRetriever()
        mmr.setDataSource(ctx,music)
        holder.title.text = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        holder.author.text = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        holder.lay.setOnClickListener {
            callback(music,position)
        }
        var mediaPlayer = MediaPlayer.create(ctx,music)
        val sdf = SimpleDateFormat("mm:ss")
        holder.time.text = sdf.format(mediaPlayer!!.duration).toString()
        mediaPlayer.release()

        if(position == callbackMusic()){
            holder.lay.setBackgroundResource(R.drawable.background_item_list_active)
            holder.author.setTextColor(Color.WHITE)
            holder.title.setTextColor(Color.YELLOW)
            holder.time.setTextColor(Color.WHITE)
        } else{
            holder.lay.setBackgroundResource(R.drawable.background_item_list)
            holder.author.setTextColor(Color.BLACK)
            holder.title.setTextColor(Color.parseColor("#008577"))
            holder.time.setTextColor(Color.BLACK)
        }
        holder.del.setOnClickListener {
            callbackDelete(position)
        }
    }
    override fun getItemCount(): Int {
        return musicList.size
    }
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var lay = view.layAdp
        var author = view.tvAdpAuthor
        var title = view.tvAdpTitle
        var time = view.tvAdpTime
        var del = view.btnAdpDel
    }
}