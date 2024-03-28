package com.tencent.supersonic.common.pojo;

import com.tencent.supersonic.common.pojo.enums.TimeDimensionEnum;
import com.tencent.supersonic.common.util.DateUtils;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static java.time.LocalDate.now;

@Data
public class DateConf {

    private static final long serialVersionUID = 3074129990945004340L;


    private DateMode dateMode = DateMode.RECENT;

    /**
     * like 2021-10-22, dateMode=1
     */
    private String startDate = now().plusDays(-1).toString();
    private String endDate = now().toString();

    /**
     * [2021-10-22, 2022-01-22], dateMode=2
     */
    private List<String> dateList = new ArrayList<>();

    /**
     * the last unit time unit,
     * such as the last 7 days, unit = 7
     */
    private Integer unit = 1;

    /**
     * DAY,WEEK,MONTH
     */
    private String period = Constants.DAY;

    /**
     * the text parse from , example "last 7 days" , "last mouth"
     */
    private String detectWord;

    private boolean isInherited;

    private boolean groupByDate;

    public List<String> getDateList() {
        if (!CollectionUtils.isEmpty(dateList)) {
            return dateList;
        }
        String startDateStr = getStartDate();
        String endDateStr = getEndDate();
        return DateUtils.getDateList(startDateStr, endDateStr, getPeriod());
    }

    public String getGroupByTimeDimension() {
        if (Constants.DAY.equals(period)) {
            return TimeDimensionEnum.DAY.getName();
        } else if (Constants.WEEK.equals(period)) {
            return TimeDimensionEnum.WEEK.getName();
        } else if (Constants.MONTH.equals(period)) {
            return TimeDimensionEnum.MONTH.getName();
        } else {
            return TimeDimensionEnum.DAY.getName();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DateConf dateConf = (DateConf) o;
        return dateMode == dateConf.dateMode
                && Objects.equals(startDate, dateConf.startDate)
                && Objects.equals(endDate, dateConf.endDate)
                && Objects.equals(unit, dateConf.unit)
                && Objects.equals(period, dateConf.period);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateMode, startDate, endDate, unit, period);
    }

    public enum DateMode {
        /**
         * date mode
         * 1 - BETWEEN, continuous static value, [startDate, endDate]
         * 2 - LIST, discrete static value, [dateList]
         * 3 - RECENT, dynamic time related to the actual available time of the element, [unit, period]
         * 4 - AVAILABLE, dynamic time which guaranteed to query some data, [startDate, endDate]
         */
        BETWEEN, LIST, RECENT, AVAILABLE
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"dateMode\":")
                .append(dateMode);
        sb.append(",\"startDate\":\"")
                .append(startDate).append('\"');
        sb.append(",\"endDate\":\"")
                .append(endDate).append('\"');
        sb.append(",\"dateList\":")
                .append(dateList);
        sb.append(",\"unit\":")
                .append(unit);
        sb.append(",\"period\":\"")
                .append(period).append('\"');
        sb.append(",\"text\":\"")
                .append(detectWord).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
