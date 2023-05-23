package com.example.agrorob.models

data class Settings(
    val errorPositive: Int,
    val errorNegative: Int,
    val average: Int,
    val cylinderIn: Double,
    val cylinderOut: Double
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "errorPositive" to errorPositive,
            "errorNegative" to errorNegative,
            "average" to average,
            "cylinderIn" to cylinderIn,
            "cylinderOut" to cylinderOut
        )
    }
}
