package com.ess.asl

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType
import org.jetbrains.annotations.Nls

class AslSchemaProviderFactory : JsonSchemaProviderFactory, DumbAware {

    override fun getProviders(p0: Project): List<JsonSchemaFileProvider?> {
        return listOf(AslSchemaFileProvider())
    }

    class AslSchemaFileProvider() : JsonSchemaFileProvider {
        override fun isAvailable(file: VirtualFile): Boolean = file.name.endsWith(AslUtils.FILE_SUFFIX)

        override fun getName(): @Nls String = "Amazon State Machine"

        override fun getSchemaFile(): VirtualFile? {
            return JsonSchemaProviderFactory.getResourceFile(AslSchemaProviderFactory::class.java, "/schemas/amazon-states-language.json")
        }

        override fun getSchemaType(): SchemaType = SchemaType.embeddedSchema

    }

}