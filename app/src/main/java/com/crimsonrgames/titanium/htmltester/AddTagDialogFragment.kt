/***
 *
 * AddTagDialogFragment.java
 *
 * Copyright 2016 Aldo Pedro Rangel Montiel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.crimsonrgames.titanium.htmltester

import android.support.v4.app.DialogFragment
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [OnAddTagDialogFragmentInteractionListener] interface
 * to handle interaction events.
 */
class AddTagDialogFragment : DialogFragment(), TextView.OnEditorActionListener {

    private var mListener: OnAddTagDialogFragmentInteractionListener? = null

    private var mEditText: EditText? = null

    interface OnAddTagDialogFragmentInteractionListener {
        fun onFinishTagEditDialog(tag: String)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_tag_dialog, container)
        mEditText = view.findViewById<View>(R.id.editTagName) as EditText
        dialog.setTitle(TITLE)
        mEditText!!.requestFocus()
        dialog.window!!.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        mEditText!!.setOnEditorActionListener(this)
        return view
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
            val activity = activity as OnAddTagDialogFragmentInteractionListener?
            activity!!.onFinishTagEditDialog(mEditText!!.text.toString())
            this.dismiss()
            return true
        }
        return false
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnAddTagDialogFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnAddTagDialogFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    companion object {

        val TITLE = "Add Tag"
    }

}// Required empty public constructor
