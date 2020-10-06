package com.kstefancic.lotterymaster.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class WinForLifeSuccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime time;
    private Integer generatedOnSum;
    private Boolean successOnSum;

    private Integer generatedOnRange;
    private Boolean successOnRange;

    private Integer generatedOnMc;
    private Boolean successOnMc;

    private Integer generatedOnDraw;
    private Boolean successOnDraw;

    private Integer generatedOnSumDiv;
    private Boolean successOnSumDiv;

    private Integer generatedOnRangeDiv;
    private Boolean successOnRangeDiv;

    private Integer generatedOnMcDiv;
    private Boolean successOnMcDiv;

    private Integer generatedOnDrawDiv;
    private Boolean successOnDrawDiv;

    private Integer testNum;
    private Boolean successTest;

    private Integer testNum2;
    private Boolean successTest2;

    public Integer getTestNum() {
        return testNum;
    }

    public Integer getTestNum2() {
        return testNum2;
    }

    public void setTestNum2(Integer testNum2) {
        this.testNum2 = testNum2;
    }

    public Boolean getSuccessTest2() {
        return successTest2;
    }

    public void setSuccessTest2(Boolean successTest2) {
        this.successTest2 = successTest2;
    }

    public void setTestNum(Integer testNum) {
        this.testNum = testNum;
    }

    public Boolean getSuccessTest() {
        return successTest;
    }

    public void setSuccessTest(Boolean successTest) {
        this.successTest = successTest;
    }

    public Integer getGeneratedOnMc() {
        return generatedOnMc;
    }

    public void setGeneratedOnMc(Integer generatedOnMc) {
        this.generatedOnMc = generatedOnMc;
    }

    public Boolean getSuccessOnMc() {
        return successOnMc;
    }

    public void setSuccessOnMc(Boolean successOnMc) {
        this.successOnMc = successOnMc;
    }

    public Integer getGeneratedOnDraw() {
        return generatedOnDraw;
    }

    public void setGeneratedOnDraw(Integer generatedOnDraw) {
        this.generatedOnDraw = generatedOnDraw;
    }

    public Boolean getSuccessOnDraw() {
        return successOnDraw;
    }

    public void setSuccessOnDraw(Boolean successOnDraw) {
        this.successOnDraw = successOnDraw;
    }

    public Integer getGeneratedOnSumDiv() {
        return generatedOnSumDiv;
    }

    public void setGeneratedOnSumDiv(Integer generatedOnSumDiv) {
        this.generatedOnSumDiv = generatedOnSumDiv;
    }

    public Boolean getSuccessOnSumDiv() {
        return successOnSumDiv;
    }

    public void setSuccessOnSumDiv(Boolean successOnSumDiv) {
        this.successOnSumDiv = successOnSumDiv;
    }

    public Integer getGeneratedOnRangeDiv() {
        return generatedOnRangeDiv;
    }

    public void setGeneratedOnRangeDiv(Integer generatedOnRangeDiv) {
        this.generatedOnRangeDiv = generatedOnRangeDiv;
    }

    public Boolean getSuccessOnRangeDiv() {
        return successOnRangeDiv;
    }

    public void setSuccessOnRangeDiv(Boolean successOnRangeDiv) {
        this.successOnRangeDiv = successOnRangeDiv;
    }

    public Integer getGeneratedOnMcDiv() {
        return generatedOnMcDiv;
    }

    public void setGeneratedOnMcDiv(Integer generatedOnMcDiv) {
        this.generatedOnMcDiv = generatedOnMcDiv;
    }

    public Boolean getSuccessOnMcDiv() {
        return successOnMcDiv;
    }

    public void setSuccessOnMcDiv(Boolean successOnMcDiv) {
        this.successOnMcDiv = successOnMcDiv;
    }

    public Integer getGeneratedOnDrawDiv() {
        return generatedOnDrawDiv;
    }

    public void setGeneratedOnDrawDiv(Integer generatedOnDrawDiv) {
        this.generatedOnDrawDiv = generatedOnDrawDiv;
    }

    public Boolean getSuccessOnDrawDiv() {
        return successOnDrawDiv;
    }

    public void setSuccessOnDrawDiv(Boolean successOnDrawDiv) {
        this.successOnDrawDiv = successOnDrawDiv;
    }

    public Integer getGeneratedOnRange() {
        return generatedOnRange;
    }

    public void setGeneratedOnRange(Integer generatedOnRange) {
        this.generatedOnRange = generatedOnRange;
    }

    public Boolean getSuccessOnRange() {
        return successOnRange;
    }

    public void setSuccessOnRange(Boolean successOnRange) {
        this.successOnRange = successOnRange;
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

    public Integer getGeneratedOnSum() {
        return generatedOnSum;
    }

    public void setGeneratedOnSum(Integer generatedOnSum) {
        this.generatedOnSum = generatedOnSum;
    }

    public Boolean getSuccessOnSum() {
        return successOnSum;
    }

    public void setSuccessOnSum(Boolean successOnSum) {
        this.successOnSum = successOnSum;
    }
}
