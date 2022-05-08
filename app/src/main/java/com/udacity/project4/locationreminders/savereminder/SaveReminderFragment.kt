package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity.Companion.TAG
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            val reminder = ReminderDataItem(
                title, description, location, latitude, longitude
            )
            if (_viewModel.validateEnteredData(reminder)) {
                _viewModel.saveReminder(reminder)
                val geofence = getGeofence(reminder.id, latitude, longitude)
                val geofencingRequest = getGeofencingRequest(geofence)
                val geofencingClient = LocationServices.getGeofencingClient(requireContext())
                geofencingRequest?.let { it1 ->
                    geofencingClient.addGeofences(it1, getPendingIntent())
                        .addOnCompleteListener {
                            Toast.makeText(context, R.string.geofences_added, Toast.LENGTH_SHORT)
                                .show()
                            Log.d(TAG, getString(R.string.geofences_added))
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                R.string.geofences_not_added,
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(TAG, getString(R.string.geofences_not_added) + it.message)
                        }
                }
            }
        }
    }

    // Get a geofencing request
    private fun getGeofencingRequest(geofence: Geofence?): GeofencingRequest? {
        val geofencingRequest = geofence?.let {
            GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(it)
                .build()
        }
        return geofencingRequest
    }

    // Get a geofence
    private fun getGeofence(
        id: String,
        latitude: Double?,
        longitude: Double?
    ): Geofence {
        return Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latitude!!, longitude!!, GEOFENCE_RADIUS_IN_METERS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setLoiteringDelay(5000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    // Get a PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        return PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        const val GEOFENCE_RADIUS_IN_METERS = 100f
    }
}
