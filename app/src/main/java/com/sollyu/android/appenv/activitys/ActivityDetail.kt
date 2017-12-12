/*
 * Copyright © 2017 Sollyu <https://www.sollyu.com/>
 *
 * Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed.
 *
 * This version of the GNU Lesser General Public License incorporates the terms and conditions of version 3 of the GNU General Public License, supplemented by the additional permissions listed below.
 */

package com.sollyu.android.appenv.activitys

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.support.design.widget.Snackbar
import android.telephony.TelephonyManager
import android.view.View
import android.widget.PopupMenu
import com.alibaba.fastjson.JSONObject
import com.elvishew.xlog.XLog
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.sollyu.android.appenv.R
import com.sollyu.android.appenv.R.id.*
import com.sollyu.android.appenv.bean.PhoneModel
import com.sollyu.android.appenv.commons.Phones
import com.sollyu.android.appenv.commons.Random
import com.sollyu.android.appenv.commons.SettingsXposed
import com.sollyu.android.appenv.events.EventSample
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_activity_detail.*
import kotlinx.android.synthetic.main.content_activity_detail.view.*
import kotlinx.android.synthetic.main.include_toolbar.*
import org.greenrobot.eventbus.EventBus
import org.xutils.view.annotation.Event
import org.xutils.x

@Suppress("unused")
class ActivityDetail : ActivityBase() {

    companion object {
        fun launch(activity: Activity, applicationInfo: ApplicationInfo?) {
            val intent = Intent(activity, ActivityDetail::class.java)

            intent.putExtra("packageName", applicationInfo?.packageName)
            activity.startActivity(intent)
        }
    }

    /**
     * 随机对象
     */
    val random = Random.New()

    /**
     * 上个界面传送过来的应用信息
     */
    val appInfo: ApplicationInfo by lazy {
        val packageName = activity.intent.getStringExtra("packageName")
        return@lazy activity.packageManager.getApplicationInfo(packageName, 0)
    }

    override fun onInitView() {
        super.onInitView()
        setContentView(R.layout.activity_detail)

        x.view().inject(activity)

        setSupportActionBar(toolbar)
        supportActionBar?.title = appInfo.loadLabel(packageManager)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onInitData() {
        super.onInitData()

        this.jsonObjectToUi(SettingsXposed.Instance.get(appInfo.packageName))
    }

    override fun onInitDone() {
        super.onInitDone()
        Phones.Reload()

        when (appInfo.packageName) {
            "com.tencent.mobileqq" -> {
                Snackbar.make(fab, "\uD83D\uDCF1手机QQ无法设置成iPhone在线，请谅解！", Snackbar.LENGTH_INDEFINITE).show();
            }
            "com.sankuai.meituan" -> {
                Snackbar.make(fab, "🈲请不要使用本软件恶意刷单!", Snackbar.LENGTH_INDEFINITE).show()
            }
            "me.ele" -> {
                Snackbar.make(fab, "🈲请不要使用本软件恶意刷单!", Snackbar.LENGTH_INDEFINITE).show()
            }
            "com.tencent.mm" -> {
                Snackbar.make(fab, "⚠️使用本软件用来微信养号，更容易被封！", Snackbar.LENGTH_INDEFINITE).show()
            }
            "com.qzone" -> {
                Snackbar.make(fab, "⚠如果您将机型乱写，QQ空间会把您的机型变成小写", Snackbar.LENGTH_INDEFINITE).show()
            }
            "com.sina.weibo" -> {
                Snackbar.make(fab, "⚠微博显示的继续有点少，有时候修改无效可能是微博没有收录这个机型", Snackbar.LENGTH_INDEFINITE).show()
            }
            "com.tencent.tmgp.sgame" -> {
                Snackbar.make(fab, "⚠️使用本软件可以打开王者荣耀高帧率模式\n但是也有很小的几率封号，希众知。", Snackbar.LENGTH_INDEFINITE).setAction("开启") {
                    oieBuildManufacturer.rightEditText = "Xiaomi"
                    oieBuildModel.rightEditText = "MIX"
                }.show()
            }
        }
    }

    /**
     *
     */
    @Event(R.id.menu_save_config)
    private fun onBtnClickFinish(view: View) {
        val jsonObject = JSONObject()
        jsonObject.put("android.os.Build.ro.product.manufacturer", oieBuildManufacturer.rightEditText.toString(), true)
        jsonObject.put("android.os.Build.ro.product.model"       , oieBuildModel.rightEditText.toString()       , true)
        jsonObject.put("android.os.Build.ro.serialno"            , oieBuildSerial.rightEditText.toString()      , true)
        jsonObject.put("android.os.Build.VERSION.RELEASE"        , oieBuildVersionName.rightEditText.toString() , true)

        jsonObject.put("android.os.SystemProperties.android_id", oieAndroidId.rightEditText.toString(), true)

        jsonObject.put("android.telephony.TelephonyManager.getLine1Number"    , oieSimLine1Number.rightEditText.toString() , true)
        jsonObject.put("android.telephony.TelephonyManager.getDeviceId"       , oieSimGetDeviceId.rightEditText.toString() , true)
        jsonObject.put("android.telephony.TelephonyManager.getSubscriberId"   , oieSimSubscriberId.rightEditText.toString(), true)
        jsonObject.put("android.telephony.TelephonyManager.getSimOperator"    , oieSimOperator.rightEditText.toString()    , true)
        jsonObject.put("android.telephony.TelephonyManager.getSimOperatorName", oieSimOperatorName.rightEditText.toString(), true)
        jsonObject.put("android.telephony.TelephonyManager.getSimSerialNumber", oieSimSerialNumber.rightEditText.toString(), true)
        jsonObject.put("android.telephony.TelephonyManager.getSimState"       , oieSimStatus.rightEditText.toString()      , true)

        jsonObject.put("android.net.wifi.WifiInfo.getSSID"      , oieWifiName.rightEditText.toString()      , true)
        jsonObject.put("android.net.wifi.WifiInfo.getBSSID"     , oieWifiBssid.rightEditText.toString()     , true)
        jsonObject.put("android.net.wifi.WifiInfo.getMacAddress", oieWifiMacAddress.rightEditText.toString(), true)

        XLog.json(jsonObject.toJSONString())
        SettingsXposed.Instance.set(appInfo.packageName, jsonObject)
        EventBus.getDefault().postSticky(EventSample(EventSample.TYPE.MAIN_REFRESH))
        Snackbar.make(view, R.string.detail_finish_snackbar, Snackbar.LENGTH_LONG).setAction(R.string.finish) { activity.finish() }.show()
    }

    private fun jsonObjectToUi(jsonObject: JSONObject?) {

        //
        if (jsonObject?.containsKey("android.os.Build.ro.product.manufacturer") == true)
            oieBuildManufacturer.rightEditText = jsonObject.getString("android.os.Build.ro.product.manufacturer")
        if (jsonObject?.containsKey("android.os.Build.ro.product.model") == true)
            oieBuildModel.rightEditText = jsonObject.getString("android.os.Build.ro.product.model")
        if (jsonObject?.containsKey("android.os.Build.ro.serialno") == true)
            oieBuildSerial.rightEditText = jsonObject.getString("android.os.Build.ro.serialno")
        if (jsonObject?.containsKey("android.os.Build.VERSION.RELEASE") == true)
            oieBuildVersionName.rightEditText = jsonObject.getString("android.os.Build.VERSION.RELEASE")

        //
        if (jsonObject?.containsKey("android.os.SystemProperties.android_id") == true)
            oieAndroidId.rightEditText = jsonObject.getString("android.os.SystemProperties.android_id")

        //
        if (jsonObject?.containsKey("android.telephony.TelephonyManager.getLine1Number") == true)
            oieSimLine1Number.rightEditText = jsonObject.getString("android.telephony.TelephonyManager.getLine1Number")
        if (jsonObject?.containsKey("android.telephony.TelephonyManager.getDeviceId") == true)
            oieSimGetDeviceId.rightEditText = jsonObject.getString("android.telephony.TelephonyManager.getDeviceId")
        if (jsonObject?.containsKey("android.telephony.TelephonyManager.getSubscriberId") == true)
            oieSimSubscriberId.rightEditText = jsonObject.getString("android.telephony.TelephonyManager.getSubscriberId")
        if (jsonObject?.containsKey("android.telephony.TelephonyManager.getSimOperator") == true)
            oieSimOperator.rightEditText = jsonObject.getString("android.telephony.TelephonyManager.getSimOperator")
        if (jsonObject?.containsKey("android.telephony.TelephonyManager.getSimOperatorName") == true)
            oieSimOperatorName.rightEditText = jsonObject.getString("android.telephony.TelephonyManager.getSimOperatorName")
        if (jsonObject?.containsKey("android.telephony.TelephonyManager.getSimSerialNumber") == true)
            oieSimSerialNumber.rightEditText = jsonObject.getString("android.telephony.TelephonyManager.getSimSerialNumber")
        if (jsonObject?.containsKey("android.telephony.TelephonyManager.getSimState") == true)
            oieSimStatus.rightEditText = jsonObject.getString("android.telephony.TelephonyManager.getSimState")

        //
        if (jsonObject?.containsKey("android.net.wifi.WifiInfo.getSSID") == true)
            oieWifiName.rightEditText = jsonObject.getString("android.net.wifi.WifiInfo.getSSID")
        if (jsonObject?.containsKey("android.net.wifi.WifiInfo.getBSSID") == true)
            oieWifiBssid.rightEditText = jsonObject.getString("android.net.wifi.WifiInfo.getBSSID")
        if (jsonObject?.containsKey("android.net.wifi.WifiInfo.getMacAddress") == true)
            oieWifiMacAddress.rightEditText = jsonObject.getString("android.net.wifi.WifiInfo.getMacAddress")

    }

    /**
     *
     */
    @Event(R.id.oieBuildManufacturer)
    private fun onItemClickBuildManufacturer(view: View) {

        val menuPop = PopupMenu(activity, view)

        Phones.Instance.phoneList.keys.forEach { menuPop.menu.add(it) }

        BottomSheetBuilder(activity, R.style.AppTheme_BottomSheetDialog)
                .setMode(BottomSheetBuilder.MODE_LIST)
                .expandOnStart(true)
                .setMenu(menuPop.menu)
                .setItemClickListener { item -> oieBuildManufacturer.rightEdit.setText(Phones.Instance.phoneList[item.title]?.get(0)?.manufacturer) }
                .createDialog()
                .show()
    }

    /**
     *
     */
    @Event(R.id.oieBuildModel)
    private fun onItemClickBuild(view: View) {
        val menuPop = PopupMenu(activity, view)
        val menuHash = HashMap<String, PhoneModel>()
        Phones.Instance.phoneList.forEach {
            it.value.filter { oieBuildManufacturer.rightEditText.toString() == it.manufacturer }.forEach {
                menuHash.put(it.name!!, it)
                menuPop.menu.add(it.name)
            }
        }

        BottomSheetBuilder(activity, R.style.AppTheme_BottomSheetDialog)
                .setMode(BottomSheetBuilder.MODE_LIST)
                .expandOnStart(true)
                .setMenu(menuPop.menu)
                .setItemClickListener { item ->
                    oieBuildManufacturer.rightEditText = menuHash[item.title]?.manufacturer
                    oieBuildModel.rightEditText = menuHash[item.title]?.model
                }
                .createDialog()
                .show()
    }

    /**
     *
     */
    @Event(R.id.oieBuildSerial)
    private fun onItemClickBuildSerial(view: View) {
        val menuPop = PopupMenu(activity, view)
        menuPop.menu.add(R.string.random)
        BottomSheetBuilder(activity, R.style.AppTheme_BottomSheetDialog)
                .setMode(BottomSheetBuilder.MODE_LIST)
                .expandOnStart(true)
                .setMenu(menuPop.menu)
                .setItemClickListener { oieBuildSerial.rightEditText = random.buildSerial() }
                .createDialog()
                .show()
    }

    /**
     *
     */
    @Event(R.id.oieBuildVersionName)
    private fun onItemClickBuildVersionName(view: View) {
        val popupMenu = PopupMenu(activity, view)
        Random.ANDROID_VERSION.values().forEach { popupMenu.menu.add(it.versionName) }
        BottomSheetBuilder(activity, R.style.AppTheme_BottomSheetDialog)
                .setMode(BottomSheetBuilder.MODE_LIST)
                .expandOnStart(true)
                .setMenu(popupMenu.menu)
                .setItemClickListener { item -> oieBuildVersionName.rightEditText = Random.ANDROID_VERSION.get(item.title.toString()).versionName }
                .createDialog()
                .show()
    }

    /**
     *
     */
    @Event(R.id.oieAndroidId)
    private fun onItemClickAndroidId(view: View) {
        val popupMenu = PopupMenu(activity, view)
        popupMenu.menu.add(R.string.random)
        BottomSheetBuilder(activity, R.style.AppTheme_BottomSheetDialog)
                .setMode(BottomSheetBuilder.MODE_LIST)
                .expandOnStart(true)
                .setMenu(popupMenu.menu)
                .setItemClickListener { oieAndroidId.rightEditText = random.androidId() }
                .createDialog()
                .show()
    }

    /**
     *
     */
    @Event(R.id.oieSimLine1Number)
    private fun onItemClickSimLine1Number(view: View) {
        val popupMenu = PopupMenu(activity, view)
        popupMenu.menu.add(R.string.random)
        BottomSheetBuilder(activity, R.style.AppTheme_BottomSheetDialog)
                .setMode(BottomSheetBuilder.MODE_LIST)
                .expandOnStart(true)
                .setMenu(popupMenu.menu)
                .setItemClickListener { oieSimLine1Number.rightEditText = random.simLine1Number() }
                .createDialog()
                .show()
    }

    @Event(R.id.oieSimGetDeviceId)
    private fun onItemClickSimGetDeviceId(view: View) {
        val popupMenu = PopupMenu(activity, view)
        popupMenu.menu.add(R.string.random)
        BottomSheetBuilder(activity, R.style.AppTheme_BottomSheetDialog)
                .setMode(BottomSheetBuilder.MODE_LIST)
                .expandOnStart(true)
                .setMenu(popupMenu.menu)
                .setItemClickListener { oieSimGetDeviceId.rightEditText = random.simGetDeviceId() }
                .createDialog()
                .show()
    }

    @Event(R.id.oieSimOperator, R.id.oieSimOperatorName, R.id.oieSimSubscriberId, R.id.oieSimSerialNumber)
    private fun onItemClickSimOperator(view: View) {
        val popupMenu = PopupMenu(activity, view)
        Random.SIM_TYPE.values().forEach { popupMenu.menu.add(it.label) }
        BottomSheetBuilder(activity, R.style.AppTheme_BottomSheetDialog)
                .setMode(BottomSheetBuilder.MODE_LIST)
                .expandOnStart(true)
                .setMenu(popupMenu.menu)
                .setItemClickListener { item ->
                    oieSimSerialNumber.rightEditText = random.simSerialNumber(Random.SIM_TYPE.get(item.title.toString()))
                    oieSimSubscriberId.rightEditText = random.simSubscriberId(Random.SIM_TYPE.get(item.title.toString()))
                    oieSimOperator    .rightEditText = Random.SIM_TYPE.get(item.title.toString()).simCode
                    oieSimOperatorName.rightEditText = Random.SIM_TYPE.get(item.title.toString()).label
                    oieSimStatus      .rightEditText = TelephonyManager.SIM_STATE_READY.toString()
                }
                .createDialog()
                .show()
    }

    @Event(R.id.oieWifiName)
    private fun onItemClickWifiName(view: View) {
        val popupMenu = PopupMenu(activity, view)
        popupMenu.menu.add(R.string.random)
        BottomSheetBuilder(activity, R.style.AppTheme_BottomSheetDialog)
                .setMode(BottomSheetBuilder.MODE_LIST)
                .expandOnStart(true)
                .setMenu(popupMenu.menu)
                .setItemClickListener { oieWifiName.rightEditText = random.wifiName() }
                .createDialog()
                .show()
    }

    @Event(R.id.oieWifiMacAddress)
    private fun onItemClickWifiMacAddress(view: View) {
        val popupMenu = PopupMenu(activity, view)
        popupMenu.menu.add(R.string.random)
        BottomSheetBuilder(activity, R.style.AppTheme_BottomSheetDialog)
                .setMode(BottomSheetBuilder.MODE_LIST)
                .expandOnStart(true)
                .setMenu(popupMenu.menu)
                .setItemClickListener { oieWifiMacAddress.rightEditText = random.wifiMacAddress() }
                .createDialog()
                .show()
    }

    @Event(R.id.oieWifiBssid)
    private fun onItemClickWifiBssid(view: View) {
        val popupMenu = PopupMenu(activity, view)
        popupMenu.menu.add(R.string.random)
        BottomSheetBuilder(activity, R.style.AppTheme_BottomSheetDialog)
                .setMode(BottomSheetBuilder.MODE_LIST)
                .expandOnStart(true)
                .setMenu(popupMenu.menu)
                .setItemClickListener { oieWifiBssid.rightEditText = random.wifiMacAddress() }
                .createDialog()
                .show()
    }

    @Event(R.id.menu_random_all)
    private fun onItemClickRandomAll(view: View) {
        this.jsonObjectToUi(Random.New().randomAll())
    }

    private fun JSONObject.put(key: String, value: String, boolean: Boolean) {
        if (value.isEmpty() && boolean)
            this.remove(key)
        else
            this.put(key, value)
    }

}
