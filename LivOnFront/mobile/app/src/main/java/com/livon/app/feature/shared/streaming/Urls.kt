package com.livon.app.feature.shared.streaming

import com.livon.app.BuildConfig

object Urls {
//    var applicationServerUrl = "http://192.168.45.96:6080/"
//    var livekitUrl = "ws://192.168.45.96:7880"

    var applicationServerUrl = BuildConfig.APPLICATION_SERVER_URL+"/"
    var livekitUrl = BuildConfig.OPENVIDU_LIVEKIT_URL
}

