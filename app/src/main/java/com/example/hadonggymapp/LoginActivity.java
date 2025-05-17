package com.example.hadonggymapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText; // Dùng EditText trong Dialog
import android.widget.LinearLayout; // Để tạo layout cho EditText trong Dialog

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText editTextLoginEmail, editTextLoginPassword;
    private Button buttonLogin;
    private TextView textViewGoToRegister;
    private ProgressBar progressBarLogin;

    private FirebaseAuth mAuth;
    private TextView textViewForgotPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ Views
        editTextLoginEmail = findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewGoToRegister = findViewById(R.id.textViewGoToRegister);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword); // Nếu có
        progressBarLogin = findViewById(R.id.progressBarLogin);

        // Sự kiện click cho nút Đăng nhập
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Sự kiện click cho TextView "Chưa có tài khoản? Tạo tài khoản mới"
        textViewGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                // Không cần finish() ở đây nếu bạn muốn người dùng có thể back lại màn hình login
            }
        });

         if (textViewForgotPassword != null) {
             textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     showForgotPasswordDialog();
                 }
             });
         }
    }

    private void loginUser() {
        String email = editTextLoginEmail.getText().toString().trim();
        String password = editTextLoginPassword.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào
        if (TextUtils.isEmpty(email)) {
            editTextLoginEmail.setError("Email không được để trống.");
            editTextLoginEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextLoginEmail.setError("Vui lòng nhập email hợp lệ.");
            editTextLoginEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextLoginPassword.setError("Mật khẩu không được để trống.");
            editTextLoginPassword.requestFocus();
            return;
        }

        // Hiển thị ProgressBar và bắt đầu quá trình đăng nhập
        progressBarLogin.setVisibility(View.VISIBLE);
        buttonLogin.setEnabled(false); // Vô hiệu hóa nút

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBarLogin.setVisibility(View.GONE); // Ẩn ProgressBar
                        buttonLogin.setEnabled(true); // Kích hoạt lại nút

                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            // (Tùy chọn) Kiểm tra xem email đã được xác thực chưa
                            // if (user != null && !user.isEmailVerified()) {
                            //     Toast.makeText(LoginActivity.this, "Vui lòng xác thực email của bạn trước khi đăng nhập.", Toast.LENGTH_LONG).show();
                            //     mAuth.signOut(); // Đăng xuất nếu chưa xác thực
                            //     return;
                            // }

                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                            // Chuyển đến MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            // Xóa các activity cũ khỏi stack để người dùng không back lại được login/register
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // Đóng LoginActivity

                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String errorMessage = getString(R.string.error_login_failed_default); // Chuỗi mặc định

                            // KIỂM TRA LOẠI EXCEPTION CỤ THỂ
                            Exception exception = task.getException();
                            if (exception instanceof FirebaseAuthInvalidUserException) {
                                errorMessage = getString(R.string.error_user_not_found);
                            } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = getString(R.string.error_wrong_password_or_email);
                            } else if (exception instanceof FirebaseNetworkException) {
                                errorMessage = getString(R.string.error_network_issue);
                            } else if (exception != null && exception.getMessage() != null) {
                                // errorMessage += " (" + exception.getMessage() + ")";
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quên Mật Khẩu");
        builder.setMessage("Nhập email của bạn để nhận link đặt lại mật khẩu:");

        // Tạo một EditText để người dùng nhập email trong Dialog
        final EditText inputEmail = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        // Thêm margin cho EditText
        int margin = (int) getResources().getDisplayMetrics().density * 16; // 16dp
        lp.setMargins(margin, margin/2, margin, margin/2);
        inputEmail.setLayoutParams(lp);
        inputEmail.setHint("Email");
        inputEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // Đặt EditText vào Dialog
        // Để tránh lỗi "The specified child already has a parent", nếu bạn muốn tái sử dụng layout
        // thì cần inflate một layout XML riêng cho dialog. Ở đây ta tạo EditText động.
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(inputEmail);
        builder.setView(container);


        // Nút "Gửi"
        builder.setPositiveButton("Gửi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = inputEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập email.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(LoginActivity.this, "Email không hợp lệ.", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendPasswordResetEmail(email);
            }
        });

        // Nút "Hủy"
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void sendPasswordResetEmail(String email) {
        progressBarLogin.setVisibility(View.VISIBLE); // Hiển thị ProgressBar
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBarLogin.setVisibility(View.GONE); // Ẩn ProgressBar
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email đặt lại mật khẩu đã được gửi.");
                            Toast.makeText(LoginActivity.this, "Link đặt lại mật khẩu đã được gửi đến email của bạn.", Toast.LENGTH_LONG).show();
                        } else {
                            Log.w(TAG, "sendPasswordResetEmail:failure", task.getException());
                            String errorMessage = getString(R.string.toast_reset_email_failed_default); // Chuỗi mặc định

                            Exception exception = task.getException();
                            if (exception instanceof FirebaseAuthInvalidUserException) {
                                // Email không tương ứng với tài khoản nào
                                errorMessage = getString(R.string.error_email_not_found_for_reset);
                            } else if (exception instanceof FirebaseNetworkException) {
                                errorMessage = getString(R.string.error_network_issue);
                            } else if (exception != null && exception.getMessage() != null) {
                                // errorMessage += " (" + exception.getMessage() + ")";
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}