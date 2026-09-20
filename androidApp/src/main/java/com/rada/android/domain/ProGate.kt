package com.rada.android.domain

import com.rada.shared.domain.ReportType

fun ReportType.requiresPro(): Boolean = this != ReportType.CAMERA

fun ReportType.isVisibleTo(isPro: Boolean): Boolean = isPro || !requiresPro()
