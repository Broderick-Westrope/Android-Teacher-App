package com.broderickwestrope.whiteboard.student_records.Models;

import android.util.Log;

// The model of what a task will contain
public class RecordModel {
    /*
    id - This is the manually assigned student ID. This is also used for identification within the database so there can only be one student with each ID
    age - This is the age of the student
    name - This is the students full name
    gender - This is the students gender. It is either Male, Female, or Other.
    course - This is the name of the course that the student is enrolled in.
    address - This is the students residential address.
    */

    private int id, age;
    private String name, gender, course, address;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        Log.d("ID Inside", String.valueOf(id));
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
