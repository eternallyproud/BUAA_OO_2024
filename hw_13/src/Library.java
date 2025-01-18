import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryRequest;

import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.oocourse.library1.LibrarySystem.PRINTER;

public class Library {
    private LocalDate date;
    private final HashMap<String, Person> persons;
    private final HashMap<LibraryBookId, ArrayList<Book>> bookShelf;
    private final HashMap<LibraryBookId, ArrayList<Book>> lendingOffice;
    private final HashMap<LibraryBookId, ArrayList<Book>> reservationOffice;
    private final ArrayList<Reservation> reservationRecord;

    public Library(Map<LibraryBookId, Integer> initMap) {
        persons = new HashMap<>();
        bookShelf = new HashMap<>();
        lendingOffice = new HashMap<>();
        reservationOffice = new HashMap<>();
        reservationRecord = new ArrayList<>();
        for (LibraryBookId id : initMap.keySet()) {
            ArrayList<Book> bookArray = new ArrayList<>();
            bookShelf.put(id, bookArray);
            for (int i = 0; i < initMap.get(id); i++) {
                bookArray.add(new Book(id));
            }
            lendingOffice.put(id, new ArrayList<>());
            reservationOffice.put(id, new ArrayList<>());
        }
    }

    public void open(LocalDate date) {
        this.date = date;
        manageBooks();
    }

    public void close() {
        manageBooks();
    }

    private void manageBooks() {
        List<LibraryMoveInfo> moveList = new ArrayList<>();
        for (LibraryBookId id : lendingOffice.keySet()) {
            Iterator<Book> iterator = lendingOffice.get(id).iterator();
            while (iterator.hasNext()) {
                Book book = iterator.next();
                iterator.remove();
                addBookToBookShelf(book);
                moveList.add(new LibraryMoveInfo(book.getId(), "bro", "bs"));
            }
        }
        for (LibraryBookId id : reservationOffice.keySet()) {
            Iterator<Book> iterator = reservationOffice.get(id).iterator();
            while (iterator.hasNext()) {
                Book book = iterator.next();
                if (book.getDate().until(date, ChronoUnit.DAYS) >= 5) {
                    iterator.remove();
                    addBookToBookShelf(book);
                    moveList.add(new LibraryMoveInfo(book.getId(), "ao", "bs"));
                }
            }
        }
        for (Reservation reservation : reservationRecord) {
            LibraryBookId bookId = reservation.getBookId();
            if (!bookShelf.get(bookId).isEmpty()) {
                Book reserved = getBookFromBookShelf(bookId);
                addBookToReservationOffice(reserved);
                reserved.reserve(date.plusDays(1), reservation.getPersonId());
                moveList.add(new LibraryMoveInfo(bookId, "bs", "ao", reservation.getPersonId()));
            }
        }
        reservationRecord.clear();
        PRINTER.move(date, moveList);
    }

    public void execute(LibraryRequest request) {
        String studentId = request.getStudentId();
        LibraryBookId bookId = request.getBookId();
        if (!persons.containsKey(studentId)) {
            persons.put(studentId, new Person());
        }
        assert bookShelf.containsKey(bookId);
        switch (request.getType()) {
            case QUERIED:
                queries(bookId);
                break;
            case BORROWED:
                borrows(request, studentId, bookId);
                break;
            case ORDERED:
                orders(request, studentId, bookId);
                break;
            case RETURNED:
                returns(request, studentId, bookId);
                break;
            case PICKED:
                picks(request, studentId, bookId);
                break;
            default:
                break;
        }
    }

    public void queries(LibraryBookId bookId) {
        PRINTER.info(date, bookId, bookShelf.get(bookId).size());
    }

    public void borrows(LibraryRequest request, String studentId, LibraryBookId bookId) {
        if (bookId.isTypeA() || (bookShelf.get(bookId).isEmpty())) {
            PRINTER.reject(date, request);
        } else {
            Book target = getBookFromBookShelf(bookId);
            if (persons.get(studentId).canBorrowTypeBC(bookId)) {
                PRINTER.accept(date, request);
                persons.get(studentId).addBook(target);
            } else {
                PRINTER.reject(date, request);
                addBookToLendingOffice(target);
            }
        }
    }

    public void orders(LibraryRequest request, String studentId, LibraryBookId bookId) {
        if (bookId.isTypeA() || !persons.get(studentId).canBorrowTypeBC(bookId)) {
            PRINTER.reject(date, request);
        } else {
            PRINTER.accept(date, request);
            addReservationRecord(new Reservation(studentId, bookId));
        }
    }

    public void returns(LibraryRequest request, String studentId, LibraryBookId bookId) {
        addBookToLendingOffice(persons.get(studentId).returnBook(bookId));
        PRINTER.accept(date, request);
    }

    public void picks(LibraryRequest request, String studentId, LibraryBookId bookId) {
        Book picked = null;
        for (Book book : reservationOffice.get(bookId)) {
            if (book.getPersonId().equals(studentId)
                    && (picked == null || picked.getDate().isAfter(book.getDate()))) {
                picked = book;
            }
        }
        if (picked == null || !persons.get(studentId).canBorrowTypeBC(bookId)) {
            PRINTER.reject(date, request);
        } else {
            PRINTER.accept(date, request);
            reservationOffice.get(bookId).remove(picked);
            persons.get(studentId).addBook(picked);
        }
    }

    private Book getBookFromBookShelf(LibraryBookId bookId) {
        Book target = bookShelf.get(bookId).get(0);
        bookShelf.get(bookId).remove(0);
        return target;
    }

    private void addBookToBookShelf(Book book) {
        bookShelf.get(book.getId()).add(book);
    }

    private void addBookToLendingOffice(Book book) {
        lendingOffice.get(book.getId()).add(book);
    }

    private void addBookToReservationOffice(Book book) {
        reservationOffice.get(book.getId()).add(book);
    }

    private void addReservationRecord(Reservation reservation) {
        reservationRecord.add(reservation);
    }
}
