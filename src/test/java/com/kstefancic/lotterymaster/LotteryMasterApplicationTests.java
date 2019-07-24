package com.kstefancic.lotterymaster;

import com.kstefancic.lotterymaster.domain.Draw;
import com.kstefancic.lotterymaster.domain.Lottery;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kstefancic.lotterymaster.domain.Constants.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LotteryMasterApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    @Ignore
    public void germaniaTest() throws IOException {
        Document document = Jsoup.connect(GERMANIA_LINK).get();
        Element greek = document.select(".result").stream()
            .filter(r -> r.select("span.game-name").text().equals(GREEK_KINO_GERMANIA_NAME.toUpperCase())).findFirst()
            .orElse(null);
        if (greek != null) {
            Element greekEl = greek.select("article.result .other-results").get(0);
            Elements results = greekEl.select("article.result-wrapper");
            for (int i = 0; i < results.size(); i++) {
                String dateStr = greekEl.select("span.date").get(i).text();
                LocalDateTime now = LocalDateTime.now();
                dateStr += "-" + now.getYear();
                LocalDateTime time = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM HH:mm-yyyy"));
                //Problem with no year -> if result (31.12 23:55) is fetched after midnight - year should be subtracted
                if (now.isBefore(time)) time = time.minusYears(1);
                List<Integer> numbers = greekEl.select("div.drawn_balls").get(i).select(".drawn_balls div").stream().map(ns -> Integer.parseInt(ns.text())).collect(Collectors.toList());
                Draw draw = new Draw(time, numbers);
            }
        }

    }

    @Test
    @Ignore
    public void testParsing() throws IOException {
        Instant t1 = Instant.now();
        Map<String, Lottery> lotteries = new HashMap<>();
        LocalDate date = LocalDate.of(2019, 1, 1);
        LocalDate today = LocalDate.now();
        while (date.isBefore(today) || date.isEqual(today)) {
            Document document = Jsoup.connect(String.format("https://www.psk.hr/Results/Lotto?date=%s", date.toString())).get();
            Elements rows = document.select(".result-row");
            for (Element row : rows) {
                String name = row.select(".cell.name").text();
                if (!name.equals("Italija 10e Lotto 20/90") && !name.equals("Grčka Kino Lotto 20/80")) {
                    LocalDateTime time = LocalDateTime.parse(row.select(".cell.date").text(), DateTimeFormatter.ofPattern("d.M.yyyy. H:mm:ss"));
                    List<Integer> numbers = Arrays.stream(
                        row.select(".cell.winning").text().split(",")).map(Integer::parseInt).collect(Collectors.toList()
                    );
                    String uniqueName = name.replaceAll("\\s+", "");
                    if (!lotteries.containsKey(uniqueName)) {
                        String[] draws = name.split(" ");
                        String drawnMax = draws[draws.length - 1];
                        int draw = Integer.parseInt(drawnMax.split("/")[0]);
                        int max = Integer.parseInt(drawnMax.split("/")[1]);
                        lotteries.put(
                            uniqueName,
                            new Lottery(name.replace(String.format(" %s", draws[draws.length - 1]), ""), uniqueName, draw, max)
                        );
                    }
                    lotteries.get(uniqueName).addDraw(new Draw(time, numbers));
                }
            }

            date = date.plusDays(1);
        }
        Instant i2 = Instant.now();
        System.out.println("Time needed: " + (i2.getEpochSecond() - t1.getEpochSecond()));
        lotteries.size();
    }

}
