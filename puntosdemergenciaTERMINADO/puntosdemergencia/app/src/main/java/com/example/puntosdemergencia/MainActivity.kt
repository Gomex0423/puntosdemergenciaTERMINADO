package com.example.puntosdeemergencia

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.location.Location
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    private lateinit var fusedLocationClient:
            FusedLocationProviderClient

    private lateinit var tvUbicacion: TextView

    private lateinit var btnMiUbicacion: Button

    private lateinit var spinnerTipo: Spinner

    private var marcadorUsuario: Marker? = null


    // ==========================================
    // PUNTOS DE EMERGENCIA
    // ==========================================



    private val puntosEmergencia = arrayListOf(

        // ==========================
        // HOSPITALES
        // ==========================

        PuntoEmergencia(
            "SES Hospital Universitario de Caldas",
            "Hospital",
            "Cl. 48 #25-71, Manizales",
            5.06900,
            -75.51750,
            "8782500"
        ),

        PuntoEmergencia(
            "Hospital Santa Sofía",
            "Hospital",
            "Cl. 5 #40-02, Manizales",
            5.05800,
            -75.52960,
            "8879200"
        ),

        PuntoEmergencia(
            "Hospital Infantil Rafael Henao Toro",
            "Hospital",
            "Av. Santander #49-30, Manizales",
            5.06436,
            -75.49854,
            "8932859"
        ),

        PuntoEmergencia(
            "Clínica Avidanti Manizales",
            "Hospital",
            "Cl. 10 #2C-10, Villa Pilar, Manizales",
            5.08331,
            -75.52618,
            "8990000"
        ),


        // ==========================
        // POLICÍA
        // ==========================

        PuntoEmergencia(
            "Estación de Policía Manizales",
            "Policía",
            "Carrera 21 Calle 31, Manizales",
            5.06889,
            -75.51738,
            "123"
        ),

        PuntoEmergencia(
            "CAI El Cable",
            "Policía",
            "Carrera 23 Calle 64A, Manizales",
            5.05685,
            -75.48640,
            "123"
        ),

        PuntoEmergencia(
            "CAI Chipre",
            "Policía",
            "Avenida 12 de Octubre, Manizales",
            5.07354,
            -75.52617,
            "123"
        ),

        PuntoEmergencia(
            "CAI Alfonso López",
            "Policía",
            "Sector Alfonso López, Manizales",
            5.07200,
            -75.52000,
            "123"
        ),


        // ==========================
        // BOMBEROS
        // ==========================

        PuntoEmergencia(
            "Cuerpo Oficial de Bomberos Fundadores",
            "Bomberos",
            "Cl. 33A #20-52, Manizales",
            5.06904,
            -75.51043,
            "119"
        ),

        PuntoEmergencia(
            "Estación de Bomberos Palogrande",
            "Bomberos",
            "Calle 62 Carrera 25, Manizales",
            5.05823,
            -75.48977,
            "119"
        ),

        PuntoEmergencia(
            "Estación de Bomberos Maltería",
            "Bomberos",
            "Zona Industrial de Maltería, Manizales",
            5.04207,
            -75.43156,
            "119"
        ),

        PuntoEmergencia(
            "Cuerpo de Bomberos Voluntarios de Manizales",
            "Bomberos",
            "Calle 30 #15-57, Manizales",
            5.07150,
            -75.51950,
            "119"
        )
    )






    // ==========================================
    // FUENTE DEL MAPA
    // ==========================================

    private val fuenteOpenStreetMap = XYTileSource(
        "OpenStreetMap",
        0,
        19,
        256,
        ".png",
        arrayOf(
            "https://tile.openstreetmap.org/"
        )
    )


    // ==========================================
    // ON CREATE
    // ==========================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        // CONFIGURACIÓN DE OSMDROID

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences(
                "osmdroid",
                MODE_PRIVATE
            )
        )

        Configuration.getInstance()
            .setUserAgentValue(
                "PuntosDeEmergencia/1.0 (Android; com.example.puntosdeemergencia)"
            )


        // CONECTAR MAPA

        mapView = findViewById(R.id.mapView)

        mapView.setTileSource(
            fuenteOpenStreetMap
        )

        mapView.setMultiTouchControls(true)

        mapView.controller.setZoom(15.0)


        // TEXTVIEW

        tvUbicacion =
            findViewById(R.id.tvUbicacion)


        // BOTÓN MI UBICACIÓN

        btnMiUbicacion =
            findViewById(R.id.btnMiUbicacion)


        // SPINNER

        spinnerTipo =
            findViewById(R.id.spinnerTipo)


        val opciones = arrayOf(
            "Todos",
            "Policía",
            "Hospital",
            "Bomberos"
        )


        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opciones
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerTipo.adapter = adapter


        spinnerTipo.setOnItemSelectedListener(
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {

                    filtrarPuntos(
                        opciones[position]
                    )
                }


                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {
                }
            }
        )


        // SERVICIO DE UBICACIÓN

        fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(this)


        // COLOCAR PUNTOS

        colocarPuntosEmergencia()


        // BOTÓN MI UBICACIÓN

        btnMiUbicacion.setOnClickListener {

            obtenerUbicacion()
        }


        // BOTÓN LLAMAR AL 123

        val btnLlamar123 =
            findViewById<Button>(R.id.btnLlamar123)

        btnLlamar123.setOnClickListener {

            if (
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    200
                )

            } else {

                val intent = Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:123")
                )

                startActivity(intent)
            }
        }

        // PEDIR PERMISO

        solicitarPermisoUbicacion()
    }


    // ==========================================
    // SOLICITAR PERMISO DE UBICACIÓN
    // ==========================================

    private fun solicitarPermisoUbicacion() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )

            return
        }

        obtenerUbicacion()
    }


    // ==========================================
    // OBTENER UBICACIÓN
    // ==========================================

    private fun obtenerUbicacion() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }


        fusedLocationClient
            .lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    val latitud =
                        location.latitude

                    val longitud =
                        location.longitude


                    mostrarPuntosCercanos(
                        latitud,
                        longitud
                    )


                    tvUbicacion.text =
                        "Mi ubicación:\n" +
                                "Latitud: $latitud\n" +
                                "Longitud: $longitud"


                    val ubicacion =
                        GeoPoint(
                            latitud,
                            longitud
                        )


                    mapView.controller
                        .setCenter(ubicacion)


                    mostrarUsuario(
                        ubicacion
                    )
                }
            }
    }


    // ==========================================
    // MARCADOR DEL USUARIO
    // ==========================================

    private fun mostrarUsuario(
        ubicacion: GeoPoint
    ) {

        if (
            marcadorUsuario != null
        ) {

            mapView.overlays.remove(
                marcadorUsuario
            )
        }


        marcadorUsuario =
            Marker(mapView)


        marcadorUsuario!!.position =
            ubicacion


        marcadorUsuario!!.title =
            "Mi ubicación"


        mapView.overlays.add(
            marcadorUsuario
        )


        mapView.invalidate()
    }


    // ==========================================
    // COLOCAR PUNTOS DE EMERGENCIA
    // ==========================================

    private fun colocarPuntosEmergencia() {

        for (
        punto in puntosEmergencia
        ) {

            val ubicacion =
                GeoPoint(
                    punto.latitud,
                    punto.longitud
                )


            val marcador =
                Marker(mapView)


            marcador.position =
                ubicacion


            marcador.title =
                punto.nombre


            marcador.snippet =
                punto.tipo +
                        "\n" +
                        punto.direccion


            marcador.setOnMarkerClickListener {

                    _,
                    _ ->

                mostrarInformacion(
                    punto
                )

                true
            }


            mapView.overlays.add(
                marcador
            )
        }


        mapView.invalidate()
    }


    // ==========================================
    // INFORMACIÓN DEL PUNTO
    // ==========================================

    private fun mostrarInformacion(
        punto: PuntoEmergencia
    ) {

        tvUbicacion.text =
            "Punto de emergencia\n\n" +
                    "Nombre: ${punto.nombre}\n" +
                    "Tipo: ${punto.tipo}\n" +
                    "Dirección: ${punto.direccion}\n" +
                    "Teléfono: ${punto.telefono}"


        mapView.controller.setCenter(

            GeoPoint(
                punto.latitud,
                punto.longitud
            )
        )
    }


    // ==========================================
    // DISTANCIA
    // ==========================================

    private fun calcularDistancia(
        latitudUsuario: Double,
        longitudUsuario: Double,
        punto: PuntoEmergencia
    ): Float {

        val resultado =
            FloatArray(1)


        Location.distanceBetween(
            latitudUsuario,
            longitudUsuario,
            punto.latitud,
            punto.longitud,
            resultado
        )


        return resultado[0]
    }


    // ==========================================
    // PUNTOS MÁS CERCANOS
    // ==========================================

    private fun mostrarPuntosCercanos(
        latitudUsuario: Double,
        longitudUsuario: Double
    ) {

        val puntosOrdenados =
            puntosEmergencia
                .map { punto ->

                    val distancia =
                        calcularDistancia(
                            latitudUsuario,
                            longitudUsuario,
                            punto
                        )

                    Pair(
                        punto,
                        distancia
                    )
                }
                .sortedBy {
                    it.second
                }


        val texto =
            StringBuilder()


        texto.append(
            "Puntos de emergencia cercanos:\n\n"
        )


        for (
        (punto, distancia)
        in puntosOrdenados
        ) {

            val distanciaKm =
                distancia / 1000


            texto.append(
                "🚨 ${punto.nombre}\n"
            )

            texto.append(
                "Tipo: ${punto.tipo}\n"
            )

            texto.append(
                "Distancia: %.2f km\n\n"
                    .format(distanciaKm)
            )
        }


        tvUbicacion.text =
            texto.toString()
    }


    // ==========================================
    // FILTRAR PUNTOS
    // ==========================================

    private fun filtrarPuntos(
        tipo: String
    ) {

        // Primero quitamos los marcadores
        // de los puntos de emergencia.

        val overlays =
            mapView.overlays

        val marcadores =
            overlays.filter {
                it is Marker &&
                        it != marcadorUsuario
            }

        overlays.removeAll(
            marcadores
        )


        // Puntos que queremos mostrar

        val puntosFiltrados =
            if (tipo == "Todos") {

                puntosEmergencia

            } else {

                puntosEmergencia.filter {
                    it.tipo == tipo
                }
            }


        // Crear nuevamente los marcadores

        for (
        punto in puntosFiltrados
        ) {

            val marcador =
                Marker(mapView)


            marcador.position =
                GeoPoint(
                    punto.latitud,
                    punto.longitud
                )


            marcador.title =
                punto.nombre


            marcador.snippet =
                punto.tipo +
                        "\n" +
                        punto.direccion


            marcador.setOnMarkerClickListener {

                    _,
                    _ ->

                mostrarInformacion(
                    punto
                )

                true
            }


            mapView.overlays.add(
                marcador
            )
        }


        mapView.invalidate()
    }


    // ==========================================
    // ABRIR GOOGLE MAPS
    // ==========================================

    private fun abrirGoogleMaps(
        punto: PuntoEmergencia
    ) {

        val uri =
            Uri.parse(
                "google.navigation:q=" +
                        "${punto.latitud}," +
                        "${punto.longitud}"
            )


        val intent =
            Intent(
                Intent.ACTION_VIEW,
                uri
            )


        intent.setPackage(
            "com.google.android.apps.maps"
        )


        startActivity(intent)
    }


    // ==========================================
    // REANUDAR MAPA
    // ==========================================

    override fun onResume() {

        super.onResume()

        mapView.onResume()
    }


    // ==========================================
    // PAUSAR MAPA
    // ==========================================

    override fun onPause() {

        super.onPause()

        mapView.onPause()
    }
}