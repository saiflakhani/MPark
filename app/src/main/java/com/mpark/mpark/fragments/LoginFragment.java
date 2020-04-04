package com.mpark.mpark.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mpark.mpark.R;
import com.mpark.mpark.activities.StartParkingActivity;
import com.mpark.mpark.activities.TimerRunning;
import com.mpark.mpark.utilities.AppGlobalData;

import static android.content.ContentValues.TAG;
import static com.basgeekball.awesomevalidation.ValidationStyle.COLORATION;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    AwesomeValidation mAwesomeValidation = new AwesomeValidation(COLORATION);
    private boolean redirect;
    SpinKitView spinKit;
    private String mParam1;
    private String mParam2;

    private EditText email,password;
    private Button btnLogin;
    private OnFragmentInteractionListener mListener;

    private FirebaseDatabase fbDb = AppGlobalData.getDatabase();
    private DatabaseReference users = fbDb.getReference("users");
    private DatabaseReference currentUser;
    private DatabaseReference parkings;

    private FirebaseAuth mAuth;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        email = v.findViewById(R.id.eTemail);
        mAwesomeValidation.addValidation(email,android.util.Patterns.EMAIL_ADDRESS,"invalid email");
        password = v.findViewById(R.id.eTPassword);
        mAwesomeValidation.addValidation(password,"[0-9a-zA-Z]{6,}","invalid password. At least 6 characters");
        btnLogin = v.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(loginClickListener);
        spinKit = v.findViewById(R.id.spinKit);
        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if(parkings!=null)
            parkings.removeEventListener(dataChangeListener);
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


    private View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            spinKit.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser() != null) {
                            currentUser = users.child(mAuth.getCurrentUser().getUid());
                            parkings = currentUser.child("parkings");
                            checkAndRedirect();
                        }
                    }else{
                        Toast.makeText(getActivity(), "Authentication failed: "+task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        spinKit.setVisibility(View.INVISIBLE);
                    }
                }
            });


        }
    };




    private ValueEventListener dataChangeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            DataSnapshot postSnapShot = null;
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                postSnapShot = postSnapshot;
            }
            String status = "";
            try {
                status = String.valueOf(postSnapShot.child("status").getValue());
            }catch (Exception e)
            {
                Log.e(TAG,"Snapshot is null!!");
            }
            if (status.equalsIgnoreCase("Active")) {
                Intent i = new Intent(getContext(), TimerRunning.class);
                startActivity(i);
                redirect = true;
                getActivity().finish();
            }else{
                Intent i = new Intent(getContext(), StartParkingActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };


    private boolean checkAndRedirect()
    {
        parkings.addValueEventListener(dataChangeListener);
        return redirect;
    }
}
