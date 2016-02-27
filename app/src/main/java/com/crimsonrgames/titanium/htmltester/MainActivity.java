/***

 MainActivity.java

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

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements AddTagDialogFragment.OnAddTagDialogFragmentInteractionListener{


    public static final String TAG = "MainActivity";
    public static final String SAVED = "Saved";
    public static final String RESTORED_TO_DEFAULT_TEXT = "Restored to default text";
    public static final String RESTORED_TO_LAST_SAVED_FILE = "Restored to last saved file";
    public static final String TAG_PATTERN = "<%s> </%s>";
    private static String DEFAULT_HTML_STRING = "<html><body><h1>TODO: Write your own code here!!</h1></body></html>";

    private static String DEFAULT_FILE_NAME = "test.html";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    /**
     * The EditorFragment that will be used to edit the source code
     */
    private EditorFragment mEditorFragment;
    /**
     * The PreviewFragment that will be used to show the WebView with the contents of the source
     * code
     */
    private PreviewFragment mPreviewFragment;

    private String mSourceCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mSourceCode = readHTMLFromDefaultFile();
        if(mEditorFragment == null){
            mEditorFragment = EditorFragment.newInstance(mSourceCode);
        }
        if(mPreviewFragment == null){
            mPreviewFragment = PreviewFragment.newInstance(mSourceCode);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mEditorFragment = ((EditorFragment)mSectionsPagerAdapter.getRegisteredFragment(0));
                if(mEditorFragment == null) return;
                mSourceCode = mEditorFragment.getmSourceCode();
                mPreviewFragment = ((PreviewFragment)mSectionsPagerAdapter.getRegisteredFragment(1));
                if(mPreviewFragment == null) return;
                mPreviewFragment.setmSourceCode(mSourceCode);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditorFragment = ((EditorFragment)mSectionsPagerAdapter.getRegisteredFragment(0));
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mEditorFragment.getmSourceCode());
                sendIntent.setType("text/html");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        mEditorFragment = ((EditorFragment)mSectionsPagerAdapter.getRegisteredFragment(0));
        mPreviewFragment = ((PreviewFragment)mSectionsPagerAdapter.getRegisteredFragment(1));
        switch (id){
            case R.id.action_save:
                mSourceCode = mEditorFragment.getmSourceCode();
                this.writeHTMLToDefaultFile(mSourceCode);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(this.getApplicationContext(), SAVED, duration);
                toast.show();
                break;
            case R.id.action_restore_default:
                mSourceCode = DEFAULT_HTML_STRING;
                mEditorFragment.setmSourceCode(mSourceCode);
                mPreviewFragment.setmSourceCode(mSourceCode);
                duration = Toast.LENGTH_SHORT;
                toast = Toast.makeText(this.getApplicationContext(), RESTORED_TO_DEFAULT_TEXT, duration);
                toast.show();
                break;
            case R.id.action_return_to_saved:
                mSourceCode = this.readHTMLFromDefaultFile();
                mEditorFragment.setmSourceCode(mSourceCode);
                mPreviewFragment.setmSourceCode(mSourceCode);
                duration = Toast.LENGTH_SHORT;
                toast = Toast.makeText(this.getApplicationContext(), RESTORED_TO_LAST_SAVED_FILE, duration);
                toast.show();
                break;
            case R.id.action_insert_tag:
                showAddTagDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showAddTagDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AddTagDialogFragment addTagDialog = new AddTagDialogFragment();
        addTagDialog.show(fm,TAG);
    }


    private boolean writeHTMLToDefaultFile(String HTMLString) {

        File file = new File(getExternalFilesDir(null), DEFAULT_FILE_NAME);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(HTMLString.getBytes());
            } catch (IOException e) {
                Log.e(TAG,"Error: "+ e.getLocalizedMessage());

            } finally {
                stream.close();
            }
        } catch (IOException e) {
            Log.e(TAG,"Error: "+ e.getLocalizedMessage());

        }
        return true;
    }

    private String readHTMLFromDefaultFile(){
        String fileContents;
        File file = new File(getExternalFilesDir(null), DEFAULT_FILE_NAME);
        int length = (int) file.length();

        byte[] bytes = new byte[length];
        try{
            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
                fileContents = new String(bytes);
            } catch (IOException e){
                fileContents = DEFAULT_HTML_STRING;
                Log.e(TAG,"Error: "+ e.getLocalizedMessage());
            }finally {
                in.close();
            }
        }catch (IOException e){
            fileContents = DEFAULT_HTML_STRING;
            Log.e(TAG,"Error: "+ e.getLocalizedMessage());
        }
        Log.d(TAG,fileContents);
        if(fileContents.isEmpty()) fileContents = DEFAULT_HTML_STRING;
        return fileContents;
    }

    /***
     * Overriden from AddTagDialogFragment.OnAddTagDialogFragmentInteractionListener
     * Receives the input from the AddTagDialogFragment
     * @param tag the input tag
     */
    @Override
    public void onFinishTagEditDialog(String tag) {

        mEditorFragment = ((EditorFragment)mSectionsPagerAdapter.getRegisteredFragment(0));
        mEditorFragment.insertTextAtCursorPoint(String.format(TAG_PATTERN, tag,tag));

    }


    /**
     * The Editor Fragment which contains the EditText element that we'll use as the source editor
     */
    public static class EditorFragment extends Fragment {
        /**
         * The EditText element reference
         */
        private EditText mEditor;

        /**
         * Convenience storage of the html code
         */
        private String mSourceCode;

        public EditorFragment() {
        }

        /**
         * Returns a new instance of this fragment with the initializing the mEditor
         * with the initial Code
         * @param initialCode The code which will initialize the mEditor
         */
        public static EditorFragment newInstance(String initialCode) {
            EditorFragment fragment = new EditorFragment();
            fragment.mSourceCode = initialCode;
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mEditor = (EditText) rootView.findViewById(R.id.editText);
            setTextToEditor();
            return rootView;
        }

        /**
         * Inserts text at the current cursor point of the EditText
         * @param textToAdd the text to add
         * @return if the operation was successful or not
         */
        public boolean insertTextAtCursorPoint(String textToAdd){
            boolean success = false;
            if(mEditor != null ){
                mEditor.getText().insert(mEditor.getSelectionStart(), textToAdd);
                success = true;
            }
            return success;
        }

        public void setTextToEditor(){
            if(mEditor != null ){
                mEditor.setText(mSourceCode);
            }
        }

        //Getters

        public String getmSourceCode() {
            if(mEditor != null ) {
                mSourceCode = mEditor.getText().toString();
            }
            return mSourceCode;

        }

        //Setters

        public void setmSourceCode(String mSourceCode) {
            this.mSourceCode = mSourceCode;
            setTextToEditor();
        }
    }

    /**
     * The Preview Fragment which contains the WebView element used to test the HTML
     */
    public static class PreviewFragment extends Fragment {
        /**
         * The WebView element reference
         */
        private WebView mWebView;

        /**
         * Convenience storage of the html code
         */
        private String mSourceCode;

        public PreviewFragment() {
        }

        /**
         * Returns a new instance of this fragment with the initializing the mEditor
         * with the initial Code
         * @param initialCode The code which will initialize the mEditor
         */
        public static PreviewFragment newInstance(String initialCode) {
            PreviewFragment fragment = new PreviewFragment();
            fragment.mSourceCode = initialCode;
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_html_preview, container, false);
            mWebView = (WebView) rootView.findViewById(R.id.webView);
            return rootView;
        }
        @Override
        public void setUserVisibleHint(boolean visible)
        {
            super.setUserVisibleHint(visible);
            if (visible && isResumed())
            {
                //Manually call onResume so it loads the HTMLStringAgain
                onResume();
            }
        }

        @Override
        public void onResume()
        {
            super.onResume();
            if (!getUserVisibleHint() || mWebView == null)
            {
                return;
            }
            loadHTMLInWebView();
        }
        private void loadHTMLInWebView(){
            if(mWebView != null){
                mWebView.loadData(mSourceCode, "text/html", null);
            }
        }

        //Getters

        public String getmSourceCode() {
            return mSourceCode;
        }

        //Setters

        public void setmSourceCode(String mSourceCode) {
            this.mSourceCode = mSourceCode;
            loadHTMLInWebView();
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a EditorFragment (defined as a static inner class below).
            if(mEditorFragment == null){
                mEditorFragment = EditorFragment.newInstance(mSourceCode);
            }
            if(mPreviewFragment == null){
                mPreviewFragment = PreviewFragment.newInstance(mSourceCode);
            }
            return position == 0 ? mEditorFragment : mPreviewFragment;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "EDITOR";
                case 1:
                    return "PREVIEW";
            }
            return null;
        }
    }
}
