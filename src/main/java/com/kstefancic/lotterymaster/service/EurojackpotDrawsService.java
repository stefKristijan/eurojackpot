package com.kstefancic.lotterymaster.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.kstefancic.lotterymaster.domain.Constants;
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

@Service
@Transactional
public class EurojackpotDrawsService {

    private static final Logger logger = LoggerFactory.getLogger(LotteryServiceImpl.class);

    private final DrawRepository drawRepository;
    private final LotteryRepository lotteryRepository;

    public EurojackpotDrawsService(DrawRepository drawRepository, LotteryRepository lotteryRepository) {
        this.drawRepository = drawRepository;
        this.lotteryRepository = lotteryRepository;
    }

    public void updateDraws(boolean onlyNew) {
        Optional<Lottery> lottery = lotteryRepository.findByUniqueName(Constants.EUROJACKPOT);
        if (!lottery.isPresent()) {
            logger.error("Eurojackpot does not exist. It can not be updated");
        } else {
            try {
                for(int year = LocalDateTime.now().getYear(); year >= (onlyNew ? 2020 : 2012); year--) {
                    Document document = Jsoup.connect(
                        String.format("https://www.euro-jackpot.net/hr/rezultati-arhiva-%d", year)
                    ).get();

                    Elements drawEls = document.select("table tbody tr td");
                    for (int i = 0; i < (onlyNew ? 1 : drawEls.size()); i += 2) {
                        LocalDate date = LocalDate.parse(
                            drawEls.get(i).select("a").attr("href").split("/")[3],
                            DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                        if (!drawRepository.getByDateAndLotteryId(date.toString(), lottery.get().getId()).isPresent()) {
                            List<Integer> nums = new ArrayList<>();
                            List<Integer> extra = new ArrayList<>();
                            for (Element numbers : drawEls.get(i + 1).select("ul li.ball span"))
                                nums.add(Integer.parseInt(numbers.text()));
                            for (Element extraNum : drawEls.get(i + 1).select("ul li.euro span"))
                                extra.add(Integer.parseInt(extraNum.text()));

                            Draw draw = new Draw(LocalDateTime.of(date, LocalTime.of(20, 0)), nums, extra);
                            draw.setLottery(lottery.get());
                            drawRepository.save(draw);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error while connecting or parsing Eurojackpot website", e);
            }
        }
    }
}
