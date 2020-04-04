package com.mpark.mpark.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.mpark.mpark.R;
import com.mpark.mpark.activities.StartParkingActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import static android.content.ContentValues.TAG;
import static com.basgeekball.awesomevalidation.ValidationStyle.COLORATION;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegisterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    AwesomeValidation mAwesomeValidation = new AwesomeValidation(COLORATION);

    EditText phone;
    EditText email, password,name;
    FirebaseAuth mAuth;
    SpinKitView spinKit;


    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFragment.
     */

    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view =  inflater.inflate(R.layout.activity_register, container, false);
        spinKit = view.findViewById(R.id.spinKit);
        email = view.findViewById(R.id.eTEmail);
        password = view.findViewById(R.id.eTPassword);
        mAwesomeValidation.addValidation(password,"[0-9a-zA-Z]{6,}","invalid password. At least 6 characters");
        name = view.findViewById(R.id.eTName);
        mAwesomeValidation.addValidation(name,"[a-zA-Z\\s]+", "invalid name");
        mAwesomeValidation.addValidation(email,android.util.Patterns.EMAIL_ADDRESS,"invalid email");
        phone = view.findViewById(R.id.eTPhone);
        mAwesomeValidation.addValidation(phone,"^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$","invalid phone");
        Button register = view.findViewById(R.id.btnRegister);
        register.setOnClickListener(registerClickListener);

        //TODO TERMS AND CONDITIONS
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
    private View.OnClickListener registerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mAwesomeValidation.validate())
            {
                spinKit.setVisibility(View.VISIBLE);
                registerUser();
            }

        }
    };

    protected void registerUser()
    {
        mAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "createUserWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name.getText().toString()+","+phone.getText().toString())
                            .build();
                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User profile updated.");
                                        redirectToNewActivity();
                                    }
                                }
                            });


                } else {
                    // If sign in fails, display a message to the user.
                    Log.d("TAG", "createUserWithEmail:failure", task.getException());
                    Toast.makeText(getActivity(), "Authentication failed: "+task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    spinKit.setVisibility(View.INVISIBLE);
                }
            }
        });
        //redirectToNewActivity();

    }

    private void redirectToNewActivity()
    {
        try {
            int SDK_INT = android.os.Build.VERSION.SDK_INT;
            if (SDK_INT > 8)
            {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);
                registerUserOnOurServers();

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        Intent i = new Intent(getContext(),StartParkingActivity.class);
        startActivity(i);
        getActivity().finish();
    }

    private void registerUserOnOurServers() throws Exception{
        URL url = new URL("http://www.quicsolv.com/m-park/api/register.php/");
        HttpURLConnection https = (HttpURLConnection) url.openConnection();
        //https.setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
        https.setRequestMethod("POST");
        https.setDoInput(true);
        https.setDoOutput(true);
        https.setReadTimeout(10000);
        https.setConnectTimeout(15000);


        JSONObject params = new JSONObject();
        params.put("un",name.getText().toString());
        params.put("ph",phone.getText().toString());
        params.put("e",email.getText().toString());
        //params.put("p",password.getText().toString());
        //params.add(new HashMap<String, String>("ps", paramValue1));


        OutputStream os = https.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(getPostDataString(params));
        writer.flush();
        writer.close();

        System.out.println(checkResponse(https));

    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

    private String checkResponse(HttpURLConnection connection) throws IOException
    {
        int responseCode=connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {

            BufferedReader in=new BufferedReader(new
                    InputStreamReader(
                    connection.getInputStream()));

            StringBuffer sb = new StringBuffer("");
            String line="";

            while((line = in.readLine()) != null) {

                sb.append(line);
                break;
            }

            in.close();
            return sb.toString();

        }
        else {
            return "false : "+responseCode;
        }
    }
}
