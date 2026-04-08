package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.example.myapplication.models.Movie;
import com.example.myapplication.models.Showtime;
import com.example.myapplication.models.Ticket;
import com.example.myapplication.notification.NotificationWorker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    private String movieTitleStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // Khởi tạo Firebase và View
        db = FirebaseFirestore.getInstance();
        movieId = getIntent().getStringExtra("movieId");

        if (movieId == null) {
            Toast.makeText(this, "Error: Movie not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        
        showtimeList = new ArrayList<>();
        showtimeLabels = new ArrayList<>();

        loadMovieInfo();
        loadShowtimes();

        btnBook.setOnClickListener(v -> bookTicket());
    }

    private void initViews() {
        ivPoster = findViewById(R.id.ivDetailPoster);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvGenre = findViewById(R.id.tvDetailGenre);
        tvDescription = findViewById(R.id.tvDetailDescription);
        spShowtimes = findViewById(R.id.spShowtimes);
        etSeat = findViewById(R.id.etSeatNumber);
        btnBook = findViewById(R.id.btnBookTicket);
    }

    private void loadMovieInfo() {
        db.collection("movies").document(movieId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Movie movie = documentSnapshot.toObject(Movie.class);
                    if (movie != null) {
                        movieTitleStr = movie.getTitle();
                        tvTitle.setText(movieTitleStr);
                        
                        // Xử lý hiển thị thể loại tương thích với API < 26
                        String genres = "";
                        if (movie.getGenres() != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                genres = String.join(", ", movie.getGenres());
                            } else {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < movie.getGenres().size(); i++) {
                                    sb.append(movie.getGenres().get(i));
                                    if (i < movie.getGenres().size() - 1) sb.append(", ");
                                }
                                genres = sb.toString();
                            }
                        }
                        tvGenre.setText(genres);
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
                    if (task.isSuccessful() && task.getResult() != null) {
                        showtimeList.clear();
                        showtimeLabels.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Showtime st = doc.toObject(Showtime.class);
                            st.setId(doc.getId());
                            showtimeList.add(st);
                            showtimeLabels.add(st.getStartTime().toString() + " - " + st.getPrice() + " VND");
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                                android.R.layout.simple_spinner_item, showtimeLabels);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spShowtimes.setAdapter(adapter);
                    }
                });
    }

    private void bookTicket() {
        if (showtimeList.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn suất chiếu", Toast.LENGTH_SHORT).show();
            return;
        }

        String seat = etSeat.getText().toString().trim();
        if (TextUtils.isEmpty(seat)) {
            etSeat.setError("Vui lòng nhập số ghế");
            return;
        }

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = prefs.getString("userId", "");

        if (userId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        Showtime selectedShowtime = showtimeList.get(spShowtimes.getSelectedItemPosition());
        
        String ticketId = UUID.randomUUID().toString();
        Ticket ticket = new Ticket(ticketId, userId, selectedShowtime.getId(), seat, 
                new Date(), selectedShowtime.getPrice(), "BOOKED");

        // Lưu vé lên Firestore
        db.collection("tickets").document(ticketId)
                .set(ticket)
                .addOnSuccessListener(aVoid -> {
                    // Lên lịch thông báo đẩy
                    scheduleNotification(selectedShowtime.getStartTime(), movieTitleStr);
                    Toast.makeText(this, "Đặt vé thành công! Đã đặt lịch nhắc nhở.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void scheduleNotification(Date startTime, String movieTitle) {
        long delay = startTime.getTime() - System.currentTimeMillis();
        // Nếu giờ chiếu đã qua hoặc còn quá ít thời gian, hẹn giờ sau 5 giây để test
        if (delay < 0) delay = 5000; 

        Data inputData = new Data.Builder()
                .putString("movieTitle", movieTitle)
                .build();

        OneTimeWorkRequest notificationRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).enqueue(notificationRequest);
    }
}