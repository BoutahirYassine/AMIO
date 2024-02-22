package com.example.projectamio;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class LightHttpRequestTask extends AsyncTask<String, Void, List<Data>> {

    private static final String TAG = "LightRequest";
    private MyParser parser = new MyParser();
    private final MainActivity mActivity;
    // Interface de rappel
    private IDataListener mListener;

    public LightHttpRequestTask(MainActivity activity) {
        this.mActivity = activity;
    }

    public LightHttpRequestTask() {
        this.mActivity = null;
    }

    @Override
    protected List<Data>  doInBackground(String... params) {
        String apiUrl = params[0];
        List<Data> data_list = null;

        try {
            URL url = new URL(apiUrl);
            Log.e(TAG, url.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = urlConnection.getInputStream();
                Log.d(TAG, "HTTP connection successful");
                data_list = parser.readJsonStream(in);
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error during HTTP connection: " + e.getMessage());

            e.printStackTrace();
        }
        return data_list;
    }

    protected void onPostExecute(List<Data> data_list) {
        Log.d(TAG, "Réponse du serveur : " + data_list);
        if (data_list == null) {
            if ( mActivity != null) {
                Toast.makeText(mActivity, "Une erreur est survenue lors de la récupération des données", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (mListener != null) {
                mListener.onDataReceived(data_list);
            }
        }
    }

    // Interface de rappel pour notifier la classe appelante lorsque les données sont reçues
    public interface IDataListener {
        void onDataReceived(List<Data> dataList);
    }

    public void setOnDataReceivedListener(IDataListener listener) {
        this.mListener = listener;
    }

}
