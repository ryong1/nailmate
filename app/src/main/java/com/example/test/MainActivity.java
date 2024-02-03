package com.example.test;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Fragment homeFragment;
    private Fragment writeFragment;
    private Fragment mypageFragment;
    private ImageView iconImageView;

    private static final int LOGIN_REQUEST_CODE = 123; // 원하는 requestCode로 설정

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mAuth = FirebaseAuth.getInstance();

        // Initialize fragments
        homeFragment = new HomeFragment();
        writeFragment = new WriteFragment();
        mypageFragment = new MyPageFragment();

        // Set the initial fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Bottom navigation item click listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_menu_write:
                        // Check if the user is logged in

                        if (isUserLoggedIn()) {
                            // User is logged in, proceed to WriteFragment
                            //changeImage(R.drawable.write_color);
                            writeFragment = new WriteFragment();
                            replaceFragment(writeFragment);
                        } else {
                            // User is not logged in, redirect to login fragment
                            startLoginFragment(LOGIN_REQUEST_CODE, writeFragment);
                        }
                        return true;
                    case R.id.bottom_menu_home:
                        //changeImage(R.drawable.home_color);
                        homeFragment = new HomeFragment();
                        replaceFragment(homeFragment);
                        return true;
                    case R.id.bottom_menu_mypage:
                        // Check if the user is logged in
                        if (isUserLoggedIn()) {
                            // User is logged in, proceed to MyPageFragment
                            //changeImage(R.drawable.user_color);
                            mypageFragment = new MyPageFragment();
                            replaceFragment(mypageFragment);
                        } else {
                            // User is not logged in, redirect to login fragment
                            startLoginFragment(LOGIN_REQUEST_CODE, mypageFragment);
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    // Function to change the image
    private void changeImage(int imageResource) {
        iconImageView.setImageResource(imageResource);
    }

    // Function to replace the fragment
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // Helper function to start the login fragment
    private void startLoginFragment(int requestCode, Fragment targetFragment) {
        // Create a new instance of the LoginFragment
        LoginFragment loginFragment = new LoginFragment();

        // Pass the target fragment to the LoginFragment
        Bundle args = new Bundle();
        args.putString("targetFragment", targetFragment.getClass().getName());
        loginFragment.setArguments(args);

        // Replace the current fragment with the LoginFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, loginFragment)
                .addToBackStack(null) // Add to back stack so that user can navigate back
                .commit();
    }

    // Function to check if the user is logged in
    private boolean isUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // 사용자가 로그인한 경우 currentUser는 null이 아닙니다.
        return currentUser != null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            // Check if the login was successful
            boolean loginSuccess = data.getBooleanExtra("login_success", false);
            if (loginSuccess) {
                // Successfully logged in, do nothing as the target fragment is already replaced
            } else {
                // Handle login failure if needed
            }
        }
    }
}
