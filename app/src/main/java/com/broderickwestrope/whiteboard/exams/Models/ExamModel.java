package com.broderickwestrope.whiteboard.exams.Models;

// The model of what a task will contain
public class ExamModel {
    /*
    name - This is the name of the exam/assignment (eg. Final Exam)
    unit - This is the name or number of the unit (eg. Mobile Application Development)
    date - This is the date that the exam is scheduled to take place (eg. 10/9/21)
    time - This is the time that the exam is scheduled to take place or be due (eg. 12pm)
    location - This is the location at which the exam is to take place (eg. WSU Parramatta Campus)
    duration - This is the duration for which the exam will run (eg. 2 Hours)
    id - This is the auto-incremented ID of the exam used to identify it
    */

    private String name, unit, date, time, location;
    private int id, duration;

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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
