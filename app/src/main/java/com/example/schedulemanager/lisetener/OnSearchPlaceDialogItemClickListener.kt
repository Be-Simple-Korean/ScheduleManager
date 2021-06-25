package com.example.schedulemanager.lisetener

import android.view.View
import com.example.schedulemanager.data.location.DocumentsVO

interface OnSearchPlaceDialogItemClickListener {
    fun onPlaceDialogItemClickListener(v: View, documentsVO: DocumentsVO)
}