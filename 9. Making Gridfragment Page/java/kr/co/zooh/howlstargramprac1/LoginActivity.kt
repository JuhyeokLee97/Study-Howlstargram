package kr.co.zooh.howlstargramprac1

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null     // Firebase를 사용하기 위한 변수
    var googleSignInClient: GoogleSignInClient? = null
    // 구글 로그인할 때 사용할 리퀘스트 코드에 들어갈 변수
    var GOOGLE_LOGIN_CODE = 9001
    // 페이스북 로그인 결과값을 가져오는 변수
    var callbackManager : CallbackManager?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()   // 로그인 작업의 onCreate 메서드에서 FirebaseAuth 객체의 공유 인스턴스를 가져온다.
        email_login_button.setOnClickListener {
            signinAndSignup()
        }
        google_login_button.setOnClickListener {
            // First Step
            googleLogin()
        }
        facebook_login_button.setOnClickListener {
            // First step
            facebookLogin()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail() // Gmail 받아오기
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)  // 옵션값을 셋팅
        printHashKey()
        callbackManager = CallbackManager.Factory.create()  // 이 결과값은 onActivityResult에 넘어간다.
    }

    override fun onStart(){
        super.onStart()
        moveMainPage(auth?.currentUser)
    }
    // NCB0bu8oHKxL2D1SjQO9K4srrbg=
    fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }
    fun googleLogin() {  // 1st Step
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }
    fun facebookLogin(){
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    // 2nd Step
                    // 페이스북 데이터를 Firebase에 넘기는 코드
                    handleFaceBookAccessToken(result?.accessToken)
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {

                }
            })

    }
    fun handleFaceBookAccessToken(token : AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login
                    moveMainPage(task.result?.user)
                } else {
                    // Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }
    /*
    onActivityResult가 원래 어떤 함수인지 알아보자.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result =
                Auth.GoogleSignInApi.getSignInResultFromIntent(data)   // 구글에서 받아온 로그인 데이터 값
            if (result != null) {
                if (result.isSuccess) { // result값이 성공했을 때 Firebase에 넘겨줄 수 있도록 하자
                    var account = result?.signInAccount
                    // Second step
                    firebaseAuthWithGoogle(account)
                }
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential =
            GoogleAuthProvider.getCredential(account?.idToken, null)   // account에 있는 Token값을 받아온다.
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                /*
                task.isSuccessful >> 의미하는 값이 무엇일까??? Firebase에서 찾아보자!
                 */
                if (task.isSuccessful) {
                    // 아이디가 성공적으로 만들어졌을 때
                    moveMainPage(task.result?.user);
                } else if (task.exception?.message.isNullOrEmpty()) {
                    // 로그인 에러가 나타났을 경우
                    // show the error messge
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                } else {
                    // 회원가입 되는 경우도 아니고 에러 메시지가 나오는 경우도 아닌 때, 로그인 화면으로 빠지자.
                    signinEmail()
                }
            }
    }

    // 신규 사용자의 이메일 주소와 비밀번호를 createUserWithEmailAndPassword에 전달하여 신규 계정을 생성한다.
    fun signinAndSignup() {
        //createUserWithEmailAndPassword()에서의 인자에서 EditText뷰의 데이터를 text형태로 가져와서 String형태로 바꾸는 것 같다.
        auth?.createUserWithEmailAndPassword(
            email_edittext.text.toString(),
            password_edittext.text.toString()
        )
            ?.addOnCompleteListener { task ->
                /*
                task.isSuccessful >> 의미하는 값이 무엇일까??? Firebase에서 찾아보자!
                 */
                if (task.isSuccessful) {
                    // 아이디가 성공적으로 만들어졌을 때
                    moveMainPage(task.result?.user);
                } else if (task.exception?.message.isNullOrEmpty()) {
                    // 로그인 에러가 나타났을 경우
                    // show the error messge
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                } else {
                    // 회원가입 되는 경우도 아니고 에러 메시지가 나오는 경우도 아닌 때, 로그인 화면으로 빠지자.
                    signinEmail()
                }
            }
    }


    fun signinEmail() {
        auth?.createUserWithEmailAndPassword(
            email_edittext.text.toString(),
            password_edittext.text.toString()
        )
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    moveMainPage(task.result?.user)
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
