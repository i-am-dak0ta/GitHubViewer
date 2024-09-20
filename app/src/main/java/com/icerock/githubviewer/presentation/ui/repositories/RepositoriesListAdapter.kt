package com.icerock.githubviewer.presentation.ui.repositories

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.icerock.githubviewer.databinding.RepositoriesListItemBinding
import com.icerock.githubviewer.domain.entity.Repo

class RepositoriesListAdapter(
    private val repos: List<Repo>,
    private val onRepoClicked: (Repo) -> Unit
) : RecyclerView.Adapter<RepositoriesListAdapter.RepositoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        val binding = RepositoriesListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RepositoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return repos.size
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        val repo = repos[position]
        holder.bind(repo)
        holder.itemView.setOnClickListener {
            onRepoClicked(repo)
        }
    }

    class RepositoryViewHolder(private val binding: RepositoriesListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(repo: Repo) {
            binding.apply {
                tvRepositoryItemName.text = repo.name
                tvRepositoryItemDescription.visibility =
                    if (repo.description.isNullOrEmpty()) View.GONE else View.VISIBLE
                tvRepositoryItemDescription.text = repo.description
                tvRepositoryItemLanguage.visibility =
                    if (repo.description.isNullOrEmpty()) View.GONE else View.VISIBLE
                tvRepositoryItemLanguage.text = repo.language
                tvRepositoryItemLanguage.setTextColor(getColorForLanguage(repo.language))
            }
        }
    }

    companion object {
        private val languageColors = mapOf(
            "Kotlin" to Color.parseColor("#A97BFF"),
            "Java" to Color.parseColor("#b07219"),
            "JavaScript" to Color.parseColor("#FFf1e05a"),
            "Python" to Color.parseColor("#3572A5"),
            "C++" to Color.parseColor("#f34b7d"),
            "Ruby" to Color.parseColor("#701516"),
            "Swift" to Color.parseColor("#ffac45"),
            "Go" to Color.parseColor("#00ADD8")
        )

        fun getColorForLanguage(language: String?): Int {
            return languageColors[language] ?: Color.WHITE
        }
    }
}