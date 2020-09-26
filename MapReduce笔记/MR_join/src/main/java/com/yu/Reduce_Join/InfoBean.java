package com.yu.Reduce_Join;

import org.apache.hadoop.io.WritableComparable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class InfoBean implements WritableComparable<InfoBean> {
    private String id;
    private String pid;
    private int amount;
    private String pname;

    @Override
    public String toString() {
        return id + "\t" + pname + "\t" + amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }


    //按照Pid分组，组内按照pname排序，有pname的在前
    public int compareTo(InfoBean i) {
        int compare = this.pid.compareTo(i.pid);
        if (compare == 0) {
            return i.getPname().compareTo(this.getPname());
        } else {
            return compare;
        }
    }
   /** 序列化方法
	  如果是String，则调用writeUTF()方法
	  此外还有writeInt()、writeLong()、writeDouble()等方法。
    **/
    public void write(DataOutput out) throws IOException {
        out.writeUTF(id);
        out.writeUTF(pid);
        out.writeInt(amount);
        out.writeUTF(pname);
    }
    /**
     * 反序列化一定要注意顺序，一定和序列化的顺序一致
     */
    public void readFields(DataInput in) throws IOException {
        id = in.readUTF();
        pid = in.readUTF();
        amount = in.readInt();
        pname = in.readUTF();
    }
}

