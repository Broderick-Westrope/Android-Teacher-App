package com.broderickwestrope.whiteboard.Models;

// The model of what a task will contain
public class ExamModel {
    /*
    name - This is the name of the exam/assignment (eg. Final Exam)
    unit - This is the name or number of the unit (eg. Mobile Application Development)
    date - This is the date that the exam is scheduled to take place (eg. 10/9/21)
    time - This is the time that the exam is scheduled to take place or be due (eg. 12pm)
    location - This is the location at which the exam is to take place (eg. WSU Parramatta Campus)
    duration - This is the duration for which the exam will run (eg. 2 Hours)
    studentID - This is the ID of the student whose exam it is
    examID - This is the identifier of the exam assigned automatically by the database manager
    */

    private String name, unit, date, time, location;
    private int studentID, examID;
    private float duration;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public int getStudentId() {
        return studentID;
    }

    public void setStudentId(int id) {
        this.studentID = id;
    }

    public int getExamID() {
        return examID;
    }

    public void setExamID(int examID) {
        this.examID = examID;
    }
}
