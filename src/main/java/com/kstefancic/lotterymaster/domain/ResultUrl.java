package com.kstefancic.lotterymaster.domain;

import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
@Table(name = "result_urls")
public class ResultUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @URL
    private String url;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable =  false, name = "lottery_id", referencedColumnName = "id")
    private Lottery lottery;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Lottery getLottery() {
        return lottery;
    }

    public void setLottery(Lottery lottery) {
        this.lottery = lottery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResultUrl resultUrl = (ResultUrl) o;

        if (!Objects.equals(id, resultUrl.id)) return false;
        if (!url.equals(resultUrl.url)) return false;
        return Objects.equals(lottery, resultUrl.lottery);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + url.hashCode();
        result = 31 * result + (lottery != null ? lottery.hashCode() : 0);
        return result;
    }
}
