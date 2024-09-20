package com.icerock.githubviewer.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class RepoReadme(
    val content: String
)