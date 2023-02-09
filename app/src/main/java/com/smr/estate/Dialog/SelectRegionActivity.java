package com.smr.estate.Dialog;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.smr.estate.Adapter.StateAdapter;
import com.smr.estate.Application.G;
import com.smr.estate.Constant.Constant;
import com.smr.estate.Model.State;
import com.smr.estate.R;
import com.smr.estate.Utils.AppSingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import wiadevelopers.com.library.Components.CustomProgressDialog;
import wiadevelopers.com.library.DivarUtils;

public class SelectRegionActivity extends AppCompatActivity
{
    TextView txtTitle;
    EditText edtSearch;
    ListView lstStates;
    StateAdapter statesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_state);
        initialize();

        int height = DivarUtils.heightPX;
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height - (height / 3));
        getWindow().setGravity(Gravity.BOTTOM);
    }

    private void initialize()
    {
        findViews();
        setupActivity();
    }

    private void findViews()
    {
        txtTitle = findViewById(R.id.txtToolbarTitle);
        edtSearch = findViewById(R.id.edtSearch);
        lstStates = findViewById(R.id.lstStates);
    }

    private void setupActivity()
    {
        setStateData();
        setListeners();
    }

    private void setStateData()
    {
        final ArrayList<State> states = new ArrayList<>();

        final CustomProgressDialog progressDialog = new CustomProgressDialog(SelectRegionActivity.this);
        Typeface typeFace = ResourcesCompat.getFont(this, R.font.font_family);
        progressDialog.setTypeface(typeFace);
        progressDialog.setMessage("در حال ارسال...");
        progressDialog.setIndicatorColor(R.color.mainColor);
        progressDialog.setTextColor(R.color.textColor);
        progressDialog.setTextSize(15);
        progressDialog.show();

        HashMap hashMap = new HashMap();
        String stateId = getIntent().getStringExtra("cityId");
        hashMap.put("city", stateId);
        final JSONObject jsonData = new JSONObject(hashMap);
        final JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonData);

        final String url = Constant.BASE_URL + "getregion";

        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {
                try
                {
                    for (int i = 0; i < response.length(); i++)
                        states.add(new State(response.getJSONObject(i).getString("title"), response.getJSONObject(i).getString("_id")));
                    statesAdapter = new StateAdapter(SelectRegionActivity.this, states);
                    lstStates.setAdapter(statesAdapter);
                    progressDialog.cancel();
                } catch (Exception e)
                {
                    Log.i(G.TAG, e.getMessage());
                }

            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError e)
            {
                Log.i(G.TAG, String.valueOf(e));
                AppSingleton.getInstance(getApplicationContext()).cancelPendingRequests();
            }
        };

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, url, jsonArray, listener, errorListener)
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("User-agent", System.getProperty("http.agent"));
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 1, 1.0f));
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        overridePendingTransition(0, R.anim.anim_slide_out_bottom);
    }

    private void setListeners()
    {
        lstStates.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                String state = statesAdapter.getItem(i).getTitle();
                String stateId = statesAdapter.getItem(i).getId();
                Intent intent = new Intent();
                intent.putExtra("region", state);
                intent.putExtra("regionId", stateId);
                setResult(Constant.RESULT_OK, intent);
                finish();
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                statesAdapter.getFilter().filter(edtSearch.getText().toString());
            }
        });
    }
}
