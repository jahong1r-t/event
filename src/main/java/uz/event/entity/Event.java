package uz.event.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Event {
    private String id;
    private String name;
    private String date;
    private String description;
    private Integer price;
    private Integer capacity;
    private Integer availableSpace;
}
