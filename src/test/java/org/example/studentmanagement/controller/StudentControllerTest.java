package org.example.studentmanagement.controller;

import org.example.studentmanagement.entity.Student;
import org.example.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentControllerTest {

    @Test
    void getDashboardReturnsStudentInfo() {
        StudentRepository studentRepository = mock(StudentRepository.class);
        StudentController studentController = new StudentController(studentRepository);

        Student student = new Student();
        student.setId(1L);
        student.setName("Alice");
        student.setRollNo("R-01");
        student.setUsername("alice");

        Principal principal = () -> "alice";
        when(studentRepository.findByUsername("alice")).thenReturn(Optional.of(student));

        Map<String, Object> dashboard = studentController.getDashboard(principal);

        assertEquals(1L, dashboard.get("id"));
        assertEquals("Alice", dashboard.get("name"));
        assertEquals("R-01", dashboard.get("rollNo"));
        assertEquals("alice", dashboard.get("username"));
    }

    @Test
    void getDashboardThrowsWhenStudentNotFound() {
        StudentRepository studentRepository = mock(StudentRepository.class);
        StudentController studentController = new StudentController(studentRepository);

        Principal principal = () -> "missing-user";
        when(studentRepository.findByUsername("missing-user")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> studentController.getDashboard(principal));

        assertEquals("Student not found", exception.getMessage());
    }
}