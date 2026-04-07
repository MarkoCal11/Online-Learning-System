# Online-Learning-System
A desktop-based online learning system built with Java and JavaFX, designed to simulate the core functionality of modern e-learning platforms.

## Features
- Manage students and courses.
- Assign students to courses and track enrollments.
- Take quizzes and read through lessons as a student.
- Create quizzes and lessons, grade students as a teacher.

## Technologies used
- Java
- JavaFX
- Maven
- H2 database

## Run
Requirements: JDK 25 (or change pom.xml to 21)

```sh
./mvnw clean javafx:run
```

### macOS note
If `./mvnw` is blocked, run:
```sh
xattr -dr com.apple.quarantine .
chmod +x mvnw
```


## Demo login
- Username: admin
- Password: adminpass
