package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.TestRepository
import com.example.model.TestCategory
import com.example.model.TestStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Device Test", appName)
  }

  @Test
  fun `repository status updates and report generation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = TestRepository()
    assertEquals(TestStatus.UNTESTED, repo.testResults.value[TestCategory.DISPLAY])

    repo.setStatus(TestCategory.DISPLAY, TestStatus.PASSED)
    assertEquals(TestStatus.PASSED, repo.testResults.value[TestCategory.DISPLAY])

    repo.setStatus(TestCategory.BATTERY, TestStatus.FAILED)
    val report = repo.generateReport(context)
    assertTrue(report.contains("Passed: 1"))
    assertTrue(report.contains("Failed: 1"))
    assertTrue(report.contains("DEVICE TEST TOOLKIT"))
  }

  @Test
  fun `preferences manager theme and onboarding state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.PreferencesManager(context)
    assertEquals(true, prefs.isFirstLaunch.value)
    prefs.setThemeMode(com.example.data.AppThemeMode.DARK)
    assertEquals(com.example.data.AppThemeMode.DARK, prefs.themeMode.value)
    prefs.completeFirstLaunch()
    assertEquals(false, prefs.isFirstLaunch.value)
  }
}
