package com.example.recyclerview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclerview.databinding.ActivityMainBinding;
import com.example.recyclerview.databinding.DetailItemBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements AlbumAdapter.ItemClickListener {

    private static final String TAG = MainActivity.class.getName();

    private ActivityMainBinding activityMainBinding;
    private View view;
    private RecyclerView.Adapter albumAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Album> albumList;
    private DetailItemBinding detailItemBinding;
    private ProgressDialog progressDialog;
    private int itemPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        view = activityMainBinding.getRoot();
        setContentView(view);

        albumList = new ArrayList<>();

        prepareAlbumDownloadImage();

        buildRecyclerView();

        //Initialize progress dialog
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Downloading album ...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);


    }

    private void buildRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        albumAdapter = new AlbumAdapter(albumList, this);
        activityMainBinding.recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));

        activityMainBinding.recyclerView.setLayoutManager(layoutManager);
        activityMainBinding.recyclerView.setAdapter(albumAdapter);
    }

    private void checkPermissionToSavePdf() {
        Dexter.withActivity(MainActivity.this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startDownload();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void startDownload() {
        //Execute this when the downloader must be fired
        final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
        downloadTask.execute("https://commonsware.com/misc/test.mp4");


        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //Here
                downloadTask.cancel(true); //cancel the task
            }
        });

    }

    private void prepareAlbumDownloadImage() {
        int[] imageDownload = new int[]{
                R.drawable.ic_download,
                R.drawable.ic_download,
                R.drawable.ic_download,
                R.drawable.ic_download,
                R.drawable.ic_download,
                R.drawable.ic_download,
                R.drawable.ic_download,
                R.drawable.ic_download,
                R.drawable.ic_download,
                R.drawable.ic_download
        };


        int[] imageAlbum = new int[]{
                R.drawable.img,
                R.drawable.img,
                R.drawable.img,
                R.drawable.img,
                R.drawable.img,
                R.drawable.img,
                R.drawable.img,
                R.drawable.img,
                R.drawable.img,
                R.drawable.img,
        };

        Album album = new Album(1, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumList.add(album);

        album = new Album(2, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumList.add(album);

        new Album(3, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumList.add(album);

        new Album(4, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumList.add(album);

        new Album(5, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumList.add(album);

        new Album(6, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumList.add(album);

        new Album(7, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumList.add(album);

        new Album(8, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumList.add(album);

        new Album(9, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumList.add(album);

        new Album(10, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumList.add(album);
    }

    @Override
    public void onItemClick(int position) {
        this.itemPosition = position;
        int pos = position + 1;
        Log.d(TAG, "Image download clicked " + pos);
        checkPermissionToSavePdf();

    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {
        private final Context context;
        private PowerManager.WakeLock wakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.connect();

                //Expect HTTP 200 OK, so we don't mistakenly save error report
                //instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                //This will be useful to display download percentage
                //might be -1 : server did not report length
                int fileLength = connection.getContentLength();

                //download the file
                inputStream = connection.getInputStream();
                File directory = new File(Environment.getExternalStorageDirectory().toString() + '/' + getString(R.string.app_name));
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                @SuppressLint("SimpleDateFormat")
                String fileName = "rekap_murojaah_" + new SimpleDateFormat("yyyyMMddHHmmss'.mp4'").format(new Date());
                File file = new File(directory, fileName);
                outputStream = new FileOutputStream(file);

                byte[] data = new byte[4096];
                long total = 0;
                int count;

                while ((count = inputStream.read(data)) != -1) {
                    //allow canceling with back button
                    if (isCancelled()) {
                        inputStream.close();
                        return null;
                    }
                    total += count;
                    //publishing the progress ...
                    if (fileLength > 0) //Only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    outputStream.write(data, 0, count);

                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (outputStream != null)
                        outputStream.close();
                    if (inputStream != null)
                        inputStream.close();
                } catch (IOException ignored) {

                }
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Take CPu lock to prevent CPU from going off if the user
            //presses the power button during download
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            wakeLock.acquire();
            progressDialog.show();
            Toast.makeText(context, "Download start ...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            //If we get here, length is known, no set indeterminate to false
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(progress[0]);

            // Access item view based on RecyclerView position
            RecyclerView.ViewHolder viewHolder = activityMainBinding.recyclerView.findViewHolderForAdapterPosition(itemPosition);
            assert viewHolder != null;
            View view = viewHolder.itemView;
            AppCompatImageView etDesc = view.findViewById(R.id.image_download);
            etDesc.setImageResource(R.drawable.ic_downloading);

        }

        @Override
        protected void onPostExecute(String result) {
            wakeLock.release();
            progressDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error : " + result, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, "File success downloaded", Toast.LENGTH_SHORT).show();
                RecyclerView.ViewHolder viewHolder = activityMainBinding.recyclerView.findViewHolderForAdapterPosition(itemPosition);
                assert viewHolder != null;
                View view = viewHolder.itemView;
                AppCompatImageView etDesc = view.findViewById(R.id.image_download);
                etDesc.setVisibility(View.INVISIBLE);

        }
    }


}