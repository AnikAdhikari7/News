package com.example.news

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley


class MainActivity : AppCompatActivity(), NewsItemClicked {

    private lateinit var mAdapter: NewsListAdapter
    lateinit var progressBar: ProgressBar

    private val githubRepo = "https://github.com/AnikAdhikari7/News.git"

    private val apiKey = "apiKey=d01edbc3c3b74d13bda7c02182817c26"
    private var url = "https://newsapi.org/v2/top-headlines?country=in&lang=en&sortBy=publishedAt&$apiKey"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // findViewById
        //val categoriesRecyclerView = findViewById<RecyclerView>(R.id.categoriesRecyclerView)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val search = findViewById<EditText>(R.id.etSearch)
        val searchButton = findViewById<Button>(R.id.btSearchButton)
        val homeButton = findViewById<ImageButton>(R.id.btHome)
        progressBar = findViewById(R.id.pbProgressBar)


        // actionbar
        val actionBar = supportActionBar
        actionBar!!.title = "News"
        actionBar.subtitle = "developed by: github.com/AnikAdhikari7"
        actionBar.setDisplayUseLogoEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)


        // api call function
        fetchData()
        mAdapter = NewsListAdapter(this)
        recyclerView.adapter = mAdapter

        // search input and search query
        val searchText = search.text

        searchButton.setOnClickListener {
            if (search.text.isEmpty()) {
                Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show()
            } else {
                url = "https://newsapi.org/v2/everything?q=$searchText&lang=en&sortBy=publishedAt&$apiKey"
                fetchData()
            }
        }

        homeButton.setOnClickListener {
            url = "https://newsapi.org/v2/top-headlines?country=in&lang=en&sortBy=publishedAt&$apiKey"
            fetchData()
        }
    }

    // menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // methods to control the operations that will
    // happen when user clicks on the action buttons
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.github_logo -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubRepo))
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    // api call
    private fun fetchData() {
        progressBar.visibility = View.VISIBLE

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val getRequest: JsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, url, null,
            {
                val newsJsonArray = it.getJSONArray("articles")
                val newsArray = ArrayList<News>()
                for (i in 0 until newsJsonArray.length()) {
                    val newsJsonObject = newsJsonArray.getJSONObject(i)
                    val news = News(
                        newsJsonObject.getString("title"),
                        newsJsonObject.getString("author"),
                        newsJsonObject.getString("url"),
                        newsJsonObject.getString("urlToImage"),
                        newsJsonObject.getJSONObject("source").getString("name")
                    )

                    newsArray.add(news)
                }

                mAdapter.updateNews(newsArray)
                progressBar.visibility = View.GONE
            },
            {
                Toast.makeText(this, "Something went wrong! Please re-open the App.", Toast.LENGTH_LONG).show()
                Log.d("Exception","Error loading data")
                progressBar.visibility = View.GONE
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["User-Agent"] = "Mozilla/5.0"
                return headers
            }
        }

        // Access the RequestQueue through your singleton class.
        queue.add(getRequest)
    }


    override fun onItemClicked(item: News) {
        val builder = CustomTabsIntent.Builder()


        val colorInt: Int = Color.parseColor("#FF3131") //bright red

        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(colorInt)
            .build()
        builder.setDefaultColorSchemeParams(defaultColors)


        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(item.url))
    }
}
