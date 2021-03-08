package me.kyuubiran.qqcleaner

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.kyuubiran.qqcleaner.data.hostInfo
import me.kyuubiran.qqcleaner.data.init
import me.kyuubiran.qqcleaner.hook.ModuleEntryHook
import me.kyuubiran.qqcleaner.utils.*
import me.kyuubiran.qqcleaner.utils.HookUtil.getMethod
import me.kyuubiran.qqcleaner.utils.HookUtil.hookAfter


private const val WE_CHAT_CLEANER_TAG = "WECHAT_CLEANER_TAG"
private var firstInit = false
var secondInitWeChat = false
    private set

class WeChatHookLoader(lpparam: XC_LoadPackage.LoadPackageParam) {
    init {
        doInit(lpparam.classLoader)
    }

    private fun initItem(classLoader: ClassLoader) {
        Utils(classLoader)
        ModuleEntryHook()
        ResInject.initForStubActivity()
        ResInject.injectModuleResources(hostInfo.application.resources)
        ConfigManager.checkConfigIsExists()
        CleanManager.AutoClean()
    }

    private fun doInit(rtLoader: ClassLoader) {
        if (firstInit) return
        try {
            "Landroid/app/Application;->attach(Landroid/content/Context;)V"
                .getMethod(rtLoader)
                .hookAfter {
                    if (secondInitWeChat) return@hookAfter
                    val ctx = it.thisObject as Application
                    init(ctx)
                    appContext = hostInfo.application
                    if ("true" == System.getProperty(WE_CHAT_CLEANER_TAG)) return@hookAfter
                    val classLoader = ctx.classLoader
                    System.setProperty(WE_CHAT_CLEANER_TAG, "true")
                    initItem(classLoader)
                    secondInitWeChat = true
                }
            firstInit = true
        } catch (e: Throwable) {
            if (e.toString().contains("com.google.android.webview")) return
            loge(e)
            throw e
        }
    }
}