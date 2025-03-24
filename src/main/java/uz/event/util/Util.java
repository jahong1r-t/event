package uz.event.util;

import static uz.event.util.Button.*;

public interface Util {
    String[][] adminMain = {
            {addEvent, showEvent},
            {editEvent, deleteEvent},
            {history},
    };
    String[][] userMain = {
            {showEventUser, eventInfo},
            {buyTicket, upcomingEvents},
            {cancelEvent, myEvents},
            {balance}
    };
}
