package com.smartclassroom.dto;

import java.util.List;

public class ClassroomDTO {
    private Long id;
    private String classCode;
    private String className;
    private String facultyName;
    private int studentCount;
    private List<StudentDTO> students;

    public ClassroomDTO(Long id, String classCode, String className,
                        String facultyName, List<StudentDTO> students) {
        this.id = id;
        this.classCode = classCode;
        this.className = className;
        this.facultyName = facultyName;
        this.students = students;
        this.studentCount = students.size();
    }

    public Long getId()                    { return id; }
    public String getClassCode()           { return classCode; }
    public String getClassName()           { return className; }
    public String getFacultyName()         { return facultyName; }
    public int getStudentCount()           { return studentCount; }
    public List<StudentDTO> getStudents()  { return students; }

    public static class StudentDTO {
        private Long studentId;
        private String fullName;
        private String username;
        private String email;

        public StudentDTO(Long studentId, String fullName, String username, String email) {
            this.studentId = studentId;
            this.fullName = fullName;
            this.username = username;
            this.email = email;
        }

        public Long getStudentId()  { return studentId; }
        public String getFullName() { return fullName; }
        public String getUsername() { return username; }
        public String getEmail()    { return email; }
    }
}
