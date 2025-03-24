package uz.event.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class History {
    private String id;
    private Long userId;
    private String userName;
    private String date;
    private HistoryState historyState;
}
