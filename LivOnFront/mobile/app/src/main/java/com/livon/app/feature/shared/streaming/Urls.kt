package com.livon.app.feature.shared.streaming

import com.livon.app.BuildConfig

object Urls {
//    var applicationServerUrl = "http://192.168.30.137:6080/"
//    var livekitUrl = "ws://192.168.30.137:7880"

    var applicationServerUrl = BuildConfig.APPLICATION_SERVER_URL+"/"
    var livekitUrl = BuildConfig.OPENVIDU_LIVEKIT_URL
}

