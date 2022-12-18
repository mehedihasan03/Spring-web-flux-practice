package com.example.domain;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@With
@Table(name = "anime")
public class Anime {

    @Id
    private long id;

    @NotNull
    @NotEmpty(message = "Name can not be empty")
    private String name;
}
