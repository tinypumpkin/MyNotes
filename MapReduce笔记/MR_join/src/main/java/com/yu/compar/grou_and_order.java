package com.yu.compar;

import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 利用比较器实现分组排序
 */
public class grou_and_order {
    //1-->利用personbean自身实现comparable进行自然排序
    @Test
    public void test1(){
        System.out.println("我是Test-1");
        PersonBean[] plist = new PersonBean[5];
        plist[0]=new PersonBean(1, "王子晨", 24, "女", "1232332");
        plist[1]=new PersonBean(2, "杨国泽", 25, "男", "1232333");
        plist[2]=new PersonBean(3, "刘世涛", 22, "男", "1232334");
        plist[3]=new PersonBean(4, "刑涛", 20, "男", "1232335");
        plist[4]=new PersonBean(5, "杜志旭", 23, "男", "1232336");
        //利用 compareTo实现排序
        Arrays.sort(plist);
        System.out.println(Arrays.toString(plist));
    }
    //2-->利用compartor实现定制排序,这里通过if 条件实现分组排序
    @Test
    public void test2(){
        System.out.println("我是Test-2");
        PersonBean[] plist = new PersonBean[5];
        plist[0]=new PersonBean(1, "王子晨", 24, "女", "1232332");
        plist[1]=new PersonBean(2, "杨国泽", 25, "男", "1232333");
        plist[2]=new PersonBean(3, "刘世涛", 22, "男", "1232334");
        plist[3]=new PersonBean(4, "刑涛", 20, "男", "1232335");
        plist[4]=new PersonBean(5, "杜志旭", 23, "男", "1232336");

        Arrays.sort(plist,new my_conparator<PersonBean>(){
            public int compare(PersonBean o1, PersonBean o2) {
                if (o1 instanceof PersonBean && o2 instanceof PersonBean){
                    if(((PersonBean)o1).getSex()==((PersonBean)o2).getSex() && ((PersonBean)o1).getAge()>((PersonBean)o2).getAge())
                        return 1;
                    else if(((PersonBean)o1).getSex()==((PersonBean)o2).getSex() && ((PersonBean)o1).getAge()<=((PersonBean)o2).getAge())
                        return -1;
                    else return 0;
                }
                throw new RuntimeException("类型不一致");}
        });
        System.out.println(Arrays.toString(plist));
    }
}

