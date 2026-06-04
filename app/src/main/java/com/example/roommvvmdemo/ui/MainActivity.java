package com.example.roommvvmdemo.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roommvvmdemo.R;
import com.example.roommvvmdemo.data.local.Note;
import com.example.roommvvmdemo.viewmodel.NoteViewModel;

public class MainActivity extends AppCompatActivity {

    private NoteViewModel noteViewModel;
    private EditText etTitle;
    private EditText etDescription;
    private Button btnAdd;
    private Button btnDeleteAll;
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnAdd = findViewById(R.id.btnAdd);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        noteViewModel.getAllNotes().observe(this, notes -> {
            adapter.setNotes(notes);
        });

        btnAdd.setOnClickListener(v -> {
            saveNote();
        });

        btnDeleteAll.setOnClickListener(v -> {
            noteViewModel.deleteAllNotes();
            Toast.makeText(this, "Toutes les notes ont été supprimées", Toast.LENGTH_SHORT).show();
        });

        adapter.setOnItemLongClickListener(note -> {
            noteViewModel.delete(note);
            Toast.makeText(this, "Note supprimée", Toast.LENGTH_SHORT).show();
        });

        adapter.setOnItemClickListener(note -> {
            Toast.makeText(this, "Titre : " + note.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Remplir le titre et la description", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note(title, description);
        noteViewModel.insert(note);

        etTitle.setText("");
        etDescription.setText("");

        Toast.makeText(this, "Note ajoutée", Toast.LENGTH_SHORT).show();
    }
}