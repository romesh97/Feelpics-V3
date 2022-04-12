/**
 * Photo EXIF Toolkit for Android.
 *
 * Copyright (C) 2017 Ángel Iván Gladín García
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package io.millionic.feelpics.exif.domain

/**
 * Created on 12/24/16.
 */
data class ExifTagsContainer(val list: List<ExifField>, val type: Type) {
    fun getOnStringProperties(): String = when {
        list.isEmpty() -> "No data provided"
        else -> {
            val s = StringBuilder()
            list.forEach { s.append("${it.tag}: ${it.attribute}\n") }
            s.toString().substring(0, s.length - 1)
        }
    }
}

enum class Type { GPS, DATE, CAMERA_PROPERTIES, DIMENSION, OTHER }