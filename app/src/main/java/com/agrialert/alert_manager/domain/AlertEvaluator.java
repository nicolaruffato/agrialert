package com.agrialert.alert_manager.domain;

import com.agrialert.R;
import com.agrialert.api.WeatherApiResponse;
import com.agrialert.data_manager.Alert;
import com.agrialert.data_manager.AlertType;
import com.agrialert.data_manager.AlertWithThreshold;
import com.agrialert.data_manager.Field;
import com.agrialert.data_manager.Threshold;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Evaluates weather data against configured thresholds to produce candidate alerts
 * for the alert manager workflow.
 */
public class AlertEvaluator {

    private static final int DEFAULT_ICON_RES = R.drawable.ic_alert;
    private static final int HUMIDITY_MIN_SCARSA_VENTILAZIONE = 80;
    private static final int DAILY_WINDOW_HOURS = 24;
    private static final double PRECIPITATION_EPSILON = 0.1d;
    private static final long HOUR_MS = 3_600_000L;

    /**
     * Generates alert candidates for the given field based on the supplied weather response
     * and active alert thresholds. Returned alerts are not persisted.
     *
     * @param weather      weather response providing current and hourly forecasts
     * @param field        field metadata used for identifiers and location context
     * @param groupName    optional group name override; falls back to the field group name
     * @param activeAlerts active alert definitions with their thresholds
     * @return a list of alert candidates, or an empty list when inputs are missing or no match occurs
     */
    public List<Alert> evaluate(WeatherApiResponse weather,
                                Field field,
                                String groupName,
                                List<AlertWithThreshold> activeAlerts) {
        List<Alert> result = new ArrayList<>();

        if (weather == null || field == null) {
            return result;
        }
        if (activeAlerts == null || activeAlerts.isEmpty()) {
            return result;
        }

        // Serie orarie (possibili fino a 7 giorni)
        List<String> timeSeries = weather.hourly != null ? weather.hourly.time : null;
        List<Double> tempSeries = weather.hourly != null ? weather.hourly.temperature2m : null;
        List<Double> humiditySeries = weather.hourly != null ? weather.hourly.relativeHumidity2m : null;
        List<Double> precipitationSeries = weather.hourly != null ? weather.hourly.precipitation : null;
        List<Double> windSeries = weather.hourly != null ? weather.hourly.windSpeed10m : null;
        int weatherCode = weather.currentWeather != null ? weather.currentWeather.weathercode : -1;
        long startTimeMs = nextHourEpochMs(System.currentTimeMillis());
        int startIndex = firstIndexAtOrAfter(timeSeries, startTimeMs);

        for (AlertWithThreshold entry : activeAlerts) {
            if (entry == null) {
                continue;
            }

            AlertType type = entry.getAlertType();
            if (type == null) {
                continue;
            }

            Threshold thresholds = entry.getThreshold();
            Double threshold1 = thresholds != null ? thresholds.getThreshold1() : null;
            Double threshold2 = thresholds != null ? thresholds.getThreshold2() : null;

            String key = normalizeTypeName(type.getName());
            if (key == null) {
                continue;
            }

            switch (key) {
                case "ONDATA_CALORE":
                    if (threshold1 == null) {
                        break;
                    }
                    if (threshold2 == null || threshold2 <= 0d) {
                        addIfForecastMatches(result, type, field, groupName, tempSeries, timeSeries, startIndex,
                                value -> value > threshold1,
                                "Ondata di calore",
                                value -> "Temperatura prevista " + formatValueCompact(value, "\u00B0C") +
                                        " (>" + formatValueCompact(threshold1, "\u00B0C") + ")",
                                R.drawable.ic_alert_calore);
                    } else {
                        int hours = toPositiveIntHours(threshold2);
                        ThresholdPredicate predicate = value -> value > threshold1;
                        int idx = firstIndexStreak(tempSeries, predicate, hours, startIndex);
                        if (idx >= 0) {
                            double value = safeGet(tempSeries, idx);
                            long forecastAt = parseIsoTime(timeSeries, idx);
                            long durationMs = streakDurationMs(tempSeries, predicate, idx);
                            String description = "Temperatura prevista " + formatValueCompact(value, "\u00B0C") +
                                    " (>" + formatValueCompact(threshold1, "\u00B0C") + ")" +
                                    formatDurationSuffix(durationMs);
                            Alert alert = build(type, "Ondata di calore", description,
                                    R.drawable.ic_alert_calore, field, groupName, forecastAt);
                            alert.setDurationMs(durationMs);
                            result.add(alert);
                        }
                    }
                    break;
                case "GELO_BRINA":
                    if (threshold1 == null) {
                        break;
                    }
                    if (threshold2 == null || threshold2 <= 0d) {
                        addIfForecastMatches(result, type, field, groupName, tempSeries, timeSeries, startIndex,
                                value -> value < threshold1,
                                "Gelo / brina",
                                value -> "Temperatura prevista " + formatValueCompact(value, "\u00B0C") +
                                        " (<" + formatValueCompact(threshold1, "\u00B0C") + ")",
                                R.drawable.ic_alert_gelo);
                    } else {
                        int hours = toPositiveIntHours(threshold2);
                        ThresholdPredicate predicate = value -> value < threshold1;
                        int idx = firstIndexStreak(tempSeries, predicate, hours, startIndex);
                        if (idx >= 0) {
                            double value = safeGet(tempSeries, idx);
                            long forecastAt = parseIsoTime(timeSeries, idx);
                            long durationMs = streakDurationMs(tempSeries, predicate, idx);
                            String description = "Temperatura prevista " + formatValueCompact(value, "\u00B0C") +
                                    " (<" + formatValueCompact(threshold1, "\u00B0C") + ")" +
                                    formatDurationSuffix(durationMs);
                            Alert alert = build(type, "Gelo / brina", description,
                                    R.drawable.ic_alert_gelo, field, groupName, forecastAt);
                            alert.setDurationMs(durationMs);
                            result.add(alert);
                        }
                    }
                    break;
                case "PIOGGIA_INTENSA":
                    if (threshold1 == null) {
                        break;
                    }
                    if (threshold2 == null || threshold2 <= 0d) {
                        addIfForecastMatches(result, type, field, groupName, precipitationSeries, timeSeries, startIndex,
                                value -> value > threshold1,
                                "Pioggia intensa",
                                value -> "Pioggia prevista " + formatValueCompact(value, "mm/h") +
                                        " (>" + formatValueCompact(threshold1, "mm/h") + ")",
                                R.drawable.ic_alert_pioggia);
                    } else {
                        int windowHours = toPositiveIntHours(threshold2);
                        RollingWindowMatch match = firstIndexRollingSum(precipitationSeries, windowHours, threshold1, startIndex);
                        if (match != null) {
                            long forecastAt = parseIsoTime(timeSeries, match.index);
                            long durationMs = rollingSumDurationMs(precipitationSeries, windowHours, threshold1, match.index);
                            String description = "Pioggia prevista " + formatValueCompact(match.sum, "mm") +
                                    " in " + windowHours + "h (>" + formatValueCompact(threshold1, "mm") + ")" +
                                    formatDurationSuffix(durationMs);
                            Alert alert = build(type, "Pioggia intensa", description,
                                    R.drawable.ic_alert_pioggia, field, groupName, forecastAt);
                            alert.setDurationMs(durationMs);
                            result.add(alert);
                        }
                    }
                    break;
                case "VENTO_FORTE":
                    if (threshold1 == null) {
                        break;
                    }
                    addIfForecastMatches(result, type, field, groupName, windSeries, timeSeries, startIndex,
                            value -> value > threshold1,
                            "Vento forte",
                            value -> "Vento previsto " + formatValueCompact(value, "km/h") +
                                    " (>" + formatValueCompact(threshold1, "km/h") + ")",
                            R.drawable.ic_alert_vento);
                    break;
                case "TEMPORALE_GRANDINE":
                    if (threshold1 == null || threshold2 == null) {
                        break;
                    }
                    StormHailMatch stormMatch = firstStormHailMatch(
                            precipitationSeries, windSeries, humiditySeries, tempSeries,
                            weatherCode, threshold1, threshold2, startIndex);
                    if (stormMatch != null) {
                        long forecastAt = parseIsoTime(timeSeries, stormMatch.index);
                        long durationMs = stormHailDurationMs(precipitationSeries,
                                windSeries,
                                humiditySeries,
                                tempSeries,
                                weatherCode,
                                threshold1,
                                threshold2,
                                stormMatch.index);
                        String description = "Prob. temporale " + formatValueCompact(stormMatch.stormProbability, "%") +
                                " (>" + formatValueCompact(threshold1, "%") + ")" +
                                " e grandine " + formatValueCompact(stormMatch.hailProbability, "%") +
                                " (>" + formatValueCompact(threshold2, "%") + ")" +
                                formatDurationSuffix(durationMs);
                        Alert alert = build(type, "Temporale / grandine", description,
                                R.drawable.ic_alert_temporale, field, groupName, forecastAt);
                        alert.setDurationMs(durationMs);
                        result.add(alert);
                    }
                    break;
                case "SICCITA_PROLUNGATA":
                    if (threshold1 == null || threshold1 <= 0d) {
                        break;
                    }
                    int requiredHours = toPositiveIntHours(threshold1 * 24d);
                    ThresholdPredicate droughtPredicate = value -> value <= PRECIPITATION_EPSILON;
                    int droughtIdx = firstIndexStreak(precipitationSeries, droughtPredicate, requiredHours, startIndex);
                    if (droughtIdx >= 0) {
                        long forecastAt = parseIsoTime(timeSeries, droughtIdx);
                        long durationMs = streakDurationMs(precipitationSeries, droughtPredicate, droughtIdx);
                        String description = "Assenza di pioggia prevista (>= " + formatDays(threshold1) + ")" +
                                formatDurationSuffix(durationMs);
                        Alert alert = build(type, "Siccita prolungata", description,
                                R.drawable.ic_alert_siccita, field, groupName, forecastAt);
                        alert.setDurationMs(durationMs);
                        result.add(alert);
                    }
                    break;
                case "UMIDITA_ELEVATA":
                    if (threshold1 == null) {
                        break;
                    }
                    addIfForecastMatches(result, type, field, groupName, humiditySeries, timeSeries, startIndex,
                            value -> value > threshold1,
                            "Umidita elevata",
                            value -> "Umidita prevista " + formatValueCompact(value, "%") +
                                    " (>" + formatValueCompact(threshold1, "%") + ")",
                            R.drawable.ic_alert_umidita);
                    break;
                case "ESCURSIONE_TERMICA_ELEVATA":
                    if (threshold1 == null || threshold1 <= 0d) {
                        break;
                    }
                    RangeMatch rangeMatch = firstDailyRangeMatch(tempSeries, DAILY_WINDOW_HOURS, threshold1, startIndex);
                    if (rangeMatch != null) {
                        double delta = rangeMatch.max - rangeMatch.min;
                        long forecastAt = parseIsoTime(timeSeries, rangeMatch.index);
                        long durationMs = dailyRangeDurationMs(tempSeries, DAILY_WINDOW_HOURS, threshold1, rangeMatch.index);
                        String description = "Escursione termica prevista " + formatValueCompact(delta, "\u00B0C") +
                                " (>" + formatValueCompact(threshold1, "\u00B0C") + ")" +
                                formatDurationSuffix(durationMs);
                        Alert alert = build(type, "Escursione termica elevata", description,
                                R.drawable.ic_alert_escursione, field, groupName, forecastAt);
                        alert.setDurationMs(durationMs);
                        result.add(alert);
                    }
                    break;
                case "RISCHIO_INCENDIO":
                    if (threshold1 == null || threshold2 == null) {
                        break;
                    }
                    addIfForecastMatchesPaired(result, type, field, groupName,
                            tempSeries, humiditySeries, timeSeries, startIndex,
                            value -> value > threshold1,
                            hum -> hum < threshold2,
                            "Rischio incendio",
                            (tempVal, humVal) -> "Temperatura " + formatValueCompact(tempVal, "\u00B0C") +
                                    " (>" + formatValueCompact(threshold1, "\u00B0C") + ")" +
                                    " e umidita " + formatValueCompact(humVal, "%") +
                                    " (<" + formatValueCompact(threshold2, "%") + ")",
                            R.drawable.ic_alert_incendio);
                    break;
                case "SCARSA_VENTILAZIONE":
                    if (threshold1 == null) {
                        break;
                    }
                    double humidityMin = threshold2 != null && threshold2 > 0d
                            ? threshold2
                            : HUMIDITY_MIN_SCARSA_VENTILAZIONE;
                    addIfForecastMatchesPaired(result, type, field, groupName,
                            windSeries, humiditySeries, timeSeries, startIndex,
                            value -> value < threshold1,
                            hum -> hum > humidityMin,
                            "Scarsa ventilazione",
                            (windVal, humVal) -> "Vento " + formatValueCompact(windVal, "km/h") +
                                    " (<" + formatValueCompact(threshold1, "km/h") + ")" +
                                    " e umidita " + formatValueCompact(humVal, "%") +
                                    " (>" + formatValueCompact(humidityMin, "%") + ")",
                            R.drawable.ic_alert_ventilazione);
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    /**
     * Returns the first index whose parsed time is at or after the provided epoch timestamp.
     */
    private int firstIndexAtOrAfter(List<String> times, long startTimeMs) {
        if (times == null || times.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < times.size(); i++) {
            long ts = parseIsoTime(times, i);
            if (ts >= startTimeMs) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Computes the next whole-hour boundary strictly after {@code nowMs}.
     */
    private long nextHourEpochMs(long nowMs) {
        return nowMs - (nowMs % HOUR_MS) + HOUR_MS;
    }

    /**
     * Adds a new alert to the result list if the first matching forecast value is found.
     *
     * @param result               destination list for new alerts
     * @param type                 alert type metadata
     * @param field                field context for the alert
     * @param groupName            optional group name override
     * @param values               hourly series to scan
     * @param times                ISO timestamps aligned with the series
     * @param startIndex           index from which to start scanning the hourly series
     * @param predicate            condition used to match a forecast value
     * @param title                alert title to use
     * @param descriptionFormatter formatter for the alert description
     * @param iconRes              icon resource identifier
     */
    private void addIfForecastMatches(List<Alert> result,
                                      AlertType type,
                                      Field field,
                                      String groupName,
                                      List<Double> values,
                                      List<String> times,
                                      int startIndex,
                                      ThresholdPredicate predicate,
                                      String title,
                                      ValueFormatter descriptionFormatter,
                                      int iconRes) {
        int idx = firstIndexMatching(values, predicate, startIndex);
        if (idx < 0) return;
        long forecastAt = parseIsoTime(times, idx);
        double value = safeGet(values, idx);
        long durationMs = streakDurationMs(values, predicate, idx);
        String description = descriptionFormatter.format(value) + formatDurationSuffix(durationMs);
        Alert alert = build(type, title, description, iconRes, field, groupName, forecastAt);
        alert.setDurationMs(durationMs);
        result.add(alert);
    }

    /**
     * Adds a new alert to the result list if the first matching pair of forecast values is found.
     *
     * @param result               destination list for new alerts
     * @param type                 alert type metadata
     * @param field                field context for the alert
     * @param groupName            optional group name override
     * @param seriesA              first hourly series to scan
     * @param seriesB              second hourly series to scan
     * @param times                ISO timestamps aligned with the series
     * @param startIndex           index from which to start scanning the hourly series
     * @param predicateA           condition for the first series
     * @param predicateB           condition for the second series
     * @param title                alert title to use
     * @param descriptionFormatter formatter for the alert description
     * @param iconRes              icon resource identifier
     */
    private void addIfForecastMatchesPaired(List<Alert> result,
                                            AlertType type,
                                            Field field,
                                            String groupName,
                                            List<Double> seriesA,
                                            List<Double> seriesB,
                                            List<String> times,
                                            int startIndex,
                                            ThresholdPredicate predicateA,
                                            ThresholdPredicate predicateB,
                                            String title,
                                            BiValueFormatter descriptionFormatter,
                                            int iconRes) {
        int idx = firstIndexMatching(seriesA, predicateA, seriesB, predicateB, startIndex);
        if (idx < 0) return;
        long forecastAt = parseIsoTime(times, idx);
        double valA = safeGet(seriesA, idx);
        double valB = safeGet(seriesB, idx);
        long durationMs = pairedStreakDurationMs(seriesA, predicateA, seriesB, predicateB, idx);
        String description = descriptionFormatter.format(valA, valB) + formatDurationSuffix(durationMs);
        Alert alert = build(type, title, description, iconRes, field, groupName, forecastAt);
        alert.setDurationMs(durationMs);
        result.add(alert);
    }

    /**
     * Returns the first index where the value satisfies the predicate.
     *
     * @param values    series to scan
     * @param predicate condition to apply
     * @param startIndex index from which to start scanning the series
     * @return the first matching index, or -1 when no match is found
     */
    private int firstIndexMatching(List<Double> values, ThresholdPredicate predicate, int startIndex) {
        if (values == null || predicate == null) return -1;
        int safeStart = Math.max(0, startIndex);
        for (int i = safeStart; i < values.size(); i++) {
            double v = safeGet(values, i);
            if (!Double.isNaN(v) && predicate.test(v)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the first index where both series satisfy their predicates.
     *
     * @param seriesA    first series to scan
     * @param predicateA predicate for the first series
     * @param seriesB    second series to scan
     * @param predicateB predicate for the second series
     * @param startIndex index from which to start scanning the series
     * @return the first matching index, or -1 when no match is found
     */
    private int firstIndexMatching(List<Double> seriesA,
                                   ThresholdPredicate predicateA,
                                   List<Double> seriesB,
                                   ThresholdPredicate predicateB,
                                   int startIndex) {
        if (seriesA == null || seriesB == null || predicateA == null || predicateB == null) return -1;
        int size = Math.min(seriesA.size(), seriesB.size());
        int safeStart = Math.max(0, startIndex);
        for (int i = safeStart; i < size; i++) {
            double a = safeGet(seriesA, i);
            double b = safeGet(seriesB, i);
            if (!Double.isNaN(a) && !Double.isNaN(b) && predicateA.test(a) && predicateB.test(b)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the first index of a contiguous streak that satisfies the predicate.
     *
     * @param values    series to scan
     * @param predicate condition to apply
     * @param minLength minimum contiguous length required
     * @param startIndex index from which to start scanning the series
     * @return the starting index of the streak, or -1 when no match is found
     */
    private int firstIndexStreak(List<Double> values, ThresholdPredicate predicate, int minLength, int startIndex) {
        if (values == null || predicate == null || minLength <= 0) {
            return -1;
        }
        if (minLength <= 1) {
            return firstIndexMatching(values, predicate, startIndex);
        }
        int streak = 0;
        int safeStart = Math.max(0, startIndex);
        for (int i = safeStart; i < values.size(); i++) {
            double v = safeGet(values, i);
            if (!Double.isNaN(v) && predicate.test(v)) {
                streak++;
                if (streak >= minLength) {
                    return i - minLength + 1;
                }
            } else {
                streak = 0;
            }
        }
        return -1;
    }

    /**
     * Computes the duration (ms) of a contiguous streak starting at {@code startIndex}.
     */
    private long streakDurationMs(List<Double> values, ThresholdPredicate predicate, int startIndex) {
        if (values == null || predicate == null || startIndex < 0 || startIndex >= values.size()) {
            return 0L;
        }
        long hours = 0L;
        for (int i = startIndex; i < values.size(); i++) {
            double v = safeGet(values, i);
            if (Double.isNaN(v) || !predicate.test(v)) {
                break;
            }
            hours++;
        }
        return hours > 0L ? hours * HOUR_MS : 0L;
    }

    /**
     * Computes the duration (ms) of a contiguous streak where both series satisfy their predicates.
     */
    private long pairedStreakDurationMs(List<Double> seriesA,
                                        ThresholdPredicate predicateA,
                                        List<Double> seriesB,
                                        ThresholdPredicate predicateB,
                                        int startIndex) {
        if (seriesA == null || seriesB == null || predicateA == null || predicateB == null) {
            return 0L;
        }
        int size = Math.min(seriesA.size(), seriesB.size());
        if (startIndex < 0 || startIndex >= size) {
            return 0L;
        }
        long hours = 0L;
        for (int i = startIndex; i < size; i++) {
            double a = safeGet(seriesA, i);
            double b = safeGet(seriesB, i);
            if (Double.isNaN(a) || Double.isNaN(b) || !predicateA.test(a) || !predicateB.test(b)) {
                break;
            }
            hours++;
        }
        return hours > 0L ? hours * HOUR_MS : 0L;
    }

    /**
     * Computes the duration (ms) of consecutive rolling-sum windows above the threshold.
     */
    private long rollingSumDurationMs(List<Double> values, int windowSize, double threshold, int startIndex) {
        if (values == null || windowSize <= 0) {
            return 0L;
        }
        int size = values.size();
        if (startIndex < 0 || startIndex > size - windowSize) {
            return 0L;
        }

        int lastStart = -1;
        for (int start = startIndex; start <= size - windowSize; start++) {
            double sum = 0d;
            boolean valid = true;
            for (int i = start; i < start + windowSize; i++) {
                double v = safeGet(values, i);
                if (Double.isNaN(v)) {
                    valid = false;
                    break;
                }
                sum += v;
            }
            if (valid && sum > threshold) {
                lastStart = start;
            } else {
                break;
            }
        }

        if (lastStart < 0) {
            return 0L;
        }
        long hours = (long) (lastStart + windowSize - startIndex);
        return hours > 0L ? hours * HOUR_MS : 0L;
    }

    /**
     * Computes the duration (ms) of consecutive day windows whose range exceeds the threshold.
     */
    private long dailyRangeDurationMs(List<Double> values, int windowSize, double threshold, int startIndex) {
        if (values == null || windowSize <= 0) {
            return 0L;
        }
        int size = values.size();
        if (startIndex < 0 || startIndex > size - windowSize) {
            return 0L;
        }

        long windows = 0L;
        for (int start = startIndex; start <= size - windowSize; start += windowSize) {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            boolean valid = true;
            for (int i = start; i < start + windowSize; i++) {
                double v = safeGet(values, i);
                if (Double.isNaN(v)) {
                    valid = false;
                    break;
                }
                if (v < min) {
                    min = v;
                }
                if (v > max) {
                    max = v;
                }
            }
            if (!valid || max - min <= threshold) {
                break;
            }
            windows++;
        }

        long hours = windows * windowSize;
        return hours > 0L ? hours * HOUR_MS : 0L;
    }

    /**
     * Computes the duration (ms) of consecutive hours where storm and hail probabilities exceed thresholds.
     */
    private long stormHailDurationMs(List<Double> precipitationSeries,
                                     List<Double> windSeries,
                                     List<Double> humiditySeries,
                                     List<Double> tempSeries,
                                     int weatherCode,
                                     double stormThreshold,
                                     double hailThreshold,
                                     int startIndex) {
        int size = Math.max(Math.max(sizeOf(precipitationSeries), sizeOf(windSeries)),
                Math.max(sizeOf(humiditySeries), sizeOf(tempSeries)));
        if (size == 0) {
            return 0L;
        }
        int safeStart = Math.max(0, startIndex);
        if (safeStart >= size) {
            return 0L;
        }

        long hours = 0L;
        for (int i = safeStart; i < size; i++) {
            double precipitation = safeGet(precipitationSeries, i);
            double wind = safeGet(windSeries, i);
            double humidity = safeGet(humiditySeries, i);
            double temperature = safeGet(tempSeries, i);
            double stormProbability = computeStormProbability(precipitation, wind, humidity, weatherCode);
            double hailProbability = computeHailProbability(precipitation, wind, humidity, temperature, weatherCode);
            if (stormProbability > stormThreshold && hailProbability > hailThreshold) {
                hours++;
            } else {
                break;
            }
        }

        return hours > 0L ? hours * HOUR_MS : 0L;
    }

    /**
     * Returns the first rolling window whose sum exceeds the provided threshold.
     *
     * @param values     series to scan
     * @param windowSize size of the rolling window
     * @param threshold  minimum sum required
     * @param startIndex index from which to start scanning the series
     * @return a {@link RollingWindowMatch} when a match is found; {@code null} otherwise
     */
    private RollingWindowMatch firstIndexRollingSum(List<Double> values, int windowSize, double threshold, int startIndex) {
        if (values == null || windowSize <= 0) {
            return null;
        }
        int size = values.size();
        if (size < windowSize) {
            return null;
        }
        int safeStart = Math.max(0, startIndex);
        for (int start = safeStart; start <= size - windowSize; start++) {
            double sum = 0d;
            boolean valid = true;
            for (int i = start; i < start + windowSize; i++) {
                double v = safeGet(values, i);
                if (Double.isNaN(v)) {
                    valid = false;
                    break;
                }
                sum += v;
            }
            if (valid && sum > threshold) {
                return new RollingWindowMatch(start, sum);
            }
        }
        return null;
    }

    /**
     * Returns the first window whose min/max range exceeds the provided threshold.
     *
     * @param values     series to scan
     * @param windowSize size of the window in hours
     * @param threshold  minimum range required
     * @param startIndex index from which to start scanning the series
     * @return a {@link RangeMatch} when a match is found; {@code null} otherwise
     */
    private RangeMatch firstDailyRangeMatch(List<Double> values, int windowSize, double threshold, int startIndex) {
        if (values == null || windowSize <= 0) {
            return null;
        }
        int size = values.size();
        if (size < windowSize) {
            return null;
        }
        int safeStart = Math.max(0, startIndex);
        for (int start = safeStart; start <= size - windowSize; start++) {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            boolean valid = true;
            for (int i = start; i < start + windowSize; i++) {
                double v = safeGet(values, i);
                if (Double.isNaN(v)) {
                    valid = false;
                    break;
                }
                if (v < min) {
                    min = v;
                }
                if (v > max) {
                    max = v;
                }
            }
            if (valid && max - min > threshold) {
                return new RangeMatch(start, min, max);
            }
        }
        return null;
    }

    /**
     * Finds the first index where both storm and hail probabilities exceed thresholds.
     *
     * @param precipitationSeries hourly precipitation values
     * @param windSeries          hourly wind speed values
     * @param humiditySeries      hourly humidity values
     * @param tempSeries          hourly temperature values
     * @param weatherCode         current weather code used as a boost
     * @param stormThreshold      minimum storm probability required
     * @param hailThreshold       minimum hail probability required
     * @param startIndex          index from which to start scanning the series
     * @return a {@link StormHailMatch} when a match is found; {@code null} otherwise
     */
    private StormHailMatch firstStormHailMatch(List<Double> precipitationSeries,
                                               List<Double> windSeries,
                                               List<Double> humiditySeries,
                                               List<Double> tempSeries,
                                               int weatherCode,
                                               double stormThreshold,
                                               double hailThreshold,
                                               int startIndex) {
        int size = Math.max(Math.max(sizeOf(precipitationSeries), sizeOf(windSeries)),
                Math.max(sizeOf(humiditySeries), sizeOf(tempSeries)));
        if (size == 0) {
            return null;
        }
        int safeStart = Math.max(0, startIndex);
        for (int i = safeStart; i < size; i++) {
            double precipitation = safeGet(precipitationSeries, i);
            double wind = safeGet(windSeries, i);
            double humidity = safeGet(humiditySeries, i);
            double temperature = safeGet(tempSeries, i);
            double stormProbability = computeStormProbability(precipitation, wind, humidity, weatherCode);
            double hailProbability = computeHailProbability(precipitation, wind, humidity, temperature, weatherCode);
            if (stormProbability > stormThreshold && hailProbability > hailThreshold) {
                return new StormHailMatch(i, stormProbability, hailProbability);
            }
        }
        return null;
    }

    /**
     * Computes a storm probability percentage from precipitation, wind, humidity, and weather code.
     *
     * @param precipitation precipitation value (mm/h)
     * @param wind          wind speed value (km/h)
     * @param humidity      relative humidity percentage
     * @param weatherCode   current weather code used as a boost
     * @return a probability in the range [0, 100]
     */
    private double computeStormProbability(double precipitation,
                                           double wind,
                                           double humidity,
                                           int weatherCode) {
        double value = 0d;
        if (!Double.isNaN(precipitation)) {
            value += precipitation * 8d;
        }
        if (!Double.isNaN(wind)) {
            value += wind * 0.6d;
        }
        if (!Double.isNaN(humidity)) {
            value += humidity * 0.3d;
        }
        value += weatherCodeStormBoost(weatherCode);
        return clamp(value, 0d, 100d);
    }

    /**
     * Computes a hail probability percentage from precipitation, wind, humidity, temperature,
     * and weather code.
     *
     * @param precipitation precipitation value (mm/h)
     * @param wind          wind speed value (km/h)
     * @param humidity      relative humidity percentage
     * @param temperature   air temperature (C)
     * @param weatherCode   current weather code used as a boost
     * @return a probability in the range [0, 100]
     */
    private double computeHailProbability(double precipitation,
                                          double wind,
                                          double humidity,
                                          double temperature,
                                          int weatherCode) {
        double value = 0d;
        if (!Double.isNaN(precipitation)) {
            value += precipitation * 6d;
        }
        if (!Double.isNaN(wind)) {
            value += wind * 0.7d;
        }
        if (!Double.isNaN(humidity)) {
            value += humidity * 0.2d;
        }
        if (!Double.isNaN(temperature)) {
            if (temperature <= 0d) {
                value += 25d;
            } else if (temperature <= 5d) {
                value += 20d;
            } else if (temperature <= 10d) {
                value += 10d;
            }
        }
        value += weatherCodeHailBoost(weatherCode);
        return clamp(value, 0d, 100d);
    }

    /**
     * Computes a boost for storm probability based on the weather code.
     *
     * @param weatherCode weather code from the API
     * @return additive boost to apply
     */
    private double weatherCodeStormBoost(int weatherCode) {
        if (weatherCode >= 95 && weatherCode <= 99) {
            return 25d;
        }
        if (weatherCode >= 80 && weatherCode <= 82) {
            return 15d;
        }
        if (weatherCode >= 61 && weatherCode <= 67) {
            return 10d;
        }
        return 0d;
    }

    /**
     * Computes a boost for hail probability based on the weather code.
     *
     * @param weatherCode weather code from the API
     * @return additive boost to apply
     */
    private double weatherCodeHailBoost(int weatherCode) {
        if (weatherCode >= 96 && weatherCode <= 99) {
            return 30d;
        }
        if (weatherCode == 95) {
            return 20d;
        }
        return 0d;
    }

    /**
     * Clamps a value within the provided bounds.
     *
     * @param value input value
     * @param min   minimum allowed value
     * @param max   maximum allowed value
     * @return the clamped value
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Returns the size of a list or zero when the list is {@code null}.
     *
     * @param values list to measure
     * @return list size, or zero when {@code null}
     */
    private int sizeOf(List<Double> values) {
        return values != null ? values.size() : 0;
    }

    /**
     * Converts a floating-point hour count to a positive integer, rounding up.
     *
     * @param hours hour count that may be {@code null} or non-finite
     * @return a positive integer value, or zero when the input is invalid
     */
    private int toPositiveIntHours(Double hours) {
        if (hours == null || hours.isNaN() || hours.isInfinite()) {
            return 0;
        }
        int value = (int) Math.ceil(hours);
        return Math.max(1, value);
    }

    /**
     * Builds an {@link Alert} instance from the provided metadata.
     *
     * @param type        alert type metadata
     * @param title       title override; falls back to type name when {@code null}
     * @param description description override; falls back to type description when empty
     * @param iconRes     icon resource identifier
     * @param field       field context for identifiers and location
     * @param groupName   optional group name override
     * @param forecastAt  forecast timestamp in milliseconds
     * @return a populated {@link Alert} instance
     */
    private Alert build(AlertType type,
                        String title,
                        String description,
                        int iconRes,
                        Field field,
                        String groupName,
                        long forecastAt) {
        Alert alert = new Alert();
        alert.setFieldId(field.getId());
        alert.setGroupName(groupName != null ? groupName : field.getGroupName());
        alert.setTypeId(type.getId());
        String resolvedTitle = title != null ? title : type.getName();
        alert.setTitle(resolvedTitle);
        String resolvedDescription = description;
        if (resolvedDescription == null || resolvedDescription.isEmpty()) {
            resolvedDescription = type.getDescription();
        }
        alert.setDescription(resolvedDescription);
        alert.setFieldAddress(field.getAddress());
        alert.setForecastAt(forecastAt);
        alert.setCreatedAt(System.currentTimeMillis());
        alert.setResolved(false);
        alert.setIconRes(iconRes != 0 ? iconRes : DEFAULT_ICON_RES);
        return alert;
    }

    /**
     * Normalizes an alert type name into an uppercase, underscore-separated key.
     *
     * @param name raw alert type name
     * @return normalized key, or {@code null} when the input is empty
     */
    private String normalizeTypeName(String name) {
        if (name == null) return null;
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return null;
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}+", "");
        normalized = normalized.replace('/', ' ');
        normalized = normalized.replaceAll("[^A-Za-z0-9]+", " ");
        String[] parts = normalized.trim().toLowerCase(Locale.ROOT).split("\\s+");
        if (parts.length == 0) return null;
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty() || isStopWord(part)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('_');
            }
            sb.append(part.toUpperCase(Locale.ROOT));
        }
        if (sb.length() == 0) return null;
        String key = sb.toString();
        if ("ONDATA_DI_CALORE".equals(key)) {
            return "ONDATA_CALORE";
        }
        return key;
    }

    /**
     * Returns {@code true} if the token is an Italian stop word to be skipped.
     *
     * @param token normalized token
     * @return {@code true} when the token should be skipped
     */
    private boolean isStopWord(String token) {
        return "di".equals(token)
                || "del".equals(token)
                || "dello".equals(token)
                || "della".equals(token)
                || "dei".equals(token)
                || "degli".equals(token)
                || "delle".equals(token)
                || "dell".equals(token);
    }

    /**
     * Safely retrieves a numeric value or {@link Double#NaN} when missing.
     *
     * @param list  list to read from
     * @param index index to access
     * @return the value at the index, or {@link Double#NaN} when unavailable
     */
    private double safeGet(List<Double> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return Double.NaN;
        }
        Double value = list.get(index);
        return value != null ? value : Double.NaN;
    }

    /**
     * Parses an ISO-8601 timestamp (UTC) into epoch milliseconds.
     *
     * @param times list of ISO timestamps
     * @param index index to parse
     * @return epoch milliseconds, or zero when parsing fails
     */
    private long parseIsoTime(List<String> times, int index) {
        if (times == null || index < 0 || index >= times.size()) {
            return 0L;
        }
        try {
            // Open-Meteo usa ISO8601, es. 2025-01-01T12:00
            String iso = times.get(index);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsed = df.parse(iso);
            return parsed != null ? parsed.getTime() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private String formatDurationSuffix(long durationMs) {
        String duration = formatDuration(durationMs);
        if (duration.isEmpty()) {
            return "";
        }
        return " \u2022 Durata stimata: " + duration;
    }

    private String formatDuration(long durationMs) {
        if (durationMs <= 0L) {
            return "";
        }
        long hours = Math.max(1L, durationMs / HOUR_MS);
        if (hours < 24L) {
            return hours + "h";
        }
        long days = hours / 24L;
        long remHours = hours % 24L;
        if (remHours == 0L) {
            return days + "g";
        }
        return days + "g " + remHours + "h";
    }

    private String formatValueCompact(double value, String unit) {
        if (Double.isNaN(value)) {
            return "-";
        }
        double rounded = Math.rint(value);
        boolean integerLike = Math.abs(value - rounded) < 0.05d;
        String number = integerLike
                ? String.format(Locale.getDefault(), "%.0f", rounded)
                : String.format(Locale.getDefault(), "%.1f", value);
        if (unit == null || unit.isEmpty()) {
            return number;
        }
        if ("%".equals(unit) || unit.startsWith("\u00B0")) {
            return number + unit;
        }
        return number + " " + unit;
    }

    private String formatDays(Double days) {
        if (days == null || days.isNaN() || days.isInfinite()) {
            return "-";
        }
        double rounded = Math.rint(days);
        boolean integerLike = Math.abs(days - rounded) < 0.05d;
        String number = integerLike
                ? String.format(Locale.getDefault(), "%.0f", rounded)
                : String.format(Locale.getDefault(), "%.1f", days);
        return number + " giorni";
    }

    /**
     * Predicate for threshold evaluation.
     */
    private interface ThresholdPredicate {
        /**
         * Evaluates the predicate against a numeric value.
         *
         * @param value value to test
         * @return {@code true} when the value satisfies the predicate
         */
        boolean test(double value);
    }

    /**
     * Formats a single value for inclusion in an alert description.
     */
    private interface ValueFormatter {
        /**
         * Formats a numeric value.
         *
         * @param value value to format
         * @return formatted string
         */
        String format(double value);
    }

    /**
     * Formats a pair of values for inclusion in an alert description.
     */
    private interface BiValueFormatter {
        /**
         * Formats a pair of numeric values.
         *
         * @param valueA first value
         * @param valueB second value
         * @return formatted string
         */
        String format(double valueA, double valueB);
    }

    /**
     * Holds the index and sum for a rolling-window threshold match.
     */
    private static class RollingWindowMatch {
        private final int index;
        private final double sum;

        /**
         * Creates a rolling window match record.
         *
         * @param index start index of the window
         * @param sum   sum of values in the window
         */
        private RollingWindowMatch(int index, double sum) {
            this.index = index;
            this.sum = sum;
        }
    }

    /**
     * Holds the index and range values for a windowed min/max match.
     */
    private static class RangeMatch {
        private final int index;
        private final double min;
        private final double max;

        /**
         * Creates a range match record.
         *
         * @param index start index of the window
         * @param min   minimum value in the window
         * @param max   maximum value in the window
         */
        private RangeMatch(int index, double min, double max) {
            this.index = index;
            this.min = min;
            this.max = max;
        }
    }

    /**
     * Holds the index and probabilities for a storm/hail match.
     */
    private static class StormHailMatch {
        private final int index;
        private final double stormProbability;
        private final double hailProbability;

        /**
         * Creates a storm/hail match record.
         *
         * @param index            index of the match
         * @param stormProbability storm probability percentage
         * @param hailProbability  hail probability percentage
         */
        private StormHailMatch(int index, double stormProbability, double hailProbability) {
            this.index = index;
            this.stormProbability = stormProbability;
            this.hailProbability = hailProbability;
        }
    }
}
