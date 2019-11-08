package br.com.uware.musicplayer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.uware.musicplayer.adapter.MusicAdapter
import android.widget.SeekBar
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.view.animation.AlphaAnimation

//  Autor: Rodrigo Leutz
//  Licensa: GPL

class MainActivity : AppCompatActivity() {
    var check: Boolean = false
    var menu: Boolean = true
    var menuVol: Boolean = false
    // Adapter
    var musicList = ArrayList<Uri>()
    var musicAdapter: MusicAdapter? = null
    var linearLayoutManager: LinearLayoutManager? = null
    // Player
    var mediaPlayer: MediaPlayer? = null
    private var currentMusic: Int = -1
    private val handler = Handler()
    // Volume
    var audioManager: AudioManager? = null
    var volMax: Int = 0
    var volCur: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPermissions()
        showVolume()
        initVolume()
        initView()
        openMenu()
        fabAdd.setOnClickListener {
            openMenu()
        }
        fabAddFile.setOnClickListener {
            if(check) openFile()
            else errorPermission()
        }
        fabClear.setOnClickListener {
            musicList.clear()
            initView()
        }
        fabVolume.setOnClickListener {
            showVolume()
        }
        // Player Buttons
        btnPlay.setOnClickListener {
            playSong()
        }
        btnStop.setOnClickListener {
            stop()
        }
        btnPause.setOnClickListener {
            mediaPlayer?.pause()
        }
        btnSkipNext.setOnClickListener {
            next()
        }
        btnSkipPrevious.setOnClickListener {
            previous()
        }
        btnFastForward.setOnClickListener {
            if(mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying){
                    mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition+5000)
                }
            }
        }
        btnFastRewind.setOnClickListener {
            if(mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying){
                    mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition-5000)
                }
            }
        }
        // ProgressBar Player
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    if(mediaPlayer != null) mediaPlayer!!.seekTo(i)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
        // Seek Volume
        seekVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                audioManager!!.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    i,
                    AudioManager.FLAG_SHOW_UI
                )
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }
    // Player
    private fun playAdp(uri: Uri, pos: Int){
        clearMediaPlayer()
        currentMusic = pos
        mediaPlayer = MediaPlayer.create(this, uri)
        if(mediaPlayer != null) {
            playSong()
            initView()
            recycler.scrollToPosition(currentMusic)
        }
    }
    private fun playSong(){
        if(musicList.size != 0 && currentMusic != -1) {
            if (mediaPlayer != null) {
                if (!mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.start()
                    mediaPlayer!!.setOnCompletionListener {
                        next()
                    }
                    var mmr = MediaMetadataRetriever()
                    mmr.setDataSource(this, musicList[currentMusic])
                    tvControlTitle.text =
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) + " - " + mmr.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_TITLE
                        )
                    playControl()
                } else {
                    mediaPlayer!!.stop()
                    playSong()
                }
            }
        }
    }
    private fun playControl(){
        seekBar.max = mediaPlayer!!.duration
        setCurrentControl()
    }
    private fun setCurrentControl(){
        handler.postDelayed({
            val sdf = SimpleDateFormat("mm:ss")
            val d = mediaPlayer!!.currentPosition
            tvTime.text = sdf.format(d).toString() + " - " + sdf.format(mediaPlayer!!.duration).toString()
            seekBar.progress = d
            setCurrentControl()
        }, 1000)
    }
    private fun stop(){
        if(mediaPlayer != null) {
            clearMediaPlayer()
            seekBar.progress = 0
            if(musicList.size != 0 && currentMusic != -1) {
                mediaPlayer = MediaPlayer.create(this, musicList[currentMusic])
                val sdf = SimpleDateFormat("mm:ss")
                val d = mediaPlayer!!.currentPosition
                tvTime.text =
                    sdf.format(d).toString() + " - " + sdf.format(mediaPlayer!!.duration).toString()
            }
        }
    }
    private fun clearMediaPlayer(){
        if(mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
            handler.removeCallbacksAndMessages(null)
        }
    }
    private fun next(){
        if(musicList.size != 0 && currentMusic != -1) {
            val next = currentMusic + 1
            if (next == musicList.size) {
                currentMusic = 0
            } else {
                currentMusic = next
            }
            playAdp(musicList[currentMusic], currentMusic)
        }
        else{
            erroNoList()
        }
    }
    private fun previous(){
        if(musicList.size != 0 && currentMusic != -1){
            val back = currentMusic -1
            if(back < 0){
                currentMusic = musicList.size - 1
            } else {
                currentMusic = back
            }
            playAdp(musicList[currentMusic], currentMusic)
        }
        else{
            erroNoList()
        }
    }
    private fun erroNoList(){
        Toast.makeText(this, "Não existe lista de músicas.", Toast.LENGTH_SHORT).show()
    }
    // Volume
    private fun showVolume(){
        if(menuVol){
            menuVol = false
            Anime().fadeOut(seekVolume)
        }
        else {
            Anime().fadeIn(seekVolume)
            menuVol = true
            if(menuVol) {
                Handler().postDelayed({
                    if(seekVolume.visibility == View.VISIBLE) Anime().fadeOut(seekVolume)
                    menuVol = false
                }, 7000)
            }
        }
    }
    private fun initVolume(){
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        volMax = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volCur = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        seekVolume.max = volMax
        seekVolume.progress = volCur
    }
    // Player RecyclerView
    private fun removeMusic(position: Int){
        if(position == showCurrentMusic()) currentMusic = -1
        else if(position < showCurrentMusic()) currentMusic -= 1
        musicList.removeAt(position)
        musicAdapter!!.notifyItemRemoved(position)
        Handler().postDelayed({
            musicAdapter!!.notifyDataSetChanged()
        },500)
    }
    private fun showCurrentMusic(): Int{
        return currentMusic
    }
    // Menu
    private fun openMenu(){
        if(menu){
            menu = false
            Handler().postDelayed({
                fabAddFile.hide()
            },250)
            Handler().postDelayed({
                fabClear.hide()
            },500)
        }
        else{
            menu = true
            Handler().postDelayed({
                fabAddFile.show()
            },750)
            Handler().postDelayed({
                fabClear.show()
            },500)
        }
    }
    // Abrir diretório
    private fun openFile() {
        val intent = Intent()
            .setType("audio/*")
            .setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == RESULT_OK) {
            val selectedFile = data?.data
            musicList.add(selectedFile!!)
            val position = musicAdapter!!.itemCount+1
            musicAdapter!!.notifyItemInserted(position)
        }
    }
    // Load Recycler
    private fun initView(){
        musicAdapter = MusicAdapter(musicList,this, this::playAdp,this::showCurrentMusic,this::removeMusic)
        linearLayoutManager = LinearLayoutManager(this)
        recycler.layoutManager = linearLayoutManager
        recycler.adapter = musicAdapter
    }
    // Permissions
    private fun initPermissions(){
        if(!getPermission()) setPermission()
        else check = true
    }
    private fun getPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }
    private fun setPermission(){
        val permissionsList = listOf<String>(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        ActivityCompat.requestPermissions(this, permissionsList.toTypedArray(), 1235)
    }
    private fun errorPermission(){
        Toast.makeText(this, "Não tem permissão para ler arquivos.", Toast.LENGTH_SHORT).show()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            1235 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("Permission: ", "Permission has been denied by user")
                    errorPermission()
                } else {
                    Log.i("Permission: ", "Permission has been granted by user")
                    check = true
                }
            }
        }
    }
    class Anime {
        fun fadeOut(view: View){
            val time = 1000L
            val animation = AlphaAnimation(1f,0f)
            animation.duration = time
            animation.repeatCount = 0
            view.startAnimation(animation)
            Handler().postDelayed({
                view.visibility = View.GONE
            }, time)
        }
        fun fadeIn(view: View){
            val time = 1000L
            val animation = AlphaAnimation(0f,1f)
            animation.duration = time
            animation.repeatCount = 0
            view.startAnimation(animation)
            Handler().postDelayed({
                view.visibility = View.VISIBLE
            }, time)
        }
    }
}