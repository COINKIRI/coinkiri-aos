package com.cokiri.coinkiri.presentation.post.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cokiri.coinkiri.data.remote.model.ApiResponse
import com.cokiri.coinkiri.data.remote.model.ImageData
import com.cokiri.coinkiri.data.remote.model.PostDataRequest
import com.cokiri.coinkiri.domain.usecase.SubmitPostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityWriteViewModel @Inject constructor(
    private val submitPostUseCase: SubmitPostUseCase
) : ViewModel() {

    // 게시글 제목을 관리하는 MutableStateFlow
    private val _title = MutableStateFlow("")
    // 외부에서 읽기만 가능한 게시글 제목 StateFlow
    val title: StateFlow<String> = _title

    // 게시글 내용을 관리하는 MutableStateFlow
    private val _content = MutableStateFlow("")
    // 외부에서 읽기만 가능한 게시글 내용 StateFlow
    val content: StateFlow<String> = _content

    // 로딩 상태를 관리하는 MutableStateFlow
    private val _isLoading = MutableStateFlow(false)
    // 외부에서 읽기만 가능한 로딩 상태 StateFlow
    val isLoading: StateFlow<Boolean> = _isLoading

    // 에러 메시지를 관리하는 MutableStateFlow
    private val _errorMessage = MutableStateFlow<String?>(null)
    // 외부에서 읽기만 가능한 에러 메시지 StateFlow
    val errorMessage: StateFlow<String?> = _errorMessage

    // 이미지 목록을 관리하는 MutableStateFlow
    private val _images = MutableStateFlow<List<Pair<Int, String>>>(emptyList())

    // 게시글 등록 결과를 관리하는 MutableStateFlow
    private val _submitResult = MutableStateFlow<Result<ApiResponse>?>(null)
    // 외부에서 읽기만 가능한 게시글 등록 결과 StateFlow
    val submitResult: StateFlow<Result<ApiResponse>?> = _submitResult

    // 제목이 변경될 때 호출되는 함수
    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    // 내용이 변경될 때 호출되는 함수
    fun onContentChange(newContent: String) {
        _content.value = newContent
    }

    // 이미지 목록이 변경될 때 호출되는 함수
    fun onImagesChange(newImages: List<Pair<Int, String>>) {
        _images.value = newImages
    }

    // 게시글 등록 처리
    fun submitPost() {
        viewModelScope.launch {
            try {
                // 로딩 상태 설정 (추가)
                _isLoading.value = true
                _errorMessage.value = null

                // 게시글 데이터를 요청 객체로 변환
                val postDataRequest = PostDataRequest(
                    title = _title.value,
                    content = _content.value,
                    images = _images.value.map { ImageData(it.first, it.second) }
                )
                // 게시글 등록 UseCase 호출
                _submitResult.value = submitPostUseCase(postDataRequest)
            } catch (e: Exception) {
                // 예외 발생 시 에러 메시지 설정 (추가)
                _errorMessage.value = e.message
            } finally {
                // 로딩 상태 해제 (추가)
                _isLoading.value = false
            }
        }
    }
}
