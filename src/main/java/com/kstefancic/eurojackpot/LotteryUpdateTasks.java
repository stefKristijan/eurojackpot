package com.kstefancic.eurojackpot;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.kstefancic.eurojackpot.domain.Draw;
import com.kstefancic.eurojackpot.domain.Lottery;
import com.kstefancic.eurojackpot.repository.DrawRepository;
import com.kstefancic.eurojackpot.repository.LotteryRepository;
import com.kstefancic.eurojackpot.service.Loto6od45Service;
import com.kstefancic.eurojackpot.service.UpdateDrawsService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.kstefancic.eurojackpot.domain.Constants.LOTO_6_OD_45;
import static com.kstefancic.eurojackpot.domain.Constants.LOTO_6_OD_45_UK;

@Component
public class LotteryUpdateTasks {

    Logger logger = LoggerFactory.getLogger(LotteryUpdateTasks.class);

    private final Loto6od45Service loto6od45Service;

    public LotteryUpdateTasks(Loto6od45Service loto6od45Service) {
        this.loto6od45Service = loto6od45Service;
    }

    @Scheduled(cron = "* */30 20-23 * * 0,4")
    public void updateLoto6od45(){
        logger.info("Updating Loto 6 od 45 draws");
        loto6od45Service.updateDraws();
    }
}
