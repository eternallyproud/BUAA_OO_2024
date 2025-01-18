import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryCloseCmd;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.LibraryOpenCmd;
import com.oocourse.library2.LibraryReqCmd;

import java.util.Map;

import static com.oocourse.library2.LibrarySystem.SCANNER;

public class MainClass {
    public static void main(String[] args) {
        Map<LibraryBookId, Integer> initMap = SCANNER.getInventory();
        Library library = new Library(initMap);
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) {
                break;
            }
            if (command instanceof LibraryOpenCmd) {
                // 在开馆时做点什么
                library.open(command.getDate());
            } else if (command instanceof LibraryCloseCmd) {
                // 在闭馆时做点什么
                library.close();
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;
                // 对指令进行处理
                library.execute(req);
            }
        }
    }
}
