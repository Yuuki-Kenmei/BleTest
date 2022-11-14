package com.sys_ky.mogakicontroller

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.sys_ky.mogakicontroller.control.StickView

class Fragment_StickController : Fragment() {

    interface OnTouchListener{
        fun onTouchStick(view: StickView, motionEvent: MotionEvent): Boolean
    }
    lateinit var onTouchListener: OnTouchListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onTouchListener = context as OnTouchListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_stick_controller, container, false)

        val stick_left = view.findViewById<StickView>(R.id.main_stickView_left)
        stick_left.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            onTouchListener.onTouchStick(view as StickView, motionEvent)
        })
        val stick_right = view.findViewById<StickView>(R.id.main_stickView_right)
        stick_right.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            onTouchListener.onTouchStick(view as StickView, motionEvent)
        })

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_stick_controller, container, false)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Fragment_StickController().apply {
                arguments = Bundle().apply {
                }
            }
    }
}