import com.oocourse.library2.LibraryBookId;

import java.time.LocalDate;

public class Book {
    private final LibraryBookId id;
    private LocalDate date;
    private LocalDate deadline;
    private String personId;
    private int borrowedTimes;

    public Book(LibraryBookId id) {
        this.id = id;
        date = null;
        personId = null;
        borrowedTimes = 0;
    }

    public void borrowed(LocalDate date) {
        if (id.isTypeBU() || id.isTypeCU()) {
            borrowedTimes++;
        }
        setDeadline(date);
    }

    public boolean isPopular() {
        return borrowedTimes >= 2;
    }

    public void reserve(LocalDate date, String personId) {
        this.date = date;
        this.personId = personId;
    }

    public LocalDate getReservationDate() {
        return date;
    }

    public LibraryBookId getId() {
        return id;
    }

    public String getPersonId() {
        return personId;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void renews() {
        deadline = deadline.plusDays(30);
    }

    private void setDeadline(LocalDate date) {
        switch (id.getType()) {
            case B:
                deadline = date.plusDays(30);
                break;
            case C:
                deadline = date.plusDays(60);
                break;
            case BU:
                deadline = date.plusDays(7);
                break;
            case CU:
                deadline = date.plusDays(14);
                break;
            default:
                break;
        }
    }
}
