import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created by Anna on 2017-08-26.
 */
public class Contact {
    private int id;
    private String name;
    private String surname;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
