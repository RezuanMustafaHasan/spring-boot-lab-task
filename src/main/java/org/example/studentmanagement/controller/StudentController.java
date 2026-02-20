package org.example.studentmanagement.controller;

import org.example.studentmanagement.entity.Student;
import org.example.studentmanagement.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/student")
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Get student's own dashboard info by username
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(Principal principal) {
        String username = principal.getName();
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        return Map.of(
            "id", student.getId(),
            "name", student.getName(),
            "rollNo", student.getRollNo(),
            "username", student.getUsername()
        );
    }
}
