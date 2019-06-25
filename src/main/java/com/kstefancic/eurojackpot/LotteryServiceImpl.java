package com.kstefancic.eurojackpot;

import com.kstefancic.eurojackpot.domain.Constants;
import com.kstefancic.eurojackpot.domain.Draw;
import com.kstefancic.eurojackpot.domain.Lottery;
import com.kstefancic.eurojackpot.repository.DrawRepository;
import com.kstefancic.eurojackpot.repository.LotteryRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.kstefancic.eurojackpot.domain.Constants.*;

@Service
@Transactional
public class LotteryServiceImpl implements LotteryService {

    private final DrawRepository drawRepository;
    private final LotteryRepository lotteryRepository;

    public LotteryServiceImpl(DrawRepository drawRepository, LotteryRepository lotteryRepository) {
        this.drawRepository = drawRepository;
        this.lotteryRepository = lotteryRepository;
    }

    @Override
    public List<Draw> updateDraws() {
        List<Document> documents = new ArrayList<>();
        Lottery lottery = lotteryRepository.findByUniqueName(EUROJACKPOT).orElseGet(() -> {
            Lottery lottery1 = new Lottery();
            lottery1.setUniqueName(EUROJACKPOT);
            lottery1.setName(EUROJACKPOT);
            lottery1.setDraw(5);
            lottery1.setMaxNumber(50);
            lottery1.setExtraDraw(2);
            lottery1.setMaxExtraNumber(10);
            return lotteryRepository.save(lottery1);
        });
        try {
            LocalDateTime time = drawRepository.latestDrawTime(lottery.getId());
            if (time == null) {
                time = LocalDateTime.of(2012, 1, 1, 20, 0);
            }
            for (int year = time.getYear(); year <= LocalDate.now().getYear(); year++) {
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
                    if (drawRepository.getByDateAndLotteryId(date.toString(), lottery.getId()).isPresent())
                        i++;
                } else {
                    List<Integer> nums = new ArrayList<>();
                    List<Integer> extra = new ArrayList<>();
                    for (Element numbers : drawEls.get(i).select("ul li.ball span"))
                        nums.add(Integer.parseInt(numbers.text()));
                    for (Element extraNum : drawEls.get(i).select("ul li.euro span"))
                        extra.add(Integer.parseInt(extraNum.text()));

                    draws.add(new Draw(LocalDateTime.of(date, LocalTime.of(20, 0)), nums, extra));
                    nums.clear();
                }
            }
        }

        draws.forEach(d -> d.setLottery(lottery));
        initializeLotteries();
        return drawRepository.saveAll(draws);
    }

    public void initializeLotteries() {
        Optional<Lottery> lotteryOpt = lotteryRepository.findByUniqueName(LOTO_6_OD_45_UK);
        if (!lotteryOpt.isPresent()) {
            Lottery lottery = new Lottery();
            lottery.setName(LOTO_6_OD_45);
            lottery.setUniqueName(LOTO_6_OD_45_UK);
            lottery.setMaxNumber(45);
            lottery.setDraw(6);
            lottery.setExtraDraw(1);
            lottery.setMaxExtraNumber(45);
            List<Draw> drawsFromCsv = getDrawsFromCsv("Loto6od452018.csv", lottery.getDraw(), lottery.getExtraDraw());
            Lottery saved = lotteryRepository.save(lottery);
            drawsFromCsv.forEach(d -> d.setLottery(saved));
            List<Draw> draws2019 = getDrawsFromCsv("Loto6od452019.csv", lottery.getDraw(), lottery.getExtraDraw());
            draws2019.forEach(d -> d.setLottery(saved));
            drawRepository.saveAll(drawsFromCsv);
            drawRepository.saveAll(draws2019);
        }
    }

    private List<Draw> getDrawsFromCsv(String fileName, int draw, int extraDraw) {
        List<Draw> draws = new ArrayList<>();
        try {
            InputStream is = new ClassPathResource("static/" + fileName).getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                List<Integer> numbers = new ArrayList<>();
                List<Integer> extraNumbers = new ArrayList<>();
                for (int i = 1; i < values.length - extraDraw; i++) {
                    numbers.add(Integer.parseInt(values[i]));
                }
                for (int i = values.length - extraDraw; i < values.length; i++) {
                    extraNumbers.add(Integer.parseInt(values[i]));
                }
                LocalDate parsedDate = LocalDate.parse(values[0], DateTimeFormatter.ofPattern("d.M.yyyy"));
                Draw drawObj = new Draw(LocalDateTime.of(parsedDate, LocalTime.of(20, 0)), numbers, extraNumbers);
                draws.add(drawObj);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        return draws;
    }

    @Override
    public List<Lottery> findAll() {
        return lotteryRepository.findAll();
    }

    @Override
    public Draw calculateDraw() {
        List<Integer> nums = new ArrayList<>();
        List<Integer> extraNums = new ArrayList<>();


        return null;
    }

    @Override
    public List<Draw> findByLottery(Integer lotteryId, Pageable pageReq) {
        Lottery lottery = lotteryRepository.findById(lotteryId)
                .orElseThrow(() -> new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));
        Pageable pageable = PageRequest.of(pageReq.getPageNumber(), pageReq.getPageSize(),
                Sort.by("date").descending());
        return drawRepository.findAllByLotteryId(lotteryId, pageable);
    }

    @Override
    public Lottery findById(Integer lotteryId) {
        return this.lotteryRepository.findById(lotteryId).orElseThrow(() ->
                new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));
    }
}
