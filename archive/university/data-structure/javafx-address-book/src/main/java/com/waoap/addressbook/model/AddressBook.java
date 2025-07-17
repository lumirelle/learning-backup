package com.waoap.addressbook.model;

import com.waoap.addressbook.utils.PinYinComparator;

import java.util.*;

public class AddressBook {
    /**
     * 用优先队列来实现联系人按拼音首字母顺序排序
     */
    private final PriorityQueue<String> names = new PriorityQueue<>(new PinYinComparator());

    /**
     * 联系人姓名与联系人实例的哈希表
     */
    private final HashMap<String, ArrayList<Person>> names2contacts = new HashMap<>();

    public PriorityQueue<String> getNames() {
        return names;
    }

    public HashMap<String, ArrayList<Person>> getNames2contacts() {
        return names2contacts;
    }

    /**
     * 添加联系人
     *
     * @param contact 要添加的联系人
     */
    public void add(Person contact) {
        names.offer(contact.getName());
        if (names2contacts.containsKey(contact.getName())) {
            names2contacts.get(contact.getName()).add(contact);
        } else {
            ArrayList<Person> contacts = new ArrayList<>();
            contacts.add(contact);
            names2contacts.put(contact.getName(), contacts);
        }
    }

    /**
     * 删除指定联系人
     *
     * @param contact 要删除的联系人
     */
    public void delete(Person contact) {
        names.remove(contact.getName());
        if (names2contacts.containsKey(contact.getName())) {
            ArrayList<Person> contacts = names2contacts.get(contact.getName());
            contacts.remove(contact);
            if (contacts.isEmpty()) {
                names2contacts.remove(contact.getName());
            }
        }
    }

    /**
     * 修改联系人
     *
     * @param oldContact 旧联系人
     * @param newContact 新联系人
     */
    public void modify(Person oldContact, Person newContact) {
        delete(oldContact);
        add(newContact);
    }

    public ArrayList<String> findByName(String name) {
        ArrayList<String> matchContacts = new ArrayList<>();
        if (!names2contacts.containsKey(name) || name.length() < 2) {
            return matchContacts;
        }
        for (Person p : names2contacts.get(name)) {
            matchContacts.add(p.getName());
        }
        return matchContacts;
    }

    public ArrayList<String> findByTelephone(String telephone) {
        ArrayList<String> matchContacts = new ArrayList<>();
        // 保证查找到的联系人仍旧按拼音首字母排序
        Queue<String> tmp = new LinkedList<>();
        while (!names.isEmpty()) {
            names2contacts.get(names.peek()).forEach(person -> {
                if (person.getStatus() == Person.Status.NAVIGATION_TYPE) {
                    return;
                }
                person.getTelephones().forEach(telephone1 -> {
                    if (telephone1.equals(telephone)) {
                        matchContacts.add(person.getName());
                    }
                });
            });
            tmp.offer(names.poll());
        }
        while (!tmp.isEmpty()) {
            names.offer(tmp.poll());
        }
        return matchContacts;
    }

    public ArrayList<String> findByEmail(String email) {
        ArrayList<String> matchContacts = new ArrayList<>();
        // 保证查找到的联系人仍旧按拼音首字母排序
        Queue<String> tmp = new LinkedList<>();
        while (!names.isEmpty()) {
            names2contacts.get(names.peek()).forEach(person -> {
                if (person.getStatus() == Person.Status.NAVIGATION_TYPE) {
                    return;
                }
                if (person.getEmail().equals(email)) {
                    matchContacts.add(person.getName());
                }
            });
            tmp.offer(names.poll());
        }
        while (!tmp.isEmpty()) {
            names.offer(tmp.poll());
        }
        return matchContacts;
    }
}
