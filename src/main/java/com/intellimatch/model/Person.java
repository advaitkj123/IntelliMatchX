package com.intellimatch.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Abstract base entity for both Applicants and Recruiters.
 */
public abstract class Person {

    protected final String id;
    protected String name;
    protected String email;
    protected List<Skill> skills;

    protected Person(String name, String email) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.skills = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public List<Skill> getSkills() { return Collections.unmodifiableList(skills); }

    public void addSkill(Skill skill) {
        if (skill != null && !skills.contains(skill)) {
            skills.add(skill);
        }
    }

    public abstract String getRole();

    @Override
    public String toString() {
        return getRole() + "[" + name + ", skills=" + skills + "]";
    }
}
