package com.example.projet_kotlin_randazzo_benjamin

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie")
data class Movie(
    @PrimaryKey var id: Int,
    @ColumnInfo var title: String
)

data class Movies(var movies: List<Movie>)