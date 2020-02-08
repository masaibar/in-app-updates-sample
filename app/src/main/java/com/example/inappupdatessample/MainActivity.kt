package com.example.inappupdatessample

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : AppCompatActivity(), InstallStateUpdatedListener {

    companion object {
        private const val REQUEST_CODE_FLEXIBLE = 1234
    }

    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初期化する
        appUpdateManager = AppUpdateManagerFactory.create(this)

        // インストール状況の更新を受け取れるようにする
        appUpdateManager.registerListener(this)

        // アップデート有無を確認し、成功時のみ処理をする
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                // Flexibleアップデートを実行する
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    REQUEST_CODE_FLEXIBLE
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                // ユーザーに通知する
                showDownloadedSnackbar()
            }
        }
    }

    override fun onDestroy() {
        appUpdateManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FLEXIBLE) {
            when (resultCode) {
                Activity.RESULT_OK -> { // アップデートに同意
                }
                Activity.RESULT_CANCELED -> { // アップデートをキャンセル
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> { // アップデートが失敗
                }
            }
        }
    }

    override fun onStateUpdate(state: InstallState?) {
        if (state?.installStatus() == InstallStatus.DOWNLOADED) {
            // ユーザーに通知する
            showDownloadedSnackbar()
        }
    }

    private fun showDownloadedSnackbar() {
        Snackbar.make(
            findViewById(R.id.layout_root),
            "更新がダウンロードされました。",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("更新する") {
            // アプリを更新して再起動する
            appUpdateManager.completeUpdate()
        }.show()
    }
}
