package com.nighthawk.spring_portfolio.mvc.student;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// JPA is an object-relational mapping (ORM) to persistent data, originally relational databases (SQL). Today JPA implementations has been extended for NoSQL.
public interface StudentInfoJPARepository extends JpaRepository<StudentInfo, Long> {
    Optional<StudentInfo> findByUsername(String username);
    @Query(
        value = "SELECT * FROM students WHERE course = :course AND trimester = :trimester AND period = :period AND table_number = :table",
        nativeQuery = true
    )
    List<StudentInfo> findTeam(
        @Param("course") String course, 
        @Param("trimester") int trimester, 
        @Param("period") int period,
        @Param("table") int table
    );

    @Query(
        value = "SELECT * FROM students WHERE course = :course AND trimester = :trimester AND period = :period",
        nativeQuery = true
    )
    List<StudentInfo> findPeriod(
        @Param("course") String course, 
        @Param("trimester") int trimester, 
        @Param("period") int period
    );



    @Query(
        value = "SELECT * FROM students WHERE username = :username AND course = :course AND trimester = :trimester AND period = :period",
        nativeQuery = true
    )
    List<StudentInfo> findByUsernameCourseTrimesterPeriod(
        @Param("username") String username, 
        @Param("course") String course, 
        @Param("trimester") int trimester, 
        @Param("period") int period
    );
}
