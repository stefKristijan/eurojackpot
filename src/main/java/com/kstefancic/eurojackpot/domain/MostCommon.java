package com.kstefancic.eurojackpot.domain;

import java.util.HashSet;
import java.util.Set;

public class MostCommon {

    Set<Integer> numbers;
    int drawn;

    public MostCommon() {
    }

    public MostCommon(Set<Integer> numbers) {
        this.numbers = new HashSet<>(numbers);
        this.drawn = 0;
    }

    public MostCommon(Integer n1, Integer n2, Integer n3, Integer n4, Integer n5, Integer n6, Integer n7, Integer n8, Integer n9, Integer n10,
                      Integer n11, Integer n12, Integer n13, Integer n14, Integer n15, Integer n16, Integer n17, Integer n18, Integer n19, Integer n20, Integer c) {
        this.numbers = new HashSet<>();
        this.numbers.add(n1);
        this.numbers.add(n2);
        if (n3 != null) this.numbers.add(n3);
        if (n4 != null) this.numbers.add(n3);
        if (n5 != null) this.numbers.add(n3);
        if (n6 != null) this.numbers.add(n3);
        if (n7 != null) this.numbers.add(n3);
        if (n8 != null) this.numbers.add(n3);
        if (n9 != null) this.numbers.add(n3);
        if (n10 != null) this.numbers.add(n3);
        if (n11 != null) this.numbers.add(n3);
        if (n12 != null) this.numbers.add(n3);
        if (n13 != null) this.numbers.add(n3);
        if (n14 != null) this.numbers.add(n3);
        if (n15 != null) this.numbers.add(n3);
        if (n16 != null) this.numbers.add(n3);
        if (n17 != null) this.numbers.add(n3);
        if (n18 != null) this.numbers.add(n3);
        if (n19 != null) this.numbers.add(n3);
        if (n20 != null) this.numbers.add(n3);
        this.drawn = c;
    }

    public Set<Integer> getNumbers() {
        return numbers;
    }

    public int getDrawn() {
        return drawn;
    }
}
