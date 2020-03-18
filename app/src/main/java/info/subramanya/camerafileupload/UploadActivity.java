package info.subramanya.camerafileupload;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Matrix;

import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.exifinterface.media.ExifInterface;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import android.widget.VideoView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import info.subramanya.camerafileupload.AndroidMultiPartEntity.ProgressListener;

public class UploadActivity extends Activity {
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();
    long totalSize = 0;
    private ProgressBar progressBar;
    private String filePath = null;
    private TextView txtPercentage;
    private ImageView imgPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        Button btnUpload = (Button) findViewById(R.id.btnUpload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);


        Intent i = getIntent();

        // image or video path that is captured in previous activity
        filePath = i.getStringExtra("filePath");


        if (filePath != null) {
            // Displaying the image on the screen
            previewMedia();
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry, file path is missing!", Toast.LENGTH_LONG).show();
        }


        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // uploading the file to server
                new UploadFileToServer().execute();
            }
        });

    }

    private void previewMedia() {

            imgPreview.setVisibility(View.VISIBLE);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
            Bitmap bOutput = rotate_image(bitmap, filePath);
            imgPreview.setImageBitmap(bOutput);

    }

    private Bitmap rotate_image(Bitmap bitmap, String filePath) {
        Bitmap bOutput;
        Matrix matrix = new Matrix();
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = 0;
        if (ei != null) {
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
        }

        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                bOutput = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                bOutput = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                bOutput = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                bOutput = bitmap;
        }

        return bOutput;
    }

    /**
     * Method to show alert dialog
     */
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Uploading the file to server
     */
    @SuppressLint("StaticFieldLeak")
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero

            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(progress[0] + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        private String uploadFile() {

            String responseString;

            HttpClient httpclient = new DefaultHttpClient();
            HttpParams httpParams = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 240 * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 240 * 1000);
            HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);
            HttpParams httpParams1 = httppost.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams1, 240 * 1000);
            HttpConnectionParams.setSoTimeout(httpParams1, 240 * 1000);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                Bitmap bitmap_temp = BitmapFactory.decodeFile(filePath);
                Bitmap bitmap_compressed = rotate_image(bitmap_temp,filePath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap_compressed.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] byteArray = stream.toByteArray();
                Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                File file = new File(filePath);
                OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
                os.close();

                // Adding file data to http body
                entity.addPart("images", new FileBody(file));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);

            // showing the server response in an alert dialog
            showAlert(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                final ImageList imageList = (ImageList) getApplicationContext();
                imageList.setArrayList(jsonObject.getString("filename"));
                Toast.makeText(UploadActivity.this, imageList.getArrayList().toString(), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }

    }
	}