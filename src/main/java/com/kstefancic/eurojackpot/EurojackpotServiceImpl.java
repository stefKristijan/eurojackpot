package com.kstefancic.eurojackpot;

import com.kstefancic.eurojackpot.domain.Draw;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class EurojackpotServiceImpl implements EurojackpotService {

    private final DrawRepository drawRepository;

    public EurojackpotServiceImpl(DrawRepository drawRepository) {
        this.drawRepository = drawRepository;
    }

    @Override
    public List<Draw> updateDraws() {
        List<Document> documents = new ArrayList<>();
        try {
            LocalDate date = drawRepository.latestDrawDate();
            if (date == null) {
                date = LocalDate.of(2012, 1, 1);
            }
            for (int year = date.getYear(); year <= LocalDate.now().getYear(); year++) {
                documents.add(Jsoup.connect(
                        String.format("https://www.euro-jackpot.net/hr/rezultati-arhiva-%d", year)
                ).get());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while downloading data");
        }

        List<Draw> draws = new ArrayList<>();
        for (Document document : documents) {
            Elements drawEls = document.select("table tbody tr td");
            LocalDate date = null;
            for (int i = 0; i < drawEls.size(); i++) {
                if (i % 2 == 0) {
                    date = LocalDate.parse(
                            drawEls.get(i).select("a").attr("href").split("/")[3],
                            DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    if (drawRepository.findByDate(date).isPresent())
                        i ++;
                } else {
                    List<Integer> nums = new ArrayList<>();
                    List<Integer> extra = new ArrayList<>();
                    for (Element numbers : drawEls.get(i).select("ul li.ball span"))
                        nums.add(Integer.parseInt(numbers.text()));
                    for (Element extraNum : drawEls.get(i).select("ul li.euro span"))
                        extra.add(Integer.parseInt(extraNum.text()));

                    draws.add(new Draw(date, nums, extra));
                    nums.clear();
                }
            }
        }
        return drawRepository.saveAll(draws);
    }

    @Override
    public List<Draw> findAll() {
        return drawRepository.listAllByDateDesc();
    }
}
