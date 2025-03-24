package uz.event.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    private String chatId;
    private String userName;
    private ArrayList<String> eventIds;
}
