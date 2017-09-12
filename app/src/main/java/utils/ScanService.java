package utils;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.FileData;

/**
 * Created by sunil on 9/9/17.
 */

public class ScanService extends Service implements Runnable {
    private List<Map.Entry<String, Integer>> mAlSorted;
    private ArrayList<File> fileList = new ArrayList<File>();
    private Thread mThread;
    private FileData mData = new FileData();
    private Map<String, Integer> mHmExtensions;
    private Set<Map.Entry<String, Integer>> mESet;
    private volatile boolean isScanning;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isScanning) {
            Log.d("service started--", "service started");
            startScan();
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isScanning = false;
        mData.isScanning = 0;
        Log.d("service destroyed---", "service destroyed");
    }


    @Override
    public void run() {
//perform scan  operation in background thread.
        //getting SDcard root path
        try {
            File root = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath());
            ArrayList<File> files = getfile(root);

            Collections.sort(files, new Comparator<File>() {
                @Override
                public int compare(File file, File t1) {
                    return Long.compare(file.length(), t1.length());
                }
            });
            int count = 0;
            for (int i = files.size() - 1; i >= 0; i--) {
                File file = files.get(i);

                double inKb = file.length() / 1024;
                double d = Math.round((inKb / 1024) * 100) / 100.0d;
                mData.mTenFileNames[count] = file.getName();
                mData.mTenFileSizes[count] = d;
                Log.e("name", mData.mTenFileNames[count] + "--" + mData.mTenFileSizes[count]);
                if (count >= 9) {
                    break;
                }
                count++;

            }
            isScanning = true;
//find avg file size
            findAverageFileSize(files);

            update();
        } catch (Exception e) {
            e.printStackTrace();

        }


    }

    /**
     * Send a broadcast and update the views.
     */
    private void update() {
        isScanning = false;
        mData.isScanning = 0;
        Intent intent = new Intent(Commons.UPDATE);
        intent.putExtra("data", mData);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Commons.showNotifcation("Completed  scan: " + mData.mTotalMbScanned + "Mb", this);

    }

    private void startScan() {
        mHmExtensions = new HashMap<>();
        mThread = new Thread(this);
        isScanning = true;
        mData.isScanning = 1;
        mThread.start();
        Commons.showNotifcation("Scannning files...  " + "0/" + mData.mTotalMbScanned + "Mb", this);
    }

    /**
     * Get list of files from sd card.
     *
     * @param dir
     * @return List of files.
     */
    private ArrayList<File> getfile(File dir) {
        fileList.clear();
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getfile(file));
            } else {
                inFiles.add(file);
            }
        }
        return inFiles;
    }


    private void findAverageFileSize(ArrayList<File> files) {
        double sum = 0;
        int length = files.size();
        File file;
        for (int i = 0; i < files.size(); i++) {
            file = files.get(i);

            double inKb = file.length() / 1024;
            double inMb = inKb / 1024;
            sum = sum + inMb;
            Commons.showNotifcation("Scanning progress..." + Math.round(sum * 100) / 100.0d + " Mb", this);
            findTopExtensions(file.getName());

        }
        mData.mTotalMbScanned = Math.round(sum * 100) / 100.0d;

        double avg = mData.mTotalMbScanned / length;
        mData.mAvgFileSize = Math.round(avg * 100) / 100.0d;


        mESet = mHmExtensions.entrySet();
        mAlSorted = new ArrayList<>(mESet);

        Collections.sort(mAlSorted, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> a,
                               Map.Entry<String, Integer> b) {
                return b.getValue() - a.getValue();
            }
        });

        int sort = mAlSorted.size() > 5 ? 5 : mAlSorted.size();

        //iterate and set it to frequencies array.
        for (int i = 0; i < sort; i++) {
            mData.mFrequentFileExtensions[i] = mAlSorted.get(i).getKey() + " ~!" + mAlSorted.get(i).getValue();
            Log.e("extensions", mData.mFrequentFileExtensions[i] + " freq" + mAlSorted.get(i).getValue());
        }

    }


    private void findTopExtensions(String extensions) {
        int count = 0;
        for (int i = 0; i < extensions.length() - 1; i++) {
            if (extensions.charAt(extensions.length() - (1 + i)) == '.') {
                count = i;
                break;
            }
        }
        String key = extensions.substring(extensions.length() - (count + 1), extensions.length());
        if (!mHmExtensions.containsKey(key)) {
            mHmExtensions.put(key, 1);
        } else {
            mHmExtensions.put(key, mHmExtensions.get(key) + 1);
        }

    }


}
