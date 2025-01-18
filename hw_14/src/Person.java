import com.oocourse.library2.LibraryBookId;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

public class Person {
    private int typeBCount;
    private int typeBuCount;
    private final HashMap<LibraryBookId, Book> borrowedBooks;

    public Person() {
        this.typeBCount = 0;
        this.typeBuCount = 0;
        this.borrowedBooks = new HashMap<>();
    }

    public boolean canBorrow(LibraryBookId bookId) {
        if (bookId.isTypeA() || bookId.isTypeAU()) {
            return false;
        } else if (bookId.isTypeB()) {
            return typeBCount == 0;
        } else if (bookId.isTypeBU()) {
            return typeBuCount == 0;
        } else {
            return !borrowedBooks.containsKey(bookId);
        }
    }

    public void addBook(Book book) {
        borrowedBooks.put(book.getId(), book);
        if (book.getId().isTypeB()) {
            typeBCount++;
        }
        if (book.getId().isTypeBU()) {
            typeBuCount++;
        }
    }

    public Book returnBook(LibraryBookId bookId) {
        final Book book = borrowedBooks.get(bookId);
        borrowedBooks.remove(bookId);
        if (bookId.isTypeB()) {
            typeBCount--;
        }
        if (bookId.isTypeBU()) {
            typeBuCount--;
        }
        return book;
    }

    public boolean canRenews(LibraryBookId bookId, LocalDate date) {
        LocalDate ddl = borrowedBooks.get(bookId).getDeadline();
        return !ddl.isBefore(date) && date.until(ddl, ChronoUnit.DAYS) < 5;
    }

    public void renews(LibraryBookId bookId) {
        borrowedBooks.get(bookId).renews();
    }
}
