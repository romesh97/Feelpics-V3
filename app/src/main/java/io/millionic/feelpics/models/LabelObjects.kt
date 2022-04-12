package io.millionic.feelpics.models

data class LabelObjects(
    val mainCategory:String="",
    var important:ArrayList<String> = ArrayList<String>(),
    var nonImportant:ArrayList<String> = ArrayList<String>(),
)
