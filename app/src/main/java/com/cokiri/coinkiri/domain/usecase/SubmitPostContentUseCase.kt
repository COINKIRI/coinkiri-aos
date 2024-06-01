package com.cokiri.coinkiri.domain.usecase

import com.cokiri.coinkiri.data.remote.model.ApiResponse
import com.cokiri.coinkiri.data.remote.model.PostDataRequest
import com.cokiri.coinkiri.domain.repository.PostRepository
import javax.inject.Inject

class SubmitPostContentUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postDataRequest: PostDataRequest): Result<ApiResponse> {
        return runCatching { postRepository.submitPost(postDataRequest) }
    }
}