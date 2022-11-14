package com.sys_ky.mogakicontroller

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout

class Fragment_Title : Fragment() {

    interface OnTouchListener{
        fun onTouchTitle(view: View, motionEvent: MotionEvent): Boolean
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

        val view = inflater.inflate(R.layout.fragment_title, container, false)

        val title_background = view.findViewById<ConstraintLayout>(R.id.title_background)
        title_background.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            onTouchListener.onTouchTitle(view, motionEvent)
        })

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_title, container, false)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Fragment_Title().apply {
                arguments = Bundle().apply {
                }
            }
    }
}