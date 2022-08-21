package com.coolest.wallpaper.bot.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.coolest.wallpaper.bot.MyApplication
import com.coolest.wallpaper.bot.R
import com.coolest.wallpaper.bot.databinding.ActivityAboutAuthorBinding
import com.coolest.wallpaper.bot.utils.ViewUtils_Kotlin
import com.coolest.wallpaper.bot.utils.checkUpdate
import com.coolest.wallpaper.bot.utils.showAllRelease
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.thread


class AboutAuthor : AppCompatActivity() {

    private lateinit var binding: ActivityAboutAuthorBinding

    private var githubName = "CoolestEnoch"
    private var githubRepo = "WallpaperBot"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutAuthorBinding.inflate(layoutInflater)
        setContentView(binding.root)

//		com.coolest.wallpaper.bot.utils.setBingDailyWallpaper(binding.bgAbout,this)
        //显示返回按钮
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        binding.cardAuthor.setOnClickListener {
            Snackbar.make(window.decorView, "作者: Coolest Enoch", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            Toast.makeText(this, "作者: Coolest Enoch", Toast.LENGTH_SHORT).show()
            startActivity(Intent().apply {
                action = "android.intent.action.VIEW"
                data = Uri.parse("https://github.com/coolestenoch")
            })
        }

        //如果map里的key list里只有一个值, 那么就会被解析为github用户名并直接去github上查找这个人的信息
        //否则list格式为:{用户名:String, 个性签名:String, 头像URL, 网页URL}
        //空值不要用null, 请使用空字符串""代替

        //作者列表
        val list = mapOf(listOf("coolestenoch") to "作者", listOf("不稳定少年__忻莳", "", "https://i0.hdslb.com/bfs/face/91abd907a385ca9f1f2101551a7eeedb8eb7edec.jpg@240w_240h_1c_1s.webp", "https://space.bilibili.com/397538643") to "Icon & Others")
        processDeveloperInfo(list, binding.authorListView, null, this)

        //贡献者列表
        val list2 = mapOf(listOf("IceBear733") to "Idea provider")
        processDeveloperInfo(list2, binding.contributorListView, binding.contributorListTip, this)

        //下拉刷新检查更新
        binding.swipeRefresh.apply {
            setColorSchemeColors(
                    resources.getColor(R.color.green),
                    resources.getColor(R.color.blue),
                    resources.getColor(R.color.yellow),
                    resources.getColor(R.color.purple),
                    resources.getColor(R.color.orange),
                    resources.getColor(R.color.red),
                    resources.getColor(R.color.cyan)
            )
            setOnRefreshListener {
                checkUpdate(
                        packageManager.getPackageInfo(packageName, 0).versionName,
                        binding.swipeRefresh,
                        githubName,
                        githubRepo,
                        false
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_about, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.action_update -> {
                checkUpdate(
                        packageManager.getPackageInfo(packageName, 0).versionName,
                        binding.swipeRefresh,
                        githubName,
                        githubRepo,
                        false
                )
            }
            R.id.release_history -> {
                showAllRelease(
                        binding.swipeRefresh, "所有版本", githubName, githubRepo
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun processDeveloperInfo(
            userInfoMap: Map<List<String>, String>?,
            developersView: ViewGroup,
            tipView: TextView?,
            context: Context
    ) {
        thread {
            val tempViewList = LinkedList<MaterialCardView?>()
            if (userInfoMap != null) {
                for ((userInfo, description) in userInfoMap) {
                    if (userInfo.size == 1) {
                        val username = userInfo[0]

                        try {
                            //调用GitHub API获取json
                            var githubResponseJson: String? = ""
                            var avatar_url: String? = ""
                            var nickName: String? = ""
                            var bio: String? = ""
                            val client = OkHttpClient()
                            val request = Request.Builder()
                                    .url("https://api.github.com/users/$username")
                                    .build()
                            val response = client.newCall(request).execute()
                            githubResponseJson = response.body?.string()
                            Log.e("github", "$githubResponseJson")

                            //解析json获取图片地址
                            val responseJson = JSONObject(githubResponseJson)
                            avatar_url = "${responseJson.get("avatar_url")}"
                            nickName = "${responseJson.get("login")}"
                            bio = "${responseJson.get("bio")}"

                            //添加卡片
                            val textList = mutableListOf(nickName, bio)
                            if (!userInfoMap[listOf(username)].equals("")) {
                                textList.add("")
                                textList.add(description)
                            }
                            runOnUiThread {
                                tempViewList.add(
                                        ViewUtils_Kotlin.createBigButton(
                                                context,
                                                avatar_url,
                                                R.color.card_bg,
                                                textList,
                                                null, R.color.white
                                        ) {
                                            startActivity(Intent().apply {
                                                action = "android.intent.action.VIEW"
                                                data = Uri.parse("https://github.com/$username")
                                            })
                                        }
                                )
                            }
                            runOnUiThread {
                                developersView.removeAllViews()
                                for (view in tempViewList) {
                                    developersView.addView(view)
                                }
                            }
                        } catch (e: Exception) {
                            if (e.toString()
                                            .contains("Unable to resolve host \"api.github.com\"")
                            ) {
                                Snackbar.make(
                                        window.decorView,
                                        "无法连接到GitHub服务器, 请检查网络",
                                        Snackbar.LENGTH_LONG
                                ).show()
                            } else {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        val username = userInfo[0]
                        val bio = userInfo[1]
                        val aviatorUrl = userInfo[2]
                        val webPageUrl = userInfo[3]

                        try {
                            //添加卡片
                            val textList = mutableListOf(username, bio)
                            if (!userInfoMap[listOf(username)].equals("")) {
                                textList.add("")
                                textList.add(description)
                            }
                            runOnUiThread {
                                tempViewList.add(
                                        ViewUtils_Kotlin.createBigButton(
                                                context,
                                                aviatorUrl,
                                                R.color.card_bg,
                                                textList,
                                                null, R.color.white
                                        ) {
                                            when (TextUtils.isEmpty(webPageUrl)) {
                                                false -> startActivity(Intent().apply {
                                                    action = "android.intent.action.VIEW"
                                                    data = Uri.parse(webPageUrl)
                                                })
                                                true -> Unit
                                            }
                                        }
                                )
                            }
                            runOnUiThread {
                                developersView.removeAllViews()
                                for (view in tempViewList) {
                                    developersView.addView(view)
                                }
                            }
                        } catch (e: Exception) {
                            if (e.toString()
                                            .contains("Unable to resolve host \"api.github.com\"")
                            ) {
                                Snackbar.make(
                                        window.decorView,
                                        "无法连接到GitHub服务器, 请检查网络",
                                        Snackbar.LENGTH_LONG
                                ).show()
                            } else {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } else {
                tipView?.text = "暂无"
            }
        }
    }


}
