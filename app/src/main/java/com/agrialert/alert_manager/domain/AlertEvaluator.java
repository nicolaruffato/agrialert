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
 * Converte i dati meteo in potenziali alert applicando semplici soglie.
 */
public class AlertEvaluator {

    private static final int DEFAULT_ICON_RES = R.drawable.ic_alert;
    private static final int HUMIDITY_MIN_SCARSA_VENTILAZIONE = 80;
    private static final int DAILY_WINDOW_HOURS = 24;
    private static final double PRECIPITATION_EPSILON = 0.1d;

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
                        addIfForecastMatches(result, type, field, groupName, tempSeries, timeSeries,
                                value -> value > threshold1,
                                "Ondata di calore",
                                value -> "Prevista temperatura " + formatValue(value, "C") +
                                        " (soglia " + formatValue(threshold1, "C") + ")",
                                R.drawable.ic_alert_calore);
                    } else {
                        int hours = toPositiveIntHours(threshold2);
                        int idx = firstIndexStreak(tempSeries, value -> value > threshold1, hours);
                        if (idx >= 0) {
                            double value = safeGet(tempSeries, idx);
                            long forecastAt = parseIsoTime(timeSeries, idx);
                            String description = "Prevista temperatura " + formatValue(value, "C") +
                                    " per almeno " + formatCount(hours, "h") +
                                    " (soglia " + formatValue(threshold1, "C") + ")" +
                                    formatWhen(forecastAt);
                            result.add(build(type, "Ondata di calore", description,
                                    R.drawable.ic_alert_calore, field, groupName, forecastAt));
                        }
                    }
                    break;
                case "GELO_BRINA":
                    if (threshold1 == null) {
                        break;
                    }
                    if (threshold2 == null || threshold2 <= 0d) {
                        addIfForecastMatches(result, type, field, groupName, tempSeries, timeSeries,
                                value -> value < threshold1,
                                "Gelo / brina",
                                value -> "Prevista temperatura " + formatValue(value, "C") +
                                        " (soglia " + formatValue(threshold1, "C") + ")",
                                R.drawable.ic_alert_gelo);
                    } else {
                        int hours = toPositiveIntHours(threshold2);
                        int idx = firstIndexStreak(tempSeries, value -> value < threshold1, hours);
                        if (idx >= 0) {
                            double value = safeGet(tempSeries, idx);
                            long forecastAt = parseIsoTime(timeSeries, idx);
                            String description = "Prevista temperatura " + formatValue(value, "C") +
                                    " per almeno " + formatCount(hours, "h") +
                                    " (soglia " + formatValue(threshold1, "C") + ")" +
                                    formatWhen(forecastAt);
                            result.add(build(type, "Gelo / brina", description,
                                    R.drawable.ic_alert_gelo, field, groupName, forecastAt));
                        }
                    }
                    break;
                case "PIOGGIA_INTENSA":
                    if (threshold1 == null) {
                        break;
                    }
                    if (threshold2 == null || threshold2 <= 0d) {
                        addIfForecastMatches(result, type, field, groupName, precipitationSeries, timeSeries,
                                value -> value > threshold1,
                                "Pioggia intensa",
                                value -> "Previste precipitazioni " + formatValue(value, "mm/h") +
                                        " (soglia " + formatValue(threshold1, "mm/h") + ")",
                                R.drawable.ic_alert_pioggia);
                    } else {
                        int windowHours = toPositiveIntHours(threshold2);
                        RollingWindowMatch match = firstIndexRollingSum(precipitationSeries, windowHours, threshold1);
                        if (match != null) {
                            long forecastAt = parseIsoTime(timeSeries, match.index);
                            String description = "Previste precipitazioni " +
                                    formatValue(match.sum, "mm") + " in " + formatCount(windowHours, "h") +
                                    " (soglia " + formatValue(threshold1, "mm") + ")" +
                                    formatWhen(forecastAt);
                            result.add(build(type, "Pioggia intensa", description,
                                    R.drawable.ic_alert_pioggia, field, groupName, forecastAt));
                        }
                    }
                    break;
                case "VENTO_FORTE":
                    if (threshold1 == null) {
                        break;
                    }
                    addIfForecastMatches(result, type, field, groupName, windSeries, timeSeries,
                            value -> value > threshold1,
                            "Vento forte",
                            value -> "Previsto vento " + formatValue(value, "km/h") +
                                    " (soglia " + formatValue(threshold1, "km/h") + ")",
                            R.drawable.ic_alert_vento);
                    break;
                case "TEMPORALE_GRANDINE":
                    if (threshold1 == null || threshold2 == null) {
                        break;
                    }
                    StormHailMatch stormMatch = firstStormHailMatch(
                            precipitationSeries, windSeries, humiditySeries, tempSeries,
                            weatherCode, threshold1, threshold2);
                    if (stormMatch != null) {
                        long forecastAt = parseIsoTime(timeSeries, stormMatch.index);
                        String description = "Previsto temporale/grandine con probabilita " +
                                formatValue(stormMatch.stormProbability, "%") + " e " +
                                formatValue(stormMatch.hailProbability, "%") +
                                " (soglie " + formatValue(threshold1, "%") +
                                ", " + formatValue(threshold2, "%") + ")" +
                                formatWhen(forecastAt);
                        result.add(build(type, "Temporale / grandine", description,
                                R.drawable.ic_alert_temporale, field, groupName, forecastAt));
                    }
                    break;
                case "SICCITA_PROLUNGATA":
                    if (threshold1 == null || threshold1 <= 0d) {
                        break;
                    }
                    int requiredHours = toPositiveIntHours(threshold1 * 24d);
                    int droughtIdx = firstIndexStreak(precipitationSeries,
                            value -> value <= PRECIPITATION_EPSILON, requiredHours);
                    if (droughtIdx >= 0) {
                        long forecastAt = parseIsoTime(timeSeries, droughtIdx);
                        String description = "Previste condizioni senza pioggia per almeno " +
                                formatCount(requiredHours, "h") +
                                " (soglia " + formatValue(threshold1, "giorni") + ")" +
                                formatWhen(forecastAt);
                        result.add(build(type, "Siccita prolungata", description,
                                R.drawable.ic_alert_siccita, field, groupName, forecastAt));
                    }
                    break;
                case "UMIDITA_ELEVATA":
                    if (threshold1 == null) {
                        break;
                    }
                    addIfForecastMatches(result, type, field, groupName, humiditySeries, timeSeries,
                            value -> value > threshold1,
                            "Umidita elevata",
                            value -> "Prevista umidita " + formatValue(value, "%") +
                                    " (soglia " + formatValue(threshold1, "%") + ")",
                            R.drawable.ic_alert_umidita);
                    break;
                case "ESCURSIONE_TERMICA_ELEVATA":
                    if (threshold1 == null || threshold1 <= 0d) {
                        break;
                    }
                    RangeMatch rangeMatch = firstDailyRangeMatch(tempSeries, DAILY_WINDOW_HOURS, threshold1);
                    if (rangeMatch != null) {
                        double delta = rangeMatch.max - rangeMatch.min;
                        long forecastAt = parseIsoTime(timeSeries, rangeMatch.index);
                        String description = "Prevista escursione termica " +
                                formatValue(delta, "C") +
                                " (soglia " + formatValue(threshold1, "C") + ")" +
                                formatWhen(forecastAt);
                        result.add(build(type, "Escursione termica elevata", description,
                                R.drawable.ic_alert_escursione, field, groupName, forecastAt));
                    }
                    break;
                case "RISCHIO_INCENDIO":
                    if (threshold1 == null || threshold2 == null) {
                        break;
                    }
                    addIfForecastMatchesPaired(result, type, field, groupName,
                            tempSeries, humiditySeries, timeSeries,
                            value -> value > threshold1,
                            hum -> hum < threshold2,
                            "Rischio incendio",
                            (tempVal, humVal) -> "Prevista temperatura " + formatValue(tempVal, "C") +
                                    " e umidita " + formatValue(humVal, "%") +
                                    " (soglie " + formatValue(threshold1, "C") +
                                    ", " + formatValue(threshold2, "%") + ")",
                            R.drawable.ic_alert_incendio);
                    break;
                case "SCARSA_VENTILAZIONE":
                    if (threshold1 == null) {
                        break;
                    }
                    double humidityMin = threshold2 != null ? threshold2 : HUMIDITY_MIN_SCARSA_VENTILAZIONE;
                    addIfForecastMatchesPaired(result, type, field, groupName,
                            windSeries, humiditySeries, timeSeries,
                            value -> value < threshold1,
                            hum -> hum > humidityMin,
                            "Scarsa ventilazione",
                            (windVal, humVal) -> "Previsti vento " + formatValue(windVal, "km/h") +
                                    " e umidita " + formatValue(humVal, "%") +
                                    " (soglie " + formatValue(threshold1, "km/h") +
                                    ", " + formatValue(humidityMin, "%") + ")",
                            R.drawable.ic_alert_ventilazione);
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    private void addIfForecastMatches(List<Alert> result,
                                      AlertType type,
                                      Field field,
                                      String groupName,
                                      List<Double> values,
                                      List<String> times,
                                      ThresholdPredicate predicate,
                                      String title,
                                      ValueFormatter descriptionFormatter,
                                      int iconRes) {
        int idx = firstIndexMatching(values, predicate);
        if (idx < 0) return;
        long forecastAt = parseIsoTime(times, idx);
        double value = safeGet(values, idx);
        String description = descriptionFormatter.format(value) + formatWhen(forecastAt);
        result.add(build(type, title, description, iconRes, field, groupName, forecastAt));
    }

    private void addIfForecastMatchesPaired(List<Alert> result,
                                            AlertType type,
                                            Field field,
                                            String groupName,
                                            List<Double> seriesA,
                                            List<Double> seriesB,
                                            List<String> times,
                                            ThresholdPredicate predicateA,
                                            ThresholdPredicate predicateB,
                                            String title,
                                            BiValueFormatter descriptionFormatter,
                                            int iconRes) {
        int idx = firstIndexMatching(seriesA, predicateA, seriesB, predicateB);
        if (idx < 0) return;
        long forecastAt = parseIsoTime(times, idx);
        double valA = safeGet(seriesA, idx);
        double valB = safeGet(seriesB, idx);
        String description = descriptionFormatter.format(valA, valB) + formatWhen(forecastAt);
        result.add(build(type, title, description, iconRes, field, groupName, forecastAt));
    }

    private int firstIndexMatching(List<Double> values, ThresholdPredicate predicate) {
        if (values == null || predicate == null) return -1;
        for (int i = 0; i < values.size(); i++) {
            double v = safeGet(values, i);
            if (!Double.isNaN(v) && predicate.test(v)) {
                return i;
            }
        }
        return -1;
    }

    private int firstIndexMatching(List<Double> seriesA,
                                   ThresholdPredicate predicateA,
                                   List<Double> seriesB,
                                   ThresholdPredicate predicateB) {
        if (seriesA == null || seriesB == null || predicateA == null || predicateB == null) return -1;
        int size = Math.min(seriesA.size(), seriesB.size());
        for (int i = 0; i < size; i++) {
            double a = safeGet(seriesA, i);
            double b = safeGet(seriesB, i);
            if (!Double.isNaN(a) && !Double.isNaN(b) && predicateA.test(a) && predicateB.test(b)) {
                return i;
            }
        }
        return -1;
    }

    private int firstIndexStreak(List<Double> values, ThresholdPredicate predicate, int minLength) {
        if (values == null || predicate == null || minLength <= 0) {
            return -1;
        }
        if (minLength <= 1) {
            return firstIndexMatching(values, predicate);
        }
        int streak = 0;
        for (int i = 0; i < values.size(); i++) {
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

    private RollingWindowMatch firstIndexRollingSum(List<Double> values, int windowSize, double threshold) {
        if (values == null || windowSize <= 0) {
            return null;
        }
        int size = values.size();
        if (size < windowSize) {
            return null;
        }
        for (int start = 0; start <= size - windowSize; start++) {
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

    private RangeMatch firstDailyRangeMatch(List<Double> values, int windowSize, double threshold) {
        if (values == null || windowSize <= 0) {
            return null;
        }
        int size = values.size();
        if (size < windowSize) {
            return null;
        }
        for (int start = 0; start <= size - windowSize; start++) {
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

    private StormHailMatch firstStormHailMatch(List<Double> precipitationSeries,
                                               List<Double> windSeries,
                                               List<Double> humiditySeries,
                                               List<Double> tempSeries,
                                               int weatherCode,
                                               double stormThreshold,
                                               double hailThreshold) {
        int size = Math.max(Math.max(sizeOf(precipitationSeries), sizeOf(windSeries)),
                Math.max(sizeOf(humiditySeries), sizeOf(tempSeries)));
        if (size == 0) {
            return null;
        }
        for (int i = 0; i < size; i++) {
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

    private double weatherCodeHailBoost(int weatherCode) {
        if (weatherCode >= 96 && weatherCode <= 99) {
            return 30d;
        }
        if (weatherCode == 95) {
            return 20d;
        }
        return 0d;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private int sizeOf(List<Double> values) {
        return values != null ? values.size() : 0;
    }

    private int toPositiveIntHours(Double hours) {
        if (hours == null || hours.isNaN() || hours.isInfinite()) {
            return 0;
        }
        int value = (int) Math.ceil(hours);
        return Math.max(1, value);
    }

    private String formatCount(int value, String suffix) {
        return String.format(Locale.getDefault(), "%d %s", value, suffix);
    }

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

    private double safeGet(List<Double> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return Double.NaN;
        }
        Double value = list.get(index);
        return value != null ? value : Double.NaN;
    }

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

    private String formatWhen(long timestampMs) {
        if (timestampMs <= 0L) return "";
        SimpleDateFormat df = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        return " - previsto per " + df.format(new Date(timestampMs));
    }

    private String formatValue(double value, String suffix) {
        if (Double.isNaN(value)) return "-";
        return String.format(Locale.getDefault(), "%.1f %s", value, suffix);
    }

    private interface ThresholdPredicate {
        boolean test(double value);
    }

    private interface ValueFormatter {
        String format(double value);
    }

    private interface BiValueFormatter {
        String format(double valueA, double valueB);
    }

    private static class RollingWindowMatch {
        private final int index;
        private final double sum;

        private RollingWindowMatch(int index, double sum) {
            this.index = index;
            this.sum = sum;
        }
    }

    private static class RangeMatch {
        private final int index;
        private final double min;
        private final double max;

        private RangeMatch(int index, double min, double max) {
            this.index = index;
            this.min = min;
            this.max = max;
        }
    }

    private static class StormHailMatch {
        private final int index;
        private final double stormProbability;
        private final double hailProbability;

        private StormHailMatch(int index, double stormProbability, double hailProbability) {
            this.index = index;
            this.stormProbability = stormProbability;
            this.hailProbability = hailProbability;
        }
    }
}
