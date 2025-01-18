import com.oocourse.library3.LibraryBookId;

import java.time.LocalDate;

public class Book {
    private final LibraryBookId id;
    private LocalDate date;
    private LocalDate deadline;
    private String reserver;
    private String donator;
    private boolean isPunished;
    private int borrowedTimes;

    public Book(LibraryBookId id) {
        this.id = id;
        borrowedTimes = 0;
    }

    public Book(LibraryBookId id, String donator) {
        this.id = id;
        this.donator = donator;
        borrowedTimes = 0;
    }

    public void borrow(LocalDate date) {
        if (id.isTypeBU() || id.isTypeCU()) {
            borrowedTimes++;
        }
        setDeadline(date);
        isPunished = false;
    }

    public boolean isPopular() {
        return borrowedTimes >= 2;
    }

    public void reserve(LocalDate date, String personId) {
        this.date = date;
        this.reserver = personId;
    }

    public boolean isPunished() {
        return isPunished;
    }

    public void punish() {
        isPunished = true;
    }

    public LocalDate getReservationDate() {
        return date;
    }

    public LibraryBookId getId() {
        return id;
    }

    public String getReserver() {
        return reserver;
    }

    public String getDonator() {
        return donator;
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
