package com.project.socialconnect.services

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.Metadata
import com.project.socialconnect.constants.DropboxConstants
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import com.dropbox.core.v2.files.GetMetadataErrorException



@Service
class DropboxService: CloudService {
    private val requestConfig = DbxRequestConfig(DropboxConstants.CLIENT_IDENTIFIER)
    private lateinit var service: DbxClientV2

    override fun setCredential(accessToken: String) {
        service = DbxClientV2(requestConfig, accessToken)
    }

    override fun listFiles(pageSize: Int): List<Metadata> {
        val files = service.files().listFolder("")
        return files.entries.take(pageSize)
    }

    override fun checkIfFileExists(fileId: String?, fileName: String?): Boolean {
        try {
            service.files().getMetadata(fileName)
        } catch (e: GetMetadataErrorException) {
            return !(e.errorValue.isPath && e.errorValue.pathValue.isNotFound)
        }
        return true
    }

    override fun createFile(file: MultipartFile, fileName: String): Any {
        val inputStream = BufferedInputStream(file.inputStream)
        return service
                .files()
                .uploadBuilder(fileName)
                .uploadAndFinish(inputStream)
    }

    override fun downloadFile(fileId: String?, fileName: String?): ByteArrayOutputStream? {
        val outputStream = ByteArrayOutputStream()
        service.files().downloadBuilder(fileName).download(outputStream)
        return outputStream
    }
}