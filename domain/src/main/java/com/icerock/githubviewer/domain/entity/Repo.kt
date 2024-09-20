package com.icerock.githubviewer.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Repo(
    val id: Int,
    val name: String,
    val owner: UserInfo,
    val description: String?,
    val language: String?
)
