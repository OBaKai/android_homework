package com.llk.myapplication

import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
        listOf<String?>("android.permission.READ_CONTACTS"),
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

    private fun showDialog(user: ContactUser){
        val dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        val view = LayoutInflater.from(this).inflate(R.layout.dialog2, null)

        //发短信
        view.findViewById<Button>(R.id.btn_msg)?.setOnClickListener {
            dialog.dismiss()
            showSendMsgDialog(user)
        }

        //打电话
        view.findViewById<Button>(R.id.btn_phone)?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)

        dialog.show()
    }

    private fun showSendMsgDialog(user: ContactUser){

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
    private lateinit var datas: List<ContactUser>
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
        return datas.size
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        val user = datas[position]
        holder.layout?.setOnClickListener{
            lis?.onItemClick(user)
        }

        holder.name?.text = "${user.name}（${user.note}）"
        holder.phone?.text = user.phone
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

