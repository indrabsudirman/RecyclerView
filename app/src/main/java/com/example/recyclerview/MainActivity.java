package com.example.recyclerview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private ActivityMainBinding activityMainBinding;
    private View view;
    private RecyclerView.Adapter albumAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Album> albumArrayList;
    private DetailItemBinding detailItemBinding;
    private String downloadFileName;
    private DownloadManager downloadManager = null;
    private Uri Download_Uri;
    private long downloadId =-1L;
    private boolean isRunning = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        view = activityMainBinding.getRoot();
        setContentView(view);

        albumArrayList = new ArrayList<>();

        prepareAlbumDownloadImage();

        buildRecyclerView();

        //Initialize progress dialog
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Downloading album ...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);





    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {
        private Context context;
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

                byte data[] = new byte[4096];
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
            Toast.makeText(context, "Download start ...", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            //If we get here, length is known, no set indeterminate to false
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(progress[0]);
            detailItemBinding.imageDownload.setImageResource(R.drawable.ic_downloading);
        }

        @Override
        protected void onPostExecute(String result) {
            wakeLock.release();
            progressDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error : " + result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, "File success downloaded", Toast.LENGTH_LONG).show();
                detailItemBinding.imageDownload.setVisibility(View.GONE);

        }
    }



    private void buildRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        albumAdapter = new AlbumRecyclerViewAdapter(albumArrayList);
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

    public static class AlbumRecyclerViewAdapter extends RecyclerView.Adapter<AlbumRecyclerViewAdapter.AlbumViewHolder>{
        private final List<Album> albumList;

        //Constructor
        public AlbumRecyclerViewAdapter(List<Album> albumList) {
            this.albumList = albumList;
        }

        /**
         * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
         * an item.
         * <p>
         * This new ViewHolder should be constructed with a new View that can represent the items
         * of the given type. You can either create a new View manually or inflate it from an XML
         * layout file.
         * <p>
         * The new ViewHolder will be used to display items of the adapter using
         * {@link #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
         * different items in the data set, it is a good idea to cache references to sub views of
         * the View to avoid unnecessary {@link View#findViewById(int)} calls.
         *
         * @param parent   The ViewGroup into which the new View will be added after it is bound to
         *                 an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         * @see #getItemViewType(int)
         * @see #onBindViewHolder(ViewHolder, int)
         */
        @NonNull
        @Override
        public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AlbumViewHolder(DetailItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                    parent, false));
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
         * position.
         * <p>
         * Note that unlike {@link ListView}, RecyclerView will not call this method
         * again if the position of the item changes in the data set unless the item itself is
         * invalidated or the new position cannot be determined. For this reason, you should only
         * use the <code>position</code> parameter while acquiring the related data item inside
         * this method and should not keep a copy of it. If you need the position of an item later
         * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
         * have the updated adapter position.
         * <p>
         * Override {@link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
         * handle efficient partial bind.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the
         *                 item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
            final Album album = albumList.get(position);
            //Setting view
            //Resources can set from JSON Response
            holder.detailItemBinding.image.setImageResource(album.getImageAlbum());
            holder.detailItemBinding.imageTitle.setText(album.getTitleAlbum());
            holder.detailItemBinding.imageDesc.setText(album.getDescAlbum());
            holder.detailItemBinding.duration.setText(album.getDuration());
            holder.detailItemBinding.imageDownload.setImageResource(album.getAlbumDownload());

        }

        /**
         * Return the stable ID for the item at <code>position</code>. If {@link #hasStableIds()}
         * would return false this method should return {@link #NO_ID}. The default implementation
         * of this method returns {@link #NO_ID}.
         *
         * @param position Adapter position to query
         * @return the stable ID of the item at position
         */
        @Override
        public long getItemId(int position) {
            Album album = albumList.get(position);
            // return in real stable id from here
            return album.getAlbumId();
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return (null != albumList ? albumList.size() : 0);
        }




        public class AlbumViewHolder extends RecyclerView.ViewHolder {
            private final DetailItemBinding detailItemBinding;

            public AlbumViewHolder(DetailItemBinding detailItemBinding) {
                super(detailItemBinding.getRoot());
                this.detailItemBinding = detailItemBinding;
            }

        }

        public interface ItemClickListener {
            void onItemClick(View view, int position);
        }

    }

    private void prepareAlbumDownloadImage() {
        int [] imageDownload = new int[] {
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


            int [] imageAlbum = new int[] {
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
        albumArrayList.add(album);

        album = new Album(2, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumArrayList.add(album);

        new Album(3, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumArrayList.add(album);

        new Album(4, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumArrayList.add(album);

        new Album(5, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumArrayList.add(album);

        new Album(6, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumArrayList.add(album);

        new Album(7, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumArrayList.add(album);

        new Album(8, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumArrayList.add(album);

        new Album(9, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumArrayList.add(album);

        new Album(10, imageAlbum[0], "Some title here", getString(R.string.album_desc), getString(R.string.duration), imageDownload[0]);
        albumArrayList.add(album);
    }


}