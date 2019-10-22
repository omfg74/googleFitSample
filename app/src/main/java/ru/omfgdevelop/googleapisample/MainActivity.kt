package ru.omfgdevelop.googleapisample

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
//import androidx.core.app.ComponentActivity
//import androidx.core.app.ComponentActivity.ExtraData
import android.util.Log
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Session
//import androidx.core.app.ComponentActivity
//import androidx.core.app.ComponentActivity.ExtraData
import java.util.*
import com.google.android.gms.fitness.request.SessionReadRequest

import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.result.SessionReadResponse
import com.google.android.gms.tasks.Task
import java.util.concurrent.TimeUnit
import android.content.Intent
import android.content.pm.PackageManager
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.fitness.data.Field
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), WalksCallBack {
    override fun getWalks(walkList: MutableList<Walk>) {

        walkList.forEach {
            Log.d("Log","steps from list "+it.steps)
            Log.d("Log","duration from list "+it.duration)
        }
    }




    var mGoogleSignInClient: GoogleSignInClient? = null
    var signInButton: SignInButton? = null
    var gso: GoogleSignInOptions? = null

    var textView: TextView? = null
    override fun onStart() {
        super.onStart()
        acc()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(
                Scope(Scopes.EMAIL),
                Scope(Scopes.PROFILE),
                Scope(Scopes.FITNESS_BODY_READ),
                Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE),
                Scope(Scopes.FITNESS_NUTRITION_READ)
            )
            .build()
        textView = findViewById(R.id.main_text_view)
        signInButton = findViewById(R.id.sign_in_button)
        signInButton?.setSize(SignInButton.SIZE_STANDARD)
        signInButton?.setScopes(gso?.getScopeArray())
        signInButton?.setOnClickListener(View.OnClickListener {
            signnIn()
        })

    }

    private fun signnIn() {
        startSignInIntent()
        var signInIntent = mGoogleSignInClient?.signInIntent;
        startActivityForResult(signInIntent, 111);
    }


    private fun acc() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        Log.d("Log", "" + account)

    }

    private fun startSignInIntent(): GoogleSignInClient? {
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso!!)
        return mGoogleSignInClient
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111) {
            var task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {

        val account: GoogleSignInAccount = task?.getResult(ApiException::class.java)!!
        Log.d("Log", "account " + account)
        requestPermitions()

    }


    private fun requestPermitions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    222
                )
            }
        } else {
            initWalks()
        }
    }

    fun initWalks() {
//        val walkWarker = WalkWorker()
//        val walks = walkWarker.fetchWalks(this, this)
        val historyWorker:HistoryWorker= HistoryWorker();
        historyWorker.fetchHistoryWalks(this)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            222 -> {
                initWalks()
            }
        }
    }

}
