package com.privateinternetaccess.android.pia.loginpurchasing

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.privateinternetaccess.android.pia.interfaces.IAccount
import com.privateinternetaccess.android.ui.loginpurchasing.PurchasingProcessFragment
import com.privateinternetaccess.android.ui.views.PiaxEditText
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class PurchasingProcessFragmentTest {
    private lateinit var context: Context
    private lateinit var fragment: PurchasingProcessFragment

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        context = ApplicationProvider.getApplicationContext()
        fragment = Mockito.spy(PurchasingProcessFragment())
        Mockito.`when`(fragment.context).thenReturn(context)

        fragment.aProgress = Mockito.mock(LinearLayout::class.java)
        fragment.aSuccess = Mockito.mock(View::class.java)
        fragment.aFailure = Mockito.mock(View::class.java)
        fragment.aEmail = Mockito.mock(LinearLayout::class.java)
        fragment.button = Mockito.mock(Button::class.java)
        fragment.progress = Mockito.mock(View::class.java)
        fragment.tvUsername = Mockito.mock(TextView::class.java)
        fragment.tvPassword = Mockito.mock(TextView::class.java)
        fragment.tvFailureTitle = Mockito.mock(TextView::class.java)
        fragment.tvFailureMessage = Mockito.mock(TextView::class.java)
        fragment.etEmail = Mockito.mock(PiaxEditText::class.java)
    }

    @Test
    fun testSignUpRequestWhenTokenIsNotAvailable() {
        val account = mock<IAccount>()
        Mockito.`when`(fragment.hasToken()).thenReturn(false)
        Mockito.`when`(fragment.hasEmail()).thenReturn(false)
        Mockito.`when`(fragment.hasTempPassword()).thenReturn(false)
        fragment.setAccount(account)
        fragment.setFirePurchasing(true)
        fragment.onResume()
        verify(account).signUp(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun testEmailLayoutIsVisibleWhenEmailIsNotAvailable() {
        val emailLayout = Mockito.spy(LinearLayout(context))
        Mockito.`when`(fragment.hasToken()).thenReturn(true)
        Mockito.`when`(fragment.hasEmail()).thenReturn(false)
        Mockito.`when`(fragment.hasTempPassword()).thenReturn(false)
        fragment.aEmail = emailLayout
        fragment.onResume()
        verify(emailLayout).setVisibility(View.VISIBLE)
    }

    @Test
    fun testFailureLayoutIsVisibleWhenTempPasswordIsNotAvailable() {
        val failureLayout = Mockito.spy(View(context))
        Mockito.`when`(fragment.hasToken()).thenReturn(true)
        Mockito.`when`(fragment.hasEmail()).thenReturn(true)
        Mockito.`when`(fragment.hasTempPassword()).thenReturn(false)
        fragment.aFailure = failureLayout
        fragment.onResume()
        verify(failureLayout).setVisibility(View.VISIBLE)
    }

    @Test
    fun testSuccessLayoutIsVisibleWhenNeededDataIsAvailable() {
        val successLayout = Mockito.spy(View(context))
        Mockito.`when`(fragment.hasToken()).thenReturn(true)
        Mockito.`when`(fragment.hasEmail()).thenReturn(true)
        Mockito.`when`(fragment.hasTempPassword()).thenReturn(true)
        fragment.aSuccess = successLayout
        fragment.onResume()
        verify(successLayout).setVisibility(View.VISIBLE)
    }
}