package a.alt.z.backup

import android.content.Intent
import com.google.api.services.drive.Drive
import java.io.ByteArrayOutputStream

class DriveServiceHelper(private val driveService: Drive) {

    fun uploadFile() {}

    fun downloadFile(fileId: String) {
        val outputStream = ByteArrayOutputStream()
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
    }

    fun createFilePickerIntent(type: String = "application/db"): Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        setType(type)
    }
}