package com.example.geopunch

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.geopunch.SlideToActView


class BottomSheetFragment : BottomSheetDialogFragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.peekHeight = 400
        bottomSheetBehavior.isHideable = false
        val screenHeight = resources.displayMetrics.heightPixels
        val marginTop = 96 // Example margin in pixels
        val maxHeight = screenHeight - marginTop

        bottomSheetBehavior.maxHeight = maxHeight





    }

}
//interface OnSlideToActAnimationEventListener {
//    /**
//     * Called when the slide complete animation start. You can perform actions during the
//     * complete animations.
//     *
//     * @param view The SlideToActView who created the event
//     * @param threshold The mPosition (in percentage [0f,1f]) where the user has left the cursor
//     */
//    fun onSlideCompleteAnimationStarted(
//        view: SlideToActView,
//        threshold: Float,
//    )
//
//    /**
//     * Called when the slide complete animation finish. At this point the slider is stuck in the
//     * center of the slider.
//     *
//     * @param view The SlideToActView who created the event
//     */
//    fun onSlideCompleteAnimationEnded(view: SlideToActView)
//
//    /**
//     * Called when the slide reset animation start. You can perform actions during the reset
//     * animations.
//     *
//     * @param view The SlideToActView who created the event
//     */
//    fun onSlideResetAnimationStarted(view: SlideToActView)
//
//    /**
//     * Called when the slide reset animation finish. At this point the slider will be in the
//     * ready on the left of the screen and user can interact with it.
//     *
//     * @param view The SlideToActView who created the event
//     */
//    fun onSlideResetAnimationEnded(view: SlideToActView)
//}
//
///**
// * Event handler for the slide complete event.
// * Use this handler to react to slide event
// */
//interface OnSlideCompleteListener {
//    /**
//     * Called when user performed the slide
//     * @param view The SlideToActView who created the event
//     */
//    fun onSlideComplete(view: SlideToActView)
//}
//
///**
// * Event handler for the slide react event.
// * Use this handler to inform the user that he can slide again.
// */
//interface OnSlideResetListener {
//    /**
//     * Called when slides is again available
//     * @param view The SlideToActView who created the event
//     */
//    fun onSlideReset(view: SlideToActView)
//}
//
///**
// * Event handler for the user failure with the Widget.
// * You can subscribe to this event to get notified when the user is wrongly
// * interacting with the widget to eventually educate it:
// *
// * - The user clicked outside of the cursor
// * - The user slided but left when the cursor was back to zero
// *
// * You can use this listener to show a Toast or other messages.
// */
//interface OnSlideUserFailedListener {
//    /**
//     * Called when user failed to interact with the slider slide
//     * @param view The SlideToActView who created the event
//     * @param isOutside True if user pressed outside the cursor
//     */
//    fun onSlideFailed(
//        view: SlideToActView,
//        isOutside: Boolean,
//    )
//}




