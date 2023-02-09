package com.smr.estate.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.Volley;
import com.smr.estate.Application.G;
import com.smr.estate.Constant.Constant;
import com.smr.estate.Dialog.MapsActivity;
import com.smr.estate.Dialog.SelectCityActivity;
import com.smr.estate.Dialog.SelectRegionActivity;
import com.smr.estate.Dialog.SelectStateActivity;
import com.smr.estate.Interface.SnackBar;
import com.smr.estate.Internet.CheckInternet;
import com.smr.estate.R;
import com.smr.estate.Storage.Read;
import com.smr.estate.Storage.Write;
import com.smr.estate.Utils.AppSingleton;
import com.smr.estate.Utils.Picture;
import com.smr.estate.Utils.RuntimePermissionsActivity;
import com.smr.estate.Utils.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import wiadevelopers.com.library.Components.CustomProgressDialog;

public class RegisterActivity extends RuntimePermissionsActivity
{
    private RelativeLayout lnrProfile;
    private LinearLayout lnrLocation, toggle;
    private TextView tvSelectState, tvSelectCity, tvSelectRegion, tvLoc;
    private EditText edtTitle, edtName, edtEmail, edtTell, edtAddress, edtCode, edtPass;
    private Button btnRegister;
    private CircleImageView imgProfile;
    private ImageView miniCamera;
    private CoordinatorLayout coordinatorLayoutRegister;
    private Bitmap bitmapProfile;
    private TextView estate, consultant;
    private int role = 1;
    private boolean isStateSelected = false, isCitySelected = false, isRegionSelected = false, isProfileImageSelected = false, isLocationSelect = false;
    private String filePath, mobileNumber, strTitle, strName, strEmail, strTell, strAddress, strState, strStateId, strCity, strCityId, strRegion, strRegionId, strCode, strPass, strLat = "NO_LOCATION", strLon = "NO_LOCATION";
    private static final int REQUEST_PERMISSIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findView();
        styleView();
        toggleSelectedRole();

        if (role == 1)
            setupListeners();

        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (role == 1)
                    IfEstateDataInserted();
                if (role == 2)
                    IfConsultDataInserted();
            }
        });
    }

    private void setupListeners()
    {
        lnrLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                RegisterActivity.super.requestAppPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constant.REQUEST_PERMISSIONS_LOCATION);
            }
        });

        tvSelectState.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivityForResult(new Intent(RegisterActivity.this, SelectStateActivity.class), Constant.REQUEST_SELECT_STATE);
                overridePendingTransition(R.anim.anim_slide_in_bottom, 0);
            }
        });

        tvSelectCity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(RegisterActivity.this, SelectCityActivity.class);
                intent.putExtra("stateId", strStateId);
                startActivityForResult(intent, Constant.REQUEST_SELECT_CITY);
                overridePendingTransition(R.anim.anim_slide_in_bottom, 0);
            }
        });

        tvSelectRegion.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(RegisterActivity.this, SelectRegionActivity.class);
                intent.putExtra("cityId", strCityId);
                startActivityForResult(intent, Constant.REQUEST_SELECT_REGION);
                overridePendingTransition(R.anim.anim_slide_in_bottom, 0);
            }
        });

        lnrProfile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
                {
                    if ((ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)))
                    {
                    } else
                    {
                        ActivityCompat.requestPermissions(RegisterActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_PERMISSIONS);
                    }
                } else
                {
                    showFileChooser();
                }
            }
        });
    }

    private void showFileChooser()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "انتخاب عکس"), Constant.REQUEST_SELECT_PROFILE_IMAGE);
    }

    private void findView()
    {
        lnrProfile = findViewById(R.id.rtlProfile);
        imgProfile = findViewById(R.id.imgRegisterProfile);
        miniCamera = findViewById(R.id.ivMiniCamera);
        toggle = findViewById(R.id.toggle);
        estate = findViewById(R.id.estate);
        consultant = findViewById(R.id.consultant);
        edtTitle = findViewById(R.id.edtRegisterTitle);
        edtName = findViewById(R.id.edtRegisterName);
        edtEmail = findViewById(R.id.edtRegisterEmail);
        edtTell = findViewById(R.id.edtRegisterTell);
        tvSelectState = findViewById(R.id.tvSelectState);
        tvSelectCity = findViewById(R.id.tvSelectCity);
        tvSelectRegion = findViewById(R.id.tvSelectRegion);
        edtAddress = findViewById(R.id.edtRegisterAddress);
        edtPass = findViewById(R.id.edtRegisterPass);
        edtCode = findViewById(R.id.edtRegisterCode);
        lnrLocation = findViewById(R.id.lnrLocation);
        tvLoc = findViewById(R.id.tvRegisterLoc);
        btnRegister = findViewById(R.id.btnRegister);
        coordinatorLayoutRegister = findViewById(R.id.coordinatorRegister);
    }

    private void toggleSelectedRole()
    {
        consultant.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                ConsultantVisibilitiesAndInfo();
            }
        });

        estate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                EstateVisibilitiesAndAction();
            }
        });
    }

    private void EstateVisibilitiesAndAction()
    {
        role = 1;
        estate.setBackgroundColor(getResources().getColor(R.color.toggleCheck));
        consultant.setBackgroundColor(getResources().getColor(R.color.toggleUnCheck));
        imgProfile.setVisibility(View.VISIBLE);
        miniCamera.setVisibility(View.VISIBLE);
        edtTitle.setVisibility(View.VISIBLE);
        edtName.setHint("نام مدیر املاک *");
        edtTell.setVisibility(View.VISIBLE);
        edtAddress.setVisibility(View.VISIBLE);
        edtCode.setVisibility(View.VISIBLE);
        lnrLocation.setVisibility(View.VISIBLE);
    }

    private void styleView()
    {
        edtTitle.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String value = edtTitle.getText().toString();

                if (!value.equals(""))
                {
                    edtTitle.setBackgroundResource(R.drawable.edit_text_full_background);
                    edtTitle.setTextColor(getResources().getColor(R.color.darkGray));
                } else
                {
                    edtTitle.setBackgroundResource(R.drawable.edit_text_background);
                    edtTitle.setTextColor(getResources().getColor(R.color.textColor));
                }
            }
        });

        edtName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String value = edtName.getText().toString();

                if (!value.equals(""))
                {
                    edtName.setBackgroundResource(R.drawable.edit_text_full_background);
                    edtName.setTextColor(getResources().getColor(R.color.darkGray));
                } else
                {
                    edtName.setBackgroundResource(R.drawable.edit_text_background);
                    edtName.setTextColor(getResources().getColor(R.color.textColor));
                }
            }
        });

        edtEmail.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String value = edtEmail.getText().toString();

                if (!value.equals(""))
                {
                    edtEmail.setBackgroundResource(R.drawable.edit_text_full_background);
                    edtEmail.setTextColor(getResources().getColor(R.color.darkGray));
                } else
                {
                    edtEmail.setBackgroundResource(R.drawable.edit_text_background);
                    edtEmail.setTextColor(getResources().getColor(R.color.textColor));
                }
            }
        });

        edtTell.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String value = edtTell.getText().toString();

                if (!value.equals(""))
                {
                    edtTell.setBackgroundResource(R.drawable.edit_text_full_background);
                    edtTell.setTextColor(getResources().getColor(R.color.darkGray));
                } else
                {
                    edtTell.setBackgroundResource(R.drawable.edit_text_background);
                    edtTell.setTextColor(getResources().getColor(R.color.textColor));
                }
            }
        });

        edtAddress.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String value = edtAddress.getText().toString();

                if (!value.equals(""))
                {
                    edtAddress.setBackgroundResource(R.drawable.edit_text_full_background);
                    edtAddress.setTextColor(getResources().getColor(R.color.darkGray));
                } else
                {
                    edtAddress.setBackgroundResource(R.drawable.edit_text_background);
                    edtAddress.setTextColor(getResources().getColor(R.color.textColor));
                }
            }
        });

        edtPass.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String value = edtPass.getText().toString();

                if (!value.equals(""))
                {
                    edtPass.setBackgroundResource(R.drawable.edit_text_full_background);
                    edtPass.setTextColor(getResources().getColor(R.color.darkGray));
                } else
                {
                    edtPass.setBackgroundResource(R.drawable.edit_text_background);
                    edtPass.setTextColor(getResources().getColor(R.color.textColor));
                }
            }
        });

        edtCode.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String value = edtCode.getText().toString();

                if (!value.equals(""))
                {
                    edtCode.setBackgroundResource(R.drawable.edit_text_full_background);
                    edtCode.setTextColor(getResources().getColor(R.color.darkGray));
                } else
                {
                    edtCode.setBackgroundResource(R.drawable.edit_text_background);
                    edtCode.setTextColor(getResources().getColor(R.color.textColor));
                }
            }
        });
    }

    private void ConsultantVisibilitiesAndInfo()
    {
        role = 2;
        estate.setBackgroundColor(getResources().getColor(R.color.toggleUnCheck));
        consultant.setBackgroundColor(getResources().getColor(R.color.toggleCheck));
        imgProfile.setVisibility(View.GONE);
        miniCamera.setVisibility(View.GONE);
        edtTitle.setVisibility(View.GONE);
        edtName.setHint("نام مشاور *");
        edtTell.setVisibility(View.GONE);
        edtAddress.setVisibility(View.GONE);
        edtCode.setVisibility(View.GONE);
        lnrLocation.setVisibility(View.GONE);
    }

    @Override
    public void onPermissionsGranted(int requestCode)
    {
        if (requestCode == Constant.REQUEST_PERMISSIONS_READ_EXTERNAL_STORAGE_FOR_PROFILE)
            Picture.selectSingleImage(this, Constant.REQUEST_SELECT_PROFILE_IMAGE);
        else if (requestCode == Constant.REQUEST_PERMISSIONS_LOCATION)
            startActivityForResult(new Intent(RegisterActivity.this, MapsActivity.class), Constant.REQUEST_SELECT_LOCATION);
    }

    @Override
    public void onPermissionsDeny(int requestCode)
    {
        Toast.makeText(this, "جواز داده نشد", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.REQUEST_SELECT_STATE && resultCode == Constant.RESULT_OK)
        {
            String state = data.getStringExtra("state");
            strStateId = data.getStringExtra("stateId");
            isStateSelected = true;
            tvSelectState.setBackgroundResource(R.drawable.edit_text_full_background);
            tvSelectState.setTextColor(getResources().getColor(R.color.darkGray));
            strState = state;
            tvSelectState.setText(state);
            isCitySelected = false;
            tvSelectCity.setText("شهر *");
            isRegionSelected = false;
            tvSelectRegion.setText("ناحیه *");
            tvSelectCity.setVisibility(View.VISIBLE);
        }

        if (requestCode == Constant.REQUEST_SELECT_CITY && resultCode == Constant.RESULT_OK)
        {
            String city = data.getStringExtra("city");
            strCityId = data.getStringExtra("cityId");
            isCitySelected = true;
            tvSelectCity.setBackgroundResource(R.drawable.edit_text_full_background);
            tvSelectCity.setTextColor(getResources().getColor(R.color.darkGray));
            strCity = city;
            tvSelectCity.setText(city);
            isRegionSelected = false;
            tvSelectRegion.setText("ناحیه *");
            tvSelectRegion.setVisibility(View.VISIBLE);
        }

        if (requestCode == Constant.REQUEST_SELECT_REGION && resultCode == Constant.RESULT_OK)
        {
            String region = data.getStringExtra("region");
            strRegionId = data.getStringExtra("regionId");
            isRegionSelected = true;
            tvSelectRegion.setBackgroundResource(R.drawable.edit_text_full_background);
            tvSelectRegion.setTextColor(getResources().getColor(R.color.darkGray));
            strRegion = region;
            tvSelectRegion.setText(region);
        }

        if (requestCode == Constant.REQUEST_SELECT_LOCATION && resultCode == Constant.RESULT_OK)
        {
            strLat = data.getStringExtra("Lat");
            strLon = data.getStringExtra("Lon");
            isLocationSelect = true;
            tvLoc.setText("لوکیشن ست شد");
        }

        try
        {
            if (requestCode == Constant.REQUEST_SELECT_PROFILE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null)
            {
                Uri picUri = data.getData();
                filePath = getPath(picUri);
                if (filePath != null)
                {
                    try
                    {
                        bitmapProfile = MediaStore.Images.Media.getBitmap(getContentResolver(), picUri);
                        imgProfile.setImageBitmap(bitmapProfile);
                        isProfileImageSelected = true;
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                } else
                {
                    Toast.makeText(
                            RegisterActivity.this, "no image selected",
                            Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e)
        {
            Log.i(G.TAG, e.getMessage());
        }
    }

    private void IfConsultDataInserted()
    {
        mobileNumber = Read.readDataFromStorage("mobile", "NO_MOBILE", getApplicationContext());
        strName = edtName.getText().toString();
        strEmail = edtEmail.getText().toString().trim();
        strPass = edtPass.getText().toString();

        if (CheckInternet.Checked(getApplicationContext()))
        {
            if (!strName.equals(""))
            {
                if (isStateSelected)
                {
                    if (isCitySelected)
                    {
                        if (isRegionSelected)
                        {
                            if (!strPass.equals(""))
                            {
                                registerConsultant();
                            } else
                                SnackBar.Create(coordinatorLayoutRegister, "وارد کردن رمز عبور برای ورود به وبسایت الزامی می باشد", 3);
                        } else
                            SnackBar.Create(coordinatorLayoutRegister, "لطفا ناحیه مورد نظر خود را انتخاب نمایید", 3);
                    } else
                        SnackBar.Create(coordinatorLayoutRegister, "لطفا شهر مورد نظر خود را انتخاب نمایید", 3);
                } else
                    SnackBar.Create(coordinatorLayoutRegister, "لطفا استان مورد نظر خود را انتخاب نمایید", 3);
            } else
                SnackBar.Create(coordinatorLayoutRegister, "نام و نام خانوادگی خود را وارد نمایید", 3);
        } else
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    SnackBar.Create(coordinatorLayoutRegister, "اتصال به اینترنت قطع می باشد", 3);
                }
            });
        }
    }

    private void IfEstateDataInserted()
    {
        mobileNumber = Read.readDataFromStorage("mobile", "NO_MOBILE", getApplicationContext());
        strTitle = edtTitle.getText().toString();
        strName = edtName.getText().toString();
        strEmail = edtEmail.getText().toString().trim();
        strTell = edtTell.getText().toString().trim();
        strAddress = edtAddress.getText().toString();
        strPass = edtPass.getText().toString();
        strCode = edtCode.getText().toString();

        if (CheckInternet.Checked(getApplicationContext()))
        {
            if (!strTitle.equals(""))
            {
                if (!strName.equals(""))
                {
                    if (isStateSelected)
                    {
                        if (isCitySelected)
                        {
                            if (isRegionSelected)
                            {
                                if (!strAddress.equals(""))
                                {
                                    if (!strPass.equals(""))
                                    {
                                        if (isLocationSelect)
                                            registerEstate();
                                        else
                                            SnackBar.Create(coordinatorLayoutRegister, "لوکیشن خود را وارد نمایید", 3);
                                    } else
                                        SnackBar.Create(coordinatorLayoutRegister, "وارد کردن رمز عبور برای ورود به وبسایت الزامی می باشد", 3);
                                } else
                                    SnackBar.Create(coordinatorLayoutRegister, "آدرس دفتر املاک را وارد نمایید", 3);
                            } else
                                SnackBar.Create(coordinatorLayoutRegister, "لطفا ناحیه مورد نظر خود را انتخاب نمایید", 3);
                        } else
                            SnackBar.Create(coordinatorLayoutRegister, "لطفا شهر مورد نظر خود را انتخاب نمایید", 3);
                    } else
                        SnackBar.Create(coordinatorLayoutRegister, "لطفا استان مورد نظر خود را انتخاب نمایید", 3);
                } else
                    SnackBar.Create(coordinatorLayoutRegister, "نام و نام خانوادگی خود را وارد نمایید", 3);
            } else
                SnackBar.Create(coordinatorLayoutRegister, "عنوان دفتر املاک را وارد نمایید", 3);
        } else
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    SnackBar.Create(coordinatorLayoutRegister, "اتصال به اینترنت قطع می باشد", 3);
                }
            });
        }
    }

    public String getPath(Uri uri)
    {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();
        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();
        return path;
    }

    private void registerEstate()
    {
        final CustomProgressDialog progressDialog = new CustomProgressDialog(RegisterActivity.this);
        Typeface typeFace = ResourcesCompat.getFont(this, R.font.font_family);
        progressDialog.setTypeface(typeFace);
        progressDialog.setMessage("در حال ارسال...");
        progressDialog.setIndicatorColor(R.color.mainColor);
        progressDialog.setTextColor(R.color.textColor);
        progressDialog.setTextSize(15);
        progressDialog.show();

        // register agent URL
        final String url = Constant.BASE_URL + "register/agent";

        Response.Listener<NetworkResponse> listener = new Response.Listener<NetworkResponse>()
        {
            @Override
            public void onResponse(NetworkResponse response)
            {
                try
                {
                    JSONObject obj = new JSONObject(new String(response.data));
                    if (obj.getString("success").equals("true"))
                    {
                        Write.writeDataInStorage(Constant.USER_ID, obj.getString("user_id"), getApplicationContext());
                        Write.writeDataInStorage(Constant.ROLE, role, getApplicationContext());
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        RegisterActivity.this.finish();
                    } else
                        SnackBar.Create(coordinatorLayoutRegister, "عملیات ثبت نام ناموفق بود", 3);
                } catch (JSONException e)
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
                //1
                Log.i(G.TAG, String.valueOf(e));
                AppSingleton.getInstance(getApplicationContext()).cancelPendingRequests();
            }
        };

        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, url, listener, errorListener)
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.MOBILE, mobileNumber);
                params.put(Constant.TITLE, strTitle);
                params.put(Constant.NAME, strName);
                params.put(Constant.EMAIL, strEmail);
                params.put(Constant.TELL, strTell);
                params.put(Constant.STATE, strStateId);
                params.put(Constant.CITY, strCityId);
                params.put(Constant.REGION, strRegionId);
                params.put(Constant.ADDRESS, strAddress);
                params.put(Constant.PASS, strPass);
                params.put(Constant.REF, strCode);

                if (isLocationSelect)
                {
                    params.put(Constant.GEO_LAT, strLat);
                    params.put(Constant.GEO_LON, strLon);
                }

                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData()
            {
                Map<String, DataPart> params = new HashMap<>();
                if (isProfileImageSelected)
                {
                    long imageName = System.currentTimeMillis();
                    params.put("image", new DataPart(imageName + ".png", Picture.getFileDataFromDrawable(bitmapProfile)));
                }
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 1, 1.0f));
        Volley.newRequestQueue(this).add(request);
    }

    private void registerConsultant()
    {
        HashMap hashMap = new HashMap();
        hashMap.put(Constant.MOBILE, mobileNumber);
        hashMap.put(Constant.NAME, strName);
        hashMap.put(Constant.EMAIL, strEmail);
        hashMap.put(Constant.STATE, strStateId);
        hashMap.put(Constant.CITY, strCityId);
        hashMap.put(Constant.REGION, strRegionId);
        hashMap.put(Constant.PASS, strPass);

        final JSONObject jsonData = new JSONObject(hashMap);

        final CustomProgressDialog progressDialog = new CustomProgressDialog(RegisterActivity.this);
        Typeface typeFace = ResourcesCompat.getFont(this, R.font.font_family);
        progressDialog.setTypeface(typeFace);
        progressDialog.setMessage("در حال ارسال...");
        progressDialog.setIndicatorColor(R.color.mainColor);
        progressDialog.setTextColor(R.color.textColor);
        progressDialog.setTextSize(15);
        progressDialog.show();

        //register consultant URL
        final String url = Constant.BASE_URL + "register/consultant";

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    if (response.getString("success").equals("true"))
                    {
                        Write.writeDataInStorage(Constant.USER_ID, response.getString("user_id"), getApplicationContext());
                        Write.writeDataInStorage(Constant.ROLE, role, getApplicationContext());
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        RegisterActivity.this.finish();
                    } else
                        SnackBar.Create(coordinatorLayoutRegister, "عملیات ثبت نام ناموفق بود", 3);
                } catch (JSONException e)
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
                //1
                Log.i(G.TAG, String.valueOf(e));
                AppSingleton.getInstance(getApplicationContext()).cancelPendingRequests();
            }
        };

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonData, listener, errorListener)
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
}
