package com.kstefancic.lotterymaster.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.stream.Collectors;

import com.kstefancic.lotterymaster.domain.Draw;
import com.kstefancic.lotterymaster.domain.Lottery;
import com.kstefancic.lotterymaster.repository.DrawRepository;
import com.kstefancic.lotterymaster.repository.LotteryRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kstefancic.lotterymaster.domain.Constants.*;

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

    public void updateDraws(int days, boolean fullUpdate) {
        try {
            long start = System.currentTimeMillis();
            List<Lottery> allLotteries = lotteryRepository.findAll();
            allLotteries.removeIf(l -> l.getUniqueName().equals(LOTO_6_OD_45_UK) ||
                    l.getUniqueName().equals(LOTO_7_OD_35_UK) || l.getUniqueName().equals(EUROJACKPOT)
                    || l.getUniqueName().equals(GRCKI_KINO_UK) || l.getUniqueName().equals(TALIJANSKI_KENO_UK) || l.getUniqueName().equals(WIN_FOR_LIFE));

            LocalDate date = LocalDate.now();
            LocalDate tenDaysAgo = LocalDate.now().minusDays(days);
            int drawsAdded = 0;

            while (allLotteries.size() > 0 && date.isAfter(tenDaysAgo)) {
                if(System.currentTimeMillis() - start > 60000)
                    break;
                Document document = Jsoup.connect(String.format("https://www.psk.hr/Results/Lotto?date=%s", date.toString())).get();
                Elements rows = document.select(".result-row");
                for (int i = rows.size() - 1; i >= 0; i--) {
                    Element row = rows.get(i);
                    String name = row.select(".cell.name").text();
                    //Add all except the two that are drawing every 10minutes and CANADA MAX that has an error
                    if (!name.equals("Italija 10e Lotto 20/90") && !name.equals("Grčka Kino Lotto 20/80") && !name.equals("Canada Max 7/49") && !name.equals("Italija Win For Life 10/20")) {
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
                                drawsAdded++;
                            } else {
                                if (!fullUpdate)
                                    allLotteries.removeIf(l -> l.getId().equals(lottery.get().getId()));
                            }
                        }
                    }
                }
                date = date.minusDays(1);
            }
            logger.debug("Added {} draws from PSK results", drawsAdded);
        } catch (Exception e) {
            logger.error("Lotteries from PSK were not updated successfully!", e);
        }
    }
}
