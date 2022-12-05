package org.azure.controllers

import org.azure.services.FileUploadService
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@ApplicationScoped
@Path("file/")
class FileUploadResource {

    @Inject
    lateinit var fileUploadService: FileUploadService

    @Path("upload")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    fun fileUpload(@MultipartForm files: MultipartFormDataInput): Response {
        val message = fileUploadService.uploadFiles(files)
        return Response.ok().entity(message).build()
    }

    @Path("read/{filename}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun readFile(@PathParam("filename") fileName: String): Response {
        val message = fileUploadService.readFile(fileName)
        return Response.ok().entity(message).build()
    }
}