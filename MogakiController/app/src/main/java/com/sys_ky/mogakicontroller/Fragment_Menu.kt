package com.sys_ky.mogakicontroller

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout

class Fragment_Menu : Fragment() {

    interface OnTouchListener{
        fun onTouchMenuBackground(view: View, motionEvent: MotionEvent): Boolean
    }
    interface OnClickListener{
        fun onClickMenuButton(id: Int)
    }
    lateinit var onTouchListener: OnTouchListener
    lateinit var onClickListener: OnClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onTouchListener = context as Fragment_Menu.OnTouchListener
        onClickListener = context as Fragment_Menu.OnClickListener
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

        val view =  inflater.inflate(R.layout.fragment_menu, container, false)
        val background = view.findViewById<ConstraintLayout>(R.id.menu_background)
        background.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            onTouchListener.onTouchMenuBackground(view, motionEvent)
        })
        val button_connect = view.findViewById<Button>(R.id.menu_button_connect)
        button_connect.setOnClickListener(View.OnClickListener {
            onClickListener.onClickMenuButton(R.id.menu_button_connect)
        })
        val button_qr = view.findViewById<Button>(R.id.menu_button_qr)
        button_qr.setOnClickListener(View.OnClickListener {
            onClickListener.onClickMenuButton(R.id.menu_button_qr)
        })
        val button_change = view.findViewById<Button>(R.id.menu_button_setting)
        button_change.setOnClickListener(View.OnClickListener {
            onClickListener.onClickMenuButton(R.id.menu_button_setting)
        })
        val button_help = view.findViewById<Button>(R.id.menu_button_help)
        button_help.setOnClickListener(View.OnClickListener {
            onClickListener.onClickMenuButton(R.id.menu_button_help)
        })

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_menu, container, false)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            Fragment_Menu().apply {
                arguments = Bundle().apply {
                }
            }
    }
}