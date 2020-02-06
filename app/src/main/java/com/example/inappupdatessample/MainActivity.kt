package com.example.inappupdatessample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : AppCompatActivity(), InstallStateUpdatedListener {

    companion object {
        private const val REQUEST_CODE_IMMEDIATE = 5678
    }

    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初期化する
        appUpdateManager = AppUpdateManagerFactory.create(this)

        // インストール状況の更新を受け取れるようにする(Immediateでは不要)
        appUpdateManager.registerListener(this)

        // アップデート有無を確認し、成功時のみ処理をする
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Immediateアップデートを実行する
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    REQUEST_CODE_IMMEDIATE
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Immediateアップデートを再開する
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    REQUEST_CODE_IMMEDIATE
                )
            }
        }
    }

    override fun onDestroy() {
        appUpdateManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMMEDIATE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    // アップデートに同意
                }
                Activity.RESULT_CANCELED -> {
                    // アップデートをキャンセル
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    // アップデートが失敗
                }
            }
        }
    }

    override fun onStateUpdate(state: InstallState?) {
        // Immediateでは不要
    }
}
