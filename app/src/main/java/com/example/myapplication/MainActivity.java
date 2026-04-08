package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvMovies;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        rvMovies = findViewById(R.id.rvMovies);
        Button btnLogout = findViewById(R.id.btnLogout);
        
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

        // Ép buộc kiểm tra và cập nhật dữ liệu 10 phim
        syncDataWithFirebase();
    }

    private void syncDataWithFirebase() {
        db.collection("movies").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() < 10) {
                    add10MoviesWithShowtimes();
                } else {
                    loadMoviesFromFirestore();
                }
            } else {
                Log.e("FirebaseError", "Lỗi đọc dữ liệu: " + task.getException().getMessage());
                Toast.makeText(this, "Không thể kết nối Firebase. Kiểm tra Rules!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void add10MoviesWithShowtimes() {
        WriteBatch batch = db.batch();
        Theater t1 = new Theater("t1", "CGV Vincom", "Hà Nội");
        batch.set(db.collection("theaters").document(t1.getId()), t1);

        List<Movie> sampleMovies = new ArrayList<>();
        sampleMovies.add(new Movie("m1", "Avengers: Endgame", "The grave course of events set in motion by Thanos...", "https://image.tmdb.org/t/p/w500/or06vSqzZkaunv9n3z9rVbsp9AR.jpg", 181, Arrays.asList("Action", "Sci-Fi")));
        sampleMovies.add(new Movie("m2", "Interstellar", "A team of explorers travel through a wormhole in space...", "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlSv2Vp.jpg", 169, Arrays.asList("Sci-Fi", "Drama")));
        sampleMovies.add(new Movie("m3", "Inception", "A thief who steals corporate secrets...", "https://image.tmdb.org/t/p/w500/edv5bs1R6oseS0zLbrjvhYv186L.jpg", 148, Arrays.asList("Action", "Sci-Fi")));
        sampleMovies.add(new Movie("m4", "The Dark Knight", "When the menace known as the Joker wreaks havoc...", "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDp9QmSbmzXwcB0h0nb.jpg", 152, Arrays.asList("Action", "Crime")));
        sampleMovies.add(new Movie("m5", "Spider-Man: No Way Home", "Peter asks Doctor Strange for help...", "https://image.tmdb.org/t/p/w500/1g0mSssv9vORvUqc5uo4zF1z9yc.jpg", 148, Arrays.asList("Action", "Adventure")));
        sampleMovies.add(new Movie("m6", "The Matrix", "A computer hacker learns the truth...", "https://image.tmdb.org/t/p/w500/f89U3Y9SJuCYFJjbbG77SFI19pW.jpg", 136, Arrays.asList("Action", "Sci-Fi")));
        sampleMovies.add(new Movie("m7", "Gladiator", "A former Roman General sets out for vengeance...", "https://image.tmdb.org/t/p/w500/ty8TGRpSxcwaIu61sWpWnZziOBJ.jpg", 155, Arrays.asList("Action", "Drama")));
        sampleMovies.add(new Movie("m8", "The Lion King", "A young lion prince is cast out...", "https://image.tmdb.org/t/p/w500/sKCr78HLS0vKS9YvYSetky0uUrm.jpg", 118, Arrays.asList("Animation", "Adventure")));
        sampleMovies.add(new Movie("m9", "Joker", "Mentally troubled comedian Arthur Fleck...", "https://image.tmdb.org/t/p/w500/udDclKVUZRE1q1u8CO0nuVrnq3z.jpg", 122, Arrays.asList("Crime", "Drama")));
        sampleMovies.add(new Movie("m10", "Avatar", "A paraplegic Marine dispatched to Pandora...", "https://image.tmdb.org/t/p/w500/jRXYjXN106UvneZOBJpqvU3YHgo.jpg", 162, Arrays.asList("Action", "Sci-Fi")));

        for (Movie m : sampleMovies) {
            batch.set(db.collection("movies").document(m.getId()), m);
            
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, 2);
            Showtime s1 = new Showtime(m.getId() + "_s1", m.getId(), t1.getId(), cal.getTime(), 120000);
            batch.set(db.collection("showtimes").document(s1.getId()), s1);
            
            cal.add(Calendar.HOUR, 3);
            Showtime s2 = new Showtime(m.getId() + "_s2", m.getId(), t1.getId(), cal.getTime(), 150000);
            batch.set(db.collection("showtimes").document(s2.getId()), s2);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(MainActivity.this, "Đã cập nhật 10 phim lên Cloud!", Toast.LENGTH_SHORT).show();
            loadMoviesFromFirestore();
        }).addOnFailureListener(e -> {
            Log.e("FirebaseError", "Lỗi khi lưu dữ liệu: " + e.getMessage());
            Toast.makeText(MainActivity.this, "Lỗi ghi dữ liệu! Hãy kiểm tra Rules trên Console.", Toast.LENGTH_LONG).show();
        });
    }

    private void loadMoviesFromFirestore() {
        db.collection("movies").get().addOnCompleteListener(task -> {
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