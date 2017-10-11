package com.ammarkureja.flickrbrowser;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

enum DownloadStatus {IDLE, PROCESSING, NOT_INITIALISED, FAILED_OR_EMPTY, OK}


/**
 * Created by ammar on 19/09/2017.
 */

class GetRawData extends AsyncTask<String, Void, String> {

    private static final String TAG = "GetRawData";
    private DownloadStatus mdownloadStatus;
    private final OnDownloadComplete mCallback;

    interface OnDownloadComplete {
        void onDownloadComplete(String data, DownloadStatus status);
    }

    public GetRawData(OnDownloadComplete callback) {
        this.mdownloadStatus = DownloadStatus.IDLE;
        mCallback = callback;
    }

    void runInSameThread(String s) {
        Log.d(TAG, "runInSameThread: starts");

     //   onPostExecute(doInBackground(s));

        if (mCallback != null) {
//            String result = doInBackground(s);
//            mCallback.onDownloadComplete(result, mdownloadStatus);
            mCallback.onDownloadComplete(doInBackground(s), mdownloadStatus);
        }

        Log.d(TAG, "runInSameThread: ends");
    }

    @Override
    protected void onPostExecute(String s) {
       // Log.d(TAG, "onPostExecute: patameter = "+s);
if (mCallback != null) {
    mCallback.onDownloadComplete(s, mdownloadStatus);
}



    }

    @Override
    protected String doInBackground(String... strings) {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        if (strings == null) {
            mdownloadStatus = DownloadStatus.NOT_INITIALISED;
            return null;
        }

        try {mdownloadStatus = DownloadStatus.PROCESSING;
            URL url = new URL(strings[0]);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response = connection.getResponseCode();
            Log.d(TAG, "doInBackground: response code was "+response);

            StringBuilder result = new StringBuilder();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;

            while (null != (line = reader.readLine())) {
                result.append(line).append("\n");
            }

            mdownloadStatus = DownloadStatus.OK;
            return result.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Invalid URL "+ e.getMessage() );
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IO EXception reading data: " + e.getMessage() );
        } catch (SecurityException e) {
            Log.e(TAG, "doInBackground: Security Exception. Needs permission" + e.getMessage() );
        }   finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: Error closing stream "+ e.getMessage() );
                }
            }
        }
        mdownloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        return null;
    }

}
