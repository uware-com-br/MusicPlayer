package br.com.uware.musicplayer.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.uware.musicplayer.R

class MusicAdapter (musicList: ArrayList<Uri>,
                    private var context: Context): RecyclerView.Adapter<MusicAdapter.ViewHolder>(){
    private var musicList = ArrayList<Uri>()
    init {
        this.musicList = musicList
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.content_musicas, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val music = musicList[position]
    }
    override fun getItemCount(): Int {
        return musicList.size
    }
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
    }
}