package org.example.studentmanagement.controller;

import org.example.studentmanagement.entity.Student;
import org.example.studentmanagement.entity.User;
import org.example.studentmanagement.repository.StudentRepository;
import org.example.studentmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherControllerTest {

    @Test
    void addStudentCreatesUserAndStudent() {
        StudentRepository studentRepository = mock(StudentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        TeacherController teacherController = new TeacherController(studentRepository, userRepository, passwordEncoder);

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("1234")).thenReturn("encoded-1234");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student savedStudent = invocation.getArgument(0);
            savedStudent.setId(100L);
            return savedStudent;
        });

        Map<String, String> request = Map.of(
                "name", "John Doe",
                "rollNo", "CSE-01",
                "username", "john",
                "password", "1234"
        );

        Map<String, Object> response = teacherController.addStudent(request);

        assertEquals("success", response.get("status"));
        assertEquals(100L, response.get("id"));
        assertEquals("John Doe", response.get("name"));
        assertEquals("CSE-01", response.get("rollNo"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("john", userCaptor.getValue().getUsername());
        assertEquals("encoded-1234", userCaptor.getValue().getPassword());
        assertEquals("ROLE_STUDENT", userCaptor.getValue().getRole());
    }

    @Test
    void addStudentReturnsErrorWhenUsernameExists() {
        StudentRepository studentRepository = mock(StudentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        TeacherController teacherController = new TeacherController(studentRepository, userRepository, passwordEncoder);

        User existingUser = new User();
        existingUser.setUsername("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(existingUser));

        Map<String, String> request = Map.of(
                "name", "John Doe",
                "rollNo", "CSE-01",
                "username", "john",
                "password", "1234"
        );

        Map<String, Object> response = teacherController.addStudent(request);

        assertEquals("error", response.get("status"));
        assertEquals("Username already exists", response.get("message"));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void getAllStudentsReturnsList() {
        StudentRepository studentRepository = mock(StudentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        TeacherController teacherController = new TeacherController(studentRepository, userRepository, passwordEncoder);

        Student student = new Student();
        student.setId(1L);
        student.setName("Alice");
        student.setRollNo("R-01");
        student.setUsername("alice");

        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<Student> students = teacherController.getAllStudents();

        assertEquals(1, students.size());
        assertEquals("Alice", students.get(0).getName());
    }

    @Test
    void updateStudentChangesNameAndRollNo() {
        StudentRepository studentRepository = mock(StudentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        TeacherController teacherController = new TeacherController(studentRepository, userRepository, passwordEncoder);

        Student existingStudent = new Student();
        existingStudent.setId(1L);
        existingStudent.setName("Old Name");
        existingStudent.setRollNo("OLD-1");
        existingStudent.setUsername("oldUser");

        Student updatedStudent = new Student();
        updatedStudent.setName("New Name");
        updatedStudent.setRollNo("NEW-1");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(existingStudent));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Student result = teacherController.updateStudent(1L, updatedStudent);

        assertEquals("New Name", result.getName());
        assertEquals("NEW-1", result.getRollNo());
        assertEquals("oldUser", result.getUsername());
    }

    @Test
    void deleteStudentRemovesStudentAndLinkedUser() {
        StudentRepository studentRepository = mock(StudentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        TeacherController teacherController = new TeacherController(studentRepository, userRepository, passwordEncoder);

        Student student = new Student();
        student.setId(1L);
        student.setUsername("alice");

        User user = new User();
        user.setUsername("alice");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        Map<String, String> response = teacherController.deleteStudent(1L);

        assertEquals("success", response.get("status"));
        verify(userRepository).delete(user);
        verify(studentRepository).deleteById(1L);
    }
}