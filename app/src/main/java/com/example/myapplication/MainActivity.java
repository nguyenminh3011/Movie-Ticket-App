package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.MovieAdapter;
import com.example.myapplication.models.Movie;
import com.example.myapplication.models.Showtime;
import com.example.myapplication.models.Theater;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvMovies;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private FirebaseFirestore db;
    private Button btnLogout, btnAddData;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        rvMovies = findViewById(R.id.rvMovies);
        btnLogout = findViewById(R.id.btnLogout);
        btnAddData = new Button(this); // Nút tạm để thêm dữ liệu
        
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(movieList, movie -> {
            Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);
            intent.putExtra("movieId", movie.getId());
            startActivity(intent);
        });
        rvMovies.setAdapter(movieAdapter);

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        loadMovies();
        
        // Kiểm tra nếu chưa có phim thì thêm dữ liệu mẫu (chỉ chạy 1 lần)
        db.collection("movies").limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                addSampleData();
            }
        });
    }

    private void addSampleData() {
        // 1. Thêm Phim
        Movie m1 = new Movie("m1", "Avengers: Endgame", "Sau các sự kiện tàn khốc của Avengers: Infinity War...", "https://image.tmdb.org/t/p/w500/or06vSqzZkaunv9n3z9rVbsp9AR.jpg", 181, Arrays.asList("Action", "Sci-Fi"));
        Movie m2 = new Movie("m2", "Interstellar", "Một nhóm các nhà thám hiểm du hành xuyên không gian...", "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlSv2Vp.jpg", 169, Arrays.asList("Sci-Fi", "Drama"));
        
        db.collection("movies").document(m1.getId()).set(m1);
        db.collection("movies").document(m2.getId()).set(m2);

        // 2. Thêm Rạp
        Theater t1 = new Theater("t1", "CGV Vincom", "Hà Nội");
        db.collection("theaters").document(t1.getId()).set(t1);

        // 3. Thêm Lịch chiếu (Showtimes)
        Showtime s1 = new Showtime("s1", "m1", "t1", new Date(), 100000);
        db.collection("showtimes").document(s1.getId()).set(s1);

        Toast.makeText(this, "Đã thêm dữ liệu mẫu!", Toast.LENGTH_SHORT).show();
        loadMovies();
    }

    private void loadMovies() {
        db.collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        movieList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Movie movie = document.toObject(Movie.class);
                            movie.setId(document.getId());
                            movieList.add(movie);
                        }
                        movieAdapter.notifyDataSetChanged();
                    }
                });
    }
}