package com.example.peter.berryestimator;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.view.PagerTabStrip;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

public class MainTabActivity extends ActionBarActivity implements
        MainFragment.OnMainFragmentInteractionListener,
        RepositoryFragment.OnRepositoryFragmentInteractionListener,
        RecordInfoDialogFragment.OnRecordInfoFragmentInteractionListener{

    private static final int CREATE_IMAGE_RECORD_ACTIVITY_REQUEST_CODE = 1;

    private static final int INDEX_MAIN_FRAGMENT_TAB = 0;
    private static final int INDEX_REPOSITORY_FRAGMENT_TAB = 1;
    private static final String TAG_RECORD_INFO_DIALOG_FRAGMENT = "record_info_dialog_fragment";
    public static final String TAG_VIEW_IMAGE_DIALOG_FRAGMENT = "view_image_dialog_fragment";
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

    private Switch filterSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);

        Log.d("-----", "main tab activity on create");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // set expand before set view pager to make it work
        tabs.setShouldExpand(true);
        tabs.setViewPager(mViewPager);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        tabs.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                supportInvalidateOptionsMenu();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_tab, menu);
        MenuItem actionSort = menu.findItem(R.id.action_sort);

        ImageButton sortButton = (ImageButton) actionSort.getActionView().findViewById(R.id.sort_button);
        sortButton.setVisibility(mViewPager.getCurrentItem() == INDEX_REPOSITORY_FRAGMENT_TAB ? View.VISIBLE : View.GONE);

        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RepositoryFragment repositoryFragment = (RepositoryFragment)
                        (mSectionsPagerAdapter.getRegisteredFragment(INDEX_REPOSITORY_FRAGMENT_TAB));

                if (repositoryFragment == null) {
                    Log.e("-----", "Fragment is empty");
                    showTempInfo("repository fragment is empty");
                } else {
                    repositoryFragment.showSortingDialog();
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDisplayImage(ImageRecord imageRecord) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(TAG_VIEW_IMAGE_DIALOG_FRAGMENT);
        if (prev != null) {
            ft.remove(prev);
            ft.commit();
        }

        ViewImageDialogFragment viewImageDialogFragment = ViewImageDialogFragment.newInstance(imageRecord);
        viewImageDialogFragment.show(getSupportFragmentManager(), TAG_VIEW_IMAGE_DIALOG_FRAGMENT);
    }

    @Override
    public void onDisplayImageRecord(ImageRecord imageRecord) {
        // DialogFragment.show() will take care of adding the fragment in a transaction.
        // Remove any currently showing dialog
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(TAG_RECORD_INFO_DIALOG_FRAGMENT);
        if (prev != null) {
            ft.remove(prev);
            ft.commit();
        }

        RecordInfoDialogFragment recordInfoDialogFragment = RecordInfoDialogFragment.newInstance(imageRecord);
        recordInfoDialogFragment.show(getSupportFragmentManager(), TAG_RECORD_INFO_DIALOG_FRAGMENT);
    }

    @Override
    public void onEditImageRecord(ImageRecord imageRecord, int action) {
        Intent intent = new Intent(this, CreateImageRecordActivity.class);
        intent.putExtra(CreateImageRecordActivity.IMAGE_RECORD, imageRecord);
        intent.putExtra(CreateImageRecordActivity.IMAGE_RECORD_ACTION, action);

        startActivityForResult(intent, CREATE_IMAGE_RECORD_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_IMAGE_RECORD_ACTIVITY_REQUEST_CODE){
            if (resultCode == RESULT_OK) {

                final ImageRecord imageRecord = data.getParcelableExtra(CreateImageRecordActivity.IMAGE_RECORD);
                final int action = data.getIntExtra(CreateImageRecordActivity.IMAGE_RECORD_ACTION,
                        CreateImageRecordActivity.IMAGE_RECORD_ACTION_NOT_FOUND);

                // TODO: this is a temporary way, use another method to avoid the delay
                // the repoFragment in pager is retrieve slower
                // error happens when it instantly need get repoFragment after rotation happens
                // wait for the repository fragment being instantiated
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // try to get repositoryFragment after 30 ms
                        updateDataInFragments(imageRecord, action);
                    }
                }, 30);
            }
        }
    }

    private void updateDataInFragments(ImageRecord imageRecord, int action) {
        mViewPager.setCurrentItem(1);
        RepositoryFragment repositoryFragment = (RepositoryFragment)
                (mSectionsPagerAdapter.getRegisteredFragment(INDEX_REPOSITORY_FRAGMENT_TAB));

        if (repositoryFragment == null) {
            Log.e("-----", "Fragment is empty");
            showTempInfo("repository fragment is empty");
            return;
        }
        RecordInfoDialogFragment recordInfoDialogFragment = (RecordInfoDialogFragment)
                getSupportFragmentManager().findFragmentByTag(TAG_RECORD_INFO_DIALOG_FRAGMENT);

        switch (action){
            // add record to the top of the list and scroll to top
            case CreateImageRecordActivity.IMAGE_RECORD_CREATE:
                repositoryFragment.addImageRecord(imageRecord);
                break;

            case CreateImageRecordActivity.IMAGE_RECORD_EDIT:
                repositoryFragment.updateImageRecord(imageRecord);
                recordInfoDialogFragment.updateRecordInfoDialog(imageRecord);
                break;

            case CreateImageRecordActivity.IMAGE_RECORD_REMOVE:
                repositoryFragment.removeImageRecord(imageRecord);
                recordInfoDialogFragment.dismiss();
                break;

            default:
                Log.e("image record", "action not found");
                showTempInfo("unknown action happened");
        }
    }

    private void showTempInfo(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page
            // only when it is the first time.
            if (position == 0){
                return MainFragment.newInstance();
            } else {
                return RepositoryFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
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
    }
}
