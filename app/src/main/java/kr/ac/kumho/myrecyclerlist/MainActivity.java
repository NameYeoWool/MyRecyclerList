package kr.ac.kumho.myrecyclerlist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // protected TextView mTextView;
    public static final String QUEUE_TAG = "VolleyRequest";
    protected RequestQueue mQueue = null;
    JSONObject mResult = null;
    ArrayList<BookInfo> mList = new ArrayList<BookInfo>();
    protected BookAdapter mAdapter = new BookAdapter(mList);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mTextView = (TextView) findViewById(R.id.textView);

        RecyclerView r = (RecyclerView) findViewById(R.id.recyclerView);
        r.setAdapter(mAdapter);
        r.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        r.setItemAnimator(new DefaultItemAnimator());

        CookieHandler.setDefault(new CookieManager());

        mQueue = Volley.newRequestQueue(this);

        requestBooks();


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mQueue != null) {
            mQueue.cancelAll(QUEUE_TAG);
        }
    }


    protected void requestBooks() {
        String url = "https://www.googleapis.com/books/v1/volumes?q=maze@20runner";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mResult = response;
                        //mTextView.setText(response.toString());
                        drawList();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        request.setTag(QUEUE_TAG);
        mQueue.add(request);
    }

    public class BookInfo {
        String title;
        String authors;
        String isbn;

        public BookInfo(String title, String authors, String isbn) {
            this.title = title;
            this.authors = authors;
            this.isbn = isbn;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return authors;
        }

        public String getIsbn() {
            return isbn;
        }
    }

    public void drawList() {
        mList.clear();
        try {
            JSONArray items = mResult.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject info = ((JSONObject) items.get(i)).getJSONObject("volumeInfo");
                String title = info.getString("title");
                String author = info.optString("authors","none");
                JSONArray ids = info.getJSONArray("industryIdentifiers");
                String isbn = ((JSONObject) ids.get(0)).get("identifier").toString();
                Log.i("BookInfo", title + author + isbn);
                mList.add(new BookInfo(title, author, isbn));
            }

        } catch (JSONException | NullPointerException e) {
            Toast.makeText(getApplicationContext(), "Error " + e.toString(), Toast.LENGTH_LONG).show();
            mResult = null;

        }

        mAdapter.notifyDataSetChanged();
    }


    public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
        ArrayList<BookInfo> mArray = null;

        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener{


            //each dat item is just a string in this case
            public TextView textTitle;
            public TextView textAuthor;
            public TextView textIsbn;

            public ViewHolder(View root) {
                super(root);
                root.setOnClickListener(this);
                textTitle = (TextView) root.findViewById(R.id.textTitle);
                textAuthor = (TextView) root.findViewById(R.id.textAuthor);
                textIsbn = (TextView) root.findViewById(R.id.textISBN);
            }

            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),mArray.get(getAdapterPosition()).getTitle(),
                        Toast.LENGTH_SHORT).show();
            }
        }

        public BookAdapter(ArrayList<BookInfo> list) {
            mArray = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item, parent, false);
            return new ViewHolder(root);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textTitle.setText(mArray.get(position).getTitle());
            holder.textAuthor.setText(mArray.get(position).getAuthor());
            holder.textIsbn.setText(mArray.get(position).getIsbn());
        }



        @Override
        public int getItemCount() {
            return mArray.size();
        }
    }

}
