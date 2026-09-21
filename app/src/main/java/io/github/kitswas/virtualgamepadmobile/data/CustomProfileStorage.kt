package io.github.kitswas.virtualgamepadmobile.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

class CustomProfileStorage {
    private val profilesDir: File
    private val indexFile: File

    constructor(context: Context) : this(File(context.filesDir, "custom_profiles"))

    constructor(storageRoot: File) {
        profilesDir = File(storageRoot, "custom_profiles").apply { mkdirs() }
        indexFile = File(profilesDir, "profiles_index.json")
    }

    fun saveProfile(profileId: String, profileName: String, configs: Map<ButtonComponent, ButtonConfig>) {
        if (!profilesDir.exists() && !profilesDir.mkdirs()) {
            throw IllegalStateException("Unable to create profile directory: ${profilesDir.absolutePath}")
        }

        val profileFile = File(profilesDir, "$profileId.json")
        profileFile.writeText(Json.encodeToString(configs))

        val metadata = (loadMetadata() + discoverMetadataFromFiles())
            .distinctBy { it.id }
            .toMutableList()

        val existingIndex = metadata.indexOfFirst { it.id == profileId }
        if (existingIndex >= 0) {
            metadata[existingIndex] = metadata[existingIndex].copy(name = profileName)
        } else {
            metadata.add(CustomProfileMetadata(profileId, profileName))
        }

        indexFile.writeText(Json.encodeToString(metadata))
    }

    fun listProfiles(): List<CustomProfileMetadata> =
        (loadMetadata() + discoverMetadataFromFiles())
            .distinctBy { it.id }
            .sortedBy { it.name.lowercase() }

    fun loadProfileConfigs(profileId: String): Map<ButtonComponent, ButtonConfig>? {
        val profileFile = File(profilesDir, "$profileId.json")
        if (!profileFile.exists()) return null

        return try {
            Json.decodeFromString<Map<ButtonComponent, ButtonConfig>>(profileFile.readText())
        } catch (_: Exception) {
            null
        }
    }

    fun renameProfile(profileId: String, newName: String) {
        val metadata = listProfiles().toMutableList()
        val existingIndex = metadata.indexOfFirst { it.id == profileId }
        if (existingIndex >= 0) {
            metadata[existingIndex] = metadata[existingIndex].copy(name = newName)
            indexFile.writeText(Json.encodeToString(metadata))
        }
    }

    fun deleteProfile(profileId: String) {
        val profileFile = File(profilesDir, "$profileId.json")
        if (profileFile.exists()) profileFile.delete()

        val metadata = listProfiles().filterNot { it.id == profileId }
        indexFile.writeText(Json.encodeToString(metadata))
    }

    fun shareProfile(profileId: String): String? {
        val profileFile = File(profilesDir, "$profileId.json")
        return profileFile.takeIf { it.exists() }?.readText()
    }

    private fun loadMetadata(): List<CustomProfileMetadata> {
        if (!indexFile.exists()) return emptyList()

        return try {
            Json.decodeFromString<List<CustomProfileMetadata>>(indexFile.readText())
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun discoverMetadataFromFiles(): List<CustomProfileMetadata> {
        if (!profilesDir.exists()) return emptyList()

        return profilesDir.listFiles()
            ?.filter { it.isFile && it.extension == "json" && it.name != "profiles_index.json" }
            ?.map { file ->
                val profileId = file.nameWithoutExtension
                val fallbackName = profileId.replace(Regex("^custom_"), "Custom ")
                    .replace('_', ' ')
                    .trim()
                CustomProfileMetadata(profileId, fallbackName.ifEmpty { profileId })
            }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
    }
}

@Serializable
data class CustomProfileMetadata(
    val id: String,
    val name: String
)
