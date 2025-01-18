import com.oocourse.library1.LibraryBookId;

import java.util.HashMap;

public class Person {
    private int typeBCount;
    private final HashMap<LibraryBookId, Book> borrowedBooks;

    public Person() {
        this.typeBCount = 0;
        this.borrowedBooks = new HashMap<>();
    }

    public boolean canBorrowTypeBC(LibraryBookId bookId) {
        if (bookId.isTypeB()) {
            return typeBCount == 0;
        } else {
            return !borrowedBooks.containsKey(bookId);
        }
    }

    public void addBook(Book book) {
        borrowedBooks.put(book.getId(), book);
        if (book.getId().isTypeB()) {
            typeBCount++;
        }
    }

    public Book returnBook(LibraryBookId bookId) {
        Book book = borrowedBooks.get(bookId);
        borrowedBooks.remove(bookId);
        if (bookId.isTypeB()) {
            typeBCount--;
        }
        return book;
    }
}
