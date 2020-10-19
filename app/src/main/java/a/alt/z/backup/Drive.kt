package a.alt.z.backup

import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveRequest
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.http.Consts
import org.apache.http.entity.ContentType

suspend fun <T> DriveRequest<T>.executeWithCoroutines(): T = withContext(Dispatchers.IO) { execute() }

suspend fun Drive.createFile(
    folderId: String,
    mimeType: String,
    name: String
): String {
    val metadata = File().apply {
        setParents(listOf(folderId))
        setMimeType(mimeType)
        setName(name)
    }

    return files()
        .create(metadata)
        .executeWithCoroutines()
        .id
}

suspend fun Drive.fetchOrCreateAppFolder(folderName: String): String {
    val folder = getAppFolder()

    return if(folder.isEmpty()) {
        val metadata = File().apply {
            name = folderName
            mimeType = APP_FOLDER.mimeType
        }

        files().create(metadata)
            .setFields("id")
            .executeWithCoroutines()
            .id
    } else {
        folder.files.first().id
    }
}

suspend fun Drive.queryFiles() = files().list().setSpaces("drive").executeWithCoroutines()

suspend fun Drive.getAppFolder()
        = files().list().setSpaces("drive").setQ("mimeType = '${APP_FOLDER.mimeType}'").executeWithCoroutines()

/**
 * https://developers.google.com/drive/api/v3/mime-types
 */
val APP_FOLDER = ContentType.create("application/vnd.google-apps.folder", Consts.ISO_8859_1)