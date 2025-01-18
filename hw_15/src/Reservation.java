import com.oocourse.library3.LibraryBookId;

public class Reservation {
    private final String personId;
    private final LibraryBookId bookId;

    public Reservation(String personId, LibraryBookId bookId) {
        this.personId = personId;
        this.bookId = bookId;
    }

    public String getPersonId() {
        return personId;
    }

    public LibraryBookId getBookId() {
        return bookId;
    }
}
