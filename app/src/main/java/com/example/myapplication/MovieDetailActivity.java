package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.models.Movie;
import com.example.myapplication.models.Showtime;
import com.example.myapplication.models.Ticket;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView ivPoster;
    private TextView tvTitle, tvGenre, tvDescription;
    private Spinner spShowtimes;
    private EditText etSeat;
    private Button btnBook;
    
    private FirebaseFirestore db;
    private String movieId;
    private List<Showtime> showtimeList;
    private List<String> showtimeLabels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        db = FirebaseFirestore.getInstance();
        movieId = getIntent().getStringExtra("movieId");

        ivPoster = findViewById(R.id.ivDetailPoster);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvGenre = findViewById(R.id.tvDetailGenre);
        tvDescription = findViewById(R.id.tvDetailDescription);
        spShowtimes = findViewById(R.id.spShowtimes);
        etSeat = findViewById(R.id.etSeatNumber);
        btnBook = findViewById(R.id.btnBookTicket);

        showtimeList = new ArrayList<>();
        showtimeLabels = new ArrayList<>();

        loadMovieInfo();
        loadShowtimes();

        btnBook.setOnClickListener(v -> bookTicket());
    }

    private void loadMovieInfo() {
        db.collection("movies").document(movieId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Movie movie = documentSnapshot.toObject(Movie.class);
                    if (movie != null) {
                        tvTitle.setText(movie.getTitle());
                        tvGenre.setText(movie.getGenres() != null ? String.join(", ", movie.getGenres()) : "");
                        tvDescription.setText(movie.getDescription());
                        Glide.with(this).load(movie.getPosterUrl()).into(ivPoster);
                    }
                });
    }

    private void loadShowtimes() {
        db.collection("showtimes")
                .whereEqualTo("movieId", movieId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showtimeList.clear();
                        showtimeLabels.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Showtime st = doc.toObject(Showtime.class);
                            st.setId(doc.getId());
                            showtimeList.add(st);
                            showtimeLabels.add(st.getStartTime().toString() + " - " + st.getPrice() + " VND");
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, showtimeLabels);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spShowtimes.setAdapter(adapter);
                    }
                });
    }

    private void bookTicket() {
        if (showtimeList.isEmpty()) {
            Toast.makeText(this, "No showtime selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String seat = etSeat.getText().toString().trim();
        if (seat.isEmpty()) {
            etSeat.setError("Please enter seat number");
            return;
        }

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = prefs.getString("userId", "");

        Showtime selectedShowtime = showtimeList.get(spShowtimes.getSelectedItemPosition());
        
        String ticketId = UUID.randomUUID().toString();
        Ticket ticket = new Ticket(ticketId, userId, selectedShowtime.getId(), seat, new Date(), selectedShowtime.getPrice(), "BOOKED");

        db.collection("tickets").document(ticketId)
                .set(ticket)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Ticket Booked Successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}