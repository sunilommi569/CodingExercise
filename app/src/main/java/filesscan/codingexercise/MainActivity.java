package filesscan.codingexercise;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import adapter.FileAdapter;
import model.FileData;
import model.Header;
import model.MetaData;
import utils.Commons;
import utils.ScanService;

public class MainActivity extends AppCompatActivity {
    private Button mBtnStart;
    private Button mBtnStop;
    private Button mBtnShare;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 100;
    private BroadcastReceiver mUpdateUI;
    private FileData mFileData;
    private RecyclerView mRecyler;
    private ArrayList<Object> mList;
    private FileAdapter mFileAdapter;
    private TextView mAvgFileSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAvgFileSize = (TextView) findViewById(R.id.avg_fieSize);
        mBtnStart = (Button) findViewById(R.id.start);
        mBtnShare = (Button) findViewById(R.id.share);
        mBtnShare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String[] TO = {""};
                String[] CC = {""};
                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                emailIntent.putExtra(Intent.EXTRA_CC, CC);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Scanning");
                emailIntent.putExtra(Intent.EXTRA_TEXT, mFileData.mTotalMbScanned + " Mb scanned");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBtnShare.setVisibility(View.GONE);
        mBtnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
                    );
                } else {
                    Intent intent = new Intent(MainActivity.this, ScanService.class);
                    startService(intent);
                }
            }
        });
        mBtnStop = (Button) findViewById(R.id.stop);
        mBtnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                stopService(new Intent(getApplicationContext(), ScanService.class));
            }
        });
/**
 * Receiver where updates can be obtained.
 */
        mUpdateUI = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mFileData = intent.getParcelableExtra("data");
                updateViews();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateUI, new IntentFilter(Commons.UPDATE));
        mRecyler = (RecyclerView) findViewById(R.id.recyler_view);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyler.setLayoutManager(layoutManager);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Commons.SAVE, mFileData);
        super.onSaveInstanceState(outState);
    }


    /**
     * Update all views with existing data.
     */
    private void updateViews() {
        stopService(new Intent(this, ScanService.class));
        if (mFileAdapter == null) {
            mFileAdapter = new FileAdapter(this, sortData());
            mRecyler.setAdapter(mFileAdapter);
        } else {
            mRecyler.getAdapter().notifyDataSetChanged();
        }
        mAvgFileSize.setText("Average file size is ----" + mFileData.mAvgFileSize + " MB");

        if (mFileData.isScanning == 0) {
            mBtnShare.setVisibility(View.VISIBLE);
        } else {
            mBtnShare.setVisibility(View.GONE);
        }


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mFileData = savedInstanceState.getParcelable(Commons.SAVE);
        //set data after restore.
        if (mFileData != null) {
            updateViews();
        }
    }

    /**
     *  Creating custom data for recycler view to handle different objects.
     * @return List of files data.
     */
    private ArrayList<Object> sortData() {

        mList = new ArrayList<>();
        Header header = new Header();
        header.setHeader("10 Biggest File Names and Sizes in MB's");
        mList.add(header);
//add file name and sizes.
        for (int i = 0; i < mFileData.mTenFileNames.length; i++) {
            MetaData data = new MetaData();
            data.setFileSize(mFileData.mTenFileSizes[i]);
            data.setName(mFileData.mTenFileNames[i]);
            mList.add(data);
        }

        //add extension and frequencies.
        Header exHeader = new Header();
        exHeader.setHeader("Extensions and frequencies");
        mList.add(exHeader);
        for (int i = 0; i < mFileData.mFrequentFileExtensions.length; i++) {
            String[] arr = mFileData.mFrequentFileExtensions[i].split(" ~!");
            MetaData data = new MetaData();
            data.setFileSize(Long.parseLong(arr[1]));
            data.setName(arr[0]);
            mList.add(data);
        }
        return mList;

    }


    @Override
    public void onBackPressed() {
        stopService(new Intent(this, ScanService.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateUI);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MainActivity.this, ScanService.class);
                    startService(intent);
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;

        }
    }
}
