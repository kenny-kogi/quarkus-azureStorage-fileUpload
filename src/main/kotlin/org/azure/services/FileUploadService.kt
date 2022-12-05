package org.azure.services

import io.vertx.core.impl.logging.Logger
import io.vertx.core.impl.logging.LoggerFactory
import org.jboss.resteasy.plugins.providers.multipart.InputPart
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput
import java.net.URL
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class FileUploadService {
    @Inject
    lateinit var azureFileStorageService: AzureFileStorageService

    var logger: Logger = LoggerFactory.getLogger(FileUploadService::class.java)

    fun uploadFiles(files: MultipartFormDataInput): String {
        val files: Map<String, List<InputPart>> = files.formDataMap
        val filesInput: List<InputPart>? = files["file"]

        for (file in filesInput!!) {
            logger.info("---------------upload file-------------")
            val fileName = azureFileStorageService.uploadFile(file)
        }

        return "File Successfully Uploaded"
    }

    fun readFile(fileName: String): URL? {
        return azureFileStorageService.readFile(fileName)
    }

}