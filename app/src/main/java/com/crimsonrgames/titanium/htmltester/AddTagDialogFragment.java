/***

 AddTagDialogFragment.java

 Copyright 2016 Aldo Pedro Rangel Montiel

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package com.crimsonrgames.titanium.htmltester;

import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnAddTagDialogFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class AddTagDialogFragment extends DialogFragment implements TextView.OnEditorActionListener {

    public static final String TITLE = "Add Tag";

    public interface OnAddTagDialogFragmentInteractionListener {
        void onFinishTagEditDialog(String tag);
    }

    private OnAddTagDialogFragmentInteractionListener mListener;

    private EditText mEditText;

    public AddTagDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_tag_dialog, container);
        mEditText = (EditText) view.findViewById(R.id.editTagName);
        getDialog().setTitle(TITLE);
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mEditText.setOnEditorActionListener(this);
        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
            OnAddTagDialogFragmentInteractionListener activity = (OnAddTagDialogFragmentInteractionListener) getActivity();
            activity.onFinishTagEditDialog(mEditText.getText().toString());
            this.dismiss();
            return true;
        }
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAddTagDialogFragmentInteractionListener) {
            mListener = (OnAddTagDialogFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAddTagDialogFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
