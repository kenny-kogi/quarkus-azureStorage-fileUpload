package org.azure.model

import javax.enterprise.context.ApplicationScoped
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
@ApplicationScoped
data class FileUploadMetaData(
        @GeneratedValue(strategy = GenerationType.AUTO)
        @Id
        val id: Long = 0,
        var filename: String? = null,
        var fileType: String? = null,
        var contentSize: Long = 0
)
