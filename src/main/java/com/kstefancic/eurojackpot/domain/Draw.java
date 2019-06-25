package com.kstefancic.eurojackpot.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SqlResultSetMapping(
        name = "MostCommonResult",
        classes = {
                @ConstructorResult(
                        targetClass = MostCommon.class,
                        columns = {
                                @ColumnResult(name = "n1", type = Integer.class),
                                @ColumnResult(name = "n2", type = Integer.class),
                                @ColumnResult(name = "n3", type = Integer.class),
                                @ColumnResult(name = "n4", type = Integer.class),
                                @ColumnResult(name = "n5", type = Integer.class),
                                @ColumnResult(name = "n6", type = Integer.class),
                                @ColumnResult(name = "n7", type = Integer.class),
                                @ColumnResult(name = "n8", type = Integer.class),
                                @ColumnResult(name = "n9", type = Integer.class),
                                @ColumnResult(name = "n10", type = Integer.class),
                                @ColumnResult(name = "n11", type = Integer.class),
                                @ColumnResult(name = "n12", type = Integer.class),
                                @ColumnResult(name = "n13", type = Integer.class),
                                @ColumnResult(name = "n14", type = Integer.class),
                                @ColumnResult(name = "n15", type = Integer.class),
                                @ColumnResult(name = "n16", type = Integer.class),
                                @ColumnResult(name = "n17", type = Integer.class),
                                @ColumnResult(name = "n18", type = Integer.class),
                                @ColumnResult(name = "n19", type = Integer.class),
                                @ColumnResult(name = "n20", type = Integer.class),
                                @ColumnResult(name = "c", type = Integer.class)
                        }
                )
        }
)
@Entity
@Table(name = "draws")
public class Draw {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(updatable = false, nullable = false)
    private LocalDateTime time;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lottery_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private Lottery lottery;

    public Draw(){}

    public Draw(LocalDateTime time, List<Integer> numbers, List<Integer> extraNums) {
        this.time = time;
        Collections.sort(numbers);
        Collections.sort(extraNums);
        this.numbers = new ArrayList<>(numbers);
        this.extraNumbers = new ArrayList<>(extraNums);
    }

    public Draw(LocalDateTime time, List<Integer> numbers) {
        this.time = time;
        Collections.sort(numbers);
        this.numbers = new ArrayList<>(numbers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Draw draw = (Draw) o;

        if (!Objects.equals(id, draw.id)) return false;
        if (!time.equals(draw.time)) return false;
        if (!numbers.equals(draw.numbers)) return false;
        if (!Objects.equals(extraNumbers, draw.extraNumbers)) return false;
        return Objects.equals(lottery, draw.lottery);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + time.hashCode();
        result = 31 * result + numbers.hashCode();
        result = 31 * result + (extraNumbers != null ? extraNumbers.hashCode() : 0);
        result = 31 * result + (lottery != null ? lottery.hashCode() : 0);
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public void setNumbers(List<Integer> numbers) {
        this.numbers = numbers;
    }

    public void setExtraNumbers(List<Integer> extraNumbers) {
        this.extraNumbers = extraNumbers;
    }

    public Lottery getLottery() {
        return lottery;
    }

    public void setLottery(Lottery lottery) {
        this.lottery = lottery;
    }

    public List<Integer> getExtraNumbers() {
        extraNumbers.sort(Integer::compareTo);
        return extraNumbers;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public List<Integer> getNumbers() {
        numbers.sort(Integer::compareTo);
        return numbers;
    }
}
