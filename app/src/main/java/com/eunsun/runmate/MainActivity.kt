package com.eunsun.runmate

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eunsun.runmate.ui.theme.RUNMATETheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RUNMATETheme(darkTheme = false, dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    RunMateApp()
                }
            }
        }
    }
}

private val BrandOrange = Color(0xFFFF942E)
private val SupportingText = Color(0xFF8B8B8B)
private val InactiveDot = Color(0xFFE4E4E4)
private val FieldBackground = Color(0xFFF5F6F8)
private val NoticeBackground = Color(0xFFFFF5E9)
private val NoticeText = Color(0xFFC68139)
private const val PreferencesName = "runmate_preferences"
private const val RememberedUserIdKey = "remembered_user_id"
private const val RegisteredNicknameKey = "registered_nickname"
private const val RegisteredUserIdKey = "registered_user_id"

private enum class AppScreen {
    Onboarding, Login, SignUp, Home, Crew, CrewWrite, CrewDetail, CrewInquiry,
    Running, RunningHub, RunningSchedules, RunningCrewDashboard, RunningScheduleAdd, RunningAttendance, RunningChat,
    Record, Community, CommunityWrite, CommunityDetail
}

private data class UserAccount(
    val name: String,
    val userId: String,
    val password: String
)

private data class OnboardingPage(
    val title: String,
    val description: String
)

private data class CommunityComment(
    val author: String,
    val text: String
)

private data class CommunityPost(
    val id: Int,
    val author: String,
    val category: String,
    val content: String,
    val timeLabel: String,
    val likes: Int,
    val hasPhoto: Boolean = false,
    val isLiked: Boolean = false,
    val comments: List<CommunityComment> = emptyList()
)

private data class CrewRecruitment(
    val id: Int,
    val name: String,
    val difficulty: String,
    val location: String,
    val dateTime: String,
    val pace: String,
    val distance: String,
    val memberLimit: String,
    val introduction: String,
    val ownerName: String
)

private data class RunningRecord(
    val distance: String,
    val date: String,
    val duration: String,
    val pace: String,
    val course: String
)

private data class CrewRunningSchedule(
    val id: Int,
    val dateLabel: String,
    val time: String,
    val location: String,
    val distance: String,
    val isJoined: Boolean = false
)

private val initialCommunityPosts = listOf(
    CommunityPost(1, "나", "전체", "러닝 페이스 줄이는 법 없을까요?", "방금 전", 0),
    CommunityPost(2, "하늘사랑", "러닝 후기", "오늘 한강에서 10km 완주했어요! 날씨가 정말 좋았어요 ☀", "2시간 전", 24, hasPhoto = true, comments = listOf(CommunityComment("민준", "완주 축하드려요!"))),
    CommunityPost(3, "불법의생", "질문", "처음으로 5km를 30분 안에 뛰었어요! 감격 😄", "5시간 전", 42),
    CommunityPost(4, "달콤한커피", "추천", "강남 야러 러닝 크루 있나요? 나름 괜찮은 초보 모임 찾고 있어요!", "1일 전", 18),
    CommunityPost(5, "맑은하늘", "질문", "러닝화 추천 부탁드려요! 초보자에게 맞는 신발 찾고 있어요.", "1일 전", 15)
)

private val initialCrewRecruitments = listOf(
    CrewRecruitment(
        id = 1,
        name = "강남 러닝 크루",
        difficulty = "중급",
        location = "강남역 1번 출구",
        dateTime = "매주 목요일, 오후 07:00",
        pace = "6:30 /km",
        distance = "5 km",
        memberLimit = "8명",
        introduction = "퇴근 후 함께 달릴 러너를 찾고 있어요!",
        ownerName = "러닝메이트"
    ),
    CrewRecruitment(
        id = 2,
        name = "한강 선셋 러너스",
        difficulty = "초급",
        location = "반포 한강공원 · 2.5km",
        dateTime = "매주 금요일, 오후 19:00",
        pace = "7:00 /km",
        distance = "4 km",
        memberLimit = "10명",
        introduction = "노을을 보며 천천히 달려요. 초보 환영!",
        ownerName = "달콤한커피"
    )
)

private val recentRunningRecords = listOf(
    RunningRecord("6.2km", "2026.04.18", "35분 20초", "5'42\"", "한강 성산 러닝코스"),
    RunningRecord("7.3km", "2026.04.17", "42분 10초", "5'46\"", "국회대로"),
    RunningRecord("4.8km", "2026.04.16", "27분 50초", "5'48\"", "강남 야경 러닝 코스")
)

private val initialCrewRunningSchedules = listOf(
    CrewRunningSchedule(1, "5월 5일 (월)", "07:00", "한강공원 여의나루역", "5km"),
    CrewRunningSchedule(2, "5월 13일 (화)", "08:00", "한강공원 여의나루역", "5km"),
    CrewRunningSchedule(3, "5월 16일 (금)", "19:00", "한강공원 여의나루역", "7km")
)

private fun CommunityPost.displayAuthor(currentUserName: String): String =
    if (author == "나") currentUserName else author

private val onboardingPages = listOf(
    OnboardingPage(
        title = "혼자가 아닌\n함께 뛰는 러닝",
        description = "주변의 러닝 크루를 찾고 함께\n건강한 습관을 만들어보세요"
    ),
    OnboardingPage(
        title = "맞춤형\n러닝 크루 탐색",
        description = "위치, 시간, 난이도별 딱 맞는\n러닝 크루를 매칭해드려요"
    ),
    OnboardingPage(
        title = "함께 성장하는\n러닝 기록",
        description = "나의 기록을 확인하고\n크루원들과 함께 성장하세요"
    )
)

@Composable
private fun RunMateApp() {
    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
    }
    var currentScreen by remember { mutableStateOf(AppScreen.Onboarding) }
    var onboardingPage by remember { mutableStateOf(0) }
    var account by remember { mutableStateOf<UserAccount?>(null) }
    var showSignUpNotice by remember { mutableStateOf(false) }
    var rememberedUserId by remember {
        mutableStateOf(preferences.getString(RememberedUserIdKey, null))
    }
    var registeredNickname by remember {
        mutableStateOf(preferences.getString(RegisteredNicknameKey, null))
    }
    var registeredUserId by remember {
        mutableStateOf(preferences.getString(RegisteredUserIdKey, null))
    }
    var communityPosts by remember { mutableStateOf(initialCommunityPosts) }
    var crewRecruitments by remember { mutableStateOf(initialCrewRecruitments) }
    var crewRunningSchedules by remember { mutableStateOf(initialCrewRunningSchedules) }
    var selectedCommunityPostId by remember { mutableStateOf<Int?>(null) }
    var selectedCrewId by remember { mutableStateOf<Int?>(null) }
    var joinedCrewIds by remember { mutableStateOf(setOf<Int>()) }
    var isRunningCrewOwner by remember { mutableStateOf(false) }
    var isRunningParticipant by remember { mutableStateOf(false) }
    var attendanceMemberNames by remember { mutableStateOf(setOf<String>()) }
    val communityNickname = registeredNickname
        ?.takeIf { it.isNotBlank() }
        ?: account?.name
        ?: "러너"

    BackHandler(
        enabled = currentScreen != AppScreen.Onboarding && currentScreen != AppScreen.Home
    ) {
        currentScreen = when (currentScreen) {
            AppScreen.Login -> AppScreen.Onboarding
            AppScreen.SignUp -> AppScreen.Login
            AppScreen.CommunityWrite, AppScreen.CommunityDetail -> AppScreen.Community
            AppScreen.CrewWrite -> AppScreen.Crew
            AppScreen.CrewInquiry -> AppScreen.CrewDetail
            AppScreen.RunningSchedules -> AppScreen.RunningHub
            AppScreen.RunningCrewDashboard -> AppScreen.RunningHub
            AppScreen.RunningScheduleAdd -> AppScreen.RunningCrewDashboard
            AppScreen.RunningAttendance -> AppScreen.RunningCrewDashboard
            AppScreen.RunningChat -> AppScreen.RunningCrewDashboard
            AppScreen.Community, AppScreen.Crew, AppScreen.CrewDetail, AppScreen.Running, AppScreen.RunningHub, AppScreen.Record -> AppScreen.Home
            AppScreen.Onboarding, AppScreen.Home -> currentScreen
        }
    }

    when (currentScreen) {
        AppScreen.Onboarding -> OnboardingScreen(
            page = onboardingPages[onboardingPage],
            pageIndex = onboardingPage,
            onNextClick = {
                if (onboardingPage == onboardingPages.lastIndex) {
                    currentScreen = AppScreen.Login
                } else {
                    onboardingPage += 1
                }
            }
        )
        AppScreen.Login -> LoginScreen(
            showSignUpNotice = showSignUpNotice,
            rememberedUserId = rememberedUserId,
            onLogin = { enteredUserId, enteredPassword, shouldRememberUserId ->
                val isRegisteredUser = account?.userId == enteredUserId
                val isValidPassword = account == null || (isRegisteredUser && account?.password == enteredPassword)
                if (enteredPassword.isNotBlank() && isValidPassword) {
                    if (shouldRememberUserId) {
                        preferences.edit().putString(RememberedUserIdKey, enteredUserId).apply()
                        rememberedUserId = enteredUserId
                    } else {
                        preferences.edit().remove(RememberedUserIdKey).apply()
                        rememberedUserId = null
                    }
                    if (account == null) {
                        val nicknameForUser = registeredNickname
                            ?.takeIf { it.isNotBlank() && (registeredUserId == null || registeredUserId == enteredUserId) }
                            ?: "러너"
                        account = UserAccount(
                            name = nicknameForUser,
                            userId = enteredUserId,
                            password = ""
                        )
                        if (registeredNickname != null && registeredUserId == null) {
                            preferences.edit().putString(RegisteredUserIdKey, enteredUserId).apply()
                            registeredUserId = enteredUserId
                        }
                    }
                    currentScreen = AppScreen.Home
                    true
                } else {
                    false
                }
            },
            onSignUpClick = { currentScreen = AppScreen.SignUp }
        )
        AppScreen.SignUp -> SignUpScreen(
            onSignUp = { name, userId, password ->
                account = UserAccount(name = name, userId = userId, password = password)
                preferences.edit()
                    .putString(RegisteredNicknameKey, name)
                    .putString(RegisteredUserIdKey, userId)
                    .apply()
                registeredNickname = name
                registeredUserId = userId
                showSignUpNotice = true
                currentScreen = AppScreen.Login
            },
            onBack = { currentScreen = AppScreen.Login }
        )
        AppScreen.Home -> HomeScreen(
            account = account ?: UserAccount("러너", "", ""),
            onStartRunning = { currentScreen = AppScreen.Running },
            onCrewClick = { currentScreen = AppScreen.Crew },
            onRunningClick = { currentScreen = AppScreen.RunningHub },
            onRecordClick = { currentScreen = AppScreen.Record },
            onCommunityClick = { currentScreen = AppScreen.Community }
        )
        AppScreen.Crew -> CrewScreen(
            crews = crewRecruitments,
            onHomeClick = { currentScreen = AppScreen.Home },
            onCommunityClick = { currentScreen = AppScreen.Community },
            onRunningClick = { currentScreen = AppScreen.RunningHub },
            onRecordClick = { currentScreen = AppScreen.Record },
            onWriteClick = { currentScreen = AppScreen.CrewWrite },
            onCrewClick = { crewId ->
                selectedCrewId = crewId
                currentScreen = AppScreen.CrewDetail
            }
        )
        AppScreen.CrewWrite -> CrewWriteScreen(
            onBack = { currentScreen = AppScreen.Crew },
            onCreate = { name, difficulty, location, dateTime, pace, distance, memberLimit, introduction ->
                val nextId = (crewRecruitments.maxOfOrNull { it.id } ?: 0) + 1
                crewRecruitments = listOf(
                    CrewRecruitment(
                        id = nextId,
                        name = name,
                        difficulty = difficulty,
                        location = location,
                        dateTime = dateTime,
                        pace = pace,
                        distance = distance,
                        memberLimit = memberLimit,
                        introduction = introduction,
                        ownerName = communityNickname
                    )
                ) + crewRecruitments
                isRunningCrewOwner = true
                isRunningParticipant = false
                currentScreen = AppScreen.Crew
            }
        )
        AppScreen.CrewDetail -> CrewDetailScreen(
            crew = crewRecruitments.firstOrNull { it.id == selectedCrewId },
            isJoined = selectedCrewId in joinedCrewIds,
            onBack = { currentScreen = AppScreen.Crew },
            onInquiryClick = { currentScreen = AppScreen.CrewInquiry },
            onJoin = { crewId ->
                joinedCrewIds = joinedCrewIds + crewId
                isRunningCrewOwner = false
                isRunningParticipant = true
            }
        )
        AppScreen.CrewInquiry -> CrewInquiryScreen(
            crew = crewRecruitments.firstOrNull { it.id == selectedCrewId },
            onBack = { currentScreen = AppScreen.CrewDetail }
        )
        AppScreen.Running -> RunningScreen(
            onBack = { currentScreen = AppScreen.Home },
            onFinish = { currentScreen = AppScreen.Record }
        )
        AppScreen.RunningHub -> RunningHubScreen(
            schedules = crewRunningSchedules,
            onHomeClick = { currentScreen = AppScreen.Home },
            onCrewTabClick = { currentScreen = AppScreen.Crew },
            onRecordClick = { currentScreen = AppScreen.Record },
            onCommunityClick = { currentScreen = AppScreen.Community },
            onSchedulesClick = { currentScreen = AppScreen.RunningSchedules },
            onCrewDashboardClick = { currentScreen = AppScreen.RunningCrewDashboard }
        )
        AppScreen.RunningSchedules -> RunningSchedulesScreen(
            schedules = crewRunningSchedules,
            isOwner = isRunningCrewOwner,
            onBack = { currentScreen = AppScreen.RunningHub },
            onScheduleClick = { currentScreen = AppScreen.RunningCrewDashboard }
        )
        AppScreen.RunningCrewDashboard -> RunningCrewDashboardScreen(
            schedules = crewRunningSchedules,
            isOwner = isRunningCrewOwner,
            isParticipating = isRunningParticipant,
            attendanceCount = attendanceMemberNames.size,
            onBack = { currentScreen = AppScreen.RunningHub },
            onAddScheduleClick = { currentScreen = AppScreen.RunningScheduleAdd },
            onAttendanceClick = { currentScreen = AppScreen.RunningAttendance },
            onParticipationChange = { isRunningParticipant = it },
            onChatClick = { currentScreen = AppScreen.RunningChat }
        )
        AppScreen.RunningScheduleAdd -> RunningScheduleAddScreen(
            onBack = { currentScreen = AppScreen.RunningCrewDashboard },
            onAddSchedule = { date, time, location, distance ->
                val nextId = (crewRunningSchedules.maxOfOrNull { it.id } ?: 0) + 1
                crewRunningSchedules = crewRunningSchedules + CrewRunningSchedule(
                    id = nextId,
                    dateLabel = date,
                    time = time,
                    location = location,
                    distance = distance
                )
                currentScreen = AppScreen.RunningCrewDashboard
            }
        )
        AppScreen.RunningAttendance -> RunningAttendanceScreen(
            checkedMemberNames = attendanceMemberNames,
            onBack = { currentScreen = AppScreen.RunningCrewDashboard },
            onCheckedMemberNamesChange = { attendanceMemberNames = it },
            onComplete = { currentScreen = AppScreen.RunningCrewDashboard }
        )
        AppScreen.RunningChat -> RunningChatScreen(
            onBack = { currentScreen = AppScreen.RunningCrewDashboard }
        )
        AppScreen.Record -> RecordScreen(
            records = recentRunningRecords,
            onHomeClick = { currentScreen = AppScreen.Home },
            onCrewClick = { currentScreen = AppScreen.Crew },
            onRunningClick = { currentScreen = AppScreen.RunningHub },
            onCommunityClick = { currentScreen = AppScreen.Community }
        )
        AppScreen.Community -> CommunityScreen(
            posts = communityPosts,
            currentUserName = communityNickname,
            onHomeClick = { currentScreen = AppScreen.Home },
            onCrewClick = { currentScreen = AppScreen.Crew },
            onRunningClick = { currentScreen = AppScreen.RunningHub },
            onRecordClick = { currentScreen = AppScreen.Record },
            onWriteClick = { currentScreen = AppScreen.CommunityWrite },
            onPostClick = { postId ->
                selectedCommunityPostId = postId
                currentScreen = AppScreen.CommunityDetail
            },
            onLikeClick = { postId ->
                communityPosts = communityPosts.map { post ->
                    if (post.id == postId) post.copy(
                        isLiked = !post.isLiked,
                        likes = post.likes + if (post.isLiked) -1 else 1
                    ) else post
                }
            },
            onCommentAdd = { postId, comment ->
                val author = communityNickname
                communityPosts = communityPosts.map { post ->
                    if (post.id == postId) post.copy(comments = post.comments + CommunityComment(author, comment)) else post
                }
            }
        )
        AppScreen.CommunityWrite -> CommunityWriteScreen(
            onBack = { currentScreen = AppScreen.Community },
            onPost = { category, content ->
                val author = communityNickname
                val nextId = (communityPosts.maxOfOrNull { it.id } ?: 0) + 1
                communityPosts = listOf(
                    CommunityPost(nextId, author, category, content, "방금 전", likes = 0)
                ) + communityPosts
                currentScreen = AppScreen.Community
            }
        )
        AppScreen.CommunityDetail -> CommunityDetailScreen(
            post = communityPosts.firstOrNull { it.id == selectedCommunityPostId },
            currentUserName = communityNickname,
            onBack = { currentScreen = AppScreen.Community },
            onLikeClick = { postId ->
                communityPosts = communityPosts.map { post ->
                    if (post.id == postId) post.copy(
                        isLiked = !post.isLiked,
                        likes = post.likes + if (post.isLiked) -1 else 1
                    ) else post
                }
            },
            onCommentAdd = { postId, comment ->
                val author = communityNickname
                communityPosts = communityPosts.map { post ->
                    if (post.id == postId) post.copy(comments = post.comments + CommunityComment(author, comment)) else post
                }
            }
        )
    }
}

@Composable
private fun OnboardingScreen(
    page: OnboardingPage,
    pageIndex: Int,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(176.dp))
        RunningBrandMark(modifier = Modifier.size(96.dp))
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = page.title,
            color = Color(0xFF151515),
            fontSize = 23.sp,
            lineHeight = 31.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = page.description,
            color = SupportingText,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))
        OnboardingIndicator(activePage = pageIndex)
        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = onNextClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandOrange,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = if (pageIndex == onboardingPages.lastIndex) "시작하기   ›" else "다음   ›",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(34.dp))
    }
}

@Composable
private fun LoginScreen(
    showSignUpNotice: Boolean,
    rememberedUserId: String?,
    onLogin: (userId: String, password: String, shouldRememberUserId: Boolean) -> Boolean,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var passwordErrorMessage by remember { mutableStateOf<String?>(null) }
    var loginUserId by remember(rememberedUserId) { mutableStateOf(rememberedUserId.orEmpty()) }
    var rememberUserId by remember(rememberedUserId) { mutableStateOf(rememberedUserId != null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(108.dp))
        RunningBrandMark(modifier = Modifier.size(92.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "RunMate",
            color = Color(0xFF171717),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "함께 뛰는 즐거움",
            color = SupportingText,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(if (showSignUpNotice) 35.dp else 52.dp))

        if (showSignUpNotice) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(NoticeBackground, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "✓", color = NoticeText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "회원가입 완료! 비밀번호를 입력해 로그인하세요.",
                    color = NoticeText,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (rememberedUserId == null) {
            AppInputField(
                label = "아이디",
                value = loginUserId,
                onValueChange = {
                    loginUserId = it
                    passwordErrorMessage = null
                },
                placeholder = "아이디를 입력하세요"
            )
        } else {
            LoginIdField(userId = rememberedUserId)
        }
        Spacer(modifier = Modifier.height(18.dp))
        AppInputField(
            label = "비밀번호",
            value = password,
            onValueChange = {
                password = it
                passwordErrorMessage = null
            },
            placeholder = "비밀번호를 입력하세요",
            isPassword = true
        )
        if (passwordErrorMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = passwordErrorMessage.orEmpty(),
                color = Color(0xFFD84B4B),
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(15.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(
                        if (rememberUserId) BrandOrange else Color(0xFFE0E2E5),
                        RoundedCornerShape(3.dp)
                    )
                    .clickable { rememberUserId = !rememberUserId },
                contentAlignment = Alignment.Center
            ) {
                if (rememberUserId) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 6.sp,
                        lineHeight = 6.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = "아이디 기억하기",
                color = SupportingText,
                fontSize = 11.sp,
                modifier = Modifier.clickable { rememberUserId = !rememberUserId }
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "비밀번호 찾기", color = SupportingText, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                passwordErrorMessage = when {
                    loginUserId.isBlank() -> "아이디를 입력해주세요."
                    password.isBlank() -> "비밀번호를 입력해주세요."
                    !onLogin(loginUserId, password, rememberUserId) -> "아이디 또는 비밀번호가 일치하지 않습니다."
                    else -> null
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandOrange,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(text = "로그인", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onSignUpClick)
        ) {
            Text(text = "계정이 없으신가요?", color = SupportingText, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = "회원가입", color = BrandOrange, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun LoginIdField(userId: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "아이디", color = Color(0xFF444444), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(FieldBackground, RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = userId, color = Color(0xFF62666C), fontSize = 13.sp)
        }
    }
}

@Composable
private fun AppInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = Color(0xFF444444), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            placeholder = { Text(text = placeholder, color = Color(0xFFB9BDC3), fontSize = 13.sp) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FieldBackground,
                unfocusedContainerColor = FieldBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = BrandOrange
            )
        )
    }
}

@Composable
private fun SignUpScreen(
    onSignUp: (name: String, userId: String, password: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(54.dp))
        Text(
            text = "←",
            color = Color(0xFF202020),
            fontSize = 25.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .clickable(onClick = onBack)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        RunningBrandMark(modifier = Modifier.size(86.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "회원가입",
            color = Color(0xFF171717),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "RunMate와 함께 달려볼까요?", color = SupportingText, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(28.dp))
        AppInputField(
            label = "닉네임",
            value = name,
            onValueChange = { name = it; errorMessage = null },
            placeholder = "닉네임을 입력하세요"
        )
        Spacer(modifier = Modifier.height(14.dp))
        AppInputField(
            label = "아이디",
            value = userId,
            onValueChange = { userId = it; errorMessage = null },
            placeholder = "아이디를 입력하세요"
        )
        Spacer(modifier = Modifier.height(14.dp))
        AppInputField(
            label = "비밀번호",
            value = password,
            onValueChange = { password = it; errorMessage = null },
            placeholder = "비밀번호를 입력하세요",
            isPassword = true
        )
        Spacer(modifier = Modifier.height(14.dp))
        AppInputField(
            label = "비밀번호 확인",
            value = passwordConfirmation,
            onValueChange = { passwordConfirmation = it; errorMessage = null },
            placeholder = "비밀번호를 한 번 더 입력하세요",
            isPassword = true
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage.orEmpty(),
                color = Color(0xFFD84B4B),
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                errorMessage = when {
                    name.isBlank() || userId.isBlank() || password.isBlank() || passwordConfirmation.isBlank() ->
                        "모든 정보를 입력해주세요."
                    password != passwordConfirmation -> "비밀번호가 일치하지 않습니다."
                    else -> null
                }
                if (errorMessage == null) onSignUp(name.trim(), userId.trim(), password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandOrange,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(text = "가입하기", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(34.dp))
    }
}

@Composable
private fun HomeScreen(
    account: UserAccount,
    onStartRunning: () -> Unit,
    onCrewClick: () -> Unit,
    onRunningClick: () -> Unit,
    onRecordClick: () -> Unit,
    onCommunityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 84.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "안녕하세요 ☀", color = SupportingText, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "오늘도 함께 달려요!",
                        color = Color(0xFF1A1A1A),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                HomeHeaderButton(symbol = "bell", backgroundColor = Color(0xFFF7F7F7))
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(BrandOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "나", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HomeHeroCard(onStartClick = onStartRunning)
            Spacer(modifier = Modifier.height(14.dp))
            LiveRunningCard()
            Spacer(modifier = Modifier.height(14.dp))
            WeeklyRecordCard()
            Spacer(modifier = Modifier.height(24.dp))
            MatchedCrewSection()
            Spacer(modifier = Modifier.height(20.dp))
            RunningChallengeCard()
            Spacer(modifier = Modifier.height(20.dp))
            RecommendedCrewSection()
            Spacer(modifier = Modifier.height(20.dp))
        }

        HomeBottomNavigation(
            modifier = Modifier.align(Alignment.BottomCenter),
            onCrewClick = onCrewClick,
            onRunningClick = onRunningClick,
            onRecordClick = onRecordClick,
            onCommunityClick = onCommunityClick
        )
    }
}

@Composable
private fun HomeHeaderButton(symbol: String, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (symbol == "bell") {
            BellIcon(color = Color(0xFF737373))
        }
    }
}

@Composable
private fun HomeHeroCard(onStartClick: () -> Unit) {
    val cardShape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(cardShape)
            .background(Color(0xFF28231E))
    ) {
        Image(
            painter = painterResource(id = R.drawable.runmate_home_hero),
            contentDescription = "야간 한강 러닝 배너",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xB8000000), Color(0x7A000000), Color(0x1A000000))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(18.dp)
        ) {
            Text(
                text = "🔥  이번 주 3일 연속 러닝 중",
                color = Color(0xFFFFC184),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = "지금 이 순간,\n함께 뛸 때 더 멀리 간다",
                color = Color.White,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 18.dp, bottom = 18.dp)
                .background(BrandOrange, RoundedCornerShape(8.dp))
                .clickable(onClick = onStartClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "▶", color = Color.White, fontSize = 9.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "러닝 시작하기", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MatchedCrewSection() {
    SectionTitle(title = "나에게 맞춤 크루", action = "전체보기  ›")
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CrewPhotoCard("한강 나이트 러너스", "매주 화·목 20:00", "12명 참여 중")
        CrewPhotoCard("서울 러닝 메이트", "매주 토 07:00", "8명 참여 중")
    }
}

@Composable
private fun SectionTitle(title: String, action: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, color = Color(0xFF2E2E2E), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = action, color = BrandOrange, fontSize = 10.sp)
    }
}

@Composable
private fun CrewPhotoCard(name: String, schedule: String, participants: String) {
    val cardShape = RoundedCornerShape(13.dp)
    Box(
        modifier = Modifier
            .width(174.dp)
            .height(122.dp)
            .clip(cardShape)
            .background(Color(0xFF38322C))
    ) {
        Image(
            painter = painterResource(id = R.drawable.runmate_home_hero),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0x11000000), Color(0xD0000000))))
        )
        Text(
            text = "추천",
            color = BrandOrange,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(9.dp)
                .background(Color.White, RoundedCornerShape(9.dp))
                .padding(horizontal = 7.dp, vertical = 3.dp)
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(11.dp)) {
            Text(text = name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = schedule, color = Color(0xFFE8E8E8), fontSize = 8.sp)
            Text(text = participants, color = Color(0xFFE8E8E8), fontSize = 8.sp)
        }
    }
}

@Composable
private fun RunningChallengeCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .background(BrandOrange, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column {
            Text(text = "이번 주 러닝 챌린지", color = Color(0xFFFFE3C2), fontSize = 10.sp)
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = "3회 완주하고\n러닝 습관을 만들어보세요", color = Color.White, fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
        }
        Row(modifier = Modifier.align(Alignment.BottomStart), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "1,200명 참여 중", color = Color.White, fontSize = 9.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "1 / 3회", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RecommendedCrewSection() {
    SectionTitle(title = "러너들이 추천하는 모임", action = "전체보기  ›")
    Spacer(modifier = Modifier.height(10.dp))
    RecommendedCrewRow("김민지", "오늘 저녁, 여의도에서 함께 달려요!", "18:30")
    Spacer(modifier = Modifier.height(9.dp))
    RecommendedCrewRow("이수빈", "초보 러너 환영, 천천히 같이 뛰어요", "19:00")
    Spacer(modifier = Modifier.height(9.dp))
    RecommendedCrewRow("박지훈", "주말 아침 한강 러닝 멤버를 찾아요", "토 07:00")
}

@Composable
private fun RecommendedCrewRow(name: String, description: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(13.dp))
            .background(Color.White, RoundedCornerShape(13.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(30.dp).background(Color(0xFFFFC071), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = name.take(1), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, color = Color(0xFF373737), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, color = Color(0xFF909090), fontSize = 9.sp, maxLines = 1)
        }
        Text(text = time, color = Color(0xFFAAAAAA), fontSize = 8.sp)
    }
}

@Composable
private fun CrewScreen(
    crews: List<CrewRecruitment>,
    onHomeClick: () -> Unit,
    onCommunityClick: () -> Unit,
    onRunningClick: () -> Unit,
    onRecordClick: () -> Unit,
    onWriteClick: () -> Unit,
    onCrewClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("전체") }
    val visibleCrews = crews.filter { crew ->
        (selectedDifficulty == "전체" || crew.difficulty == selectedDifficulty) &&
            (searchQuery.isBlank() || crew.name.contains(searchQuery) || crew.location.contains(searchQuery))
    }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 82.dp)
        ) {
            Spacer(modifier = Modifier.height(51.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "크루 찾기", color = Color(0xFF202020), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFFFF0E1), CircleShape)
                        .clickable(onClick = onWriteClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "+", color = BrandOrange, fontSize = 22.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                placeholder = { Text(text = "⌕  크루 검색", color = Color(0xFFABAFB5), fontSize = 11.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FieldBackground,
                    unfocusedContainerColor = FieldBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = BrandOrange
                )
            )
            Spacer(modifier = Modifier.height(11.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("전체", "초급", "중급", "고급").forEach { difficulty ->
                    CrewDifficultyChip(
                        text = difficulty,
                        selected = selectedDifficulty == difficulty,
                        onClick = { selectedDifficulty = difficulty }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            visibleCrews.forEachIndexed { index, crew ->
                CrewRecruitmentCard(crew = crew, onClick = { onCrewClick(crew.id) })
                if (index != visibleCrews.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            if (visibleCrews.isEmpty()) {
                Spacer(modifier = Modifier.height(80.dp))
                Text(
                    text = "조건에 맞는 크루가 없어요.",
                    color = SupportingText,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 83.dp)
                .background(BrandOrange, RoundedCornerShape(24.dp))
                .clickable { openGoogleMaps(context) }
                .padding(horizontal = 15.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⌖", color = Color.White, fontSize = 17.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = "지도", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        HomeBottomNavigation(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedTab = HomeTab.Crew,
            onHomeClick = onHomeClick,
            onRunningClick = onRunningClick,
            onRecordClick = onRecordClick,
            onCommunityClick = onCommunityClick
        )
    }
}

private fun openGoogleMaps(context: Context) {
    val query = Uri.encode("한강공원 러닝")
    val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query"))
        .setPackage("com.google.android.apps.maps")
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
    )
    runCatching { context.startActivity(mapsIntent) }
        .getOrElse { context.startActivity(webIntent) }
}

@Composable
private fun CrewDifficultyChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (selected) Color.White else Color(0xFF62666C),
        fontSize = 10.sp,
        modifier = Modifier
            .background(if (selected) BrandOrange else Color(0xFFF4F5F7), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun CrewRecruitmentCard(crew: CrewRecruitment, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = crew.name, color = Color(0xFF2F2F2F), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = crew.difficulty,
                color = BrandOrange,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(Color(0xFFFFF0E1), RoundedCornerShape(9.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(9.dp))
        Text(text = "⌖  ${crew.location}", color = Color(0xFF777B81), fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "◷  ${crew.dateTime}", color = Color(0xFF777B81), fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${crew.distance} · ${crew.pace} · 최대 ${crew.memberLimit}",
            color = Color(0xFF9B9FA4),
            fontSize = 9.sp
        )
        if (crew.introduction.isNotBlank()) {
            Spacer(modifier = Modifier.height(9.dp))
            Text(text = crew.introduction, color = Color(0xFF414141), fontSize = 10.sp, maxLines = 2)
        }
    }
}

@Composable
private fun CrewWriteScreen(
    onBack: () -> Unit,
    onCreate: (
        name: String,
        difficulty: String,
        location: String,
        dateTime: String,
        pace: String,
        distance: String,
        memberLimit: String,
        introduction: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("초급") }
    var location by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var pace by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var memberLimit by remember { mutableStateOf("") }
    var introduction by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(56.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = "←", color = Color(0xFF303030), fontSize = 24.sp)
            }
            Text(
                text = "크루 모집 글 작성",
                color = Color(0xFF272727),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F1F1)))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            CrewFormField("크루 이름 *", name, "예: 강남 러닝 크루") { name = it; errorMessage = null }
            Spacer(modifier = Modifier.height(15.dp))
            Text(text = "난이도 *", color = Color(0xFF4B4B4B), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("초급", "중급", "고급").forEach { item ->
                    CrewDifficultyChip(item, difficulty == item) { difficulty = item }
                }
            }
            Spacer(modifier = Modifier.height(15.dp))
            CrewFormField("위치 *", location, "예: 강남역 1번 출구") { location = it; errorMessage = null }
            Spacer(modifier = Modifier.height(15.dp))
            CrewFormField("운동 시간 *", dateTime, "예: 매주 목요일, 오후 07:00") { dateTime = it; errorMessage = null }
            Spacer(modifier = Modifier.height(15.dp))
            CrewFormField("평균 페이스", pace, "예: 6:30") { pace = it }
            Spacer(modifier = Modifier.height(15.dp))
            CrewFormField("목표 거리", distance, "예: 5") { distance = it }
            Spacer(modifier = Modifier.height(15.dp))
            CrewFormField("최대 인원", memberLimit, "예: 10") { memberLimit = it }
            Spacer(modifier = Modifier.height(15.dp))
            CrewFormField(
                label = "한 줄 소개",
                value = introduction,
                placeholder = "크루를 소개해주세요",
                multiline = true,
                onValueChange = { introduction = it }
            )
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = errorMessage.orEmpty(), color = Color(0xFFD84B4B), fontSize = 11.sp)
            }
        }
        Button(
            onClick = {
                errorMessage = if (name.isBlank() || location.isBlank() || dateTime.isBlank()) {
                    "크루 이름, 위치, 운동 시간을 입력해주세요."
                } else {
                    null
                }
                if (errorMessage == null) {
                    onCreate(
                        name.trim(), difficulty, location.trim(), dateTime.trim(),
                        pace.trim().ifBlank { "-" }, distance.trim().ifBlank { "-" },
                        memberLimit.trim().ifBlank { "-" }, introduction.trim()
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 18.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange, contentColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(text = "모집 등록", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
private fun CrewFormField(
    label: String,
    value: String,
    placeholder: String,
    multiline: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = Color(0xFF4B4B4B), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (multiline) 108.dp else 56.dp),
            placeholder = { Text(text = placeholder, color = Color(0xFFB1B4B9), fontSize = 11.sp) },
            singleLine = !multiline,
            minLines = if (multiline) 3 else 1,
            maxLines = if (multiline) 4 else 1,
            shape = RoundedCornerShape(9.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FieldBackground,
                unfocusedContainerColor = FieldBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = BrandOrange
            )
        )
    }
}

@Composable
private fun CommunityScreen(
    posts: List<CommunityPost>,
    currentUserName: String,
    onHomeClick: () -> Unit,
    onCrewClick: () -> Unit,
    onRunningClick: () -> Unit,
    onRecordClick: () -> Unit,
    onWriteClick: () -> Unit,
    onPostClick: (Int) -> Unit,
    onLikeClick: (Int) -> Unit,
    onCommentAdd: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("전체") }
    var searchQuery by remember { mutableStateOf("") }
    var expandedPostId by remember { mutableStateOf<Int?>(null) }
    val visiblePosts = posts.filter { post ->
        (selectedCategory == "전체" || post.category == selectedCategory) &&
            (searchQuery.isBlank() || post.content.contains(searchQuery) || post.displayAuthor(currentUserName).contains(searchQuery))
    }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 82.dp)
        ) {
            Spacer(modifier = Modifier.height(51.dp))
            Text(text = "커뮤니티", color = Color(0xFF202020), fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(14.dp))
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().height(42.dp),
                placeholder = { Text(text = "⌕  게시글 검색", color = Color(0xFFABAFB5), fontSize = 11.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FieldBackground,
                    unfocusedContainerColor = FieldBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = BrandOrange
                )
            )
            Spacer(modifier = Modifier.height(11.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("전체", "러닝 후기", "질문", "추천", "자유").forEach { category ->
                    CommunityCategoryChip(
                        text = category,
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            visiblePosts.forEachIndexed { index, post ->
                CommunityPostCard(
                    post = post,
                    author = post.displayAuthor(currentUserName),
                    expanded = expandedPostId == post.id,
                    onPostClick = { onPostClick(post.id) },
                    onLikeClick = { onLikeClick(post.id) },
                    onCommentClick = {
                        expandedPostId = if (expandedPostId == post.id) null else post.id
                    },
                    onCommentAdd = { comment -> onCommentAdd(post.id, comment) }
                )
                if (index != visiblePosts.lastIndex) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F2F2)))
                }
            }
            if (visiblePosts.isEmpty()) {
                Spacer(modifier = Modifier.height(80.dp))
                Text(
                    text = "검색 결과가 없어요.",
                    color = SupportingText,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 83.dp)
                .size(48.dp)
                .background(BrandOrange, CircleShape)
                .clickable(onClick = onWriteClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "+", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Normal)
        }
        HomeBottomNavigation(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedTab = HomeTab.Community,
            onHomeClick = onHomeClick,
            onCrewClick = onCrewClick,
            onRunningClick = onRunningClick,
            onRecordClick = onRecordClick
        )
    }
}

@Composable
private fun CommunityCategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (selected) Color.White else Color(0xFF62666C),
        fontSize = 10.sp,
        modifier = Modifier
            .background(if (selected) BrandOrange else Color(0xFFF4F5F7), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun CommunityPostCard(
    post: CommunityPost,
    author: String,
    expanded: Boolean,
    onPostClick: (() -> Unit)? = null,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onCommentAdd: (String) -> Unit
) {
    var commentInput by remember(post.id) { mutableStateOf("") }
    Column(
        modifier = if (onPostClick == null) {
            Modifier.fillMaxWidth().padding(vertical = 14.dp)
        } else {
            Modifier.fillMaxWidth().clickable(onClick = onPostClick).padding(vertical = 14.dp)
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(27.dp).background(Color(0xFFFFD2A4), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = author.take(1), color = BrandOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = author, color = Color(0xFF353535), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.category,
                        color = BrandOrange,
                        fontSize = 8.sp,
                        modifier = Modifier
                            .background(Color(0xFFFFF0E1), RoundedCornerShape(7.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(text = post.timeLabel, color = Color(0xFF9D9D9D), fontSize = 9.sp)
            }
        }
        Spacer(modifier = Modifier.height(9.dp))
        Text(text = post.content, color = Color(0xFF343434), fontSize = 12.sp, lineHeight = 18.sp)
        if (post.hasPhoto) {
            Spacer(modifier = Modifier.height(11.dp))
            Image(
                painter = painterResource(id = R.drawable.runmate_home_hero),
                contentDescription = "러닝 후기 사진",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (post.isLiked) "♥ ${post.likes}" else "♡ ${post.likes}",
                color = if (post.isLiked) BrandOrange else Color(0xFF71757A),
                fontSize = 11.sp,
                modifier = Modifier.clickable(onClick = onLikeClick)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = "▢ ${post.comments.size}",
                color = Color(0xFF71757A),
                fontSize = 11.sp,
                modifier = Modifier.clickable(onClick = onCommentClick)
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))
            post.comments.forEach { comment ->
                Row(modifier = Modifier.padding(bottom = 7.dp)) {
                    Text(text = comment.author, color = Color(0xFF494949), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(text = comment.text, color = Color(0xFF676767), fontSize = 10.sp)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FieldBackground, RoundedCornerShape(9.dp))
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    modifier = Modifier.weight(1f).height(42.dp),
                    placeholder = { Text(text = "댓글을 남겨보세요", color = Color(0xFFAAADB2), fontSize = 10.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = BrandOrange
                    )
                )
                Text(
                    text = "등록",
                    color = BrandOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable {
                            if (commentInput.isNotBlank()) {
                                onCommentAdd(commentInput.trim())
                                commentInput = ""
                            }
                        }
                        .padding(horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun CommunityDetailScreen(
    post: CommunityPost?,
    currentUserName: String,
    onBack: () -> Unit,
    onLikeClick: (Int) -> Unit,
    onCommentAdd: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = "←", color = Color(0xFF272727), fontSize = 24.sp)
            }
            Text(
                text = "게시글",
                color = Color(0xFF272727),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.size(48.dp))
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F1F1)))

        if (post == null) {
            Spacer(modifier = Modifier.height(80.dp))
            Text(
                text = "게시글을 찾을 수 없어요.",
                color = SupportingText,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            CommunityPostCard(
                post = post,
                author = post.displayAuthor(currentUserName),
                expanded = true,
                onLikeClick = { onLikeClick(post.id) },
                onCommentClick = {},
                onCommentAdd = { comment -> onCommentAdd(post.id, comment) }
            )
        }
    }
}

@Composable
private fun CommunityWriteScreen(
    onBack: () -> Unit,
    onPost: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("러닝 후기") }
    var showContentError by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(56.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = "←", color = Color(0xFF303030), fontSize = 24.sp)
            }
            Text(
                text = "글쓰기",
                color = Color(0xFF272727),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            Button(
                onClick = {
                    if (content.isBlank()) {
                        showContentError = true
                    } else {
                        onPost(category, content.trim())
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(72.dp)
                    .height(42.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandOrange,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(text = "등록", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F1F1)))
        TextField(
            value = content,
            onValueChange = { content = it; showContentError = false },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            placeholder = { Text(text = "무슨 생각을 하고 계신가요?", color = Color(0xFFB1B4B9), fontSize = 12.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = BrandOrange
            )
        )
        if (showContentError) {
            Text(
                text = "게시글 내용을 입력해주세요.",
                color = Color(0xFFD84B4B),
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F1F1)))
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text(text = "카테고리 선택", color = Color(0xFF4B4B4B), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(11.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("러닝 후기", "질문", "추천", "자유").forEach { item ->
                    CommunityCategoryChip(item, category == item) { category = item }
                }
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(text = "▧  사진 추가", color = Color(0xFF656A70), fontSize = 11.sp)
        }
        HomeBottomNavigation(modifier = Modifier, selectedTab = HomeTab.Community)
    }
}

@Composable
private fun CrewDetailScreen(
    crew: CrewRecruitment?,
    isJoined: Boolean,
    onBack: () -> Unit,
    onInquiryClick: () -> Unit,
    onJoin: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showJoinDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        if (crew == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(72.dp))
                Text(text = "크루 정보를 찾을 수 없어요.", color = SupportingText, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "목록으로 돌아가기", color = BrandOrange, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onBack))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 84.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(252.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.runmate_crew_hero),
                        contentDescription = "${crew.name} 크루 이미지",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color(0x38000000), Color(0xCF000000))))
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 38.dp)
                            .size(48.dp)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = "←", color = Color.White, fontSize = 25.sp)
                    }
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DetailTag(crew.difficulty)
                            DetailTag(crew.dateTime)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = crew.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "⌖ ${crew.location}", color = Color.White, fontSize = 11.sp)
                    }
                    if (isJoined) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 88.dp)
                                .background(BrandOrange, RoundedCornerShape(18.dp))
                                .padding(horizontal = 16.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "크루 참여가 완료되었습니다!", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    CrewDetailSection(title = "크루 정보") {
                        CrewInfoRow("◷", "일정", crew.dateTime)
                        Spacer(modifier = Modifier.height(13.dp))
                        CrewInfoRow("⌖", "집결지", crew.location)
                        Spacer(modifier = Modifier.height(13.dp))
                        CrewInfoRow("ϟ", "평균 페이스", crew.pace)
                        Spacer(modifier = Modifier.height(13.dp))
                        CrewInfoRow("⌁", "목표 거리", crew.distance)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    CrewDetailSection(title = "크루 소개") {
                        Text(
                            text = crew.introduction.ifBlank { "함께 즐겁게 달릴 러너를 기다리고 있어요." },
                            color = Color(0xFF70747A),
                            fontSize = 12.sp,
                            lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F0)))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "러닝 코스", color = Color(0xFF9B9FA4), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${crew.location} → 함께 달리는 러닝 코스", color = Color(0xFF404040), fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    CrewDetailSection(title = "크루장") {
                        CrewLeaderRow(ownerName = crew.ownerName, crewName = crew.name)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    CrewDetailSection(title = "참여 멤버", trailing = "${crew.memberLimit} 모집") {
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            listOf("하", "별", "바", "햇", "자").forEachIndexed { index, initial ->
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(
                                            listOf(BrandOrange, Color(0xFF7569F4), Color(0xFF2FC49B), Color(0xFFE19A42), Color(0xFF9564DC))[index],
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = initial, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier.size(30.dp).background(Color(0xFFE8EBEF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "+", color = Color(0xFF8C9299), fontSize = 14.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    CrewDetailSection(title = "크루 규칙") {
                        listOf("시간 약속 꼭 지키기", "안전에 최우선", "서로 배려하고 격려하기", "무리하지 않기").forEach { rule ->
                            Text(text = "•  $rule", color = Color(0xFF62676D), fontSize = 11.sp, modifier = Modifier.padding(vertical = 3.dp))
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(0.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onInquiryClick,
                    modifier = Modifier.weight(0.9f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4F5F7), contentColor = Color(0xFF383D43)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(text = "◯  문의하기", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { showJoinDialog = true },
                    enabled = !isJoined,
                    modifier = Modifier.weight(1.1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandOrange,
                        disabledContainerColor = Color(0xFFD4D8DE),
                        contentColor = Color.White,
                        disabledContentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(text = if (isJoined) "✓  참여 완료" else "참여 신청", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showJoinDialog && crew != null) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text(text = "크루 참여", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
            text = { Text(text = "${crew.name} 크루에 참여하시겠습니까?\n참여 후 크루 정보와 일정은 이 화면에서 확인할 수 있어요.", fontSize = 13.sp, lineHeight = 20.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        onJoin(crew.id)
                        showJoinDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange, contentColor = Color.White)
                ) { Text(text = "예") }
            },
            dismissButton = {
                Button(
                    onClick = { showJoinDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F2F4), contentColor = Color(0xFF4B5056))
                ) { Text(text = "아니요") }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun CrewInquiryScreen(
    crew: CrewRecruitment?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf("") }
    var isSent by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(56.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = "←", color = Color(0xFF303030), fontSize = 24.sp)
            }
            Text(
                text = "크루장 문의",
                color = Color(0xFF272727),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F1F1)))

        if (crew == null) {
            Spacer(modifier = Modifier.height(80.dp))
            Text(
                text = "문의할 크루 정보를 찾을 수 없어요.",
                color = SupportingText,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = crew.name, color = Color(0xFF292929), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "방장 ${crew.ownerName}님에게 모집 관련 문의를 남겨보세요.",
                    color = SupportingText,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "문의 내용", color = Color(0xFF4B4B4B), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = message,
                    onValueChange = { message = it; isSent = false },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    placeholder = { Text(text = "방장에게 전달할 내용을 입력하세요.", color = Color(0xFFB1B4B9), fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = FieldBackground,
                        unfocusedContainerColor = FieldBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = BrandOrange
                    )
                )
                if (isSent) {
                    Spacer(modifier = Modifier.height(13.dp))
                    Text(
                        text = "문의가 접수되었습니다. 방장 ${crew.ownerName}님이 확인하면 답변할 수 있어요.",
                        color = BrandOrange,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { if (message.isNotBlank()) isSent = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 18.dp),
                shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(text = "방장에게 문의 보내기", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun CrewDetailSection(
    title: String,
    trailing: String? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(13.dp))
            .background(Color.White, RoundedCornerShape(13.dp))
            .padding(15.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, color = Color(0xFF292929), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            if (trailing != null) Text(text = trailing, color = Color(0xFFA2A6AC), fontSize = 9.sp)
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun DetailTag(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 9.sp,
        modifier = Modifier
            .background(Color(0xAA000000), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun CrewInfoRow(icon: String, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, color = BrandOrange, fontSize = 15.sp)
        Spacer(modifier = Modifier.width(11.dp))
        Text(text = label, color = Color(0xFF9A9A9A), fontSize = 11.sp, modifier = Modifier.width(72.dp))
        Text(text = value, color = Color(0xFF4A4A4A), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CrewLeaderRow(ownerName: String, crewName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).background(BrandOrange, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = ownerName.take(1), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = ownerName, color = Color(0xFF3E3E3E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "$crewName 크루장", color = Color(0xFF9A9A9A), fontSize = 10.sp)
        }
    }
}

@Composable
private fun RunningHubScreen(
    schedules: List<CrewRunningSchedule>,
    onHomeClick: () -> Unit,
    onCrewTabClick: () -> Unit,
    onRecordClick: () -> Unit,
    onCommunityClick: () -> Unit,
    onSchedulesClick: () -> Unit,
    onCrewDashboardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 82.dp)
        ) {
            Spacer(modifier = Modifier.height(51.dp))
            Text(text = "내 크루 참여", color = Color(0xFF202020), fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(14.dp))
            RunningParticipationSummary()
            Spacer(modifier = Modifier.height(15.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RunningHubTab("참여 크루", true, onCrewDashboardClick, Modifier.weight(1f))
                RunningHubTab("러닝 일정", false, onSchedulesClick, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            RunningCrewPreviewCard(onClick = onCrewDashboardClick)
            Spacer(modifier = Modifier.height(12.dp))
            schedules.take(2).forEach { schedule ->
                RunningHubScheduleRow(schedule = schedule, onClick = onCrewDashboardClick)
                Spacer(modifier = Modifier.height(9.dp))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
                    .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .clickable(onClick = onCrewTabClick),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⊕  새로운 크루 찾기", color = Color(0xFF9BA0A6), fontSize = 11.sp)
            }
        }
        HomeBottomNavigation(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedTab = HomeTab.Running,
            onHomeClick = onHomeClick,
            onCrewClick = onCrewTabClick,
            onRecordClick = onRecordClick,
            onCommunityClick = onCommunityClick
        )
    }
}

@Composable
private fun RunningParticipationSummary() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandOrange, RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ParticipationMetric("참여 크루", "2", "개")
        ParticipationMetric("총 참여", "36", "회")
        ParticipationMetric("누적 거리", "194.8", "km")
    }
}

@Composable
private fun ParticipationMetric(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFFFFE4C4), fontSize = 8.sp)
        Spacer(modifier = Modifier.height(5.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = unit, color = Color.White, fontSize = 8.sp, modifier = Modifier.padding(bottom = 2.dp))
        }
    }
}

@Composable
private fun RunningHubTab(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = if (selected) Color(0xFF383D42) else Color(0xFF969BA1),
        fontSize = 11.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
            .background(if (selected) Color.White else Color(0xFFF5F6F8), RoundedCornerShape(8.dp))
            .border(if (selected) 1.dp else 0.dp, Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp)
    )
}

@Composable
private fun RunningCrewPreviewCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = R.drawable.runmate_crew_hero),
            contentDescription = "강남 아침 러닝 크루",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x18000000), Color(0xC9000000))))
        )
        Column(modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                DetailTag("초급")
                DetailTag("18명")
            }
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
            Text(text = "강남 아침 러닝 크루", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = "⌖ 강남역", color = Color.White, fontSize = 9.sp)
        }
        Text(
            text = "›",
            color = Color.White,
            fontSize = 22.sp,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 13.dp)
        )
    }
}

@Composable
private fun RunningHubScheduleRow(schedule: CrewRunningSchedule, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(11.dp))
            .background(Color.White, RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(30.dp).background(Color(0xFFFFF0E1), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = schedule.dateLabel.take(2), color = BrandOrange, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = schedule.dateLabel, color = Color(0xFF303030), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = "${schedule.time} · ${schedule.location}", color = Color(0xFF92979D), fontSize = 9.sp, maxLines = 1)
        }
        Text(text = schedule.distance, color = BrandOrange, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RunningSchedulesScreen(
    schedules: List<CrewRunningSchedule>,
    isOwner: Boolean,
    onBack: () -> Unit,
    onScheduleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(56.dp).clickable(onClick = onBack), contentAlignment = Alignment.CenterStart) {
                Text(text = "←", color = Color(0xFF303030), fontSize = 24.sp)
            }
            Text(text = "러닝 일정 확인", color = Color(0xFF272727), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.size(56.dp))
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F1F1)))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(text = "참여 중인 크루의 러닝 일정을 확인하세요", color = SupportingText, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(14.dp))
            schedules.forEach { schedule ->
                RunningScheduleDetailCard(schedule, isOwner, onScheduleClick)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun RunningScheduleDetailCard(schedule: CrewRunningSchedule, isOwner: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "강남 아침 러닝 크루", color = Color(0xFF2D2D2D), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = schedule.distance, color = BrandOrange, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(9.dp))
        Text(text = "◷  ${schedule.dateLabel} ${schedule.time}", color = Color(0xFF676D73), fontSize = 10.sp)
        Spacer(modifier = Modifier.height(5.dp))
        Text(text = "⌖  ${schedule.location}", color = Color(0xFF676D73), fontSize = 10.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isOwner) {
                Text(
                    text = "◯  출석 체크",
                    color = Color(0xFF62676D),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF4F5F7), RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp)
                )
            }
            Text(
                text = if (isOwner) "일정 보기" else "러닝 참여 확인",
                color = Color.White,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(if (isOwner) 1f else 2f)
                    .background(BrandOrange, RoundedCornerShape(8.dp))
                    .clickable(onClick = onClick)
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun RunningCrewDashboardScreen(
    schedules: List<CrewRunningSchedule>,
    isOwner: Boolean,
    isParticipating: Boolean,
    attendanceCount: Int,
    onBack: () -> Unit,
    onAddScheduleClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onParticipationChange: (Boolean) -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showParticipationDialog by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 18.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                Image(painter = painterResource(id = R.drawable.runmate_crew_hero), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x28000000), Color(0xC9000000))))
                )
                Box(modifier = Modifier.padding(start = 16.dp, top = 34.dp).size(48.dp).clickable(onClick = onBack), contentAlignment = Alignment.CenterStart) {
                    Text(text = "←", color = Color.White, fontSize = 24.sp)
                }
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text(text = "강남 아침 러닝 크루", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = "⌖ 강남역", color = Color.White, fontSize = 9.sp)
                }
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                RunningParticipationSummary()
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "♧  크루원 연결",
                        color = Color.White,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFFFA54D), RoundedCornerShape(9.dp))
                            .clickable(onClick = onChatClick)
                            .padding(vertical = 10.dp)
                    )
                    if (isOwner) {
                        Text(
                            text = "✓  출석 체크${if (attendanceCount > 0) " ($attendanceCount)" else ""}",
                            color = Color.White,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .background(BrandOrange, RoundedCornerShape(9.dp))
                                .clickable(onClick = onAttendanceClick)
                                .padding(vertical = 10.dp)
                        )
                    } else {
                        Text(
                            text = if (isParticipating) "✓  러닝 참여 예정" else "러닝 참여하기",
                            color = Color.White,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isParticipating) Color(0xFFBFC5CC) else BrandOrange, RoundedCornerShape(9.dp))
                                .clickable { if (!isParticipating) showParticipationDialog = true }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                RunningInfoSection(title = "러닝 일정") {
                    schedules.take(2).forEach { schedule ->
                        Text(text = "${schedule.dateLabel} ${schedule.time}", color = Color(0xFF383D42), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = "${schedule.location} · ${schedule.distance}", color = Color(0xFF8E949A), fontSize = 9.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (isOwner) {
                        Text(
                            text = "+  러닝 일정 추가",
                            color = Color.White,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFA54D), RoundedCornerShape(8.dp))
                                .clickable(onClick = onAddScheduleClick)
                                .padding(vertical = 8.dp)
                        )
                    } else {
                        Text(
                            text = "예정된 러닝에서 함께 만나요!",
                            color = Color(0xFF969BA1),
                            fontSize = 9.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                RunningInfoSection(title = "나의 러닝 기록") {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        ParticipationMetricLight("97.4km", "누적 거리")
                        ParticipationMetricLight("6'10", "평균 페이스")
                        ParticipationMetricLight("2025.02.14", "최근 러닝")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                RunningInfoSection(title = "최근 러닝 기록", trailing = "전체") {
                    listOf("5월 2일 (금)" to "6'11", "4월 30일 (수)" to "6'04", "4월 28일 (월)" to "6'22", "4월 25일 (금)" to "6'06").forEach { (date, pace) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                            Text(text = date, color = Color(0xFF555A60), fontSize = 10.sp, modifier = Modifier.weight(1f))
                            Text(text = pace, color = BrandOrange, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                RunningInfoSection(title = "크루 정보") {
                    Text(text = "24명", color = Color(0xFF62676D), fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "매주 월, 수, 금 07:00", color = Color(0xFF62676D), fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "강남역 4번 출구", color = Color(0xFF62676D), fontSize = 10.sp)
                }
            }
        }
    }

    if (showParticipationDialog) {
        AlertDialog(
            onDismissRequest = { showParticipationDialog = false },
            title = { Text(text = "러닝 참여", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
            text = { Text(text = "5월 5일 (월) 07:00 러닝에 참여하시겠습니까?", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        onParticipationChange(true)
                        showParticipationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange, contentColor = Color.White)
                ) { Text(text = "참여할게요") }
            },
            dismissButton = {
                Button(
                    onClick = { showParticipationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F2F4), contentColor = Color(0xFF4B5056))
                ) { Text(text = "이번엔 안 돼요") }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun RunningInfoSection(title: String, trailing: String? = null, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(13.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = title, color = Color(0xFF303030), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            if (trailing != null) Text(text = trailing, color = Color(0xFFA0A5AB), fontSize = 9.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun ParticipationMetricLight(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Color(0xFF34393F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = label, color = Color(0xFF9A9FA5), fontSize = 8.sp)
    }
}

@Composable
private fun RunningScheduleAddScreen(
    onBack: () -> Unit,
    onAddSchedule: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp)) {
            Box(modifier = Modifier.align(Alignment.CenterStart).size(56.dp).clickable(onClick = onBack), contentAlignment = Alignment.CenterStart) {
                Text(text = "←", color = Color(0xFF303030), fontSize = 24.sp)
            }
            Text(text = "러닝 일정 추가", color = Color(0xFF272727), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F1F1)))
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp)) {
            CrewFormField("날짜 *", date, "예: 5월 12일 (월)") { date = it; errorMessage = null }
            Spacer(modifier = Modifier.height(15.dp))
            CrewFormField("시간 *", time, "예: 07:00") { time = it; errorMessage = null }
            Spacer(modifier = Modifier.height(15.dp))
            CrewFormField("집결지 *", location, "예: 강남역 4번 출구") { location = it; errorMessage = null }
            Spacer(modifier = Modifier.height(15.dp))
            CrewFormField("목표 거리 *", distance, "예: 5km") { distance = it; errorMessage = null }
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = errorMessage.orEmpty(), color = Color(0xFFD84B4B), fontSize = 11.sp)
            }
        }
        Button(
            onClick = {
                errorMessage = if (date.isBlank() || time.isBlank() || location.isBlank() || distance.isBlank()) "모든 일정을 입력해주세요." else null
                if (errorMessage == null) onAddSchedule(date.trim(), time.trim(), location.trim(), distance.trim())
            },
            modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 18.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange, contentColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(text = "일정 추가하기", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
private fun RunningAttendanceScreen(
    checkedMemberNames: Set<String>,
    onBack: () -> Unit,
    onCheckedMemberNamesChange: (Set<String>) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val members = listOf(
        "김민준" to "20대 초반",
        "이수빈" to "30대 초반",
        "박재준" to "20대 후반"
    )
    var showCompleteDialog by remember { mutableStateOf(false) }
    val absentCount = members.size - checkedMemberNames.size

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(56.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = "←", color = Color(0xFF303030), fontSize = 24.sp)
            }
            Text(
                text = "출석 관리",
                color = Color(0xFF272727),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F1F1)))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(text = "러닝 시작 전 참석 인원을 확인해주세요.", color = SupportingText, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AttendanceCount("전체", members.size.toString())
                AttendanceCount("출석", checkedMemberNames.size.toString())
                AttendanceCount("결석", absentCount.toString())
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(text = "출석 체크", color = Color(0xFF343434), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(9.dp))
            members.forEachIndexed { index, (name, info) ->
                val checked = name in checkedMemberNames
                AttendanceMemberRow(
                    name = name,
                    info = info,
                    initial = name.take(1),
                    checked = checked,
                    onPresentClick = {
                        onCheckedMemberNamesChange(checkedMemberNames + name)
                    },
                    onAbsentClick = {
                        onCheckedMemberNamesChange(checkedMemberNames - name)
                    }
                )
                if (index != members.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Button(
            onClick = { showCompleteDialog = true },
            modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 16.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange, contentColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(text = "출석 확인 완료", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(18.dp))
    }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text(text = "출석 확인", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
            text = { Text(text = "출석 ${checkedMemberNames.size}명, 결석 ${absentCount}명으로 확인하시겠습니까?", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showCompleteDialog = false
                        onComplete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange, contentColor = Color.White)
                ) { Text(text = "확인") }
            },
            dismissButton = {
                Button(
                    onClick = { showCompleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F2F4), contentColor = Color(0xFF4B5056))
                ) { Text(text = "취소") }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun AttendanceCount(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFFA0A5AB), fontSize = 9.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = count, color = Color(0xFF31363C), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AttendanceMemberRow(
    name: String,
    info: String,
    initial: String,
    checked: Boolean,
    onPresentClick: () -> Unit,
    onAbsentClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(10.dp))
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp).background(BrandOrange, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = initial, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, color = Color(0xFF383D42), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(text = info, color = Color(0xFF9BA0A6), fontSize = 8.sp)
        }
        Box(
            modifier = Modifier
                .size(27.dp)
                .background(if (checked) BrandOrange else Color(0xFFF0F2F4), RoundedCornerShape(8.dp))
                .clickable(onClick = onPresentClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✓", color = if (checked) Color.White else Color(0xFF9BA0A6), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(27.dp)
                .background(if (!checked) Color(0xFFF0F2F4) else Color(0xFFFFF0E1), RoundedCornerShape(8.dp))
                .clickable(onClick = onAbsentClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "×", color = if (!checked) Color(0xFF9BA0A6) else BrandOrange, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RunningChatScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                "달콤한커피" to "안녕하세요! 강남 아침 러닝 크루에 오신 걸 환영합니다 👋",
                "이수정" to "다들 내일 아침 7시 몇 곳에서 뵐까요?",
                "박재준" to "저는 반포대로 근처에 있어요! 5분 정도 늦을 예정입니다.",
                "달콤한커피" to "네 좋아요! 강남역 4번 출구에서 7시 정각에 출발합니다. 늦지 않게 와주세요!"
            )
        )
    }
    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp)) {
            Box(modifier = Modifier.align(Alignment.CenterStart).size(56.dp).clickable(onClick = onBack), contentAlignment = Alignment.CenterStart) {
                Text(text = "←", color = Color(0xFF303030), fontSize = 24.sp)
            }
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "강남 아침 러닝 크루", color = Color(0xFF272727), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = "5명", color = Color(0xFF999EA4), fontSize = 9.sp)
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F1F1)))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = "참가하셔서 크루에 참여하셨습니다",
                color = Color(0xFF8E9399),
                fontSize = 8.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).background(Color(0xFFF0F1F3), RoundedCornerShape(9.dp)).padding(horizontal = 10.dp, vertical = 5.dp)
            )
            Spacer(modifier = Modifier.height(15.dp))
            messages.forEach { (author, message) ->
                val mine = author == "나"
                ChatBubble(author = author, message = message, mine = mine)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(0.dp)).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(30.dp).background(BrandOrange, CircleShape), contentAlignment = Alignment.Center) {
                Text(text = "☻", color = Color.White, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f).height(52.dp),
                placeholder = { Text(text = "메시지를 입력하세요", color = Color(0xFFADB1B6), fontSize = 11.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FieldBackground,
                    unfocusedContainerColor = FieldBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = BrandOrange
                )
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = "➤",
                color = BrandOrange,
                fontSize = 20.sp,
                modifier = Modifier.clickable {
                    if (input.isNotBlank()) {
                        messages = messages + ("나" to input.trim())
                        input = ""
                    }
                }
            )
        }
    }
}

@Composable
private fun ChatBubble(author: String, message: String, mine: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (mine) Alignment.End else Alignment.Start
    ) {
        if (!mine) Text(text = author, color = Color(0xFF6E7379), fontSize = 9.sp, modifier = Modifier.padding(start = 4.dp, bottom = 3.dp))
        Text(
            text = message,
            color = if (mine) Color.White else Color(0xFF40454B),
            fontSize = 11.sp,
            lineHeight = 16.sp,
            modifier = Modifier
                .background(if (mine) BrandOrange else Color(0xFFF5F6F8), RoundedCornerShape(11.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun RunningScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMeasuring by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(isMeasuring) {
        while (isMeasuring) {
            delay(1_000)
            elapsedSeconds += 1
        }
    }

    val distance = elapsedSeconds / 334f
    val speed = if (elapsedSeconds == 0) 0.0 else 10.8
    val calories = elapsedSeconds / 12

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFF971D), Color(0xFFFF7800))))
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(54.dp))
        Text(
            text = "←  돌아가기",
            color = Color.White,
            fontSize = 17.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .clickable(onClick = onBack)
        )

        Spacer(modifier = Modifier.height(125.dp))
        Text(text = "거리", color = Color.White, fontSize = 17.sp)
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = "%.2f".format(distance),
            color = Color.White,
            fontSize = 73.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = "km", color = Color.White, fontSize = 22.sp)

        Spacer(modifier = Modifier.height(58.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RunningDataCard("시간", runningTimeText(elapsedSeconds), "", Modifier.weight(1f))
                RunningDataCard("평균 페이스", if (elapsedSeconds == 0) "0.0" else "5.6", "분/km", Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RunningDataCard("칼로리", calories.toString(), "kcal", Modifier.weight(1f))
                RunningDataCard("평균 속도", "%.1f".format(speed), "km/h", Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        if (isMeasuring) {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                RunningControlButton(symbol = "Ⅱ", onClick = { isMeasuring = false })
                RunningControlButton(symbol = "■", onClick = onFinish)
            }
        } else {
            RunningControlButton(symbol = "▶", onClick = { isMeasuring = true })
        }
        Spacer(modifier = Modifier.height(23.dp))
        Text(
            text = if (isMeasuring) "측정 중입니다" else "시작 버튼을 눌러주세요",
            color = Color.White,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun RunningDataCard(label: String, value: String, unit: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .height(138.dp)
            .background(Color(0x66FFFFFF), RoundedCornerShape(20.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = label, color = Color.White, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(13.dp))
        Text(text = value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        if (unit.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = unit, color = Color.White, fontSize = 13.sp)
        }
    }
}

@Composable
private fun RunningControlButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .background(Color.White, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, color = BrandOrange, fontSize = 31.sp, fontWeight = FontWeight.Bold)
    }
}

private fun runningTimeText(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
private fun LiveRunningCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(15.dp))
            .background(Color.White, RoundedCornerShape(15.dp))
            .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(BrandOrange, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "실시간 러닝", color = Color(0xFF303030), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "지금 뛰는 중  ›", color = Color(0xFFAAAAAA), fontSize = 10.sp)
        }
        RunnerRow("김", "김민준", "한강공원", "4.2km", "5:30/km")
        RunnerRow("이", "이수빈", "올림픽공원", "6.8km", "5:45/km")
        RunnerRow("박", "박지훈", "여의도", "2.1km", "6:10/km")
    }
}

@Composable
private fun RunnerRow(
    initial: String,
    name: String,
    location: String,
    distance: String,
    pace: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(22.dp).background(Color(0xFFFFC071), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = initial, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, color = Color(0xFF3E3E3E), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "⌖ $location", color = Color(0xFFA4A4A4), fontSize = 8.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = distance, color = Color(0xFF363636), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = pace, color = Color(0xFFA4A4A4), fontSize = 8.sp)
        }
    }
}

@Composable
private fun WeeklyRecordCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(198.dp)
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(15.dp))
            .background(Color.White, RoundedCornerShape(15.dp))
            .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "이번 주 나의 기록", color = Color(0xFF303030), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "전체보기  ›", color = BrandOrange, fontSize = 10.sp)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            RecordMetric("〽", "24.3", "km", "총 거리", Modifier.weight(1f))
            RecordMetric("ϟ", "3", "회", "러닝 횟수", Modifier.weight(1f))
            RecordMetric("♜", "5:52", "/km", "평균 페이스", Modifier.weight(1f))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "주간 목표 달성률", color = Color(0xFFA4A4A4), fontSize = 9.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "68%", color = BrandOrange, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(Color(0xFFF0F0F0), CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(5.dp)
                    .background(BrandOrange, CircleShape)
            )
        }
    }
}

@Composable
private fun RecordMetric(icon: String, value: String, unit: String, label: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, color = BrandOrange, fontSize = 11.sp)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, color = Color(0xFF292929), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = unit, color = Color(0xFF777777), fontSize = 8.sp, modifier = Modifier.padding(start = 1.dp, bottom = 2.dp))
        }
        Text(text = label, color = Color(0xFFA4A4A4), fontSize = 8.sp)
    }
}

@Composable
private fun RecordScreen(
    records: List<RunningRecord>,
    onHomeClick: () -> Unit,
    onCrewClick: () -> Unit,
    onRunningClick: () -> Unit,
    onCommunityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf("주간") }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 82.dp)
        ) {
            Spacer(modifier = Modifier.height(51.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "러닝 기록", color = Color(0xFF202020), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "지난 주보다 12% 더 달렸어요! 🔥", color = SupportingText, fontSize = 10.sp)
                }
                Text(text = "⌯", color = Color(0xFF60656B), fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecordSummaryCard("↗", "이번 주 총 거리", "32.5km", Modifier.weight(1f))
                RecordSummaryCard("◎", "평균 페이스", "5'42\"", Modifier.weight(1f))
                RecordSummaryCard("♙", "달성한 목표", "12회", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "러닝 거리", color = Color(0xFF292929), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                listOf("주간", "월간", "연간").forEach { period ->
                    RecordPeriodChip(
                        text = period,
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period }
                    )
                    if (period != "연간") Spacer(modifier = Modifier.width(5.dp))
                }
            }
            Spacer(modifier = Modifier.height(11.dp))
            RunningDistanceChart(period = selectedPeriod)
            Spacer(modifier = Modifier.height(22.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "최근 러닝 기록", color = Color(0xFF292929), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "전체 기록  ›", color = BrandOrange, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            records.forEachIndexed { index, record ->
                RecentRunningRecordCard(record)
                if (index != records.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
        }
        HomeBottomNavigation(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedTab = HomeTab.Record,
            onHomeClick = onHomeClick,
            onCrewClick = onCrewClick,
            onRunningClick = onRunningClick,
            onCommunityClick = onCommunityClick
        )
    }
}

@Composable
private fun RecordSummaryCard(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(104.dp)
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(11.dp))
            .background(Color.White, RoundedCornerShape(11.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(23.dp).background(Color(0xFFFFF0E1), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, color = BrandOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = Color(0xFF94989D), fontSize = 7.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = Color(0xFF303030), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecordPeriodChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (selected) Color.White else Color(0xFF62666C),
        fontSize = 9.sp,
        modifier = Modifier
            .background(if (selected) BrandOrange else Color(0xFFF4F5F7), RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp)
    )
}

@Composable
private fun RunningDistanceChart(period: String) {
    val values = when (period) {
        "월간" -> listOf(18.5f, 22.3f, 19.8f, 25.1f)
        "연간" -> listOf(85.2f, 92.5f, 78.3f, 95.8f, 32.5f)
        else -> listOf(3.2f, 2.1f, 5.1f, 3.8f, 4.8f, 7.3f, 6.2f)
    }
    val labels = when (period) {
        "월간" -> listOf("1주", "2주", "3주", "4주")
        "연간" -> listOf("1월", "2월", "3월", "4월", "5월")
        else -> listOf("월", "화", "수", "목", "금", "토", "일")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(start = 8.dp, end = 8.dp, top = 9.dp, bottom = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(104.dp)) {
            val horizontalPadding = 8.dp.toPx()
            val topPadding = 9.dp.toPx()
            val bottomPadding = 12.dp.toPx()
            val chartWidth = size.width - horizontalPadding * 2
            val chartHeight = size.height - topPadding - bottomPadding
            val maxValue = (values.maxOrNull() ?: 1f) * 1.15f
            val points = values.mapIndexed { index, value ->
                val x = horizontalPadding + chartWidth * index / (values.lastIndex.coerceAtLeast(1))
                val y = topPadding + chartHeight * (1f - value / maxValue)
                Offset(x, y)
            }
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { point -> lineTo(point.x, point.y) }
            }
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(points.last().x, size.height - bottomPadding)
                lineTo(points.first().x, size.height - bottomPadding)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x55FF942E), Color(0x05FF942E)),
                    startY = topPadding,
                    endY = size.height
                )
            )
            drawPath(linePath, BrandOrange, style = Stroke(width = 1.7.dp.toPx()))
            points.forEach { point ->
                drawCircle(color = Color.White, radius = 3.1.dp.toPx(), center = point)
                drawCircle(color = BrandOrange, radius = 2.1.dp.toPx(), center = point)
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEachIndexed { index, label ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = label, color = Color(0xFF8D9298), fontSize = 8.sp)
                    Text(
                        text = "${values[index]}km",
                        color = Color(0xFF54595E),
                        fontSize = 7.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentRunningRecordCard(record: RunningRecord) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(11.dp))
            .background(Color.White, RoundedCornerShape(11.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = record.distance, color = Color(0xFF303030), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = record.course,
                color = Color(0xFFC98239),
                fontSize = 8.sp,
                modifier = Modifier
                    .background(Color(0xFFFFF0E1), RoundedCornerShape(8.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = record.date, color = Color(0xFF9A9EA3), fontSize = 9.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "시간 ${record.duration}    평균 페이스 ${record.pace}", color = Color(0xFF64696F), fontSize = 9.sp)
    }
}

@Composable
private fun HomeBottomNavigation(
    modifier: Modifier = Modifier,
    selectedTab: HomeTab = HomeTab.Home,
    onHomeClick: (() -> Unit)? = null,
    onCrewClick: (() -> Unit)? = null,
    onRunningClick: (() -> Unit)? = null,
    onRecordClick: (() -> Unit)? = null,
    onCommunityClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(1.dp, Color(0xFFF3F3F3), RoundedCornerShape(14.dp))
            .background(Color.White, RoundedCornerShape(14.dp))
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeNavigationItem(HomeTab.Home, "홈", selectedTab == HomeTab.Home, onHomeClick)
        HomeNavigationItem(HomeTab.Crew, "크루", selectedTab == HomeTab.Crew, onCrewClick)
        HomeNavigationItem(HomeTab.Running, "참여", selectedTab == HomeTab.Running, onRunningClick)
        HomeNavigationItem(HomeTab.Record, "기록", selectedTab == HomeTab.Record, onRecordClick)
        HomeNavigationItem(HomeTab.Community, "커뮤니티", selectedTab == HomeTab.Community, onCommunityClick)
    }
}

@Composable
private fun HomeNavigationItem(tab: HomeTab, label: String, selected: Boolean, onClick: (() -> Unit)? = null) {
    val itemColor = if (selected) BrandOrange else Color(0xFF9E9E9E)
    Column(
        modifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HomeNavigationIcon(tab = tab, color = itemColor)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = label, color = itemColor, fontSize = 8.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

private enum class HomeTab { Home, Crew, Running, Record, Community }

@Composable
private fun BellIcon(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        val centerX = size.width / 2
        drawCircle(color = color, radius = 4.5.dp.toPx(), center = Offset(centerX, 9.dp.toPx()), style = stroke)
        drawLine(color, Offset(centerX - 6.dp.toPx(), 13.dp.toPx()), Offset(centerX + 6.dp.toPx(), 13.dp.toPx()), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = color, radius = 1.3.dp.toPx(), center = Offset(centerX, 16.dp.toPx()))
    }
}

@Composable
private fun HomeNavigationIcon(tab: HomeTab, color: Color) {
    Canvas(modifier = Modifier.size(19.dp)) {
        val strokeWidth = 1.7.dp.toPx()
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        val center = size.width / 2
        when (tab) {
            HomeTab.Home -> {
                val homePath = Path().apply {
                    moveTo(1.5.dp.toPx(), 9.dp.toPx())
                    lineTo(center, 1.5.dp.toPx())
                    lineTo(17.5.dp.toPx(), 9.dp.toPx())
                    lineTo(17.5.dp.toPx(), 17.dp.toPx())
                    lineTo(12.dp.toPx(), 17.dp.toPx())
                    lineTo(12.dp.toPx(), 12.dp.toPx())
                    lineTo(7.dp.toPx(), 12.dp.toPx())
                    lineTo(7.dp.toPx(), 17.dp.toPx())
                    lineTo(1.5.dp.toPx(), 17.dp.toPx())
                    close()
                }
                drawPath(homePath, color)
            }
            HomeTab.Crew -> {
                drawCircle(color, radius = 2.7.dp.toPx(), center = Offset(6.5.dp.toPx(), 6.dp.toPx()), style = stroke)
                drawCircle(color, radius = 2.25.dp.toPx(), center = Offset(13.dp.toPx(), 7.dp.toPx()), style = stroke)
                val crewPath = Path().apply {
                    moveTo(1.5.dp.toPx(), 16.5.dp.toPx())
                    cubicTo(1.5.dp.toPx(), 12.dp.toPx(), 3.8.dp.toPx(), 10.dp.toPx(), 6.5.dp.toPx(), 10.dp.toPx())
                    cubicTo(9.4.dp.toPx(), 10.dp.toPx(), 11.5.dp.toPx(), 12.dp.toPx(), 11.5.dp.toPx(), 16.5.dp.toPx())
                    moveTo(10.dp.toPx(), 16.5.dp.toPx())
                    cubicTo(10.dp.toPx(), 13.dp.toPx(), 11.5.dp.toPx(), 11.dp.toPx(), 13.5.dp.toPx(), 11.dp.toPx())
                    cubicTo(16.dp.toPx(), 11.dp.toPx(), 17.5.dp.toPx(), 13.dp.toPx(), 17.5.dp.toPx(), 16.5.dp.toPx())
                }
                drawPath(crewPath, color, style = stroke)
            }
            HomeTab.Running -> {
                drawCircle(color, radius = 2.dp.toPx(), center = Offset(12.dp.toPx(), 3.dp.toPx()), style = stroke)
                drawLine(color, Offset(10.dp.toPx(), 7.dp.toPx()), Offset(7.dp.toPx(), 11.dp.toPx()), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(9.dp.toPx(), 8.dp.toPx()), Offset(14.dp.toPx(), 10.dp.toPx()), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(8.dp.toPx(), 11.dp.toPx()), Offset(4.dp.toPx(), 16.dp.toPx()), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(9.dp.toPx(), 11.dp.toPx()), Offset(14.dp.toPx(), 16.dp.toPx()), strokeWidth, StrokeCap.Round)
            }
            HomeTab.Record -> {
                val path = Path().apply {
                    moveTo(1.dp.toPx(), 11.dp.toPx())
                    lineTo(5.dp.toPx(), 11.dp.toPx())
                    lineTo(7.dp.toPx(), 5.dp.toPx())
                    lineTo(10.dp.toPx(), 16.dp.toPx())
                    lineTo(13.dp.toPx(), 8.dp.toPx())
                    lineTo(18.dp.toPx(), 8.dp.toPx())
                }
                drawPath(path, color, style = stroke)
            }
            HomeTab.Community -> {
                drawRoundRect(color, Offset(2.dp.toPx(), 3.dp.toPx()), Size(15.dp.toPx(), 11.dp.toPx()), CornerRadius(4.dp.toPx()), style = stroke)
                drawLine(color, Offset(7.dp.toPx(), 14.dp.toPx()), Offset(5.dp.toPx(), 17.dp.toPx()), strokeWidth, StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun OnboardingIndicator(activePage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(onboardingPages.size) { index ->
            val isActive = index == activePage
            Box(
                modifier = Modifier
                    .width(if (isActive) 24.dp else 6.dp)
                    .height(6.dp)
                    .background(
                        color = if (isActive) BrandOrange else InactiveDot,
                        shape = if (isActive) RoundedCornerShape(50) else CircleShape
                    )
            )
        }
    }
}

/** Uses the supplied source logo directly so the original curves remain crisp. */
@Composable
private fun RunningBrandMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.runmate_logo),
        contentDescription = null,
        modifier = modifier
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun OnboardingPreview() {
    RUNMATETheme(darkTheme = false, dynamicColor = false) {
        RunMateApp()
    }
}
