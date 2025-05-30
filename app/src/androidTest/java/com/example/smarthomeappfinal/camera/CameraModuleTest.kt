package com.example.smarthomeappfinal.camera

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.webrtc.EglBase

@RunWith(AndroidJUnit4::class)
class CameraModuleTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    private lateinit var context: Context
    private lateinit var eglBase: EglBase
    private lateinit var cameraModule: CameraModule

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        eglBase = EglBase.create()
        cameraModule = CameraModule(context, eglBase.eglBaseContext)
    }

    @Test
    fun testCameraInitialization() {
        assert(cameraModule != null)
    }

    @Test
    fun testCameraRelease() {
        cameraModule.release()
        // Verify no crashes occur during release
    }

    @Test
    fun testStreamingLifecycle() = runBlocking {
        val iceServers = listOf(
            org.webrtc.PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                .createIceServer()
        )

        cameraModule.startStreaming(iceServers) { _ -> }
        assert(cameraModule.cameraState.value == CameraState.Streaming)

        cameraModule.stopStreaming()
        assert(cameraModule.cameraState.value == CameraState.Initialized)
    }
} 