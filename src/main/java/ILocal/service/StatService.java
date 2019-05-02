package ILocal.service;

// TODO: ATTENTION! This is kostyli and we should review this part in future

import ILocal.entity.*;
import ILocal.repository.ProjectRepository;
import ILocal.repository.StatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class StatService {

    @Autowired
    private StatRepository statRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public void simpleEdit(User user, String oldValue, String newValue, long projectId) {
        if (oldValue.equals("") && !newValue.equals("")) createStat(StatType.TRANSLATE, user.getId(), projectId);
        else if (!oldValue.equals("") && !newValue.equals("") && !oldValue.equals(newValue))
            createStat(StatType.EDIT, user.getId(), projectId);
    }

    private void createStat(StatType type, long id, long projectId) {
        Stat stat = new Stat();
        stat.setUserId(id);
        stat.setAction(type);
        stat.setDate();
        stat.setProjectId(projectId);
        stat.setContributor(id != projectRepository.getAuthorIdByProjectId(projectId));
        statRepository.save(stat);
    }

    public void createStats(List<StatType> stats, long id, long projectId) {
        List<Stat> statList = new ArrayList<>();
        long authorId = projectRepository.getAuthorIdByProjectId(projectId);
        stats.forEach(a -> {
            Stat stat = new Stat();
            stat.setUserId(id);
            stat.setDate();
            stat.setAction(a);
            stat.setProjectId(projectId);
            stat.setContributor(id != authorId);
            statList.add(stat);
        });
        statRepository.saveAll(statList);
    }

    @Transactional
    public ResultStat getAllUserStats(User user) {
        ResultStat resultStat = new ResultStat();
        List<Double> doubles = new ArrayList<>();
        long count = projectRepository.countAllByAuthor(user);
        resultStat.setProjectsCount(count);
        double differenceCoefficient = createDifferenceCoefficient(user);
        List<Long> longs = statRepository.countByUserIdAndAction(user.getId(), StatType.AUTO_TRANSLATE);
        doubles.add(createCoefficient(longs, 150.0 * differenceCoefficient) * 0.06);
        resultStat.setAutoTranslateCount(getSum(longs));

        longs = statRepository.countByUserIdAndAction(user.getId(), StatType.TRANSLATE);
        doubles.add(createCoefficient(longs, 30.0 * differenceCoefficient) * 0.5);
        resultStat.setTranslateCount(getSum(longs));

        longs = statRepository.countByUserIdAndAction(user.getId(), StatType.EDIT);
        doubles.add(createCoefficient(longs, 60.0 * differenceCoefficient) * 0.3);
        resultStat.setEditCount(getSum(longs));

        longs = statRepository.countByUserIdAndAction(user.getId(), StatType.EDIT_BY_IMPORT);
        doubles.add(createCoefficient(longs, 300.0 * differenceCoefficient) * 0.07);
        resultStat.setEditByImportCount(getSum(longs));

        longs = statRepository.countByUserIdAndAction(user.getId(), StatType.TRANSLATE_BY_IMPORT);
        doubles.add(createCoefficient(longs, 300.0 * differenceCoefficient) * 0.07);
        resultStat.setTranslateByImportCount(getSum(longs));
        double sum = 0;
        for (double d : doubles) {
            sum += d;
        }
        resultStat.setRating(sum);
        return resultStat;
    }

    private double createDifferenceCoefficient(User user) {
        int contacts = user.getContacts().size() > 3 ? 3 : user.getContacts().size();
        int jobs = user.getJobs().size() > 2 ? 4 : 2 * user.getJobs().size();
        int langs = user.getLangs().size() > 3 ? 3 : user.getLangs().size();
        return 1.0 - (contacts + jobs + langs) * 0.02;
    }

    private double createCoefficient(List<Long> longs, double diff) {
        if (longs.size() == 0) return 0.0;
        int div = 0;
        for (Long a : longs) {
            if(a > 10) div++;
        }
        if(div == 0) return 0;
        double average = (double)getSumForCoeff(longs) /  (double) div;
        return average / (average + diff);
    }

    private long getSumForCoeff(List<Long> longs) {
        long sum = 0;
        for (Long o : longs) {
            if (o > 10) sum += o;
        }
        return sum;
    }

    private long getSum(List<Long> longs) {
        long sum = 0;
        for (Long o : longs) {
            sum += o;
        }
        return sum;
    }

    @Transactional
    public ResultStat getAllUserStatsInProject(User user, long projectId) {
        ResultStat resultStat = new ResultStat();
        boolean isContributor = user.getId() != projectRepository.getAuthorIdByProjectId(projectId);
        long count = statRepository.countAllByUserIdAndActionAndProjectIdAndContributor(user.getId(), StatType.AUTO_TRANSLATE, projectId, isContributor);
        resultStat.setAutoTranslateCount(count);
        count = statRepository.countAllByUserIdAndActionAndProjectIdAndContributor(user.getId(), StatType.EDIT, projectId, isContributor);
        resultStat.setEditCount(count);
        count = statRepository.countAllByUserIdAndActionAndProjectIdAndContributor(user.getId(), StatType.TRANSLATE, projectId, isContributor);
        resultStat.setTranslateCount(count);
        count = statRepository.countAllByUserIdAndActionAndProjectIdAndContributor(user.getId(), StatType.EDIT_BY_IMPORT, projectId, isContributor);
        resultStat.setEditByImportCount(count);
        count = statRepository.countAllByUserIdAndActionAndProjectIdAndContributor(user.getId(), StatType.TRANSLATE_BY_IMPORT, projectId, isContributor);
        resultStat.setTranslateByImportCount(count);
        return resultStat;
    }

    @Transactional
    public ChartItem createChartItem(ChartPeriodType type, User user, StatType statType) throws ParseException {
        ChartItem item = new ChartItem();
        item.setNodes(createDateList(type));
        switch (statType) {
            case ALL:
                item.setTranslatedByImportStats(createStatList(user, type, StatType.TRANSLATE_BY_IMPORT));
                item.setEditedByImportStats(createStatList(user, type, StatType.EDIT_BY_IMPORT));
                item.setEditedStats(createStatList(user, type, StatType.EDIT));
                item.setTranslatedStats(createStatList(user, type, StatType.TRANSLATE));
                item.setAutoTranslatedStats(createStatList(user, type, StatType.AUTO_TRANSLATE));
                break;
            case TRANSLATE_BY_IMPORT:
                item.setTranslatedByImportStats(createStatList(user, type, StatType.TRANSLATE_BY_IMPORT));
                break;
            case EDIT_BY_IMPORT:
                item.setEditedByImportStats(createStatList(user, type, StatType.EDIT_BY_IMPORT));
                break;
            case EDIT:
                item.setEditedStats(createStatList(user, type, StatType.EDIT));
                break;
            case TRANSLATE:
                item.setTranslatedStats(createStatList(user, type, StatType.TRANSLATE));
                break;
            case AUTO_TRANSLATE:
                item.setAutoTranslatedStats(createStatList(user, type, StatType.AUTO_TRANSLATE));
                break;
            case SUMMARY:
                item.setSummaryStats(createSummaryList(user, type));
                break;
        }
        return item;
    }

    @Transactional
    public List<Long> createSummaryList(User user, ChartPeriodType type) throws ParseException {
        List<Long> longs = new ArrayList<>();
        switch (type) {
            case WEEK: {
                longs = statRepository.countAllByUserIdAndDateBetween(user.getId(), getPreviousDay(6), getNextRandomDate(getPreviousDay(0)));
                List<Date> dates = statRepository.findByUserIdAndDateBetween(user.getId(), getPreviousDay(6), getNextRandomDate(getPreviousDay(0)));
                List<Date> datesFull = getDateListBetween(getPreviousDay(6), getPreviousDay(0));
                longs = createFullList(longs, datesFull, dates);
                break;
            }
            case MONTH: {
                longs = statRepository.countAllByUserIdAndDateBetween(user.getId(), getPreviousDay(29), getNextRandomDate(getPreviousDay(0)));
                List<Date> dates = statRepository.findByUserIdAndDateBetween(user.getId(), getPreviousDay(29), getNextRandomDate(getPreviousDay(0)));
                List<Date> datesFull = getDateListBetween(getPreviousDay(29), getPreviousDay(0));
                longs = createFullList(longs, datesFull, dates);
                break;
            }
            case YEAR: {
                Long sum;
                int index = Calendar.MONTH + 2;
                for (int i = 11; i > -1; i--) {
                    Date start = createMonth(index - i - 1);
                    Date end = createMonth(index - i);
                    sum = statRepository.countByUserIdAndDateBetween(user.getId(), start, end);
                    longs.add(sum);
                }
                break;
            }
        }
        return longs;
    }

    @Transactional
    public List<Long> createStatList(User user, ChartPeriodType type, StatType statType) throws ParseException {
        List<Long> longs = new ArrayList<>();
        switch (type) {
            case WEEK: {
                longs = statRepository.countAllByUserIdAndActionAndDateBetween(user.getId(), statType, getPreviousDay(6), getNextRandomDate(getPreviousDay(0)));
                List<Date> dates = statRepository.findByUserIdAndActionAndDateBetween(user.getId(), statType, getPreviousDay(6), getNextRandomDate(getPreviousDay(0)));
                List<Date> datesFull = getDateListBetween(getPreviousDay(6), getPreviousDay(0));
                longs = createFullList(longs, datesFull, dates);
                break;
            }
            case YEAR: {
                Long sum;
                int index = Calendar.MONTH + 2;
                for (int i = 11; i > -1; i--) {
                    Date start = createMonth(index - i - 1);
                    Date end = createMonth(index - i);
                    sum = statRepository.countByUserIdAndActionAndDateBetween(user.getId(), statType, start, end);
                    longs.add(sum);
                }
                break;
            }
            case MONTH: {
                longs = statRepository.countAllByUserIdAndActionAndDateBetween(user.getId(), statType, getPreviousDay(29), getNextRandomDate(getPreviousDay(0)));
                List<Date> dates = statRepository.findByUserIdAndActionAndDateBetween(user.getId(), statType, getPreviousDay(29), getNextRandomDate(getPreviousDay(0)));
                List<Date> datesFull = getDateListBetween(getPreviousDay(29), getPreviousDay(0));
                longs = createFullList(longs, datesFull, dates);
                break;
            }
        }
        return longs;
    }

    @Transactional
    public List<String> createDateList(ChartPeriodType type) throws ParseException {
        List<String> dates = new ArrayList<>();
        switch (type) {
            case WEEK: {
                List<Date> dateList = getDateListBetween(getPreviousDay(6), getPreviousDay(0));
                for (Date date : dateList) {
                    dates.add(new SimpleDateFormat("yyyy-MM-dd").format(date.getTime()));
                }
                break;
            }
            case YEAR: {
                int index = Calendar.MONTH + 1;
                for (int i = 11; i > -1; i--) {
                    Date date = createMonth(index - i);
                    dates.add(new SimpleDateFormat("MM").format(date.getTime()));
                }
                break;
            }
            case MONTH: {
                List<Date> dateList = getDateListBetween(getPreviousDay(29), getPreviousDay(0));
                for (Date date : dateList) {
                    dates.add(new SimpleDateFormat("yyyy-MM-dd").format(date.getTime()));
                }
                break;
            }
        }
        return dates;
    }

    private Date getPreviousDay(int minus) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.parse(LocalDate.now().minusDays(minus).toString());
    }

    private Date createMonth(int i) throws ParseException {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        if (i <= 0) {
            i = i + 12;
            year--;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate date = LocalDate.of(year, i, 1);
        return format.parse(date.toString());
    }

    @Transactional
    public ChartItem createChartItemByPeriod(Date start, Date end, User user, StatType type) throws ParseException {
        ChartItem item = new ChartItem();
        int days = daysBetween(start, end);
        item.setNodes(createPeriodDateList(end, days));
        switch (type) {
            case ALL:
                item.setSummaryStats(createPeriodSummaryList(user, end, days));
                item.setTranslatedStats(createPeriodStatList(user, end, days, StatType.TRANSLATE));
                item.setAutoTranslatedStats(createPeriodStatList(user, end, days, StatType.AUTO_TRANSLATE));
                item.setEditedStats(createPeriodStatList(user, end, days, StatType.EDIT));
                item.setEditedByImportStats(createPeriodStatList(user, end, days, StatType.EDIT_BY_IMPORT));
                item.setTranslatedByImportStats(createPeriodStatList(user, end, days, StatType.TRANSLATE_BY_IMPORT));
                break;
            case TRANSLATE_BY_IMPORT:
                item.setTranslatedByImportStats(createPeriodStatList(user, end, days, StatType.TRANSLATE_BY_IMPORT));
                break;
            case EDIT_BY_IMPORT:
                item.setEditedByImportStats(createPeriodStatList(user, end, days, StatType.EDIT_BY_IMPORT));
                break;
            case EDIT:
                item.setEditedStats(createPeriodStatList(user, end, days, StatType.EDIT));
                break;
            case TRANSLATE:
                item.setTranslatedStats(createPeriodStatList(user, end, days, StatType.TRANSLATE));
                break;
            case AUTO_TRANSLATE:
                item.setAutoTranslatedStats(createPeriodStatList(user, end, days, StatType.AUTO_TRANSLATE));
                break;
            case SUMMARY:
                item.setSummaryStats(createPeriodSummaryList(user, end, days));
                break;
        }
        return item;
    }

    private int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    @Transactional
    public List<Long> createPeriodSummaryList(User user, Date inputDate, int days) throws ParseException {
        List<Long> longs = statRepository.countAllByUserIdAndDateBetween(user.getId(), getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate));
        List<Date> dates = statRepository.findByUserIdAndDateBetween(user.getId(), getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate));
        List<Date> datesFull = getDateListBetween(getPreviousRandomDate(inputDate, days), inputDate);
        longs = createFullList(longs, datesFull, dates);
        return longs;
    }

    @Transactional
    public List<Long> createPeriodStatList(User user, Date inputDate, int days, StatType type) throws ParseException {
        List<Long> longs = statRepository.countAllByUserIdAndActionAndDateBetween(user.getId(), type, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate));
        List<Date> dates = statRepository.findByUserIdAndActionAndDateBetween(user.getId(), type, getPreviousRandomDate(inputDate, days), getNextRandomDate(inputDate));
        List<Date> datesFull = getDateListBetween(getPreviousRandomDate(inputDate, days), inputDate);
        longs = createFullList(longs, datesFull, dates);
        return longs;
    }

    @Transactional
    public List<String> createPeriodDateList(Date inputDate, int days) throws ParseException {
        List<String> dates = new ArrayList<>();
        List<Date> dateList = getDateListBetween(getPreviousRandomDate(inputDate, days), getPreviousRandomDate(inputDate, 0));
        for (Date date : dateList) {
            dates.add(new SimpleDateFormat("yyyy-MM-dd").format(date.getTime()));
        }
        return dates;
    }

    private Date getPreviousRandomDate(Date date, int minus) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        return format.parse(localDate.minusDays(minus).toString());
    }

    private Date getNextRandomDate(Date date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        return format.parse(localDate.plusDays(1).toString());
    }

    private boolean dateCompareEqDays(Date date1, Date date2) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date1).equals(format.format(date2));
    }

    private List<Date> getDateListBetween(Date d1, Date d2) throws ParseException {
        List<Date> dates = new ArrayList<>();
        while (d1.compareTo(d2) < 1) {
            dates.add(d1);
            d1 = getNextRandomDate(d1);
        }
        return dates;
    }

    private List<Long> createFullList(List<Long> longs, List<Date> fullDates, List<Date> incomingDates) {
        List<Long> result = new ArrayList<>();
        boolean isContains = false;
        int index = 0;
        for (Date date : fullDates) {
            isContains = false;
            for (Date d : incomingDates) {
                index = -1;
                if (dateCompareEqDays(date, d)) {
                    isContains = true;
                    index = incomingDates.indexOf(d);
                    break;
                }
            }
            if (isContains) {
                result.add(longs.get(index));
            } else result.add(0l);
        }
        return result;
    }
}