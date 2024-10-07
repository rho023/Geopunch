package com.example.geopunch

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextClock
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

//
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Profile_Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Profile_Fragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val setHours = view.findViewById<ImageButton>(R.id.hrs)
        setHours.setOnClickListener {
            showCustomDialog()
        }
    }

    private fun showCustomDialog() {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom, null)

        // Create and configure the dialog
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)

        // Set up the close button
        val closeButton = dialogView.findViewById<Button>(R.id.dialog_button)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        // Show the dialog
        dialog.show()
        val clock1 = dialogView.findViewById<TextClock>(R.id.text_clock)
        val clock2 = dialogView.findViewById<TextClock>(R.id.text_clock2)

        clock1.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Appointment time")
                .build()

            picker.show(parentFragmentManager, "tag")

            picker.addOnPositiveButtonClickListener {
                val hour = picker.hour
                val minute = picker.minute
                val selectedTime = String.format("%02d:%02d", hour, minute)
                clock1.text = selectedTime
            }
        }

        clock2.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Appointment time")
                .build()

            picker.show(parentFragmentManager, "tag")

            picker.addOnPositiveButtonClickListener {
                val hour = picker.hour
                val minute = picker.minute
                val selectedTime = String.format("%02d:%02d", hour, minute)
                clock2.text = selectedTime
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Profile_Fragment.
         */

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Profile_Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}