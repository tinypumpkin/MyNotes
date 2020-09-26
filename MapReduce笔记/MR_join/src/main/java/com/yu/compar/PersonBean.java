package com.yu.compar;

public class PersonBean implements my_comparable<PersonBean>{
    public int id;
    public String name;
    public int age;
    public String sex;
    public String phone;

    public PersonBean(int id,String name,int age,String sex ,String phone) {
        this.id=id;
        this.name=name;
        this.age=age;
        this.sex=sex;
        this.phone=phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int compareTo(Object o) {
        if(o instanceof PersonBean){
            if(this.getAge()>((PersonBean) o).getAge())
                return 1;
            else if(this.getAge()<((PersonBean) o).getAge())
                return -1;
            else return 0;
        }
        throw new RuntimeException("输入类型有问题！");
    }

    @Override
    public String toString() {
        return "PersonBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
