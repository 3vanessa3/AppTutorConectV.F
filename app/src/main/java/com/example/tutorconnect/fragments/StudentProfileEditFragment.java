package com.example.tutorconnect.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tutorconnect.R;
import com.example.tutorconnect.models.Student;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class StudentProfileEditFragment extends Fragment {
    private TextInputEditText etName, etEmail, etAge, etProgram, etFavoriteSubjects, etProgramming;
    private MaterialButton btnSave;
    private Student currentStudent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_profile_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupListeners();
        loadStudentData();
    }

    private void initializeViews(View view) {
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etAge = view.findViewById(R.id.etAge);
        etProgram = view.findViewById(R.id.etProgram);
        etFavoriteSubjects = view.findViewById(R.id.etFavoriteSubjects);
        etProgramming = view.findViewById(R.id.etProgramming);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadStudentData() {
        SharedPreferences prefs = getActivity().getSharedPreferences("StudentPrefs", Context.MODE_PRIVATE);
        currentStudent = new Student();
        
        // Cargar datos del SharedPreferences
        currentStudent.setName(prefs.getString("student_name", ""));
        currentStudent.setEmail(prefs.getString("student_email", ""));
        currentStudent.setAge(prefs.getInt("student_age", 0));
        currentStudent.setProgram(prefs.getString("student_program", ""));
        currentStudent.setFavoriteSubjects(prefs.getString("student_favorites", ""));
        currentStudent.setProgrammingSkills(prefs.getString("student_programming", ""));
        
        // Mostrar datos en los campos
        etName.setText(currentStudent.getName());
        etEmail.setText(currentStudent.getEmail());
        etAge.setText(String.valueOf(currentStudent.getAge()));
        etProgram.setText(currentStudent.getProgram());
        etFavoriteSubjects.setText(currentStudent.getFavoriteSubjects());
        etProgramming.setText(currentStudent.getProgrammingSkills());
    }

    private void saveProfileChanges() {
        // Validar campos
        if (validateFields()) {
            // Actualizar datos del estudiante
            currentStudent.setName(etName.getText().toString());
            currentStudent.setEmail(etEmail.getText().toString());
            currentStudent.setAge(Integer.parseInt(etAge.getText().toString()));
            currentStudent.setProgram(etProgram.getText().toString());
            currentStudent.setFavoriteSubjects(etFavoriteSubjects.getText().toString());
            currentStudent.setProgrammingSkills(etProgramming.getText().toString());

            // Guardar datos en SharedPreferences
            SharedPreferences prefs = getActivity().getSharedPreferences("StudentPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("student_name", currentStudent.getName());
            editor.putString("student_email", currentStudent.getEmail());
            editor.putInt("student_age", currentStudent.getAge());
            editor.putString("student_program", currentStudent.getProgram());
            editor.putString("student_favorites", currentStudent.getFavoriteSubjects());
            editor.putString("student_programming", currentStudent.getProgrammingSkills());
            editor.apply();

            Toast.makeText(getContext(), "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
            // Cerrar el fragment después de guardar
            getParentFragmentManager().popBackStack();
        }
    }

    private boolean validateFields() {
        if (etName.getText().toString().isEmpty()) {
            etName.setError("El nombre es requerido");
            return false;
        }
        if (etEmail.getText().toString().isEmpty()) {
            etEmail.setError("El correo electrónico es requerido");
            return false;
        }
        if (etAge.getText().toString().isEmpty()) {
            etAge.setError("La edad es requerida");
            return false;
        }
        return true;
    }
}
