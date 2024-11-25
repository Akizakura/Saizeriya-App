package com.nyaa.saizeriya

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    private val MIN_FLICK_DISTANCE: Float = 100.0f
    private var downx: Float = 0f
    private var animated: Int = 0
    //注文かご
    private val order_box: MutableList<Order_Element> = mutableListOf()

    private var datamanager: dataManager? = null
    private var menu_data: JSONArray? = null
    private var loaded_history: MutableList<Order_Element>? = mutableListOf()
    private var recommend_data: JSONArray? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val data : MutableList<Order_Element> = mutableListOf(Order_Element(id = "1401", count = 1), Order_Element(id = "1401", count = 1))
//        datamanager.saveJsonData("history", data)

        //キャッシュ
        datamanager = dataManager(this)
        //メニューをロード
        menu_data = JSONObject(datamanager!!.loadJsonFromAssets("menu.json", assets)).getJSONArray("results")
        //履歴をロード
        loaded_history = datamanager!!.getJsonData("history", object : TypeToken<MutableList<Order_Element>>() {})

        recommend_data = menu_data!!.getRandomElements(8)

        //タイトル画面を表示
        setContentView(R.layout.main)
        val main_ImgView_logo = findViewById<ImageView>(R.id.ImgView_logo)
        val logo = getBitmapFromAsset("no-image.png")
        main_ImgView_logo.setImageBitmap(logo)

        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 500
        main_ImgView_logo.startAnimation(fadeIn)

        //その後、メインを表示
        Handler().postDelayed(Runnable {
            ShowActivityMain()
        }, 1000)
    }
    private fun getBitmapFromAsset(strName: String): Bitmap? {
        val assetManager = assets
        var istr: InputStream? = null
        try {
            istr = assetManager.open(strName)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return BitmapFactory.decodeStream(istr)
    }
    private fun dpToPx(dp: Int): Int {
        val metrics = getResources().getDisplayMetrics()
        return (dp * metrics.density).toInt()
    }

    private fun OrderBoxManagerAdd(button: Button, animation: Animation) {
        if (order_box.isEmpty()) {
            button.setBackgroundResource(R.drawable.order_box_rounded_corner)
            button.setTextColor(Color.WHITE)
            button.startAnimation(animation)
        }
    }
    @SuppressLint("ResourceAsColor")
    private fun OrderBoxManagerRemoved(button: Button) {
        if (order_box.isEmpty()) {
            button.setBackgroundResource(R.drawable.order_box_empty_rounded_corner)
            button.setTextColor(R.color.black_1)
        }
    }

    @SuppressLint("ClickableViewAccessibility", "MissingInflatedId")
    private fun ShowExploreAll(main_container: LinearLayout, order_box_button: Button, order_box_animation: Animation) {
        val includedLayout_category = layoutInflater.inflate(R.layout.category_all, main_container, false)
        main_container.addView(includedLayout_category)

        //よく見るメニューの設定
        if (loaded_history == null) {
            val history_contaienr = includedLayout_category.findViewById<LinearLayout>(R.id.history_container)
            //よく見るメニューに何もないカードを追加
            val includedLayout_history_layout_null = layoutInflater.inflate(R.layout.history_layout_null, history_contaienr, false)
            history_contaienr.addView(includedLayout_history_layout_null, 0)
        }
        else {
            val history_layout = includedLayout_category.findViewById<LinearLayout>(R.id.history_layout)

            var count = 0
            loaded_history!!.forEach{
                //すべてのカテゴリ内のよく見るメニューにカードを追加
                val includedLayout_history_layout = layoutInflater.inflate(R.layout.history_layout, history_layout, false)
                //カードの設定
                val includedLayout_history_layout_img = includedLayout_history_layout.findViewById<ImageView>(R.id.history_img)
                val includedLayout_history_layout_title = includedLayout_history_layout.findViewById<TextView>(R.id.history_title)
                val includedLayout_history_layout_id = includedLayout_history_layout.findViewById<TextView>(R.id.history_id)
                val includedLayout_history_layout_price = includedLayout_history_layout.findViewById<TextView>(R.id.history_price)
                val includedLayout_history_layout_button_add = includedLayout_history_layout.findViewById<Button>(R.id.history_add_button)
                val includedLayout_history_layout_count = includedLayout_history_layout.findViewById<TextView>(R.id.history_orderbox_count)
                val includedLayout_history_layout_button_minus = includedLayout_history_layout.findViewById<Button>(R.id.history_minus_button)

                val data = menu_data!!.findElementById("id", it.id)
                if (data != null) {
                    val str_img = data.getString("image")
                    val str_title = data.getString("title")
                    val str_category = data.getString("category")
                    val str_price = data.getString("price")
                    val img = getBitmapFromAsset("menu/" + str_category + "/" + str_img)
                    includedLayout_history_layout_img.setImageBitmap(img)
                    includedLayout_history_layout_title.text = str_title.take(8)
                    includedLayout_history_layout_id.text = it.id
                    includedLayout_history_layout_price.text = "¥$str_price"
                    history_layout.addView(includedLayout_history_layout)

                    val order_element = order_box.findElementById(it.id)
                    if (order_element != null) {
                        includedLayout_history_layout_count.setText(order_element.count.toString())
                    }

                    MenuCardButton(includedLayout_history_layout_button_add, includedLayout_history_layout_count, includedLayout_history_layout_button_minus, it.id, order_box_button, order_box_animation)

                    if (count > 6) {
                        return@forEach
                    }
                    count += 1
                }
            }
        }

        val recommend_container = findViewById<LinearLayout>(R.id.recommend_container)

        for (i in 0 until recommend_data!!.length()) {
            val str_img = recommend_data!!.getJSONObject(i).getString("image")
            val str_title = recommend_data!!.getJSONObject(i).getString("title")
            val str_id = recommend_data!!.getJSONObject(i).getString("id")
            val str_category = recommend_data!!.getJSONObject(i).getString("category")
            val str_price = recommend_data!!.getJSONObject(i).getString("price")

            val includedLayout_menu_card_layout = layoutInflater.inflate(R.layout.menu_card_layout, main_container, false)
            val includedLayout_menu_card_add_button = includedLayout_menu_card_layout.findViewById<Button>(R.id.menu_card_add_button)
            val includedLayout_menu_card_count = includedLayout_menu_card_layout.findViewById<TextView>(R.id.menu_card_count)
            val includedLayout_menu_card_minus_button = includedLayout_menu_card_layout.findViewById<Button>(R.id.menu_card_minus_button)

            MenuCardSetting(recommend_container, includedLayout_menu_card_layout, str_img, str_title, str_id, str_category, str_price)

            val order_element = order_box.findElementById(str_id)
            if (order_element != null) {
                includedLayout_menu_card_count.setText(order_element.count.toString())
            }

            MenuCardButton(includedLayout_menu_card_add_button, includedLayout_menu_card_count, includedLayout_menu_card_minus_button, str_id, order_box_button, order_box_animation)
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun MenuCardButton(includedLayout_history_layout_button_add: Button, includedLayout_history_layout_count: TextView, includedLayout_history_layout_button_minus: Button, str_id: String, order_box_button: Button, order_box_animation: Animation) {
        includedLayout_history_layout_button_add.setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    includedLayout_history_layout_button_add.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_down))
                    false
                }
                MotionEvent.ACTION_UP -> {
                    includedLayout_history_layout_button_add.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))

                    val order_element = order_box.findElementById(str_id)
                    val order_element_index = order_box.getIndexById(str_id)
                    if (order_element == null) {
                        OrderBoxManagerAdd(order_box_button, order_box_animation)
                        order_box.add(Order_Element(id = str_id, count = 1))
                        includedLayout_history_layout_count.setText("1")
                    }
                    else {
                        includedLayout_history_layout_button_add.startAnimation(
                            AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))
                        order_box[order_element_index].id = order_element.id
                        order_box[order_element_index].count = order_element.count + 1
                        includedLayout_history_layout_count.setText((order_element.count).toString())
                    }
                    false
                }
                MotionEvent.ACTION_CANCEL -> {
                    includedLayout_history_layout_button_add.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))
                    false
                }
                else -> false
            }
        }
        includedLayout_history_layout_button_minus.setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    includedLayout_history_layout_button_minus.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_down))
                    false
                }
                MotionEvent.ACTION_UP -> {
                    includedLayout_history_layout_button_minus.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))

                    val order_element = order_box.findElementById(str_id)
                    val order_element_index = order_box.getIndexById(str_id)
                    if (order_element != null) {
                        if (order_element.count == 1) {
                            order_box.removeAt(order_element_index)
                            OrderBoxManagerRemoved(order_box_button)
                            includedLayout_history_layout_count.setText("0")
                        }
                        else {
                            includedLayout_history_layout_button_minus.startAnimation(
                                AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))
                            order_box[order_element_index].id = order_element.id
                            order_box[order_element_index].count = order_element.count - 1
                            includedLayout_history_layout_count.setText((order_element.count).toString())
                        }
                    }
                    false
                }
                MotionEvent.ACTION_CANCEL -> {
                    includedLayout_history_layout_button_minus.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))
                    false
                }
                else -> false
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun ShowCategoryCard(category_data: JSONArray, main_container: LinearLayout, scrollView: ScrollView, order_box_button: Button, order_box_animation: Animation) {
        for (i in 0 until category_data.length()) {
            val str_img = category_data.getJSONObject(i).getString("image")
            val str_title = category_data.getJSONObject(i).getString("title")
            val str_id = category_data.getJSONObject(i).getString("id")
            val str_category = category_data.getJSONObject(i).getString("category")
            val str_price = category_data.getJSONObject(i).getString("price")


            val includedLayout_menu_card_layout = layoutInflater.inflate(R.layout.menu_card_layout, main_container, false)
            val includedLayout_menu_card_add_button = includedLayout_menu_card_layout.findViewById<Button>(R.id.menu_card_add_button)
            val includedLayout_menu_card_count = includedLayout_menu_card_layout.findViewById<TextView>(R.id.menu_card_count)
            val includedLayout_menu_card_minus_button = includedLayout_menu_card_layout.findViewById<Button>(R.id.menu_card_minus_button)

            MenuCardSetting(main_container, includedLayout_menu_card_layout, str_img, str_title, str_id, str_category, str_price)

            val order_element = order_box.findElementById(str_id)
            if (order_element != null) {
                includedLayout_menu_card_count.setText(order_element.count.toString())
            }

            MenuCardButton(includedLayout_menu_card_add_button, includedLayout_menu_card_count, includedLayout_menu_card_minus_button, str_id, order_box_button, order_box_animation)
        }
        val includedLayout_menu_card_last_layout = layoutInflater.inflate(R.layout.menu_card_last_layout, main_container, false)
        val includedLayout_menu_card_last_button = includedLayout_menu_card_last_layout.findViewById<Button>(R.id.menu_card_last_button)
        main_container.addView(includedLayout_menu_card_last_layout)

        includedLayout_menu_card_last_button.setOnTouchListener { v: View, event: MotionEvent ->
            when(event.action) {
                MotionEvent.ACTION_UP -> {
                    scrollView.scrollTo(0, 0)
                    false
                }
                else -> false
            }
        }
    }
    private fun MenuCardSetting(container: LinearLayout, inflater: View, str_img: String, str_title: String, str_id: String, str_category: String, str_price: String) {
        val includedLayout_menu_card_img = inflater.findViewById<ImageView>(R.id.menu_card_img)
        val includedLayout_menu_card_title = inflater.findViewById<TextView>(R.id.menu_card_title)
        val includedLayout_menu_card_id = inflater.findViewById<TextView>(R.id.menu_card_id)
        val includedLayout_menu_card_price = inflater.findViewById<TextView>(R.id.menu_card_price)

        val img = getBitmapFromAsset("menu/" + str_category + "/" + str_img)

        includedLayout_menu_card_img.setImageBitmap(img)
        includedLayout_menu_card_title.text = str_title
        includedLayout_menu_card_id.text = str_id
        includedLayout_menu_card_price.text = "¥$str_price"
        container.addView(inflater)
    }
    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor")
    private fun ShowActivityMain() {
        setContentView(R.layout.activity_main)

        //注文かごのボタン
        val order_box_button = findViewById<Button>(R.id.menu_card_last_button)
        val order_box_animation = AnimationUtils.loadAnimation(this, R.anim.anim_order_box_easing)
        order_box_animation.interpolator = Easing.EaseOutElasticInterpolator()

        if (!order_box.isEmpty()) {
            order_box_button.setBackgroundResource(R.drawable.order_box_rounded_corner)
            order_box_button.setTextColor(Color.WHITE)
        }
        else {
            order_box_button.setBackgroundResource(R.drawable.order_box_empty_rounded_corner)
            order_box_button.setTextColor(R.color.black_1)
        }

        order_box_button.setOnTouchListener { v: View, event: MotionEvent ->
            when(event.action) {
                MotionEvent.ACTION_UP -> {
                    setContentView(R.layout.order_box)
                    val order_box_back_button = findViewById<Button>(R.id.order_box_back_button)
                    val order_box_preset_button = findViewById<Button>(R.id.order_box_preset_button)
                    val order_box_order_button = findViewById<Button>(R.id.order_box_order_button)

                    order_box_back_button.setOnTouchListener{v: View, event: MotionEvent ->
                        when(event.action) {
                            MotionEvent.ACTION_UP -> {
                                ShowActivityMain()
                                false
                            }
                            else -> false
                        }
                    }

                    ShowOrderBoxContainer(order_box_button, order_box_animation)
                    false
                }
                else -> false
            }
        }

        //カテゴリ、すべてを表示(デフォルト)
        val main_container = findViewById<LinearLayout>(R.id.main_container)

        //スクロール
        val scrollView = findViewById<ScrollView>(R.id.scrollView)

        ShowExploreAll(main_container, order_box_button, order_box_animation)


        val explore_button_list: List<Button> = listOf(
            findViewById<Button>(R.id.explore_all_button),
            findViewById<Button>(R.id.explore_appetizer_button),
            findViewById<Button>(R.id.explore_salad_button),
            findViewById<Button>(R.id.explore_soup_button),
            findViewById<Button>(R.id.explore_pizza_button),
            findViewById<Button>(R.id.explore_doria_button),
            findViewById<Button>(R.id.explore_pasta_button),
            findViewById<Button>(R.id.explore_hamburg_button),
            findViewById<Button>(R.id.explore_chicken_button),
            findViewById<Button>(R.id.explore_dessert_button),
            findViewById<Button>(R.id.explore_side_button),
            findViewById<Button>(R.id.explore_drink_button)
        )
        val explore_view_list: List<View> = listOf(
            findViewById<View>(R.id.explore_all_view),
            findViewById<View>(R.id.explore_appetizer_view),
            findViewById<View>(R.id.explore_salad_view),
            findViewById<View>(R.id.explore_soup_view),
            findViewById<View>(R.id.explore_pizza_view),
            findViewById<View>(R.id.explore_doria_view),
            findViewById<View>(R.id.explore_pasta_view),
            findViewById<View>(R.id.explore_hamburg_view),
            findViewById<View>(R.id.explore_chicken_view),
            findViewById<View>(R.id.explore_dessert_view),
            findViewById<View>(R.id.explore_side_view),
            findViewById<View>(R.id.explore_drink_view),)


        explore_button_list[0].setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action){
                MotionEvent.ACTION_UP -> {
                    main_container.removeAllViews()
                    ExploreViewVisibility(explore_view_list, 0)
                    ExploreButtonTextColor(explore_button_list, 0)
                    ShowExploreAll(main_container, order_box_button, order_box_animation)
                    false
                }
                else -> false
            }
        }
        for (i in 1 until explore_button_list.size) {
            explore_button_list[i].setOnTouchListener { v: View, event: MotionEvent ->
                when(event.action) {
                    MotionEvent.ACTION_UP -> {
                        main_container.removeAllViews()
                        ExploreViewVisibility(explore_view_list, i)
                        ExploreButtonTextColor(explore_button_list, i)
                        val category_data = menu_data!!.findAllElementById("category", String.format("%04d", i + 2))
                        if (category_data != null) {
                            ShowCategoryCard(category_data, main_container, scrollView, order_box_button, order_box_animation)
                        }
                        false
                    }
                    else -> false
                }
            }
        }

        val setting_button = findViewById<Button>(R.id.setting_button)
        setting_button.setOnTouchListener { v: View, event: MotionEvent ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    setting_button.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_down))
                    false
                }
                MotionEvent.ACTION_UP -> {
                    setting_button.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))
                    setContentView(R.layout.setting)
                    val setting_back_button = findViewById<Button>(R.id.setting_back_button)
                    setting_back_button.setOnTouchListener{v: View, event: MotionEvent ->
                        when(event.action) {
                            MotionEvent.ACTION_UP -> {
                                ShowActivityMain()
                                false
                            }
                            else -> false
                        }
                    }
                    val remove_history_button = findViewById<Button>(R.id.remove_history_button)
                    remove_history_button.setOnTouchListener { v: View, event: MotionEvent ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                remove_history_button.startAnimation(
                                    AnimationUtils.loadAnimation(
                                        this,
                                        R.anim.anim_pressed_button_down
                                    )
                                )
                                false
                            }

                            MotionEvent.ACTION_UP -> {
                                remove_history_button.startAnimation(
                                    AnimationUtils.loadAnimation(
                                        this,
                                        R.anim.anim_pressed_button_up
                                    )
                                )
                                var builder = AlertDialog.Builder(this)
                                builder.setTitle(R.string.remove_history)
                                builder.setMessage(R.string.alert_message)
                                builder.setPositiveButton(R.string.yes) { dialog, which ->
                                    val data: List<Order_Element> = listOf()
                                    datamanager!!.saveJsonData("history", data)
                                    loaded_history = null
                                }
                                builder.setNegativeButton(R.string.no) { dialog, which -> }
                                builder.show()
                                false
                            }

                            MotionEvent.ACTION_CANCEL -> {
                                remove_history_button.startAnimation(
                                    AnimationUtils.loadAnimation(
                                        this,
                                        R.anim.anim_pressed_button_up
                                    )
                                )
                                false
                            }

                            else -> false
                        }
                    }
                    false
                }
                MotionEvent.ACTION_CANCEL -> {
                    setting_button.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))
                    false
                }
                else -> false
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun ShowOrderBoxContainer(order_box_button: Button, order_box_animation: Animation) {
        val order_box_container = findViewById<LinearLayout>(R.id.order_box_container)
        order_box_container.removeAllViews()
        order_box.forEach{
            val includedLayout_menu_card_layout = layoutInflater.inflate(R.layout.menu_card_layout, order_box_container, false)
            val includedLayout_menu_card_add_button = includedLayout_menu_card_layout.findViewById<Button>(R.id.menu_card_add_button)
            val includedLayout_menu_card_count = includedLayout_menu_card_layout.findViewById<TextView>(R.id.menu_card_count)
            val includedLayout_menu_card_minus_button = includedLayout_menu_card_layout.findViewById<Button>(R.id.menu_card_minus_button)

            val data = menu_data!!.findElementById("id", it.id)
            if (data != null) {
                MenuCardSetting(order_box_container, includedLayout_menu_card_layout, data.getString("image"), data.getString("title"), data.getString("id"), data.getString("category"), data.getString("price"))
                val order_element = order_box.findElementById(data.getString("id"))
                if (order_element != null) {
                    includedLayout_menu_card_count.setText(order_element.count.toString())
                }
                val str_id = data.getString("id")
                MenuCardButton(includedLayout_menu_card_add_button, includedLayout_menu_card_count, includedLayout_menu_card_minus_button, str_id, order_box_button,order_box_animation)
                includedLayout_menu_card_minus_button.setOnTouchListener { v: View, event: MotionEvent ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            includedLayout_menu_card_minus_button.startAnimation(
                                AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_down))
                            false
                        }
                        MotionEvent.ACTION_UP -> {
                            includedLayout_menu_card_minus_button.startAnimation(
                                AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))

                            val order_element = order_box.findElementById(str_id)
                            val order_element_index = order_box.getIndexById(str_id)
                            if (order_element != null) {
                                if (order_element.count == 1) {
                                    order_box.removeAt(order_element_index)
                                    OrderBoxManagerRemoved(order_box_button)
                                    includedLayout_menu_card_count.setText("0")
                                    ShowOrderBoxContainer(order_box_button, order_box_animation)
                                }
                                else {
                                    includedLayout_menu_card_minus_button.startAnimation(
                                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))
                                    order_box[order_element_index].id = order_element.id
                                    order_box[order_element_index].count = order_element.count - 1
                                    includedLayout_menu_card_count.setText((order_element.count).toString())
                                }
                            }
                            false
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            includedLayout_menu_card_minus_button.startAnimation(
                                AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))
                            false
                        }
                        else -> false
                    }
                }
            }
        }

        val order_box_order_button = findViewById<Button>(R.id.order_box_order_button)
        order_box_order_button.setOnTouchListener { v: View, event: MotionEvent ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    order_box_order_button.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_down))
                    false
                }
                MotionEvent.ACTION_UP -> {
                    order_box_order_button.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))
                    setContentView(R.layout.order)
                    val order_back_button = findViewById<Button>(R.id.preset_load_button)
                    order_back_button.setOnTouchListener{v: View, event: MotionEvent ->
                        when(event.action) {
                            MotionEvent.ACTION_UP -> {
                                ShowActivityMain()
                                false
                            }
                            else -> false
                        }
                    }
                    val order_list_container = findViewById<LinearLayout>(R.id.preset_load_container)
                    val includedLayout_order_list_item = layoutInflater.inflate(R.layout.order_item, order_list_container, false)
                    val includedLayout_order_list_item_id = includedLayout_order_list_item.findViewById<TextView>(R.id.preset_item_text)
                    val includedLayout_order_list_item_count = includedLayout_order_list_item.findViewById<TextView>(R.id.order_item_text_count)
                    includedLayout_order_list_item_id.setText(R.string.id)
                    includedLayout_order_list_item_count.setText(R.string.count)
                    order_list_container.addView(includedLayout_order_list_item)

                    order_box.forEach {
                        val _includedLayout_order_list_item = layoutInflater.inflate(R.layout.order_item, order_list_container, false)
                        val _includedLayout_order_list_item_id = _includedLayout_order_list_item.findViewById<TextView>(R.id.preset_item_text)
                        val _includedLayout_order_list_item_count = _includedLayout_order_list_item.findViewById<TextView>(R.id.order_item_text_count)
                        _includedLayout_order_list_item_id.setText(it.id)
                        _includedLayout_order_list_item_count.setText(it.count.toString())
                        order_list_container.addView(_includedLayout_order_list_item)
                    }
                    val delete_order_button = findViewById<Button>(R.id.delete_order_button)
                    delete_order_button.setOnTouchListener{v: View, event: MotionEvent ->
                        when(event.action) {
                            MotionEvent.ACTION_UP -> {
                                var builder = AlertDialog.Builder(this)
                                builder.setTitle(R.string.reset_des)
                                builder.setMessage(R.string.alert_message)
                                builder.setPositiveButton(R.string.yes) { dialog, which ->
                                    var save_history: MutableList<Order_Element> = mutableListOf()
                                    order_box.forEach{
                                        if (loaded_history != null) {
                                            save_history = loaded_history!!
                                            val data = save_history.findElementById(it.id)
                                            if (data != null) {
                                                val count = data.count
                                                save_history.remove(data)
                                                save_history.add(Order_Element(id = it.id, count = count + 1))
                                            }
                                            else {
                                                save_history.add(Order_Element(id = it.id, count = 1))
                                            }
                                        }
                                        else {
                                            save_history.add(Order_Element(id = it.id, count = 1))
                                        }
                                    }
                                    order_box.clear()
                                    datamanager!!.saveJsonData("history", save_history)
                                    loaded_history = datamanager!!.getJsonData("history", object : TypeToken<MutableList<Order_Element>>() {})
                                    ShowActivityMain()
                                }
                                builder.setNegativeButton(R.string.no) { dialog, which -> }
                                builder.show()
                                false
                            }
                            else -> false
                        }
                    }
                    false
                }
                MotionEvent.ACTION_CANCEL -> {
                    order_box_order_button.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))
                    false
                }
                else -> false
            }
        }

        val order_box_preset_button = findViewById<Button>(R.id.order_box_preset_button)
        val load_preset_button = findViewById<Button>(R.id.load_preset_button)
        val save_preset_button = findViewById<Button>(R.id.save_preset_button)
        order_box_preset_button.setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    order_box_preset_button.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_down))
                    false
                }
                MotionEvent.ACTION_UP -> {
                    order_box_preset_button.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))
                    if (load_preset_button.visibility != View.VISIBLE) {
                        load_preset_button.visibility = View.VISIBLE
                        save_preset_button.visibility = View.VISIBLE
                        val animation = AnimationUtils.loadAnimation(this, R.anim.anim_order_box_preset)
                        animation.interpolator = Easing.EaseOutElasticInterpolator()
                        load_preset_button.startAnimation(animation)
                        save_preset_button.startAnimation(animation)
                    }
                    else {
                        load_preset_button.visibility = View.INVISIBLE
                        save_preset_button.visibility = View.INVISIBLE
                    }
                    false
                }
                MotionEvent.ACTION_CANCEL -> {
                    order_box_preset_button.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.anim_pressed_button_up))
                    false
                }
                else -> false
            }
        }
        load_preset_button.setOnTouchListener {v: View, event: MotionEvent ->
            when(event.action) {
                MotionEvent.ACTION_UP -> {
                    setContentView(R.layout.preset_load)
                    val preset_load_container = findViewById<LinearLayout>(R.id.preset_load_container)
                    val preset_list = datamanager!!.getJsonData("preset", object : TypeToken<MutableList<Preset_Element>>() {})
                    if (preset_list != null) {
                        preset_list.forEach{
                            val includedLayout_preset_item = layoutInflater.inflate(R.layout.preset_item, preset_load_container, false)
                            val includedLayout_preset_text = includedLayout_preset_item.findViewById<TextView>(R.id.preset_item_text)
                            val includedLayout_preset_button = includedLayout_preset_item.findViewById<Button>(R.id.preset_load_button)
                            val includedLayout_preset_delete_button = includedLayout_preset_item.findViewById<Button>(R.id.preset_delete_button)
                            includedLayout_preset_text.text = it.date

                            includedLayout_preset_button.setOnTouchListener {v:View, event: MotionEvent ->
                                when(event.action) {
                                    MotionEvent.ACTION_UP -> {
                                        order_box.addAll(it.items)
                                        ShowActivityMain()
                                        false
                                    }
                                    else -> false
                                }
                            }
                            includedLayout_preset_delete_button.setOnTouchListener {v:View, event: MotionEvent ->
                                when(event.action) {
                                    MotionEvent.ACTION_UP -> {
                                        var builder = AlertDialog.Builder(this)
                                        builder.setTitle(R.string.preset)
                                        builder.setMessage(R.string.D_load)
                                        builder.setPositiveButton(R.string.yes) { dialog, which ->
                                            val preset_list = datamanager!!.getJsonData("preset", object : TypeToken<MutableList<Preset_Element>>() {})
                                            if (preset_list != null) {
                                                val data = preset_list.findElementById(it.date)
                                                preset_list.remove(data)
                                                datamanager!!.saveJsonData("preset", preset_list)
                                            }
                                            ShowActivityMain()
                                        }
                                        builder.setNegativeButton(R.string.no) { dialog, which -> }
                                        builder.show()
                                        false
                                    }
                                    else -> false
                                }
                            }
                            preset_load_container.addView(includedLayout_preset_item)
                        }
                    }
                    val preset_load_button = findViewById<Button>(R.id.preset_load_button)
                    preset_load_button.setOnTouchListener{v: View, event: MotionEvent ->
                        when(event.action) {
                            MotionEvent.ACTION_UP -> {
                                ShowActivityMain()
                                false
                            }
                            else -> false
                        }
                    }
                    false
                }
                else -> false
            }
        }
        save_preset_button.setOnTouchListener{v:View, event: MotionEvent ->
            when(event.action) {
                MotionEvent.ACTION_UP -> {
                    var builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.preset)
                    builder.setMessage(R.string.Q_save)
                    builder.setPositiveButton(R.string.yes) { dialog, which ->
                        val now = LocalDateTime.now()
                        val formattedDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))
                        val preset_list = datamanager!!.getJsonData("preset", object : TypeToken<MutableList<Preset_Element>>() {})
                        if (preset_list != null) {
                            preset_list.add(Preset_Element(date = formattedDateTime, items = order_box))
                            datamanager!!.saveJsonData("preset", preset_list)
                        }
                        else {
                            val data: List<Preset_Element> = listOf(Preset_Element(date = formattedDateTime, items = order_box))
                            datamanager!!.saveJsonData("preset", data)
                        }
                    }
                    builder.setNegativeButton(R.string.no) { dialog, which -> }
                    builder.show()
                    false
                }
                else -> false
            }
        }
    }
    private fun ExploreViewVisibility(explore_view_list: List<View>, index: Int) {
        explore_view_list.forEach{
            it.visibility = View.INVISIBLE
        }
        explore_view_list[index].visibility = View.VISIBLE
    }
    @SuppressLint("ResourceAsColor")
    private fun ExploreButtonTextColor(explore_button_list: List<Button>, index: Int) {
        explore_button_list.forEach {
            it.setTextColor(R.color.black_1)
        }
        explore_button_list[index].setTextColor(Color.BLACK)
    }
}