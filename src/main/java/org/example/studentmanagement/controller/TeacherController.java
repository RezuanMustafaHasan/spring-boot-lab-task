package org.example.studentmanagement.controller;

import org.example.studentmanagement.entity.Student;
import org.example.studentmanagement.entity.User;
import org.example.studentmanagement.repository.StudentRepository;
import org.example.studentmanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherController(StudentRepository studentRepository,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Add student with credentials
    @PostMapping("/student")
    public Map<String, Object> addStudent(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String rollNo = request.get("rollNo");
        String username = request.get("username");
        String password = request.get("password");

        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            return Map.of("status", "error", "message", "Username already exists");
        }

        // Create user account for student
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_STUDENT");
        userRepository.save(user);

        // Create student record
        Student student = new Student();
        student.setName(name);
        student.setRollNo(rollNo);
        student.setUsername(username);
        studentRepository.save(student);

        return Map.of("status", "success", "id", student.getId(), "name", name, "rollNo", rollNo);
    }

    // Get all students
    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // Update student
    @PutMapping("/student/{id}")
    public Student updateStudent(
            @PathVariable Long id,
            @RequestBody Student updatedStudent) {

        Student student = studentRepository.findById(id)
                .orElseThrow();

        student.setName(updatedStudent.getName());
        student.setRollNo(updatedStudent.getRollNo());

        return studentRepository.save(student);
    }

    // Delete student
    @DeleteMapping("/student/{id}")
    public Map<String, String> deleteStudent(@PathVariable Long id) {
        Student student = studentRepository.findById(id).orElseThrow();
        
        // Delete user account
        userRepository.findByUsername(student.getUsername())
                .ifPresent(userRepository::delete);
        
        // Delete student record
        studentRepository.deleteById(id);
        
        return Map.of("status", "success");
    }
}
