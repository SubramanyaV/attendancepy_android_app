package info.subramanya.camerafileupload;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PostActivity extends Activity implements AdapterView.OnItemSelectedListener {

    DatePickerDialog datePickerDialog;
    EditText cal_edit;
    String[] session = {"1", "2", "3", "4", "5", "6"};
    String session_final;
    ImageButton b1;
    Button b2;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ActionBar actionBar = getActionBar();
        dialog = new ProgressDialog(PostActivity.this);
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar)));
        }
        b1 = (ImageButton) findViewById(R.id.calb);
        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                calender_picker();
            }
        });
        b2 = (Button) findViewById(R.id.postb);
        b2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                try {
                    post_final();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        String date_n = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
        cal_edit = (EditText) findViewById(R.id.cal_edit);
        cal_edit.setText(date_n);

        Spinner spin = (Spinner) findViewById(R.id.spinner_session);
        spin.setOnItemSelectedListener(PostActivity.this);
        ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, session);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(aa);
    }

    private void calender_picker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(PostActivity.this, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                /*String day_temp = String.valueOf(day);
                String month_temp = String.valueOf(month + 1);
                if (day_temp.length() == 1)
                    day_temp = "0" + day_temp;
                if (month_temp.length() == 1)
                    month_temp = "0" + month_temp;*/
                cal_edit.setText(String.format("%s-%s-%d", month+1, day, year));
            }
        }, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    private void post_final() throws JSONException {
        dialog.setMessage("Processing");
        dialog.setTitle("Status");
        dialog.setIndeterminate(true);
        dialog.show();
        final ImageList imageList = (ImageList) getApplicationContext();
        JSONObject jsonOBJ = new JSONObject();
        String[] str = new String[imageList.getArrayList().size()];

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        for (int j = 0; j < imageList.getArrayList().size(); j++) {

            // Assign each value to String array
            str[j] = imageList.getArrayList().get(j);
        }
        JSONArray ja = new JSONArray(str);
        try {
            jsonOBJ.put("img", ja);
            jsonOBJ.put("date", cal_edit.getText());
            jsonOBJ.put("session", String.valueOf(session_final));
        } catch (JSONException e) {
            e.printStackTrace();
        }


       // final String savedata = jsonOBJ.toString();
       // showAlert(savedata);

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, Config.LIST_UPLOAD_URL, jsonOBJ, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    dialog.dismiss();
                    showAlert(response.toString());
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Server Error", Toast.LENGTH_LONG).show();

                }
                Log.i("VOLLEY:", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                Log.v("VOLLEY:", error.toString());
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(480 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void showAlert(String savedata) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(savedata).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
        ((TextView) adapterView.getChildAt(0)).setTextSize(18);
        session_final = session[i];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Toast.makeText(getApplicationContext(), "Please choose a session", Toast.LENGTH_LONG).show();
    }
}
