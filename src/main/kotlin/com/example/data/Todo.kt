package com.example.data

import kotlinx.serialization.Serializable

@Serializable
data class Todo(
    var id: Int? = null,
    var title : String,
    var done: Boolean
)