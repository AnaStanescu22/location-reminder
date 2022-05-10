package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //Completed: provide testing to the RemindersListViewModel and its live data objects

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Use a fake repository to be injected into the view model.
    private lateinit var remindersRepository: FakeDataSource

    @Before
    fun setupViewModel() {
        stopKoin()
        // Initialise the repository with no reminders.
        remindersRepository = FakeDataSource()

        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), remindersRepository
        )
    }

    @Test
    fun loadReminders_loading() {
        // GIVEN - we are loading reminders
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()

        // WHEN - the dispatcher is paused, showLoading is true
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()

        // THEN - when the dispatcher is resumed, showloading is false
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadRemindersWhenUnavailable_causesError() {
        // GIVEN - there's a problem loading reminders
        // Make the repository return errors
        remindersRepository.setReturnError(true)

        // WHEN - we want to load rhe reminders
        remindersListViewModel.loadReminders()

        // THEN - It's an error, there's a snackbar
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), not(nullValue()))
    }
}