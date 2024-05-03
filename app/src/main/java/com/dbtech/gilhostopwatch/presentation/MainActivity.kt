
package com.dbtech.gilhostopwatch.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MobileOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import com.dbtech.gilhostopwatch.presentation.theme.StopWatchViewModel

// https://youtu.be/irIGZj1YON8
// C:\Users\gilho\AppData\Local\Android\Sdk\platform-tools\adb pair 192.168.137.3:34743
// C:\Users\gilho\AppData\Local\Android\Sdk\platform-tools\adb connect 192.168.137.3:43839

// 진동 전역 변수
lateinit var mVibrator: Vibrator
lateinit var vibrationEffectSingle1 : VibrationEffect
lateinit var vibrationEffectSingle2 : VibrationEffect
var Vib_01: Boolean = false
@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 진동
        // https://stackoverflow.com/questions/71745877/vibratormanager-on-wear-os-3-and-android-chipmunk-2021-2-1-beta
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // VibratorManager was only added on API level 31 release.
                val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                mVibrator = vibratorManager.defaultVibrator
            } else {
                // backward compatibility for Android API < 31
                mVibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            vibrationEffectSingle1 = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
//            var timings = longArrayOf(0, 100 )
//            var amplitudes = intArrayOf(0, 100 )
//            vibrationEffectSingle1  = VibrationEffect.createWaveform(timings, amplitudes, -1)
            val timings = longArrayOf(0,800,600,800,600,800 ) // 0 대기, x00 진동, 반복
            val amplitudes = intArrayOf(0,255,0,255,0,255 ) // 0 진동 없음, (0~255) 진동, 반복
            vibrationEffectSingle2  = VibrationEffect.createWaveform(timings,amplitudes, -1) // -1 반복 없이
        }
        Vib_01 = true
        //mVibrator.vibrate(vibrationEffectSingle1) // 진동 실행

        setContent {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // 실행중 화면 항상 켜기
            val viewModel = viewModel<StopWatchViewModel>()
            val timerState by viewModel.timerState.collectAsStateWithLifecycle()
            val stopWatchText by viewModel.stopWatchText.collectAsStateWithLifecycle()
            TimeText( // 상단 현재 시간 표시
                timeTextStyle = TimeTextDefaults.timeTextStyle(
//                fontSize = 10.sp // 크기
                )
            )
            StopWatch( // STOP WATCH
                vibstate = Vib_01,
                state = timerState,
                text = stopWatchText,
                onToggleRunning = viewModel::toggleIsRunning,
                onReset = viewModel::resetTimer,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun StopWatch(
    vibstate: Boolean,
    state: TimerState,
    text: String,
    onToggleRunning: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column( // 컬럼 추가
        modifier = modifier
            .background(MaterialTheme.colors.background), // 워치 백그라운드 컬러
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 00초,30초 진동
        if (state == TimerState.RUNNING && vibstate ) {
//            val vibchk: String = text.split(":")[2] // "HH:mm:ss:SS" 00:11:22:33 // 자주 진동함.
            val vibchk: String = text.substring(6) // "HH:mm:ss:SS" 00:11:22:33
            if ( vibchk == "00:00" || vibchk == "00:01" || vibchk == "00:02" || vibchk == "00:03" ) { // 안되고 넘어가서 추가
                mVibrator.vibrate(vibrationEffectSingle1)
            }
            if ( vibchk == "30:00" || vibchk == "30:01" || vibchk == "30:02" || vibchk == "30:03" ) {
                mVibrator.vibrate(vibrationEffectSingle2)
            }
        }

        // Time Text
        Text(
            text = "길호 STOPWATCH",
            fontSize = if (state != TimerState.RESET) // 실행 하면 폰트 크기를 크게 한다.
            { 10.sp } else { 30.sp }, // 중지 하면 작게
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp)) // 위 아래 간격
        Text(
            text = text,
            fontSize = if (state != TimerState.RESET) { 30.sp } else { 10.sp },
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp)) // 위 아래 간격
        Row( // 1 | 2 나눈다
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // 1 play 버튼
            Button(onClick = onToggleRunning) {
                Icon(
                    imageVector = if (state == TimerState.RUNNING) {
                        // implementation("androidx.compose.material:material-icons-extended:1.6.6")
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(8.dp)) // 옆 사이 간격
            // 2 stop 버튼
            Button(
                onClick = onReset,
                enabled = state != TimerState.RESET,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface //  투명 변경
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null
                )
            }

            Spacer(modifier = Modifier.width(8.dp)) // 옆 사이 간격
            // 진동 버튼
            Button(
                onClick = {
                    Vib_01 = if ( vibstate ) {
                        false
                    } else {
                        true
                    }
//                    System.out.println("vib_01: $Vib_01 vibstate: $vibstate")
                },
                enabled = state != TimerState.RESET,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface //  투명 변경
                )
            ) {
                Icon(
                    // timer 가 동작 할때만 동작 / stop 상태에서는 icon 변경이 발생 하지 않는다.
                    imageVector = if ( vibstate ) {
                        Icons.Default.Vibration
                    } else {
                        Icons.Default.MobileOff
                    },
                    contentDescription = null
                )
            }

        }
    }
}