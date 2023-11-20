package com.bignerdranch.android.nerdlauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "NerdLauncherActivity"

class NerdLauncherActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nerd_launcher)

        recyclerView = findViewById(R.id.app_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        setupAdapter()
    }

    /**
     * Позднее эта функция создаст экземпляр RecyclerView.Adapter и назначит его объекту RecyclerView,
     * но пока она просто генерирует список данных приложения.)
     * Также создайте неявный интент и получите список activity, соответствующих интенту,
     * от PackageManager. Пока мы ограничимся простой регистрацией количества activity,
     * возвращенных PackageManager. */
    private fun setupAdapter() {
        /**
         * Здесь мы создаем неявный интент с заданным действием ACTION_MAIN.
         * Переменная CATEGORY_LAUNCHER добавлена в категории интента. */
        val startupIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val activities = packageManager.queryIntentActivities(startupIntent, 0)
        Log.i(TAG, "Found ${activities.size} activities")

        /** создаем экземпляр ActivityAdapter и назначаем его адаптером RecyclerView.*/
        recyclerView.adapter = ActivityAdapter(activities)

        /**Сначала отсортируйте объекты ResolveInfo, возвращаемые PackageManager,
         * в алфавитном порядке меток, получаемых функцией ResolveInfo.loadLabel (PackageManager).
         */
        activities.sortWith(Comparator { a, b ->
            String.CASE_INSENSITIVE_ORDER.compare(
                a.loadLabel(packageManager).toString(),
                b.loadLabel(packageManager).toString()
            )
        })
        /** отображение иконок приложений
         * ТУТ!!!!!
         */
        activities.forEach{
            it.loadIcon(packageManager)
        }
    }

    /**класс ViewHolder для отображения метки activity.
     * Сохраните объект ResolveInfo activity в переменной класса (позднее мы еще не раз используем его). */

    private class ActivityHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val nameTextView = itemView as TextView
        private lateinit var resolveInfo: ResolveInfo

        init {
            nameTextView.setOnClickListener(this)
        }

        fun bindActivity(resolveInfo: ResolveInfo) {
            this.resolveInfo = resolveInfo
            val packageManager = itemView.context.packageManager
            val appName = resolveInfo.loadLabel(packageManager).toString()
            nameTextView.text = appName


        }

        /**Реализуйте в ActivityHolder слушателя нажатий.
         *  При нажатии на activity в списке по данным ActivityInfo этой activity создайте явный интент.
         *  Затем используйте этот явный интент для запуска выбранной activity. */

        override fun onClick(view: View?) {
            val activityInfo = resolveInfo.activityInfo
            val intent = Intent(Intent.ACTION_MAIN).apply {
                /**мы получаем имя пакета и имя класса из метаданных и используем их
                 *  для создания явной activity функцией Intent: */
                setClassName(activityInfo.applicationInfo.packageName, activityInfo.name)
                /**добавление флага чтобы при запуске новой активити запускалась новая задача
                 */
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val context = view?.context
            context?.startActivity(intent)
        }


    }

    private class ActivityAdapter(val activities: List<ResolveInfo>) :
        RecyclerView.Adapter<ActivityHolder>() {
        override fun onCreateViewHolder(
            container: ViewGroup, viewType: Int
        ): ActivityHolder {
            val layoutInflater = LayoutInflater.from(container.context)
            val view = layoutInflater
                .inflate(android.R.layout.simple_list_item_1, container, false)
            return ActivityHolder(view)
        }

        override fun onBindViewHolder(holder: ActivityHolder, position: Int) {
            val resolveInfo =
                activities[position]
            holder.bindActivity(resolveInfo)
        }

        override fun getItemCount(): Int {
            return activities.size
        }
    }
}