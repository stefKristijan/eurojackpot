package com.kstefancic.lotterymaster.service;

import javax.persistence.EntityNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.kstefancic.lotterymaster.domain.CoefficientStatistics;
import com.kstefancic.lotterymaster.domain.Constants;
import com.kstefancic.lotterymaster.domain.Draw;
import com.kstefancic.lotterymaster.domain.Generator;
import com.kstefancic.lotterymaster.domain.GeneratorSort;
import com.kstefancic.lotterymaster.domain.GeneratorType;
import com.kstefancic.lotterymaster.domain.ItalySuccess;
import com.kstefancic.lotterymaster.domain.Lottery;
import com.kstefancic.lotterymaster.domain.MostCommonStatistics;
import com.kstefancic.lotterymaster.domain.NumberStatistics;
import com.kstefancic.lotterymaster.domain.WinForLifeSuccess;
import com.kstefancic.lotterymaster.repository.DrawRepository;
import com.kstefancic.lotterymaster.repository.ItalySuccessRepository;
import com.kstefancic.lotterymaster.repository.LotteryRepository;
import com.kstefancic.lotterymaster.repository.WinForLifeSuccessRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kstefancic.lotterymaster.domain.Constants.EUROJACKPOT;
import static com.kstefancic.lotterymaster.domain.Constants.LOTO_6_OD_45;
import static com.kstefancic.lotterymaster.domain.Constants.LOTO_6_OD_45_UK;
import static com.kstefancic.lotterymaster.domain.Constants.LOTO_7_OD_35;
import static com.kstefancic.lotterymaster.domain.Constants.LOTO_7_OD_35_UK;

@Service
@Transactional
public class LotteryServiceImpl implements LotteryService {

    private static final Logger logger = LoggerFactory.getLogger(LotteryServiceImpl.class);
    private static int winForLifeAmount = 10;
    private static int greeceKinoAmount = 2;
    private final DrawRepository drawRepository;
    private final LotteryRepository lotteryRepository;
    private final WinForLifeSuccessRepository winForLifeSuccessRepository;
    private final ItalySuccessRepository italySuccessRepository;
    private final StatisticsService statisticsService;

    public LotteryServiceImpl(DrawRepository drawRepository, LotteryRepository lotteryRepository, WinForLifeSuccessRepository winForLifeSuccessRepository, ItalySuccessRepository italySuccessRepository, StatisticsService statisticsService) {
        this.drawRepository = drawRepository;
        this.lotteryRepository = lotteryRepository;
        this.winForLifeSuccessRepository = winForLifeSuccessRepository;
        this.italySuccessRepository = italySuccessRepository;
        this.statisticsService = statisticsService;
    }

    @Override
    public void initializeLotteries() {
        initializeLoto6od45();
        initializeLoto7od39();
        initializeEurojackpot();
        initializeLotteriesFromPsk();
    }

    private void initializeLotteriesFromPsk() {
        try {
            Map<String, Lottery> lotteries = new HashMap<>();
            LocalDate date = LocalDate.of(2019, 1, 1);
            LocalDate today = LocalDate.now();
            while (date.isBefore(today) || date.isEqual(today)) {
                Document document = Jsoup.connect(String.format("https://www.psk.hr/Results/Lotto?date=%s", date.toString())).get();
                Elements rows = document.select(".result-row");
                for (Element row : rows) {
                    String name = row.select(".cell.name").text();
                    //Add all except the two that are drawing every 10minutes and CANADA MAX that has an error
                    if (!name.equals("Italija 10e Lotto 20/90") && !name.equals("Grčka Kino Lotto 20/80") && !name.equals("Canada Max 7/49")) {
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
            lotteries.forEach((key, val) -> {
                Lottery lottery = lotteryRepository.findByUniqueName(key).orElseGet(() -> {
                    List<Draw> draws = val.getDraws();
                    val.setDraws(null);
                    Lottery saved = lotteryRepository.save(val);
                    saved.setDraws(draws);
                    return saved;
                });
                lottery.getDraws().forEach(d -> d.setLottery(lottery));
                drawRepository.saveAll(lottery.getDraws());
            });
        } catch (Exception e) {
            logger.error("Lotteries from PSK was not initialized successfully!", e);
            lotteryRepository.flush();
            drawRepository.flush();
        }
    }

    private void initializeLoto7od39() {
        try {
            Lottery lottery7od39 = lotteryRepository.findByUniqueName(LOTO_7_OD_35_UK).orElseGet(() -> {
                Lottery lottery = new Lottery();
                lottery.setName(LOTO_7_OD_35);
                lottery.setUniqueName(LOTO_7_OD_35_UK);
                lottery.setMaxNumber(35);
                lottery.setDraw(7);
                lottery.setExtraDraw(1);
                lottery.setMaxExtraNumber(35);
                return lotteryRepository.save(lottery);
            });
            parseAndSaveDraws(lottery7od39, "Loto7od392018.csv");
            parseAndSaveDraws(lottery7od39, "Loto7od392019.csv");
        } catch (Exception e) {
            logger.error("Loto 7 od 39 was not initialized successfully!", e);
            drawRepository.flush();
        }
    }

    private void initializeLoto6od45() {
        try {
            Lottery lottery6od45 = lotteryRepository.findByUniqueName(LOTO_6_OD_45_UK).orElseGet(() -> {
                Lottery lottery = new Lottery();
                lottery.setName(LOTO_6_OD_45);
                lottery.setUniqueName(LOTO_6_OD_45_UK);
                lottery.setMaxNumber(45);
                lottery.setDraw(6);
                lottery.setExtraDraw(1);
                lottery.setMaxExtraNumber(45);
                return lotteryRepository.save(lottery);
            });
            parseAndSaveDraws(lottery6od45, "Loto6od452018.csv");
            parseAndSaveDraws(lottery6od45, "Loto6od452019.csv");
        } catch (Exception e) {
            logger.error("Loto 6 od 45 was not initialized successfully!", e);
            drawRepository.flush();
        }
    }

    private void parseAndSaveDraws(Lottery lottery, String fileName) {
        List<Draw> draws = getDrawsFromCsv(fileName, lottery.getDraw(), lottery.getExtraDraw()).stream()
            .filter(d -> !drawRepository.getByDateAndLotteryId(d.getTime().toLocalDate().toString(), lottery.getId()).isPresent())
            .collect(Collectors.toList());
        draws.forEach(d -> d.setLottery(lottery));
        if (draws.size() > 0)
            drawRepository.saveAll(draws);
    }

    private void initializeEurojackpot() {
        try {
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
            LocalDateTime time = drawRepository.latestDrawTime(lottery.getId());
            if (time == null) {
                time = LocalDateTime.of(2015, 1, 1, 20, 0);
            }
            for (int year = time.getYear(); year <= LocalDate.now().getYear(); year++) {
                documents.add(Jsoup.connect(
                    String.format("https://www.euro-jackpot.net/hr/rezultati-arhiva-%d", year)
                ).get());
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
            drawRepository.saveAll(draws);
        } catch (Exception e) {
            logger.error("Eurojackpot was not initialized successfully!", e);
            drawRepository.flush();
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
    public List<Draw> findByLottery(Integer lotteryId, Pageable pageReq) {
        Lottery lottery = lotteryRepository.findById(lotteryId)
            .orElseThrow(() -> new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));
        Pageable pageable = PageRequest.of(pageReq.getPageNumber(), pageReq.getPageSize(),
            Sort.by("time").descending());
        return drawRepository.findAllByLotteryId(lotteryId, pageable);
    }

    @Override
    public Lottery findById(Integer lotteryId) {
        return this.lotteryRepository.findById(lotteryId).orElseThrow(() ->
            new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));
    }

    @Override
    public void addDrawToLottery(int lotteryId, Draw draw) {
        Lottery lottery = lotteryRepository.findById(lotteryId)
            .orElseThrow(() -> new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));

        draw.setLottery(lottery);
        drawRepository.save(draw);
    }

    @Override
    public void checkWinForLifeNumbers() {
        int winForLifeId = 62;
        winForLifeSuccessRepository.findFirstByOrderByIdDesc().ifPresent(win -> {
            if (win.getSuccessOnSum() == null) {
                List<Draw> draws = findByLottery(winForLifeId, PageRequest.of(0, 1));
                List<Integer> numbers = draws.get(0).getNumbers();
                win.setSuccessOnSum(numbers.contains(win.getGeneratedOnSum()));
                win.setSuccessOnRange(numbers.contains(win.getGeneratedOnRange()));
                win.setSuccessOnDraw(numbers.contains(win.getGeneratedOnDraw()));
                win.setSuccessOnMc(numbers.contains(win.getGeneratedOnMc()));

                win.setSuccessOnSumDiv(numbers.contains(win.getGeneratedOnSumDiv()));
                win.setSuccessOnRangeDiv(numbers.contains(win.getGeneratedOnRangeDiv()));
                win.setSuccessOnDrawDiv(numbers.contains(win.getGeneratedOnDrawDiv()));
                win.setSuccessOnMcDiv(numbers.contains(win.getGeneratedOnMcDiv()));

                win.setSuccessTest(numbers.contains(win.getTestNum()));

                win.setTime(draws.get(0).getTime());
                winForLifeSuccessRepository.save(win);
            }
        });
        Generator generator = new Generator();
        generator.setDrawnMultiplier(1.0);
        generator.setMcMultiplier(1.0);
        generator.setRangeMultiplier(1.0);
        generator.setType(GeneratorType.DRAW);
        generator.setDraws(10);
        generator.setDraws(50);

        WinForLifeSuccess win = new WinForLifeSuccess();
        String email = "kico206@gmail.com";

        win.setTestNum(calculateForWinForLife());

        generator.setSort(GeneratorSort.SUM);
        win.setGeneratedOnSum(statisticsService.nextDrawNumberCoefficients(winForLifeId, generator, email).getCoefficients().get(0).getNumber());

        generator.setSort(GeneratorSort.RANGE);
        win.setGeneratedOnRange(statisticsService.nextDrawNumberCoefficients(winForLifeId, generator, email).getCoefficients().get(0).getNumber());

        generator.setSort(GeneratorSort.DRAWS);
        win.setGeneratedOnDraw(statisticsService.nextDrawNumberCoefficients(winForLifeId, generator, email).getCoefficients().get(0).getNumber());

        generator.setSort(GeneratorSort.MC);
        win.setGeneratedOnMc(statisticsService.nextDrawNumberCoefficients(winForLifeId, generator, email).getCoefficients().get(0).getNumber());

        generator.setSort(GeneratorSort.SUM);
        generator.setLastDrawDivider(2.0);
        win.setGeneratedOnSumDiv(statisticsService.nextDrawNumberCoefficients(winForLifeId, generator, email).getCoefficients().get(0).getNumber());

        generator.setSort(GeneratorSort.RANGE);
        win.setGeneratedOnRangeDiv(statisticsService.nextDrawNumberCoefficients(winForLifeId, generator, email).getCoefficients().get(0).getNumber());

        generator.setSort(GeneratorSort.DRAWS);
        win.setGeneratedOnDrawDiv(statisticsService.nextDrawNumberCoefficients(winForLifeId, generator, email).getCoefficients().get(0).getNumber());

        generator.setSort(GeneratorSort.MC);
        win.setGeneratedOnMcDiv(statisticsService.nextDrawNumberCoefficients(winForLifeId, generator, email).getCoefficients().get(0).getNumber());

        winForLifeSuccessRepository.save(win);
    }


    @Override
    public void playWinForLife() {
        int number = calculateForWinForLife();
        try {
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            URL auth = new URL("https://germaniasport.hr/auth?username=kico206&password=30081994Germania.&fingerprint=3c35fe8ca2fc0f5ea50b84d95e17d9e9&isCasinoPage=false");
            HttpURLConnection authCon = (HttpURLConnection) auth.openConnection();
            authCon.setDoOutput(true);
            authCon.setRequestMethod("POST");
            authCon.setRequestProperty("Content-Type", "application/json");
            authCon.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36");
            authCon.addRequestProperty("Connection", "keep-alive");
            List<String> cookies = authCon.getHeaderFields().get("Set-Cookie");
            authCon.connect();

            URL url1 = new URL("https://germaniasport.hr/lotto-offer");
            authCon = (HttpURLConnection) url1.openConnection();
            authCon.setRequestMethod("GET");
            authCon.setRequestProperty("Content-Type", "application/json");
            authCon.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36");
            for (String cookie : cookies) {
                authCon.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
            }
            BufferedReader in1 = new BufferedReader(
                new InputStreamReader(authCon.getInputStream()));
            String inputLine1;
            StringBuffer content1 = new StringBuffer();
            while ((inputLine1 = in1.readLine()) != null) {
                content1.append(inputLine1);
            }
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(content1.toString());
            JsonObject jsonobj = root.getAsJsonObject();
            JsonArray offers = (JsonArray) jsonobj.get("offerNextNHours");
            JsonElement winForLifeElement = null;
            Iterator<JsonElement> offerIterator = offers.iterator();

            do {
                JsonElement next = offerIterator.next();
                if (next.getAsJsonObject().get("gameId").equals(new JsonPrimitive(1)))
                    winForLifeElement = next;
            } while (winForLifeElement == null && offerIterator.hasNext());

            if (winForLifeElement != null) {
                JsonArray events = (JsonArray) winForLifeElement.getAsJsonObject().get("lottoOffer");
                if (events.iterator().hasNext()) {
                    JsonElement eventId = events.iterator().next().getAsJsonObject().get("eventId");
                    int asInt = eventId.getAsInt();
                    String charset = "UTF-8";
                    String query = String.format("{\"amount\":%d,\"origin\":6,\"eventId\":%d,\"lottoTicketType\":false,\"numbers\":[%d],\"systemParams\":[1]}", winForLifeAmount, asInt, number);
                    URL playUrl = new URL("https://germaniasport.hr/payLottoTicket");
                    authCon = (HttpURLConnection) playUrl.openConnection();
                    authCon.setRequestMethod("POST");
                    authCon.setRequestProperty("Accept-Charset", charset);
                    authCon.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    authCon.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36");
                    authCon.addRequestProperty("X-Requested-With", "XMLHttpRequest");
                    for (String cookie : cookies) {
                        authCon.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                    }
                    authCon.setRequestProperty("origin", "https://germaniasport.hr");
                    authCon.setRequestProperty("referer", "https://germaniasport.hr/hr");
                    authCon.setRequestProperty("accept", "application/json, text/plain, */*");
                    authCon.setDoOutput(true);
                    try (OutputStream output = authCon.getOutputStream()) {
                        output.write(query.getBytes(charset));
                    }
                    BufferedReader in2 = new BufferedReader(
                        new InputStreamReader(authCon.getInputStream()));
                    String inputLine2;
                    StringBuffer content2 = new StringBuffer();
                    while ((inputLine2 = in2.readLine()) != null) {
                        content2.append(inputLine2);
                    }
                }
            }
            authCon.disconnect();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private int calculateForWinForLife() {
        NumberStatistics numberStatistics = statisticsService.lotteryNumberStats(64, 10);
        NumberStatistics numberStatistics20 = statisticsService.lotteryNumberStats(64, 20);
        MostCommonStatistics mostCommon = statisticsService.lotteryMostCommon(64, 2, 5, 2);
        Map<Integer, Double> result = new HashMap<>();
        numberStatistics.getStats().forEach(ns ->
            result.put(ns.getNumber(), ns.getDrawn() / 10 + 0.25 * (1 + ns.getCyclesNotDrawn()))
        );
        numberStatistics20.getStats().forEach(ns ->
            result.put(ns.getNumber(), ns.getDrawn() / 10 + 0.25 * (1 + ns.getCyclesNotDrawn()) * result.get(ns.getNumber()))
        );
        mostCommon.getMostCommonStats().forEach(mc -> {
            mc.getNumbers().forEach(n ->
                result.put(n, result.get(n) * (1.0 + (mc.getDrawn() / 5.0))));
        });
        AtomicInteger best = new AtomicInteger();
        AtomicReference<Double> currentScore = new AtomicReference<>((double) 0);
        result.forEach((k, v) -> {
            if (currentScore.get() < v) {
                currentScore.set(v);
                best.set(k);
            }
        });
        return best.get();
    }

    @Override
    public void playGreeceKinoLotto() {
        int greeceLotteryId = 67;
        Generator generator = new Generator();
        generator.setDrawnMultiplier(2.0);
        generator.setMcMultiplier(1.0);
        generator.setSort(GeneratorSort.RANGE);
        generator.setType(GeneratorType.DRAW);
        generator.setLastDrawDivider(1.0);
        generator.setMaxDraws(100);
        generator.setDraws(10);
        generator.setRangeMultiplier(1.0);
        CoefficientStatistics coeffs = statisticsService.nextDrawNumberCoefficients(greeceLotteryId, generator, "kico206@gmail.com");
        try {
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            URL auth = new URL("https://germaniasport.hr/auth?username=kico206&password=30081994Germania.&fingerprint=3c35fe8ca2fc0f5ea50b84d95e17d9e9&isCasinoPage=false");
            HttpURLConnection authCon = (HttpURLConnection) auth.openConnection();
            authCon.setDoOutput(true);
            authCon.setRequestMethod("POST");
            authCon.setRequestProperty("Content-Type", "application/json");
            authCon.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36");
            authCon.addRequestProperty("Connection", "keep-alive");
            List<String> cookies = authCon.getHeaderFields().get("Set-Cookie");
            authCon.connect();

            URL url1 = new URL("https://germaniasport.hr/lotto-offer");
            authCon = (HttpURLConnection) url1.openConnection();
            authCon.setRequestMethod("GET");
            authCon.setRequestProperty("Content-Type", "application/json");
            authCon.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36");
            for (String cookie : cookies) {
                authCon.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
            }
            BufferedReader in1 = new BufferedReader(
                new InputStreamReader(authCon.getInputStream()));
            String inputLine1;
            StringBuffer content1 = new StringBuffer();
            while ((inputLine1 = in1.readLine()) != null) {
                content1.append(inputLine1);
            }
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(content1.toString());
            JsonObject jsonobj = root.getAsJsonObject();
            JsonArray offers = (JsonArray) jsonobj.get("priorityLottoOffer");
            JsonElement winForLifeElement = null;
            Iterator<JsonElement> offerIterator = offers.iterator();

            do {
                JsonElement next = offerIterator.next();
                if (next.getAsJsonObject().get("gameId").equals(new JsonPrimitive(26)))
                    winForLifeElement = next;
            } while (winForLifeElement == null && offerIterator.hasNext());

            if (winForLifeElement != null) {
                JsonArray events = (JsonArray) winForLifeElement.getAsJsonObject().get("lottoOffer");
                if (events.iterator().hasNext()) {
                    JsonElement eventId = events.iterator().next().getAsJsonObject().get("eventId");
                    int asInt = eventId.getAsInt();
                    String charset = "UTF-8";
                    String query = String.format("{\"amount\":%d,\"origin\":6,\"eventId\":%d,\"lottoTicketType\":false,\"numbers\":[%d],\"systemParams\":[1]}", greeceKinoAmount, asInt, coeffs.getCoefficients().get(2).getNumber());
                    URL playUrl = new URL("https://germaniasport.hr/payLottoTicket");
                    authCon = (HttpURLConnection) playUrl.openConnection();
                    authCon.setRequestMethod("POST");
                    authCon.setRequestProperty("Accept-Charset", charset);
                    authCon.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    authCon.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36");
                    authCon.addRequestProperty("X-Requested-With", "XMLHttpRequest");
                    for (String cookie : cookies) {
                        authCon.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                    }
                    authCon.setRequestProperty("origin", "https://germaniasport.hr");
                    authCon.setRequestProperty("referer", "https://germaniasport.hr/hr");
                    authCon.setRequestProperty("accept", "application/json, text/plain, */*");
                    authCon.setDoOutput(true);
                    try (OutputStream output = authCon.getOutputStream()) {
                        output.write(query.getBytes(charset));
                    }
                    BufferedReader in2 = new BufferedReader(
                        new InputStreamReader(authCon.getInputStream()));
                    String inputLine2;
                    StringBuffer content2 = new StringBuffer();
                    while ((inputLine2 = in2.readLine()) != null) {
                        content2.append(inputLine2);
                    }
                }
            }
            authCon.disconnect();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void checkItaly2090Result() {
        int italy2090 = 66;
        italySuccessRepository.findFirstByOrderByIdDesc().ifPresent(win -> {
            if (win.getSuccess() == null) {
                List<Draw> draws = findByLottery(italy2090, PageRequest.of(0, 1));
                List<Integer> numbers = draws.get(0).getNumbers();
                win.setSuccess(numbers.contains(win.getGeneratedNum()));
                win.setSuccessRange(numbers.contains(win.getGeneratedRange()));
                List<Integer> gen = Arrays.stream(win.getGenerator().split(", ")).map(Integer::parseInt).collect(Collectors.toList());
                List<Integer> genRange = Arrays.stream(win.getGeneratorRange().split(", ")).map(Integer::parseInt).collect(Collectors.toList());
                win.setGenerator(numbers.stream().filter(gen::contains).map(Object::toString).collect(Collectors.joining(", ")));
                win.setGeneratorRange(numbers.stream().filter(genRange::contains).map(Object::toString).collect(Collectors.joining(", ")));
                win.setTime(draws.get(0).getTime());
                italySuccessRepository.save(win);
            }
        });
        ItalySuccess italySuc = new ItalySuccess();
        Generator generator = new Generator();
        generator.setDrawnMultiplier(1.0);
        generator.setMcMultiplier(1.0);
        generator.setRangeMultiplier(1.0);
        generator.setLastDrawDivider(2.0);
        generator.setSort(GeneratorSort.SUM);
        generator.setType(GeneratorType.DRAW);
        generator.setMaxDraws(50);
        generator.setDraws(10);
        CoefficientStatistics coefficientStatistics = statisticsService.nextDrawNumberCoefficients(italy2090, generator, "kico206@gmail.com");
        italySuc.setGeneratedNum(coefficientStatistics.getCoefficients().get(0).getNumber());
        italySuc.setGenerator(coefficientStatistics.getCoefficients().stream().map(c -> String.valueOf(c.getNumber())).collect(Collectors.joining(", ")));

        generator.setSort(GeneratorSort.RANGE);
        CoefficientStatistics coefficientStatistics1 = statisticsService.nextDrawNumberCoefficients(italy2090, generator, "kico206@gmail.com");
        italySuc.setGeneratorRange(coefficientStatistics1.getCoefficients().stream().map(c -> String.valueOf(c.getNumber())).collect(Collectors.joining(", ")));
        italySuc.setGeneratedRange(coefficientStatistics1.getCoefficients().get(0).getNumber());
        italySuccessRepository.save(italySuc);
    }

    @Override
    public void updateWinForLifeDraws() {
        try {
            Lottery lottery = findById(62);
            Document document = Jsoup.connect("https://www.germaniasport.hr/hr/loto/rezultati#/").get();
            Elements rows = document.select(".result");
            rows.removeIf(r -> !r.select(".result-header .result-acc .game-name").text().equals("WIN FOR LIFE CLASSICO (10/20)"));
            Elements draws = rows.select(".other-results .result-wrapper");
            DateTimeFormatter parseFormatter = new DateTimeFormatterBuilder()
                .appendPattern("dd.MM HH:mm")
                .parseDefaulting(ChronoField.YEAR, LocalDateTime.now().getYear())
                .toFormatter();
            draws.removeIf(d -> drawRepository.findByTimeAndLotteryId(LocalDateTime.parse(d.select(".date").text(), parseFormatter), lottery.getId()).isPresent());
            draws.forEach(d -> {
                Draw draw = new Draw(LocalDateTime.parse(d.select(".date").text(), parseFormatter), d.select(".drawn_balls div").stream().map(e -> Integer.parseInt(e.text())).collect(Collectors.toList()));
                draw.setLottery(lottery);
                drawRepository.save(draw);
            });
        } catch (Exception e) {
            logger.error("Win for Life not parsed successfully!", e);
        }
    }
}
