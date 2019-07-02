package com.kstefancic.eurojackpot.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.kstefancic.eurojackpot.domain.Constants;
import com.kstefancic.eurojackpot.domain.Draw;
import com.kstefancic.eurojackpot.domain.Lottery;
import com.kstefancic.eurojackpot.repository.DrawRepository;
import com.kstefancic.eurojackpot.repository.LotteryRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kstefancic.eurojackpot.domain.Constants.*;

@Service
@Transactional
public class PskLotteriesService {

    private static final Logger logger = LoggerFactory.getLogger(PskLotteriesService.class);

    private final LotteryRepository lotteryRepository;
    private final DrawRepository drawRepository;

    public PskLotteriesService(LotteryRepository lotteryRepository, DrawRepository drawRepository) {
        this.lotteryRepository = lotteryRepository;
        this.drawRepository = drawRepository;
    }

    public void updateDraws() {
        try {
            List<Lottery> allLotteries = lotteryRepository.findAll();
            allLotteries.removeIf(l -> l.getUniqueName().equals(LOTO_6_OD_45_UK) ||
                l.getUniqueName().equals(LOTO_7_OD_35_UK) || l.getUniqueName().equals(EUROJACKPOT));

            LocalDate date = LocalDate.now();

            while (allLotteries.size() > 0) {
                Document document = Jsoup.connect(String.format("https://www.psk.hr/Results/Lotto?date=%s", date.toString())).get();
                Elements rows = document.select(".result-row");
                for (int i = rows.size() - 1; i >= 0; i--) {
                    Element row = rows.get(i);
                    String name = row.select(".cell.name").text();
                    //Add all except the two that are drawing every 10minutes and CANADA MAX that has an error
                    if (!name.equals("Italija 10e Lotto 20/90") && !name.equals("Grčka Kino Lotto 20/80") && !name.equals("Canada Max 7/49")) {
                        LocalDateTime time = LocalDateTime.parse(row.select(".cell.date").text(), DateTimeFormatter.ofPattern("d.M.yyyy. H:mm:ss"));
                        List<Integer> numbers = Arrays.stream(
                            row.select(".cell.winning").text().split(",")).map(Integer::parseInt).collect(Collectors.toList()
                        );
                        String uniqueName = name.replaceAll("\\s+", "");
                        Optional<Lottery> lottery = allLotteries.stream().filter(l -> l.getUniqueName().equals(uniqueName)).findFirst();
                        if (lottery.isPresent()) {
                            if (!drawRepository.findByTimeAndLotteryId(time, lottery.get().getId()).isPresent()) {
                                Draw d = new Draw(time, numbers);
                                d.setLottery(lottery.get());
                                lottery.get().addDraw(d);
                            } else {
                                allLotteries.removeIf(l -> l.getId().equals(lottery.get().getId()));
                            }
                        }
                    }
                }
                date = date.minusDays(1);
            }
        } catch (Exception e) {
            logger.error("Lotteries from PSK were not updated successfully!", e);
        }
    }
}
