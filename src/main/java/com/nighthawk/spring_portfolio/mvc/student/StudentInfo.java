package com.nighthawk.spring_portfolio.mvc.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  // Annotations to simplify writing code (ie constructors, setters)
@NoArgsConstructor
@AllArgsConstructor
@Entity // Annotation to simplify creating an entity, which is a lightweight persistence domain object. Typically, an entity represents a table in a relational database, and each entity instance corresponds to a row in that table.
@Table(name = "students" , uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class StudentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;

    private int tableNumber;

    private String course;

    private ArrayList<String> tasks;
    
    private ArrayList<String> completed;

    private int trimester;

    private int period;
    
    

    public StudentInfo(String username, int tableNumber, String course, ArrayList<String> tasks, ArrayList<String> completed, int trimester, int period) {
        this.username = username;
        this.tableNumber = tableNumber;
        this.course = course;
        this.tasks = tasks;
        this.completed = completed;
        this.trimester = trimester;
        this.period = period;
    }

    @Service
    public static class StudentService {

        @Autowired
        private StudentInfoJPARepository studentJPARepository;

        @PostConstruct
        public void initializeData() { 
            if (studentJPARepository == null) {
                throw new RuntimeException("studentJPARepository is not initialized!");
            }
            List<StudentInfo> students = new ArrayList<>();
            students.add(new StudentInfo("nitinsandiego", 2, "CSA", new ArrayList<String>(Arrays.asList("Task 1", "Task 2")), new ArrayList<String>(Arrays.asList("Completed 1", "Completed 2")), 2, 1));
            students.add(new StudentInfo("Akhil353", 1, "CSA", new ArrayList<String>(Arrays.asList("Task 1", "Task 2")), new ArrayList<String>(Arrays.asList("Completed 1", "Completed 2")), 2, 3));
            students.add(new StudentInfo("SrinivasNampalli", 2, "CSA", new ArrayList<String>(Arrays.asList("Task 1", "Task 2")), new ArrayList<String>(Arrays.asList("Completed 1", "Completed 2")), 2, 1));
            students.add(new StudentInfo("adityasamavedam", 1, "CSA", new ArrayList<String>(Arrays.asList("Task 1", "Task 2")), new ArrayList<String>(Arrays.asList("Completed 1", "Completed 2")), 2, 3));

            for (StudentInfo student : students) {
            Optional<StudentInfo> existingStudent = studentJPARepository.findByUsername(student.getUsername());
            
            if (existingStudent.isEmpty()) {
                studentJPARepository.save(student);
            }
        }
        }

        public Iterable<StudentInfo> findAll() {
            return studentJPARepository.findAll();
        }

        public List<StudentInfo> findByUsernameCourseTrimesterPeriod(String username, String course, int trimester, int period) {
            return studentJPARepository.findByUsernameCourseTrimesterPeriod(username, course, trimester, period);
        }

        public StudentInfo createStudent(StudentInfo student) {
            // Check if a student with the same username already exists to avoid duplicates
            Optional<StudentInfo> existingStudent = studentJPARepository.findByUsername(student.getUsername());
            if (existingStudent.isPresent()) {
                throw new RuntimeException("A student with this username already exists.");
            }
            return studentJPARepository.save(student);
        }

        public void deleteById(Long id) {
            studentJPARepository.deleteById(id);
        }

        public Optional<StudentInfo> findByUsername(String username) {
            return studentJPARepository.findByUsername(username);
        }
        
        public List<StudentInfo> findTeam(String course, int trimester, int period, int table) {
            return studentJPARepository.findTeam(course, trimester, period, table);
        }

        public List<StudentInfo> findPeriod(String course, int trimester, int period) {
            return studentJPARepository.findPeriod(course, trimester, period);
        }

        
    }
}
