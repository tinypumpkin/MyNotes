package com.yu.stu_join;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class stuBean implements WritableComparable<stuBean> {
    //student 学生属性
    private String s_id;
    private String s_name;
    private int s_age;
    //class 课程属性
    private String c_name;
    private String c_id;
    //score 成绩属性
    private double c_score;

    public stuBean() {}

    public stuBean(String s_id, String s_name, int s_age, String c_name, String c_id, double c_score) {
        this.s_id = s_id;
        this.s_name = s_name;
        this.s_age = s_age;
        this.c_name = c_name;
        this.c_id = c_id;
        this.c_score = c_score;
    }

    public String getS_id() {
        return s_id;
    }

    public void setS_id(String s_id) {
        this.s_id = s_id;
    }

    public String getS_name() {
        return s_name;
    }

    public void setS_name(String s_name) {
        this.s_name = s_name;
    }

    public int getS_age() {
        return s_age;
    }

    public void setS_age(int s_age) {
        this.s_age = s_age;
    }

    public String getC_name() {
        return c_name;
    }

    public void setC_name(String c_name) {
        this.c_name = c_name;
    }

    public String getC_id() {
        return c_id;
    }

    public void setC_id(String c_id) {
        this.c_id = c_id;
    }

    public double getC_score() {
        return c_score;
    }

    public void setC_score(double c_score) {
        this.c_score = c_score;
    }

    @Override
    public String toString() {
        return  s_name + '\t' + s_age + '\t'+ c_name + '\t' +  c_score;
    }

    public int compareTo(stuBean o) {
      int tem=this.getS_id().compareTo(o.getS_id());
      if (tem==0){
//      int tem2=this.getC_id().compareTo(o.getC_id());
//        if(tem2==0)
//            return Double.compare(this.getC_score(),o.getC_score());
//        else return tem2;
          return  Integer.compare(this.getS_age(),o.getS_age());
      }
      else return  tem;
    }

    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(s_id);
        dataOutput.writeUTF(s_name);
        dataOutput.writeInt(s_age);
        dataOutput.writeUTF(c_id);
        dataOutput.writeUTF(c_name);
        dataOutput.writeDouble(c_score);
    }

    public void readFields(DataInput dataInput) throws IOException {
        s_id=dataInput.readUTF();
        s_name=dataInput.readUTF();
        s_age=dataInput.readInt();
        c_id=dataInput.readUTF();
        c_name=dataInput.readUTF();
        c_score=dataInput.readDouble();
    }
}
