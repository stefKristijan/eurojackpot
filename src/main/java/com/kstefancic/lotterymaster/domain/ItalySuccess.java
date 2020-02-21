package com.kstefancic.lotterymaster.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class ItalySuccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime time;

    private Integer generatedNum;
    private String generator;
    private Boolean success;

    private Integer generatedRange;
    private String generatorRange;
    private Boolean successRange;

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public Integer getGeneratedRange() {
        return generatedRange;
    }

    public void setGeneratedRange(Integer generatedRange) {
        this.generatedRange = generatedRange;
    }

    public String getGeneratorRange() {
        return generatorRange;
    }

    public void setGeneratorRange(String generatorRange) {
        this.generatorRange = generatorRange;
    }

    public Boolean getSuccessRange() {
        return successRange;
    }

    public void setSuccessRange(Boolean successRange) {
        this.successRange = successRange;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Integer getGeneratedNum() {
        return generatedNum;
    }

    public void setGeneratedNum(Integer generatedNum) {
        this.generatedNum = generatedNum;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
