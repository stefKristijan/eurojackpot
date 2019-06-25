package com.kstefancic.eurojackpot;

import com.kstefancic.eurojackpot.domain.Draw;
import com.kstefancic.eurojackpot.domain.Lottery;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

@RunWith(SpringRunner.class)
@SpringBootTest
public class LotteryMasterApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
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
