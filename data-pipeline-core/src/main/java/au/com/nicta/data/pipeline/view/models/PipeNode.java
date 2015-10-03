package au.com.nicta.data.pipeline.view.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tiantian on 24/09/15.
 */
public class PipeNode {

    private String name;

    private String version;

    private String author;

    private String type;

    private Date startTime;

    private Date endTime;

    private String status;

    private Integer group;


    public PipeNode() {

    }

    public PipeNode(String name, String version) {
        this.name = name + "#" + version;
        this.version = version;
        this.author = "Dongyao";
        this.type = "SparkPipe";
        this.startTime = new Date();
        this.endTime = new Date();
        this.status = "Success";
        this.group = (int) Math.round(Math.random() * 4);

    }

    public PipeNode(String name, String version, String author, String type, Date startTime, Date endTime, String status, Integer group) {
        this.name = name + "#" + version;
        this.version = version;
        this.author = author;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.group = ExecutionVO.getGroup(status);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }
}
