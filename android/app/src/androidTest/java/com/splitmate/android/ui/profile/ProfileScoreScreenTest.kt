package com.splitmate.android.ui.profile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test
import androidx.compose.runtime.mutableStateOf
import com.splitmate.android.ui.settle.ProfileScoreScreenContent

class ProfileScoreScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun profileScreen_showsUserDetails_andCanEditName() {
        val uiState = mutableStateOf(ProfileUiState(
            user = UserProfile(id = "1", phone = "+1234567890", name = "Test User", upiId = "test@upi")
        ))
        
        var editNameCalledWith: String? = null

        composeTestRule.setContent {
            ProfileScoreScreenContent(
                uiState = uiState.value,
                onUpdateName = { name -> editNameCalledWith = name },
                onUpdateUpiId = {},
                onBackClick = {}
            )
        }

        // Verify initial info displayed
        composeTestRule.onNodeWithText("Test User").assertIsDisplayed()

        // Click Edit Name
        composeTestRule.onNodeWithText("Edit Name").performClick()
        
        // Find text field (which initially has "Test User") and perform text input
        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        
        // Use performTextClearance and performTextInput since it's an OutlinedTextField
        composeTestRule.onNodeWithText("Test User").performTextClearance()
        composeTestRule.onNodeWithText("").performTextInput("Test User Updated")
        
        // Click Save
        composeTestRule.onNodeWithText("Save").performClick()

        // Assert our callback was triggered with the updated name
        assert(editNameCalledWith == "Test User Updated")
    }
}
