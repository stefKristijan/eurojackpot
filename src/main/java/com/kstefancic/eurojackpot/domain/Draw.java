package com.kstefancic.eurojackpot.domain;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "draws")
public class Draw {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, updatable = false, nullable = false)
    private LocalDate date;
    @ElementCollection
    @CollectionTable(name = "numbers",
            joinColumns = @JoinColumn(name = "draw_id")
    )
    private List<Integer> numbers;
    @ElementCollection
    @CollectionTable(name = "extra_numbers",
            joinColumns = @JoinColumn(name = "draw_id")
    )
    private List<Integer> extraNumbers;

    public Draw(){}

    public Draw(LocalDate date, List<Integer> numbers, List<Integer> extraNums) {
        this.date = date;
        Collections.sort(numbers);
        Collections.sort(extraNums);
        this.numbers = new ArrayList<>(numbers);
        this.extraNumbers = new ArrayList<>(extraNums);
    }

    public List<Integer> getExtraNumbers() {
        return extraNumbers;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<Integer> getNumbers() {
        return numbers;
    }
}
