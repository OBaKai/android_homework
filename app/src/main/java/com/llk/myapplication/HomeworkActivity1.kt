package com.llk.myapplication

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.homeword1.*

/**
 * 实现模拟登录界面
 * 并且支持安全提示以及错误重试
 */
class HomeworkActivity1 : AppCompatActivity(), View.OnClickListener {

    companion object{
        const val TRUE_USERNAME = "admin"
        const val TRUE_PASSWORD = "admin"
    }

    private var retryCount: Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homeword1)

        //依赖了kotlin-android-extensions库
        //无需findviewbyid，直接使用id拿到view对象
        btn_login.setOnClickListener(this)
        btn_login.setEnabled(true)
        
        btn_exit.setOnClickListener(this)

        tv_retry.visibility = View.INVISIBLE
    }

    override fun onClick(v: View?) {
        when(v?.id){

            R.id.btn_login -> {
                //检验账号密码
                if (TextUtils.equals(TRUE_USERNAME, et_username.text?.toString()) 
                    && TextUtils.equals(TRUE_PASSWORD, et_password.text?.toString())){
                    showDialog(this.getString(R.string.dialog_ok_hint))
                }else{
                    tv_retry.visibility = View.VISIBLE
                    retryCount--

                    if (retryCount <= 0){ //重试超过三次
                        tv_retry.text = this.getString(R.string.retry_hint, 0)
                        btn_login.setEnabled(false)
                    }else{
                        tv_retry.text = this.getString(R.string.retry_hint, retryCount)
                    }

                    showDialog(this.getString(R.string.dialog_fail_hint))
                }
            }

            R.id.btn_exit -> {
                System.exit(0)
            }
        }
    }

    private fun showDialog(msg:String){
        val dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        val view = LayoutInflater.from(this).inflate(R.layout.dialog, null)
        view.findViewById<TextView>(R.id.msg)?.text = msg
        dialog.setContentView(view)

        dialog.show()
    }
}