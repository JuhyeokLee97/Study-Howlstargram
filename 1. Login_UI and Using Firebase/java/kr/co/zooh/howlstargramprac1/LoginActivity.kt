package kr.co.zooh.howlstargramprac1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null     // Firebase를 사용하기 위한 변수
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()   // 로그인 작업의 onCreate 메서드에서 FirebaseAuth 객체의 공유 인스턴스를 가져온다.

        email_login_button.setOnClickListener {
            signinAndSignup()
        }

    }

    // 신규 사용자의 이메일 주소와 비밀번호를 createUserWithEmailAndPassword에 전달하여 신규 계정을 생성한다.
    fun signinAndSignup(){
        Log.v("태그", "message")
        //createUserWithEmailAndPassword()에서의 인자에서 EditText뷰의 데이터를 text형태로 가져와서 String형태로 바꾸는 것 같다.
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener {
                task ->
                /*
                task.isSuccessful >> 의미하는 값이 무엇일까??? Firebase에서 찾아보자!
                 */
                when {
                    task.isSuccessful -> {
                        // Creating a user account
                        moveMainPage(task.result?.user)
                    }
                    task.exception?.message.isNullOrEmpty() -> {
                        // Show the error message
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()     // 에러 발생시 Toast 메시시 출력 운영체제에 요청
                    }
                    else -> {
                        // Login if you have a account
                        signinEmail()
                    }
                }
            }
    }

    fun signinEmail(){
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener {
                task ->
                if (task.isSuccessful){
                    // Login
                    moveMainPage(task.result?.user)
                }else{
                    // Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun moveMainPage(user : FirebaseUser?){
        /*
        startActivity가 뭐야?
        MainActivity::class.java가 의미하는 것을 찾아보자
         */
        if (user != null)
            startActivity(Intent(this, MainActivity::class.java))
    }
}
