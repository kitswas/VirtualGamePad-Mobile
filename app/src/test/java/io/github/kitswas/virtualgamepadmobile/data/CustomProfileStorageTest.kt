package io.github.kitswas.virtualgamepadmobile.data

import io.github.kitswas.virtualgamepadmobile.ui.screens.shouldPromptToSaveOnExit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CustomProfileStorageTest {
    @Test
    fun saveProfileCreatesIndexAndFindsProfile() {
        val tempDir = File.createTempFile("custom-profiles-test", "").apply {
            delete()
            mkdirs()
        }

        val storage = CustomProfileStorage(tempDir)
        val profileId = "test_profile"
        val profileName = "Test Profile"
        val configs = mapOf(
            ButtonComponent.LEFT_ANALOG_STICK to ButtonConfig.default(ButtonComponent.LEFT_ANALOG_STICK),
            ButtonComponent.RIGHT_ANALOG_STICK to ButtonConfig.default(ButtonComponent.RIGHT_ANALOG_STICK)
        )

        storage.saveProfile(profileId, profileName, configs)

        val profiles = storage.listProfiles()
        assertEquals(1, profiles.size)
        assertEquals(profileId, profiles[0].id)
        assertEquals(profileName, profiles[0].name)
        assertTrue(File(tempDir, "custom_profiles/$profileId.json").exists())
        assertTrue(File(tempDir, "custom_profiles/profiles_index.json").exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun newProfilesPromptToSaveEvenWhenUnchanged() {
        val baseline = mapOf(ButtonComponent.LEFT_ANALOG_STICK to ButtonConfig.default(ButtonComponent.LEFT_ANALOG_STICK))
        val editable = mapOf(ButtonComponent.LEFT_ANALOG_STICK to ButtonConfig.default(ButtonComponent.LEFT_ANALOG_STICK))

        assertTrue(shouldPromptToSaveOnExit(isNewProfile = true, editableConfigs = editable, baselineConfigs = baseline))
    }
}
