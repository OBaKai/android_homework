package com.llk.myapplication

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.llk.myapplication.bean.ContactUser
import com.llk.myapplication.utils.ContactUtils
import kotlinx.android.synthetic.main.homeword2.*


/**
 * 实现查询电话列表
 * 选中电话并且可以打电话或者发短信
 */
class HomeworkActivity2 : AppCompatActivity(){

    private lateinit var contactAdapter :ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homeword2)

        addPermissByPermissionList(this,
        listOf<String?>("android.permission.READ_CONTACTS",
                "android.permission.CALL_PHONE",
                "android.permission.SEND_SMS"),
        100)
    }

    private fun initView(){
        hint.visibility = View.VISIBLE
        hint.text = "数据请求中，请稍候"

        contactAdapter = ContactAdapter()
        contactAdapter.setListener(object: ContactAdapter.onItemClickListener{
            override fun onItemClick(user: ContactUser) {
                showDialog(user)
            }
        })

        //依赖了kotlin-android-extensions库
        //无需findviewbyid，直接使用id拿到view对象
        recycler_view.adapter = contactAdapter
        recycler_view.layoutManager = LinearLayoutManager(this)

        Thread {
            val data = ContactUtils.getAllContacts(this)
            this.runOnUiThread {
                contactAdapter.setDatas(data)
                hint.visibility = View.GONE
            }
        }.start()
    }

    private fun showDialog(user: ContactUser?){
        val dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        val view = LayoutInflater.from(this).inflate(R.layout.dialog2, null)

        view.findViewById<Button>(R.id.btn_msg)?.setOnClickListener {
            dialog.dismiss()

            if (user != null){
                showSendMsgDialog(user)
            }else{
                Toast.makeText(this, "发短信失败，联系人信息获取异常", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.btn_phone)?.setOnClickListener {
            dialog.dismiss()

            if (user != null && !TextUtils.isEmpty(user.phone)){
                val intent = Intent(Intent.ACTION_CALL)
                val data: Uri = Uri.parse("tel:${user.phone}")
                intent.data = data
                startActivity(intent)
            }else{
                Toast.makeText(this, "打电话失败，联系人信息获取异常", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setContentView(view)

        dialog.show()
    }

    private fun showSendMsgDialog(user: ContactUser){
        val dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_msg, null)

        view.findViewById<TextView>(R.id.msg_name)?.text = user.name
        view.findViewById<TextView>(R.id.msg_phone)?.text = user.phone
        val et = view.findViewById<EditText>(R.id.et)
        view.findViewById<Button>(R.id.send).setOnClickListener{
            val msg = et?.text?.toString()
            if (!TextUtils.isEmpty(msg)){
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${user.phone}"))
                intent.putExtra("sms_body", msg)
                startActivity(intent)
            }else{
                Toast.makeText(this, "发送内容为空", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.setContentView(view)

        dialog.show()
    }

    /**
     * 动态权限
     */
    fun addPermissByPermissionList(
        activity: Activity?,
        permissions: List<String?>,
        request: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   //Android 6.0开始的动态权限，这里进行版本判断
            val mPermissionList: ArrayList<String?> = ArrayList()
            for (i in permissions.indices) {
                if (ContextCompat.checkSelfPermission(activity!!, permissions[i]!!)
                    != PackageManager.PERMISSION_GRANTED
                ){
                    mPermissionList.add(permissions[i])
                }
            }

            if (mPermissionList.isEmpty()) {
                initView()
            } else {
                //请求权限方法
                val permissionsNew: Array<String> =
                    mPermissionList.toArray(arrayOfNulls(mPermissionList.size))
                ActivityCompat.requestPermissions(
                    activity!!,
                    permissionsNew,
                    request
                )
            }
        }
    }

    /**
     * requestPermissions的回调
     * 一个或多个权限请求结果回调
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var hasAllGranted = true
        //判断是否拒绝  拒绝后要怎么处理 以及取消再次提示的处理
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                hasAllGranted = false
                break
            }
        }
        if (hasAllGranted) {
            initView()
        } else {
            recycler_view.visibility = View.GONE
            hint.visibility = View.VISIBLE
            hint.text = "麻烦去设置给予权限，谢谢"
        }
    }

}

class ContactAdapter : RecyclerView.Adapter<ContactHolder>() {
    private var datas: List<ContactUser>? = null
    private var lis: onItemClickListener? = null

    fun setDatas(list: List<ContactUser>){
        datas = list
        notifyDataSetChanged()
    }

    fun setListener(l: onItemClickListener){
        lis = l
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_item, parent, false)
        return ContactHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (datas != null) datas!!.size else 0
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        val user = datas?.get(position)
        holder.layout?.setOnClickListener{
            lis?.onItemClick(user!!)
        }

        var name = user?.name
        if (TextUtils.isEmpty(name)) name = "未知联系人"
        if (!TextUtils.isEmpty(user?.note)) name += "(${user?.note})"

        holder.name?.text = name
        holder.phone?.text = user?.phone
    }

    interface onItemClickListener{
        fun onItemClick(user: ContactUser)
    }
}

class ContactHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
    var name: TextView? = null
    var phone: TextView? = null
    var layout: LinearLayout? = null

    init {
        name = view?.findViewById(R.id.name)
        phone = view?.findViewById(R.id.phone)
        layout = view?.findViewById(R.id.layout)
    }
}

