package a.alt.z.backup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun requestSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

        val client = GoogleSignIn.getClient(this, gso)

        startActivityForResult(client.signInIntent, 1)
    }

    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener {
                lifecycleScope.launch { onSignInSuccess(it) }
            }
            .addOnFailureListener {
                Log.e(TAG, "failed to sign in", it)
            }
    }

    private suspend fun onSignInSuccess(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential
            .usingOAuth2(this, listOf(DriveScopes.DRIVE_APPDATA))
            .setSelectedAccount(account.account)

        val drive = Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential)
            .setApplicationName(APPLICATION_NAME)
            .build()

        val appFolderId = drive.fetchOrCreateAppFolder("haema")

        val metadata = File().apply {
            name = "haema.db"
            parents = Collections.singletonList(appFolderId)
        }

        val filePath = java.io.File("files/haema.db") /* TODO */
        val content = FileContent("application/db", filePath)

        val file = drive.files().create(metadata, content).setFields("id").executeWithCoroutines()

        Log.d(TAG, "File ID: ${file.id}")
    }

    override fun onStart() {
        super.onStart()

        GoogleSignIn.getLastSignedInAccount(this)
            ?.let { lifecycleScope.launch { onSignInSuccess(it) } }
            ?: requestSignIn()
    }

    companion object {
        private val TAG = "aaltz.debug"
        private const val APPLICATION_NAME = "Haema"
    }
}