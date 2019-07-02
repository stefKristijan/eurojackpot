package com.kstefancic.eurojackpot.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "lotteries")
public class Lottery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank
    private String name;
    @Column(unique = true, nullable = false)
    private String uniqueName;
    @NotNull
    private Integer maxNumber;
    @NotNull
    private Integer draw;
    private Integer maxExtraNumber;
    private Integer extraDraw;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "lottery")
    private List<Draw> draws;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "lottery")
    private List<ResultUrl> resultUrls;

    public Lottery(String name, String uniqueName, int draw, int maxNumber){
        this.name = name;
        this.uniqueName = uniqueName;
        this.draw = draw;
        this.maxNumber = maxNumber;
    }

    public Lottery(){}

    public void addDraw(Draw draw){
        if(this.draws == null){
            this.draws = new ArrayList<>();
        }
        this.draws.add(draw);
    }

    public boolean hasExtraNumbers(){
        return this.maxExtraNumber != null && this.extraDraw != null;
    }

    public List<Draw> getDraws() {
        return draws;
    }

    public void setDraws(List<Draw> draws) {
        this.draws = draws;
    }

    public List<ResultUrl> getResultUrls() {
        return resultUrls;
    }

    public void setResultUrls(List<ResultUrl> resultUrls) {
        this.resultUrls = resultUrls;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxNumber() {
        return maxNumber;
    }

    public void setMaxNumber(Integer maxNumber) {
        this.maxNumber = maxNumber;
    }

    public Integer getDraw() {
        return draw;
    }

    public void setDraw(Integer draw) {
        this.draw = draw;
    }

    public Integer getMaxExtraNumber() {
        return maxExtraNumber;
    }

    public void setMaxExtraNumber(Integer maxExtraNumber) {
        this.maxExtraNumber = maxExtraNumber;
    }

    public Integer getExtraDraw() {
        return extraDraw;
    }

    public void setExtraDraw(Integer extraDraw) {
        this.extraDraw = extraDraw;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lottery lottery = (Lottery) o;

        if (!Objects.equals(id, lottery.id)) return false;
        if (!name.equals(lottery.name)) return false;
        if (!maxNumber.equals(lottery.maxNumber)) return false;
        if (!draw.equals(lottery.draw)) return false;
        if (!Objects.equals(maxExtraNumber, lottery.maxExtraNumber))
            return false;
        return Objects.equals(extraDraw, lottery.extraDraw);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + maxNumber.hashCode();
        result = 31 * result + draw.hashCode();
        result = 31 * result + (maxExtraNumber != null ? maxExtraNumber.hashCode() : 0);
        result = 31 * result + (extraDraw != null ? extraDraw.hashCode() : 0);
        return result;
    }
}
