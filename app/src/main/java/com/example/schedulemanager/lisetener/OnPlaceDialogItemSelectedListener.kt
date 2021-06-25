package com.example.schedulemanager.lisetener

import com.example.schedulemanager.data.location.DocumentsVO
import com.example.schedulemanager.dialog.SearchPlaceDialog

interface OnPlaceDialogItemSelectedListener {
    fun onPlaceDialogItemSelectedListener(documentsVO: DocumentsVO)
}