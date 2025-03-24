package uz.event.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class User {
    private Long chatId;
    private String userName;
    private ArrayList<String> eventIds;
    private Integer balance;
}