package edu.villadarez.knockknock.shared.session

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionManagerInstrumentedTest {

    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("knockknock_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        sessionManager = SessionManager(context)
    }

    @Test
    fun saveAuthTokenPersistsTokenForLaterFetch() {
        sessionManager.saveAuthToken("jwt-token")

        assertEquals("jwt-token", sessionManager.fetchAuthToken())
    }
}
