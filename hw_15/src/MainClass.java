import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryCloseCmd;
import com.oocourse.library3.LibraryCommand;
import com.oocourse.library3.LibraryOpenCmd;
import com.oocourse.library3.LibraryQcsCmd;
import com.oocourse.library3.LibraryReqCmd;

import java.util.Map;

import static com.oocourse.library3.LibrarySystem.SCANNER;

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
            } else if (command instanceof LibraryQcsCmd) {
                // 信用积分查询
                library.query((LibraryQcsCmd) command);
            } else {
                // 对指令进行处理
                library.execute((LibraryReqCmd) command);
            }
        }
    }
}
