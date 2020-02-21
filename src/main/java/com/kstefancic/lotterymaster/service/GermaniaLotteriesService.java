package com.kstefancic.lotterymaster.service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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

import static com.kstefancic.lotterymaster.domain.Constants.GERMANIA_LINK;
import static com.kstefancic.lotterymaster.domain.Constants.GRCKI_KINO;
import static com.kstefancic.lotterymaster.domain.Constants.GRCKI_KINO_UK;
import static com.kstefancic.lotterymaster.domain.Constants.GREEK_KINO_GERMANIA_NAME;
import static com.kstefancic.lotterymaster.domain.Constants.ITALIAN_KENO_GERMANIA_NAME;
import static com.kstefancic.lotterymaster.domain.Constants.TALIJANSKI_KENO;
import static com.kstefancic.lotterymaster.domain.Constants.TALIJANSKI_KENO_UK;

@Service
@Transactional
public class GermaniaLotteriesService {
    private static final Logger logger = LoggerFactory.getLogger(GermaniaLotteriesService.class);

    private final LotteryRepository lotteryRepository;
    private final DrawRepository drawRepository;

    public GermaniaLotteriesService(LotteryRepository lotteryRepository, DrawRepository drawRepository) {
        this.lotteryRepository = lotteryRepository;
        this.drawRepository = drawRepository;
    }

    public void updateDraws() {
        try {
            Document document = Jsoup.connect(GERMANIA_LINK).get();
            Lottery italianKeno = lotteryRepository.findByUniqueName(TALIJANSKI_KENO_UK).orElseGet(() ->
                lotteryRepository.save(new Lottery(TALIJANSKI_KENO, TALIJANSKI_KENO_UK, 20, 90))
            );
            Lottery greekKino = lotteryRepository.findByUniqueName(GRCKI_KINO_UK).orElseGet(() ->
                lotteryRepository.save(new Lottery(GRCKI_KINO, GRCKI_KINO_UK, 20, 80))
            );
            int italianSize = italianKeno.getDraws().size();
            int greekSize = greekKino.getDraws().size();
            fetchAndParseResults(document, greekKino, GREEK_KINO_GERMANIA_NAME);
            fetchAndParseResults(document, italianKeno, ITALIAN_KENO_GERMANIA_NAME);
            int sizeIT = italianKeno.getDraws().size() - italianSize;
            int sizeGR = greekKino.getDraws().size() - greekSize;
            if (sizeIT != 0 || sizeGR != 0)
                logger.debug("Added {} {} draws and {} {} draws", sizeIT, TALIJANSKI_KENO, sizeGR, GRCKI_KINO);
        } catch (Exception e) {
            logger.error("Error while connecting or parsing Germania lotteries", e);
        }
    }

    private void fetchAndParseResults(Document document, Lottery lottery, String germaniaLotteryName) {
        Element result = document.select(".result").stream()
            .filter(r -> r.select("span.game-name").text().equals(germaniaLotteryName.toUpperCase())).findFirst()
            .orElse(null);
        if (result != null) {
            Element otherResult = result.select("article.result .other-results").get(0);
            Elements results = otherResult.select("article.result-wrapper");
            for (int i = 0; i < results.size(); i++) {
                String dateStr = otherResult.select("span.date").get(i).text();
                LocalDateTime now = LocalDateTime.now();
                dateStr += "-" + now.getYear();
                LocalDateTime time = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM HH:mm-yyyy"));
                //Problem with no year -> if result (31.12 23:55) is fetched after midnight - year should be subtracted
                if (now.isBefore(time)) {
                    time = time.minusYears(1);
                    logger.info("Year shift back => now: " + now +", time: " + time);
                }
                if (!drawRepository.findByTimeAndLotteryId(time, lottery.getId()).isPresent()) {
                    List<Integer> numbers = otherResult.select("div.drawn_balls").get(i).select(".drawn_balls div").stream().map(ns -> Integer.parseInt(ns.text())).collect(Collectors.toList());
                    Draw draw = new Draw(time, numbers);
                    draw.setLottery(lottery);
                    drawRepository.save(draw);
                    lottery.addDraw(draw);
                }
            }
        } else {
            logger.error("Error while parsing lottery {}", lottery.getName());
        }
    }

    public void delete2DaysAgoLotteries() {
        Lottery greekLottery = lotteryRepository.findByUniqueName(GRCKI_KINO_UK)
            .orElseThrow(() -> new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));
        Lottery italianLottery = lotteryRepository.findByUniqueName(TALIJANSKI_KENO_UK)
            .orElseThrow(() -> new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));

        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        int gSizeBefore = greekLottery.getDraws().size();
        int iSizeBefore = italianLottery.getDraws().size();
        greekLottery.getDraws().removeIf(d -> d.getTime().toLocalDate().isBefore(twoDaysAgo));
        italianLottery.getDraws().removeIf(d -> d.getTime().toLocalDate().isBefore(twoDaysAgo));
        logger.info("Removed {} draws from Italian and {} draws from Greek lottery",
            iSizeBefore - italianLottery.getDraws().size(), gSizeBefore - greekLottery.getDraws().size());
    }
}
