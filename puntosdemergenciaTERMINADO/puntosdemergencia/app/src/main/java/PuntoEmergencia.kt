package com.example.puntosdeemergencia

data class PuntoEmergencia(

    val nombre: String,

    val tipo: String,

    val direccion: String,

    val latitud: Double,

    val longitud: Double,

    val telefono: String

)