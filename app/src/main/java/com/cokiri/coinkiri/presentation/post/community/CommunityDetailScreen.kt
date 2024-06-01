package com.cokiri.coinkiri.presentation.post.community

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.cokiri.coinkiri.R
import com.cokiri.coinkiri.data.remote.model.CommentList
import com.cokiri.coinkiri.data.remote.model.CommunityDetailResponseDto
import com.cokiri.coinkiri.presentation.post.PostViewModel
import com.cokiri.coinkiri.ui.theme.CoinkiriBackground
import com.cokiri.coinkiri.ui.theme.CoinkiriPointGreen
import com.cokiri.coinkiri.util.buildHtmlContent
import com.cokiri.coinkiri.util.byteArrayToPainter
import com.cokiri.coinkiri.util.byteArrayToString
import com.cokiri.coinkiri.util.insertImagesIntoContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailScreen(
    navController: NavHostController,
    postViewModel: PostViewModel = hiltViewModel(),
    stringPostId: String
) {

    val postId = stringPostId.toLong()
    var webViewInstance: WebView? by remember { mutableStateOf(null) }
    val context = LocalContext.current

    LaunchedEffect(postId) {
        postViewModel.fetchCommunityPostDetails(postId)
        postViewModel.fetchCommentList(postId)
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.let { webView ->
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.clearCache(true)
                webView.destroy()
                webViewInstance = null
            }
        }
    }

    val communityDetail by postViewModel.communityDetail.collectAsStateWithLifecycle()
    val commentList by postViewModel.commentList.collectAsStateWithLifecycle()
    val commentContent by postViewModel.commentContent.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    if (showBottomSheet) {
        LaunchedEffect(Unit) {
            sheetState.expand()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = CoinkiriBackground,
                shadowElevation = 5.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                TopAppBar(
                    title = { },
                    colors = TopAppBarDefaults.topAppBarColors(CoinkiriBackground),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                webViewInstance?.let { webView ->
                                    (webView.parent as? ViewGroup)?.removeView(webView)
                                    webView.clearCache(true)
                                    webView.destroy()
                                    webViewInstance = null
                                }
                                navController.popBackStack()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로가기"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_post_baseline_visibility),
                                contentDescription = "더보기"
                            )
                        }
                    }
                )
            }
        },
        content = { padding ->
            when {
                communityDetail != null -> {
                    val detail = communityDetail

                    LazyColumn(
                        modifier = Modifier
                            .padding(padding)
                            .padding(vertical = 10.dp)
                            .fillMaxSize()
                            .background(CoinkiriBackground)
                    ) {
                        item {
                            if (detail != null) {
                                TitleSection(detail = detail)
                                ContentSection(detail, context) { webView ->
                                    webViewInstance = webView
                                }
                            }
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CoinkiriBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { coroutineScope.launch { showBottomSheet = false } },
                    sheetState = sheetState,
                    containerColor = CoinkiriBackground,
                ) {

                    Scaffold(
                        topBar = {
                            Surface(
                                color = CoinkiriBackground,
                                shadowElevation = 5.dp,
                                modifier = Modifier
                                    .fillMaxWidth()

                            ) {
                                CenterAlignedTopAppBar(
                                    title = {
                                        Text(
                                            text = "댓글",
                                            fontSize = 18.sp,
                                        )
                                    },
                                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                        CoinkiriBackground
                                    ),
                                    navigationIcon = {
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    showBottomSheet = false
                                                }
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_navi_home),
                                                contentDescription = "닫기"
                                            )
                                        }
                                    },
                                )
                            }
                        },
                        content = { paddingValues ->
                            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                                items(commentList.size) { index ->
                                    val comment = commentList[index]
                                    CommentCard(comment)
                                }
                            }
                        },
                        bottomBar = {
                            BottomAppBar(
                                containerColor = CoinkiriBackground,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextField(
                                        shape = RoundedCornerShape(30.dp),
                                        value = commentContent,
                                        onValueChange = { newCommentContent ->
                                            postViewModel.onCommentContentChange(
                                                newCommentContent
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.LightGray,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedContainerColor = Color.LightGray,
                                            unfocusedIndicatorColor = Color.Transparent,
                                        ),
                                        placeholder = {
                                            Text(
                                                text = "댓글을 입력하세요.",
                                                fontSize = 12.sp,
                                            )
                                        },

                                        )
                                    IconButton(
                                        onClick = {
                                            postViewModel.submitComment(postId)
                                            Log.d("CommunityDetailScreen", "postId: $postId")
                                            Log.d(
                                                "CommunityDetailScreen",
                                                "commentContent: $commentContent"
                                            )
                                            postViewModel.onCommentContentChange("")
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_navi_home),
                                            contentDescription = "전송"
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                containerColor = CoinkiriBackground,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_post_baseline_thumb_up),
                            contentDescription = "좋아요"
                        )
                    }
                    IconButton(
                        onClick = { coroutineScope.launch { showBottomSheet = true } },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_post_baseline_visibility),
                            contentDescription = "댓글"
                        )
                    }
                }
            }
        }
    )
}


@Composable
fun TitleSection(
    detail: CommunityDetailResponseDto,
) {

    val title = detail.postDetailResponseDto.title
    val name = detail.postDetailResponseDto.memberNickname
    val level = detail.postDetailResponseDto.memberLevel
    val profileImageByteArray = detail.postDetailResponseDto.memberPic
    val profileImage = byteArrayToPainter(profileImageByteArray)
    val createDate = detail.postDetailResponseDto.createdAt

    Column(
        modifier = Modifier
            .padding(10.dp)
            .background(CoinkiriBackground)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CoinkiriBackground)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(5.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CoinkiriBackground)
                .padding(5.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.size(5.dp))
                Card(
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(5.dp),
                ) {
                    Image(
                        painter = profileImage,
                        contentScale = ContentScale.Crop,
                        contentDescription = "Profile Image",
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.size(5.dp))
                Column(
                    modifier = Modifier.padding(5.dp)
                ) {
                    Text(text = "$level + $name", fontWeight = FontWeight.Bold)
                    Text(text = createDate)
                }
            }
            Button(
                onClick = { /*TODO*/ },
                colors = ButtonDefaults.buttonColors(CoinkiriPointGreen),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(10.dp)
            ) {
                Text("팔로우")
            }
        }
    }
}


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ContentSection(
    detail: CommunityDetailResponseDto,
    context: Context,
    onWebViewReady: (WebView) -> Unit
) {

    val content = detail.postDetailResponseDto.content
    val imagesList = detail.postDetailResponseDto.images
    val pairImagesList = imagesList.map {
        it.position to byteArrayToString(it.base64)
    }
    val newContent = insertImagesIntoContent(content, pairImagesList)

    AndroidView(
        factory = {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                loadDataWithBaseURL(
                    null,
                    buildHtmlContent(newContent),
                    "text/html",
                    "UTF-8",
                    null
                )
                onWebViewReady(this)
            }
        },
        update = { webView ->
            onWebViewReady(webView)
        },
        onRelease = { webView ->
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.clearCache(true)
            webView.destroy()
        }
    )
}

@Composable
fun CommentCard(comment: CommentList) {

    val level = comment.member.level
    val name = comment.member.nickname
    val data = comment.createdAt
    val content = comment.content
    val profileImageByteArray = comment.member.pic
    val profileImage = byteArrayToPainter(profileImageByteArray)


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp),
        colors = CardDefaults.cardColors(CoinkiriBackground)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(5.dp),
            ) {
                Image(
                    painter = profileImage,
                    contentScale = ContentScale.Crop,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(40.dp)
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Column(
                modifier = Modifier
                    .padding(start = 5.dp, bottom = 3.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "Lv.$level $name $data",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text(
                    text = content,
                    fontSize = 13.sp
                )
            }
        }
        HorizontalDivider()
    }
}