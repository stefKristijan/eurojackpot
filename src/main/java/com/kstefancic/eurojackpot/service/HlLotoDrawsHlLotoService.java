package com.kstefancic.eurojackpot.service;

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

@Service
@Transactional
public class HlLotoDrawsHlLotoService implements HlLotoDrawsService {

    private static final Logger logger = LoggerFactory.getLogger(HlLotoDrawsHlLotoService.class);

    private final LotteryRepository lotteryRepository;
    private final DrawRepository drawRepository;

    public HlLotoDrawsHlLotoService(DrawRepository drawRepository, LotteryRepository lotteryRepository) {
        this.drawRepository = drawRepository;
        this.lotteryRepository = lotteryRepository;
    }

    @Override
    public void updateDraws(String uniqueLotteryName, String lotteryResultUrl) {
        Optional<Lottery> lottery = lotteryRepository.findByUniqueName(uniqueLotteryName);
        if (!lottery.isPresent()) {
            logger.error(uniqueLotteryName + " does not exist. It can not be updated");
        } else {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime latestDrawTime = drawRepository.latestDrawTime(lottery.get().getId());
            if (latestDrawTime.toLocalDate().isBefore(now.toLocalDate())) {
                parseAndSaveNewDrawResult(lottery.get(), lotteryResultUrl);
            } else if (now.getHour() == 23) {
                logger.error("!!" + uniqueLotteryName + " draw WAS NOT UPDATED today on the source site!!");
            } else {
                logger.info(uniqueLotteryName + " was already updated today.");
            }
        }
    }

    private void parseAndSaveNewDrawResult(Lottery lottery, String lotteryResultUrl) {
        try {
            Document document = Jsoup.connect(lotteryResultUrl).get();
            String dateStr = document.select("p#date-info span").text();
            String[] parts = dateStr.split(" ");
            LocalDate date = LocalDate.parse(parts[parts.length - 1], DateTimeFormatter.ofPattern("dd.MM.yyyy."));
            if (drawRepository.getByDateAndLotteryId(date.toString(), lottery.getId()).isPresent()) {
                logger.info("Newest draw is already present in " + lottery.getUniqueName() + " draws");
            } else {
                LocalDateTime time = LocalDateTime.of(date, LocalTime.of(20, 0));
                List<Integer> numberList = document.select("div#winnings-info li").stream()
                        .map(li -> Integer.parseInt(li.text())).collect(Collectors.toList());
                List<Integer> extraNums = new ArrayList<>();
                extraNums.add(numberList.get(lottery.getDraw()));
                Draw draw = new Draw(time, numberList.subList(0, lottery.getDraw()), extraNums);
                draw.setLottery(lottery);
                drawRepository.save(draw);
                logger.info(lottery.getUniqueName() + " newest draw successfully saved");
            }
        } catch (IOException e) {
            logger.error("Error while connecting or parsing "+ lottery.getUniqueName() + " website", e);
        }
    }
}
