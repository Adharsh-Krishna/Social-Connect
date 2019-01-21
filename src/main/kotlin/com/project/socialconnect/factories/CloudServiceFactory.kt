package com.project.socialconnect.factories

import com.project.socialconnect.services.CloudService
import com.project.socialconnect.services.DropboxService
import com.project.socialconnect.services.GoogleDriveService

interface CloudServiceFactory {

    companion object {
        private val cloudServices: Map<String, CloudService> = mutableMapOf(
                Pair("google", GoogleDriveService()),
                Pair("dropbox", DropboxService())
        )

        fun getCloudService(cloudServiceName: String): CloudService {
            return cloudServices.get(cloudServiceName)!!
        }
    }
}