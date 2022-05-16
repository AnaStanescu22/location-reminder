package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Class under test
    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersDAO: RemindersDao
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        remindersDAO = remindersDatabase.reminderDao()
        repository =
            RemindersLocalRepository(
                remindersDAO
            )
    }

    @After
    fun closeDb() = remindersDatabase.close()

    @Test
    fun insertThreeReminders_getAllThreeFromDatabase() = runBlocking {
        // GIVEN - insert three reminders in the database
        val reminder1 = ReminderDTO(
            "title1",
            "description1",
            "somewhere1",
            11.0,
            11.0,
            "random1"
        )
        val reminder2 = ReminderDTO(
            "title2",
            "descriptio2n",
            "somewhere2",
            12.0,
            12.0,
            "random2"
        )
        val reminder3 = ReminderDTO(
            "title3",
            "description3",
            "somewhere3",
            13.0,
            13.0,
            "random3"
        )
        remindersDatabase.reminderDao().saveReminder(reminder1)
        remindersDatabase.reminderDao().saveReminder(reminder2)
        remindersDatabase.reminderDao().saveReminder(reminder3)
        val remindersList = listOf(reminder1, reminder2, reminder3).sortedBy { it.id }

        // WHEN - Get all the reminders from the database
        val loadedRemindersList = remindersDatabase.reminderDao().getReminders()
        val sortedLoadedRemindersList = loadedRemindersList.sortedBy { it.id }
        val reminder = repository.getReminder("fake") as Result.Error

        // THEN - The loaded data contains the expected values
        assertThat(reminder.message, `is`("Reminder not found!"))
        assertThat(sortedLoadedRemindersList[0].id, `is`(remindersList[0].id))
        assertThat(sortedLoadedRemindersList[1].id, `is`(remindersList[1].id))
        assertThat(sortedLoadedRemindersList[2].id, `is`(remindersList[2].id))
    }
}