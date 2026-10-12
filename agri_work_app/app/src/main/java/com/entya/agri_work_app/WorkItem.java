package com.entya.agri_work_app;

public class WorkItem {
    public String date;
    public String job;
    public String memo;
    public String image_path;
    public String status;

    public WorkItem(String date, String job, String memo, String image_path , String status) {
        this.date = date;
        this.job = job;
        this.memo = memo;
        this.image_path = image_path;
        this.status = status;
    }
}