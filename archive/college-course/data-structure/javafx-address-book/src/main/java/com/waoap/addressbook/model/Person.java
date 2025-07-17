package com.waoap.addressbook.model;

import java.util.ArrayList;
import java.util.List;

public class Person {
    private final String name;

    private final List<String> telephones;

    private final String email;

    private final String address;

    private final String note;

    private Status status = Status.NONE;

    public Person(String name, Status status) {
        this.name = name;
        this.status = status;
        telephones = new ArrayList<>();
        email = "";
        address = "";
        note = "";
    }

    /**
     * 联系人状态
     */
    public enum Status {
        /**
         * 初始状态
         */
        NONE,

        /**
         * 表示创建的联系人信息有错误
         */
        ERROR,

        /**
         * 表述联系人正在被删除
         */
        IN_DELETE,

        /**
         * 表示联系人信息正在被修改
         */
        IN_MODIFICATION,

        /**
         * 表示该联系人是占位的导航类型
         */
        NAVIGATION_TYPE,
    }

    public Person(String name, List<String> telephones, String email, String address, String note) {
        this.name = name;
        this.telephones = telephones;
        this.email = email;
        this.address = address;
        this.note = note;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<String> getTelephones() {
        return telephones;
    }

    public String getName() {
        return name;
    }

    public boolean equalTo(Person person) {
        return this.name.equals(person.getName())
                && this.telephones.equals(person.telephones)
                && this.address.equals(person.address)
                && this.email.equals(person.email)
                && this.note.equals(person.note);
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getNote() {
        return note;
    }


}
