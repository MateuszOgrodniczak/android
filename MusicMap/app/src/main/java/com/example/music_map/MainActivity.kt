package com.example.music_map

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.transition.Visibility
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.ktor.client.HttpClient
import io.ktor.client.features.ServerResponseException
import io.ktor.client.request.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URL
import java.time.Duration
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : FragmentActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private val musicBrainzUrl = "https://musicbrainz.org/ws/2/place/?query="
    private val placesQueryLimit = 20
    private var markersToLifespans = HashMap<Marker, Int>()

    @TargetApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val supportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)

        userInputBtn.setOnClickListener {
            hideKeyboard()
            markersProgress.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                findPlaces()
                markersProgress.visibility = View.INVISIBLE
                removeMarkersAfterDelay()
            }
        }

        clearBtn.setOnClickListener {
            map.clear()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun removeMarkersAfterDelay() {
        for (entrySet in markersToLifespans.entries) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(Duration.ofSeconds(entrySet.value.toLong()))
                entrySet.key.remove()
                markersToLifespans.remove(entrySet.key)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private suspend fun findPlaces() {
        val userInput = userInput.text
        var allPlacesFound = false
        val client = HttpClient()
        var currentOffset = 0

        while (!allPlacesFound) {
            val url = URL(
                musicBrainzUrl.plus(userInput).plus("&limit=").plus(placesQueryLimit).plus("&offset=").plus(
                    currentOffset
                )
            )
            var response: String
            try {
                response = client.get<String>(url)
            } catch (e: ServerResponseException) {
                println(e.message)
                delay(Duration.ofMillis(10))
                continue
            }
            val responseAsInputStream = ByteArrayInputStream(response.toByteArray())
            allPlacesFound = allPlacesFoundElseContinueParsing(responseAsInputStream)
            currentOffset += placesQueryLimit
        }
        client.close()
    }

    private fun allPlacesFoundElseContinueParsing(xmlStream: InputStream): Boolean {
        val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val document = docBuilder.parse(xmlStream)
        val places = document.getElementsByTagName("place")

        val nrOfPlaces = places.length
        if (nrOfPlaces == 0) {
            return true
        }

        var isCameraPositionSet = false

        for (i in 0 until nrOfPlaces) {
            val element = places.item(i) as Element
            val coordinates = element.getElementsByTagName("coordinates")
            if (coordinates == null || coordinates.length == 0) {
                continue
            }
            val lifespanNode =
                element.getElementsByTagName("life-span")
            if (lifespanNode == null || lifespanNode.length == 0) {
                continue
            }
            val index = lifespanNode.length - 1
            val beginDateNode =
                lifespanNode.item(index).childNodes?.item(0)?.childNodes?.item(0) ?: continue
            val beginDate = beginDateNode.nodeValue
            if (!beginDate.matches(Regex("\\d{4}.*"))) {
                continue
            }
            val lifespan = beginDate.substring(0, 4).toInt()
            if (lifespan < 1990) {
                continue
            }
            val markerLifespan = lifespan - 1990

            val latitudeNode = coordinates.item(0).childNodes.item(0).childNodes.item(0)
            val longitudeNode = coordinates.item(0).childNodes.item(1).childNodes.item(0)

            val lat = latitudeNode.nodeValue
            val lon = longitudeNode.nodeValue

            val nameNode = element.getElementsByTagName("name").item(0).childNodes.item(0)
            val name = nameNode.nodeValue

            val addressNode = element.getElementsByTagName("address")?.item(0)?.childNodes?.item(0)
            var address = addressNode?.nodeValue
            if (address == null) {
                address = "N/A"
            }

            val placeCoordinates = LatLng(lat.toDouble(), lon.toDouble())
            val marker = map.addMarker(
                MarkerOptions()
                    .position(placeCoordinates)
                    .title(name)
                    .snippet("Address: $address")
            )

            markersToLifespans[marker] = markerLifespan

            if (!isCameraPositionSet) {
                isCameraPositionSet = true
                map.moveCamera(CameraUpdateFactory.newLatLng(placeCoordinates))
            }
        }
        return false
    }
}
