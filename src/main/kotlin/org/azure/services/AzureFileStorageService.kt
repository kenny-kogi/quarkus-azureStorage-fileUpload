package org.azure.services

import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.BlobContainerClientBuilder
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.sas.BlobContainerSasPermission
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues
import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import io.vertx.core.impl.logging.Logger
import io.vertx.core.impl.logging.LoggerFactory
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.resteasy.plugins.providers.multipart.InputPart
import reactor.core.publisher.Flux
import java.io.InputStream
import java.net.URL
import java.nio.ByteBuffer
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.core.MultivaluedMap

@ApplicationScoped
class AzureFileStorageService {
    @ConfigProperty(name = "azure.bloburl")
    lateinit var  blobUrl:String
    @ConfigProperty(name = "app.config.azure.storage.container")
    lateinit var containerName:String
    @ConfigProperty(name = "azure.storage.account-name")
    lateinit var accountName:String
    @ConfigProperty(name = "azure.storage.account-key")
    lateinit var accountKey:String
    @ConfigProperty(name = "azure.sasToken")
    lateinit var sasToken:String
    @ConfigProperty(name = "app.config.azure.storage-endpoint")
    lateinit var storageEndpoint:String

    var logger: Logger = LoggerFactory.getLogger(AzureFileStorageService::class.java)

    // file upload to azure store
    fun uploadFile(inputFile: InputPart): String? {
        val containerUrl = "AccountName=$accountName;" +
                "AccountKey=$accountKey;" +
                "EndpointSuffix=core.windows.net;" +
                "DefaultEndpointsProtocol=https;"

        logger.info("Connection string: $containerUrl")

        val container: BlobContainerAsyncClient = BlobContainerClientBuilder()
                .connectionString(containerUrl).
                containerName(containerName)
                .buildAsyncClient()

        val header: MultivaluedMap<String, String> = inputFile!!.headers
        var fileName = getFileName(header)
        logger.info("Single file upload!")
        logger.info("fileName : $fileName")
        logger.info("fileSize: ${inputFile.headers.size}")
        val inputStream = inputFile.getBody(InputStream::class.java, null)

        val blob: BlobAsyncClient = container.getBlobAsyncClient(fileName)

        val blockSize = 2L * 1024L * 1024L

        blob.upload(
                Flux.just(ByteBuffer.wrap(inputStream.readAllBytes())),
                getTransferOptions(blockSize),
                true
        )
                .doOnSuccess { res-> logger.info(res.toString()) }
                .subscribe()
        logger.info("Finished uploading file to bucket")
        return fileName

    }

    // read file from azure
    fun readFile(filename: String): URL?{
        val sasToken = generateCustomSaasToken()
        logger.info("SAS Token $sasToken")
        val signedUrl = "$blobUrl/$filename?$sasToken"
        logger.info("Signed URL $signedUrl")
        println("Signed Url: $signedUrl")
        return URL(signedUrl)
    }

    private fun generateCustomSaasToken(): String? {

        val containerUrl = "AccountName=$accountName;" +
                "AccountKey=$accountKey;" +
                "EndpointSuffix=core.windows.net;" +
                "DefaultEndpointsProtocol=https;"

        logger.info("Connection string: $containerUrl")

        val container: BlobContainerAsyncClient = BlobContainerClientBuilder()
                .connectionString(containerUrl).
                containerName(containerName)
                .buildAsyncClient()

        // Configure the sas parameters. This is the minimal set.
        val expiryTime = OffsetDateTime.now().plusMinutes(15)
        val accountSasPermission = AccountSasPermission().setReadPermission(true)
        val services = AccountSasService().setBlobAccess(true)
        val resourceTypes = AccountSasResourceType().setObject(true)

        // Generate a sas using a container client
        val containerSasPermission = BlobContainerSasPermission()
                .setCreatePermission(true)
                .setReadPermission(true)
        val serviceSasValues = BlobServiceSasSignatureValues(expiryTime, containerSasPermission)
        return container.generateSas(serviceSasValues)
    }

    private fun getFileName(header: MultivaluedMap<String, String>): String? {
        val contentDisposition = header.getFirst("Content-Disposition").split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (filename in contentDisposition) {
            if (filename.trim { it <= ' ' }.startsWith("filename")) {
                val name = filename.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                return name[1].trim { it <= ' ' }.replace("\"".toRegex(), "").uppercase()
            }
        }
        return "unknown"
    }

    fun getFileType(filename: String): String {
        return filename.substringAfterLast('.', "")
    }

    private fun getTransferOptions(blockSize: Long): ParallelTransferOptions? {
        return ParallelTransferOptions()
                .setBlockSizeLong(blockSize)
                .setMaxConcurrency(5)
                .setProgressReceiver { bytesTransferred -> logger.info("Uploading bytes") }
    }



}