import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryQcsCmd;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.annotation.SendMessage;
import com.oocourse.library3.annotation.Trigger;

import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.oocourse.library3.LibrarySystem.PRINTER;

public class Library {
    private LocalDate date;
    private final HashMap<String, Person> persons;
    private final HashMap<LibraryBookId, ArrayList<Book>> bookShelf;
    private final HashMap<LibraryBookId, ArrayList<Book>> bookCorner;
    private final HashMap<LibraryBookId, ArrayList<Book>> lendingOffice;
    private final HashMap<LibraryBookId, ArrayList<Book>> reservationOffice;
    private final ArrayList<Reservation> reservationRecord;

    public Library(Map<LibraryBookId, Integer> initMap) {
        persons = new HashMap<>();
        bookShelf = new HashMap<>();
        bookCorner = new HashMap<>();
        lendingOffice = new HashMap<>();
        reservationOffice = new HashMap<>();
        reservationRecord = new ArrayList<>();
        initializeLibrary(initMap);
    }

    @Trigger(from = "InitState", to = "BookShelf")
    private void initializeLibrary(Map<LibraryBookId, Integer> initMap) {
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
        manageBooks(true);
    }

    public void close() {
        manageBooks(false);
    }

    @Trigger(from = "LendingOffice", to = {"BookShelf", "BookCorner"})
    @Trigger(from = "ReservationOffice", to = "BookShelf")
    @Trigger(from = "BookShelf", to = "ReservationOffice")
    private void manageBooks(boolean open) {
        if (!open) {
            for (Person person : persons.values()) {
                person.punishForOverdue(date);
            }
        } else {
            for (Person person : persons.values()) {
                person.punishForOverdue(date.plusDays(-1));
            }
        }
        List<LibraryMoveInfo> moveList = new ArrayList<>();
        HashMap<LibraryBookId, Book> alteredBooks = new HashMap<>();
        for (ArrayList<Book> array : lendingOffice.values()) {
            Iterator<Book> bookIterator = array.iterator();
            while (bookIterator.hasNext()) {
                Book book = bookIterator.next();
                bookIterator.remove();
                if (book.getId().isFormal()) {
                    addBookToBookShelf(book);
                    moveList.add(new LibraryMoveInfo(book.getId(), "bro", "bs"));
                } else {
                    if (book.isPopular()) {
                        persons.get(book.getDonator()).alterCredit(2);
                        alteredBooks.put(book.getId(), new Book(book.getId().toFormal()));
                        moveList.add(new LibraryMoveInfo(book.getId(), "bro", "bs"));
                    } else {
                        addBookToBookCorner(book);
                        moveList.add(new LibraryMoveInfo(book.getId(), "bro", "bdc"));
                    }
                }
            }
        }
        for (LibraryBookId informalId : alteredBooks.keySet()) {
            addFormalBook(alteredBooks.get(informalId).getId(), informalId);
            addBookToBookShelf(alteredBooks.get(informalId));
        }
        for (LibraryBookId id : reservationOffice.keySet()) {
            Iterator<Book> iterator = reservationOffice.get(id).iterator();
            while (iterator.hasNext()) {
                Book book = iterator.next();
                if (book.getReservationDate().until(date, ChronoUnit.DAYS) >= 5) {
                    iterator.remove();
                    persons.get(book.getReserver()).alterCredit(-3);
                    addBookToBookShelf(book);
                    moveList.add(new LibraryMoveInfo(book.getId(), "ao", "bs"));
                }
            }
        }
        Iterator<Reservation> iterator = reservationRecord.iterator();
        while (iterator.hasNext()) {
            Reservation reservation = iterator.next();
            LibraryBookId bookId = reservation.getBookId();
            if (!bookShelf.get(bookId).isEmpty()) {
                iterator.remove();
                Book reserved = getBookFromBookShelf(bookId);
                addBookToReservationOffice(reserved);
                reserved.reserve(date.plusDays(open ? 0 : 1), reservation.getPersonId());
                moveList.add(new LibraryMoveInfo(bookId, "bs", "ao", reservation.getPersonId()));
            }
        }
        PRINTER.move(date, moveList);
    }

    public void execute(LibraryReqCmd command) {
        String studentId = command.getStudentId();
        LibraryBookId bookId = command.getBookId();
        if (!persons.containsKey(studentId)) {
            persons.put(studentId, new Person());
        }
        assert bookShelf.containsKey(bookId);
        switch (command.getType()) {
            case QUERIED:
                queries(bookId);
                break;
            case BORROWED:
                borrows(command, studentId, bookId);
                break;
            case ORDERED:
                orderNewBook(command, studentId, bookId);
                break;
            case RETURNED:
                returns(command, studentId, bookId);
                break;
            case PICKED:
                picks(command, studentId, bookId);
                break;
            case DONATED:
                donates(command, bookId);
                break;
            case RENEWED:
                renews(command, studentId, bookId);
                break;
            default:
                break;
        }
    }

    public void query(LibraryQcsCmd command) {
        String studentId = command.getStudentId();
        if (!persons.containsKey(studentId)) {
            persons.put(studentId, new Person());
        }
        PRINTER.info(command, persons.get(studentId).getCredit());
    }

    private void queries(LibraryBookId bookId) {
        if (bookId.isFormal()) {
            PRINTER.info(date, bookId, bookShelf.get(bookId).size());
        } else {
            PRINTER.info(date, bookId, bookCorner.get(bookId).size());
        }
    }

    @Trigger(from = "BookShelf", to = {"Borrowed", "LendingOffice"})
    @Trigger(from = "BookCorner", to = {"Borrowed", "LendingOffice"})
    private void borrows(LibraryReqCmd command, String studentId, LibraryBookId bookId) {
        Book target = null;
        if (!bookId.isTypeA() && !bookId.isTypeAU()) {
            if (bookId.isFormal() && !bookShelf.get(bookId).isEmpty()) {
                target = getBookFromBookShelf(bookId);
            } else if (!bookId.isFormal() && !bookCorner.get(bookId).isEmpty()) {
                target = getBookFromBookCorner(bookId);
            }
        }
        if (target != null) {
            if (persons.get(studentId).canBorrow(bookId)
                    && persons.get(studentId).hasGoodCredit()) {
                PRINTER.accept(command);
                persons.get(studentId).getOrderedBook(target);
                target.borrow(date);
            } else {
                PRINTER.reject(command);
                addBookToLendingOffice(target);
            }
        } else {
            PRINTER.reject(command);
        }
    }

    @SendMessage(from = "Library", to = "Person")
    private void orderNewBook(LibraryReqCmd command, String studentId, LibraryBookId bookId) {
        if (bookId.isFormal() && persons.get(studentId).canBorrow(bookId)
                && persons.get(studentId).hasGoodCredit()
                && noSimilarReservations(bookId, studentId)) {
            PRINTER.accept(command);
            addReservationRecord(new Reservation(studentId, bookId));
        } else {
            PRINTER.reject(command);
        }
    }

    private boolean noSimilarReservations(LibraryBookId bookId, String personId) {
        if (bookId.isTypeB()) {
            for (Reservation reservation : reservationRecord) {
                if (reservation.getPersonId().equals(personId)
                        && reservation.getBookId().isTypeB()) {
                    return false;
                }
            }
            for (ArrayList<Book> arrayList : reservationOffice.values()) {
                for (Book book : arrayList) {
                    if (book.getReserver().equals(personId) && book.getId().isTypeB()) {
                        return false;
                    }
                }
            }
        } else if (bookId.isTypeC()) {
            for (Reservation reservation : reservationRecord) {
                if (reservation.getPersonId().equals(personId)
                        && reservation.getBookId().equals(bookId)) {
                    return false;
                }
            }
            for (Book book : reservationOffice.get(bookId)) {
                if (book.getReserver().equals(personId)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Trigger(from = "Borrowed", to = "LendingOffice")
    private void returns(LibraryReqCmd command, String studentId, LibraryBookId bookId) {
        Book returned = persons.get(studentId).returnBook(bookId);
        addBookToLendingOffice(returned);
        if (returned.getDeadline().isBefore(date)) {
            PRINTER.accept(command, "overdue");
        } else {
            persons.get(command.getStudentId()).alterCredit(1);
            PRINTER.accept(command, "not overdue");
        }
    }

    @Trigger(from = "ReservationOffice", to = "Borrowed")
    private void picks(LibraryReqCmd command, String studentId, LibraryBookId bookId) {
        Book picked = null;
        for (Book book : reservationOffice.get(bookId)) {
            if (book.getReserver().equals(studentId) && (picked == null ||
                    picked.getReservationDate().isAfter(book.getReservationDate()))) {
                picked = book;
            }
        }
        if (picked == null || !persons.get(studentId).canBorrow(bookId)) {
            PRINTER.reject(command);
        } else {
            PRINTER.accept(command);
            reservationOffice.get(bookId).remove(picked);
            persons.get(studentId).getOrderedBook(picked);
            picked.borrow(date);
        }
    }

    @Trigger(from = "InitState", to = "BookCorner")
    private void donates(LibraryReqCmd command, LibraryBookId bookId) {
        addInformalBook(bookId);
        addBookToBookCorner(new Book(bookId, command.getStudentId()));
        persons.get(command.getStudentId()).alterCredit(2);
        PRINTER.accept(command);
    }

    @Trigger(from = "Borrowed", to = "Borrowed")
    private void renews(LibraryReqCmd command, String studentId, LibraryBookId bookId) {
        Person person = persons.get(studentId);
        if (bookId.isFormal() && person.canRenews(bookId, date) && person.hasGoodCredit()
                && (!someoneHasReserved(bookId) || !bookShelf.get(bookId).isEmpty())) {
            PRINTER.accept(command);
            person.renews(bookId);
        } else {
            PRINTER.reject(command);
        }
    }

    private Book getBookFromBookShelf(LibraryBookId bookId) {
        Book target = bookShelf.get(bookId).get(0);
        bookShelf.get(bookId).remove(0);
        return target;
    }

    private Book getBookFromBookCorner(LibraryBookId bookId) {
        Book target = bookCorner.get(bookId).get(0);
        bookCorner.get(bookId).remove(0);
        return target;
    }

    private void addBookToBookShelf(Book book) {
        bookShelf.get(book.getId()).add(book);
    }

    private void addBookToBookCorner(Book book) {
        bookCorner.get(book.getId()).add(book);
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

    private void addInformalBook(LibraryBookId informalBookId) {
        bookCorner.put(informalBookId, new ArrayList<>());
        lendingOffice.put(informalBookId, new ArrayList<>());
    }

    private void addFormalBook(LibraryBookId formalBookId, LibraryBookId informalBookId) {
        bookCorner.remove(informalBookId);
        lendingOffice.remove(informalBookId);
        bookShelf.put(formalBookId, new ArrayList<>());
        lendingOffice.put(formalBookId, new ArrayList<>());
        reservationOffice.put(formalBookId, new ArrayList<>());
    }

    private boolean someoneHasReserved(LibraryBookId bookId) {
        for (Reservation reservation : reservationRecord) {
            if (reservation.getBookId().equals(bookId)) {
                return true;
            }
        }
        return false;
    }
}
