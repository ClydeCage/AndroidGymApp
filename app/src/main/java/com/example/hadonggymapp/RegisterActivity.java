package com.example.hadonggymapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest; // Để cập nhật tên hiển thị nếu cần

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText editTextRegisterEmail, editTextRegisterPassword, editTextRegisterConfirmPassword;
    private Button buttonRegister;
    private TextView textViewGoToLogin;
    private ProgressBar progressBarRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ Views
        editTextRegisterEmail = findViewById(R.id.editTextRegisterEmail);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        editTextRegisterConfirmPassword = findViewById(R.id.editTextRegisterConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewGoToLogin = findViewById(R.id.textViewGoToLogin);
        progressBarRegister = findViewById(R.id.progressBarRegister);

        // Sự kiện click cho nút Đăng ký
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Sự kiện click cho TextView "Đã có tài khoản? Đăng nhập ngay"
        textViewGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến LoginActivity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Đóng RegisterActivity để không quay lại được bằng nút back
            }
        });
    }

    private void registerUser() {
        String email = editTextRegisterEmail.getText().toString().trim();
        String password = editTextRegisterPassword.getText().toString().trim();
        String confirmPassword = editTextRegisterConfirmPassword.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào
        if (TextUtils.isEmpty(email)) {
            editTextRegisterEmail.setError("Email không được để trống.");
            editTextRegisterEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextRegisterEmail.setError("Vui lòng nhập email hợp lệ.");
            editTextRegisterEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextRegisterPassword.setError("Mật khẩu không được để trống.");
            editTextRegisterPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextRegisterPassword.setError("Mật khẩu phải có ít nhất 6 ký tự.");
            editTextRegisterPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            editTextRegisterConfirmPassword.setError("Xác nhận mật khẩu không được để trống.");
            editTextRegisterConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextRegisterConfirmPassword.setError("Mật khẩu xác nhận không khớp.");
            editTextRegisterConfirmPassword.requestFocus();
            return;
        }

        // Hiển thị ProgressBar và bắt đầu quá trình đăng ký
        progressBarRegister.setVisibility(View.VISIBLE);
        buttonRegister.setEnabled(false); // Vô hiệu hóa nút trong khi đang xử lý

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBarRegister.setVisibility(View.GONE); // Ẩn ProgressBar
                        buttonRegister.setEnabled(true); // Kích hoạt lại nút

                        if (task.isSuccessful()) {
                            // Đăng ký thành công
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                            // (Tùy chọn) Gửi email xác thực
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "Email xác thực đã được gửi.");
                                                }
                                            }
                                        });
                            }


                            // Chuyển đến LoginActivity để người dùng đăng nhập lại
                            // Hoặc bạn có thể tự động đăng nhập và chuyển đến MainActivity
                            // Hiện tại, chúng ta chuyển về LoginActivity
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            // Xóa các activity cũ trên stack để không back lại được register
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // Đóng RegisterActivity

                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            String errorMessage = getString(R.string.error_registration_failed_default); // Chuỗi mặc định

                            // KIỂM TRA LOẠI EXCEPTION CỤ THỂ
                            Exception exception = task.getException();
                            if (exception instanceof FirebaseAuthUserCollisionException) {
                                errorMessage = getString(R.string.error_email_already_in_use);
                            } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                // Lỗi này có thể do email sai định dạng hoặc mật khẩu yếu (dù đã check ở client)
                                errorMessage = getString(R.string.error_invalid_credentials_register);
                            } else if (exception instanceof FirebaseNetworkException) {
                                errorMessage = getString(R.string.error_network_issue);
                            } else if (exception != null && exception.getMessage() != null) {
                                // Nếu không phải các lỗi cụ thể trên, có thể dùng message gốc hoặc một thông báo chung
                                // errorMessage += " (" + exception.getMessage() + ")"; // Tùy chọn: thêm message gốc để debug
                            }
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}