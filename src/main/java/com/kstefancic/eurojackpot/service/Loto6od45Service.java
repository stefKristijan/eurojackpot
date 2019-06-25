package com.kstefancic.eurojackpot.service;

import com.kstefancic.eurojackpot.LotteryUpdateTasks;
import com.kstefancic.eurojackpot.domain.Draw;
import com.kstefancic.eurojackpot.domain.Lottery;
import com.kstefancic.eurojackpot.repository.DrawRepository;
import com.kstefancic.eurojackpot.repository.LotteryRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.kstefancic.eurojackpot.domain.Constants.LOTO_6_OD_45_UK;

@Service
@Transactional
public class Loto6od45Service implements UpdateDrawsService {

    Logger logger = LoggerFactory.getLogger(LotteryUpdateTasks.class);

    private final LotteryRepository lotteryRepository;
    private final DrawRepository drawRepository;

    public Loto6od45Service(DrawRepository drawRepository, LotteryRepository lotteryRepository) {
        this.drawRepository = drawRepository;
        this.lotteryRepository = lotteryRepository;
    }

    @Override
    public void updateDraws() {
        Optional<Lottery> lottery = lotteryRepository.findByUniqueName(LOTO_6_OD_45_UK);
        if (!lottery.isPresent()) {
            logger.error("Loto 6 od 45 does not exist. It can not be updated");
        } else {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime latestDrawTime = drawRepository.latestDrawTime(lottery.get().getId());
            if (latestDrawTime.toLocalDate().isBefore(now.toLocalDate())) {
                Draw draw = parseNewDrawResult(lottery.get());
            } else if (now.getHour() == 23) {
                logger.error("!!Loto 6 od 45 draw WAS NOT UPDATED today on the source site!!");
            } else {
                logger.info("Loto 6 od 45 was already updated today.");
            }
        }
    }

    private Draw parseNewDrawResult(Lottery lottery) {
        try {
            Document document = Jsoup.connect("https://www.lutrija.hr/cms/loto6od45").get();
            String dateStr = document.select("p#date-info span").text();
            String[] parts = dateStr.split(" ");
            LocalDate date = LocalDate.parse(parts[parts.length - 1], DateTimeFormatter.ofPattern("dd.MM.yyyy."));
            if (drawRepository.getByDateAndLotteryId(date.toString(), lottery.getId()).isPresent()) {
                logger.info("Newest downloaded draw is already present in Loto 6 od 45 draws");
            } else {
                LocalDateTime time = LocalDateTime.of(date, LocalTime.of(20, 0));
                List<Integer> numberList = document.select("div#winnings-info li").stream()
                        .map(li -> Integer.parseInt(li.text())).collect(Collectors.toList());
                List<Integer> extraNums = new ArrayList<>();
                extraNums.add(numberList.get(lottery.getDraw()));
                Draw draw = new Draw(time, numberList.subList(0, lottery.getDraw()), extraNums);
                draw.setLottery(lottery);
                drawRepository.save(draw);
                logger.info("Loto 6 od 45 newest draw successfully saved");
            }
        } catch (IOException e) {
            logger.error("Error while connecting or parsing Loto 6 od 45 website");
        }
        return null;
    }
}
