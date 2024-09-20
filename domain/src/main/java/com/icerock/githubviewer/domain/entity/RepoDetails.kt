package com.icerock.githubviewer.domain.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoDetails(
    val name: String,
    val owner: UserInfo,
    @SerialName("default_branch") val defaultBranch: String,
    @SerialName("html_url") val htmlUrl: String,
    val license: License?,
    @SerialName("stargazers_count") val stars: Int,
    @SerialName("forks_count") val forks: Int,
    @SerialName("watchers_count") val watchers: Int
)

@Serializable
data class License(
    val name: String,
)