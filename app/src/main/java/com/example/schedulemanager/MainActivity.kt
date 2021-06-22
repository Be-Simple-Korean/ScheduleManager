package com.example.schedulemanager

import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.schedulemanager.databinding.ActivityMainBinding
import com.google.android.gms.common.GoogleApiAvailability

class MainActivity : AppCompatActivity() {
    companion object{
        const val REQUEST_CODE=1000
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    }

    lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MyViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel=ViewModelProvider(this).get(MyViewModel::class.java)
        binding.fabMainAddSchedule.setOnClickListener{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.GET_ACCOUNTS
                ),
                REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== REQUEST_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getGoogleConditionResult()
            }
        }
    }

    /**
     * 구글 api 사용을 위한 조건 검사
     */
    private fun getGoogleConditionResult() {
        if(!viewModel.isDeviceOnline(this)){
            Toast.makeText(this,"인터넷 연결좀",Toast.LENGTH_SHORT).show()
        }else if(!viewModel.isGooglePlayServiceAvailable(this)){
            val sCode=viewModel.updateServiceGuide(this)
            if(sCode!=0){
                showErrorDialog(sCode)
            }else{
                getGoogleConditionResult()
            }
        }
    }

    /**
     * 구글플레이서비스 업데이트 수행 대화상자
     */
    private fun showErrorDialog(sCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
            this, sCode,
            REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog.show()
    }



}