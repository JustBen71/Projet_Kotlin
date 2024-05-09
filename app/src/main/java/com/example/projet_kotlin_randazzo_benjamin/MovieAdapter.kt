package com.example.projet_kotlin_randazzo_benjamin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MovieAdapter(var movies: Movies) : RecyclerView.Adapter<MovieAdapter.MovieView>() {

    class MovieView(view: View) : RecyclerView.ViewHolder(view)
    {
        val textViewTitle: TextView
        init {
            textViewTitle = view.findViewById(R.id.title_movie)
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieView {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.movies_layout, parent, false)

        return MovieView(view)
    }

    override fun onBindViewHolder(holder: MovieView, position: Int) {
        holder.textViewTitle.text = movies.movies[position].id.toString() + " : " + movies.movies[position].title
    }

    override fun getItemCount() = movies.movies.size

}