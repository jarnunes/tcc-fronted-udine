package com.jarnunes.udinetour.holder

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.jarnunes.udinetour.R

class ReceiveViewHolder(itemView: View, fragmentManager: FragmentManager) :
    RecyclerView.ViewHolder(itemView), OnMapReadyCallback {
    val receiveMessage = itemView.findViewById<TextView>(R.id.txt_receive_message)
    val receiveAudioLayout = itemView.findViewById<LinearLayout>(R.id.receiveAudioMessageLayout)
    val receiveAudio = itemView.findViewById<ImageView>(R.id.receivePlayAudioButton)
    val receiveAudioDuration = itemView.findViewById<TextView>(R.id.receiveAudioDuration)
    val receiveAudioSeekBar = itemView.findViewById<SeekBar>(R.id.receiveAudioSeekBar)
    var receiveMap = itemView.findViewById<FrameLayout>(R.id.map_container)
    private var googleMap: GoogleMap? = null

    init {
        // Cria e adiciona o SupportMapFragment ao FrameLayout
        val mapFragment = SupportMapFragment.newInstance()
        fragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        // Configura o callback do mapa
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // Customize o mapa conforme necessário, ex.: centralizar em uma localização
    }

    private fun displayNearbyTouristSpots(userLatLng: LatLng) {
        // Exemplificando como adicionar locais manualmente.
        // Com a API de Lugares do Google, use a busca por locais próximos para obter pontos turísticos

        val exampleTouristSpot = LatLng(userLatLng.latitude + 0.01, userLatLng.longitude + 0.01)
        googleMap?.addMarker(MarkerOptions().position(exampleTouristSpot).title("Ponto Turístico Próximo"))

        // Repetir para outros pontos ou utilizar resultados da API de Lugares

    }
}