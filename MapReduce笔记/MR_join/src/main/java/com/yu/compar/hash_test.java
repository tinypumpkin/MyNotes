package com.yu.compar;

import org.junit.Test;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class hash_test {
    @Test
    public void test1(){
        HashMap<Integer,PersonBean> HM1 = new HashMap<Integer,PersonBean>();
        HM1.put(1,new PersonBean(1, "王子晨", 24, "女", "1232332"));
        HM1.put(2,new PersonBean(2, "杨国泽", 25, "男", "1232333"));
        HM1.put(3,new PersonBean(3, "刘世涛", 22, "男", "1232334"));
        HM1.put(4,new PersonBean(4, "刑涛", 20, "男", "1232335"));
        HM1.put(5,new PersonBean(5, "杜志旭", 23, "男", "1232336"));
        for (Map.Entry<Integer,PersonBean> sets:HM1.entrySet()) {
            System.out.println("key是"+sets.getKey()+"------>值是"+sets.getValue());
        }

        //获取key
        for (Object key:HM1.keySet()) {
            System.out.println(key);
        }
        //获取valuse
        for (PersonBean val:HM1.values()) {
            System.out.println(val.toString());
        }
        Iterator<Map.Entry<Integer, PersonBean>> iter = HM1.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, PersonBean> temp = iter.next();
            System.out.println(temp.getKey()+"===>"+temp.getValue());
        }
    }
}
