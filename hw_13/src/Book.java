import com.oocourse.library1.LibraryBookId;

import java.time.LocalDate;

public class Book {
    private final LibraryBookId id;
    private LocalDate date;
    private String personId;

    public Book(LibraryBookId id) {
        this.id = id;
        date = null;
        personId = null;
    }

    public void reserve(LocalDate date, String personId) {
        this.date = date;
        this.personId = personId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LibraryBookId getId() {
        return id;
    }

    public String getPersonId() {
        return personId;
    }

}
